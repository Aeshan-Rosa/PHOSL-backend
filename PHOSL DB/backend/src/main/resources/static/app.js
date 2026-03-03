const state = {
  token: localStorage.getItem("phosl_token") || "",
  user: null,
  currentView: "dashboard",
  locations: [],
  workerRoles: [],
  workers: [],
  pianos: [],
  customers: [],
  suppliers: [],
  sales: [],
  purchases: []
};

const views = [
  { id: "dashboard", label: "Dashboard", roles: ["ADMIN", "ACCOUNTANT", "SALES_STAFF", "STAFF"] },
  { id: "pianos", label: "Pianos", roles: ["ADMIN", "SALES_STAFF", "TECHNICIAN", "STAFF"] },
  { id: "workers", label: "Workers", roles: ["ADMIN", "ACCOUNTANT"] },
  { id: "suppliers", label: "Suppliers", roles: ["ADMIN", "ACCOUNTANT"] },
  { id: "purchases", label: "Purchases", roles: ["ADMIN", "ACCOUNTANT"] },
  { id: "customers", label: "Customers", roles: ["ADMIN", "SALES_STAFF", "STAFF"] },
  { id: "sales", label: "Sales", roles: ["ADMIN", "SALES_STAFF", "STAFF"] },
  { id: "installments", label: "Installments", roles: ["ADMIN", "ACCOUNTANT", "SALES_STAFF", "STAFF"] },
  { id: "commissions", label: "Commissions", roles: ["ADMIN", "ACCOUNTANT"] },
  { id: "tasks", label: "Tasks", roles: ["ADMIN", "TECHNICIAN", "STAFF"] },
  { id: "repairs", label: "Repairs", roles: ["ADMIN", "TECHNICIAN", "STAFF"] }
];

const $ = (sel) => document.querySelector(sel);

function status(message, isError = false) {
  const appVisible = !$("#appView").classList.contains("hidden");
  const node = appVisible ? $("#statusMessage") : $("#loginStatus");
  node.textContent = message;
  node.classList.toggle("error", isError);
  node.classList.toggle("ok", !isError);
}

function toCurrency(value) {
  const n = Number(value || 0);
  return new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(n);
}

function fmtDate(value) {
  if (!value) return "-";
  return new Date(value).toLocaleDateString();
}

function qs(params = {}) {
  const obj = Object.entries(params).filter(([, v]) => v !== "" && v !== undefined && v !== null);
  if (!obj.length) return "";
  return "?" + obj.map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`).join("&");
}

async function api(path, options = {}) {
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
  if (state.token) headers.Authorization = `Bearer ${state.token}`;

  const response = await fetch(path, { ...options, headers });
  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = { error: text };
    }
  }

  if (!response.ok) {
    throw new Error(data?.error || data?.message || `Request failed (${response.status})`);
  }
  return data;
}

function table(columns, rows) {
  const t = document.querySelector("#tableTemplate").content.firstElementChild.cloneNode(true);
  const head = t.querySelector("thead");
  const body = t.querySelector("tbody");

  head.innerHTML = `<tr>${columns.map((c) => `<th>${c}</th>`).join("")}</tr>`;
  body.innerHTML = rows.length
    ? rows.map((r) => `<tr>${r.map((c) => `<td>${c ?? "-"}</td>`).join("")}</tr>`).join("")
    : `<tr><td colspan="${columns.length}">No data</td></tr>`;

  return t.outerHTML;
}

function allowed(role, viewRoles) {
  if (role === "ADMIN") return true;
  if (role === "STAFF") return viewRoles.includes("STAFF") || viewRoles.includes("SALES_STAFF") || viewRoles.includes("TECHNICIAN");
  return viewRoles.includes(role);
}

function mediaStoreKey(pianoId) {
  return `phosl_media_${pianoId}`;
}

function readMedia(pianoId) {
  try {
    return JSON.parse(localStorage.getItem(mediaStoreKey(pianoId)) || "[]");
  } catch {
    return [];
  }
}

function writeMedia(pianoId, media) {
  localStorage.setItem(mediaStoreKey(pianoId), JSON.stringify(media));
}

function renderNav() {
  const nav = $("#mainNav");
  const role = state.user?.role || "";
  const allowedViews = views.filter((v) => allowed(role, v.roles));

  nav.innerHTML = allowedViews
    .map((v) => `<button class="btn ${state.currentView === v.id ? "active" : ""}" data-view="${v.id}">${v.label}</button>`)
    .join("");

  nav.querySelectorAll("button").forEach((btn) => {
    btn.addEventListener("click", () => setView(btn.dataset.view));
  });
}

async function setView(view) {
  state.currentView = view;
  document.querySelectorAll(".view").forEach((v) => v.classList.add("hidden"));
  $(`#${view}`).classList.remove("hidden");
  renderNav();

  try {
    if (view === "dashboard") await loadDashboard();
    if (view === "pianos") await loadPianos();
    if (view === "workers") await loadWorkers();
    if (view === "suppliers") await loadSuppliers();
    if (view === "purchases") await loadPurchases();
    if (view === "customers") await loadCustomers();
    if (view === "sales") await loadSales();
    if (view === "installments") await loadInstallments();
    if (view === "commissions") await loadCommissions();
    if (view === "tasks") await loadTasks();
    if (view === "repairs") await loadRepairs();
  } catch (err) {
    status(err.message, true);
  }
}

async function bootstrapCommonData() {
  [state.locations, state.workerRoles, state.workers, state.pianos, state.customers, state.suppliers, state.sales, state.purchases] = await Promise.all([
    api("/api/locations"),
    api("/api/worker-roles"),
    api("/api/workers"),
    api("/api/pianos"),
    api("/api/customers"),
    api("/api/suppliers"),
    api("/api/sales"),
    api("/api/purchases")
  ]);
}

async function loadDashboard() {
  const section = $("#dashboard");
  const overview = await api("/api/dashboard/overview");
  const inventory = await api("/api/dashboard/inventory");
  const followUps = await api("/api/reports/customers-followup?daysSinceSale=365");

  section.innerHTML = `
    <div class="section-head"><h3>Business Overview Dashboard</h3></div>
    <div class="metrics">
      <div class="metric">Total Inventory Value<b>${toCurrency(overview.stockValue)}</b></div>
      <div class="metric">Total Sales<b>${toCurrency(overview.salesTotal)}</b></div>
      <div class="metric">Profit Estimate<b>${toCurrency(overview.profitEstimate)}</b></div>
      <div class="metric">Pending Payments<b>${toCurrency(overview.pendingPayments)}</b></div>
    </div>
    <div class="grid-2">
      <div class="stack">
        <h3>Inventory by Status</h3>
        ${table(["Status", "Count"], Object.entries(inventory.byStatus || {}).map(([k, v]) => [k, v]))}
      </div>
      <div class="stack">
        <h3>Top-Selling / Stock by Brand</h3>
        ${table(["Brand", "Count"], Object.entries(inventory.byBrand || {}).sort((a, b) => b[1] - a[1]).slice(0, 8))}
      </div>
    </div>
    <div class="stack">
      <h3>One-Year Follow-Up List (${followUps.length})</h3>
      ${table(
        ["Sale ID", "Customer ID", "Sold Date", "Delivery Date", "Total"],
        followUps.slice(0, 15).map((s) => [s.id, s.customerId, fmtDate(s.soldDate), fmtDate(s.deliveryDate), toCurrency(s.total)])
      )}
    </div>
  `;
}

async function loadPianos() {
  const section = $("#pianos");
  const inventorySummary = await api("/api/inventory/summary");
  const aging = await api("/api/inventory/aging");

  section.innerHTML = `
    <div class="section-head"><h3>Piano Registration & Stock</h3></div>
    <div class="metrics">
      <div class="metric">Total Pianos<b>${inventorySummary.totalCount || 0}</b></div>
      <div class="metric">Inventory Value<b>${toCurrency(inventorySummary.totalExpectedValue)}</b></div>
      <div class="metric">In Stock<b>${inventorySummary.countByStatus?.IN_STOCK || 0}</b></div>
      <div class="metric">Low Stock Alert<b>${(inventorySummary.countByStatus?.IN_STOCK || 0) < 3 ? "Attention" : "Healthy"}</b></div>
    </div>

    <form id="pianoFilterForm" class="grid-4">
      <label>Brand<input name="brand" /></label>
      <label>Status
        <select name="status"><option value="">All</option><option>IN_STOCK</option><option>RESERVED</option><option>SOLD</option><option>UNDER_REPAIR</option></select>
      </label>
      <label>Type
        <select name="type"><option value="">All</option><option>Upright</option><option>Grand</option><option>Digital</option></select>
      </label>
      <label>Search (serial/model/brand)<input name="q" /></label>
      <label>Min Price<input type="number" step="0.01" name="minPrice" /></label>
      <label>Max Price<input type="number" step="0.01" name="maxPrice" /></label>
      <label>Location
        <select name="locationId">
          <option value="">All</option>
          ${state.locations.map((l) => `<option value="${l.id}">${l.name}</option>`).join("")}
        </select>
      </label>
      <div><button class="btn" type="submit">Apply Filters</button></div>
    </form>

    <h3>Add Piano</h3>
    <form id="pianoCreateForm" class="grid-4">
      <label>Piano ID (Code)<input name="pianoCode" placeholder="Auto/Manual code" /></label>
      <label>Brand<input name="brand" required /></label>
      <label>Model<input name="model" /></label>
      <label>Serial Number<input name="serialNumber" /></label>
      <label>Type<select name="pianoType"><option>Upright</option><option>Grand</option><option>Digital</option></select></label>
      <label>Color<input name="color" /></label>
      <label>Condition<select name="conditionType"><option>New</option><option>Used</option><option>Refurbished</option></select></label>
      <label>Year<input type="number" name="manufactureYear" /></label>
      <label>Selling Price<input type="number" step="0.01" name="expectedSellingPrice" /></label>
      <label>Status<select name="status"><option>IN_STOCK</option><option>RESERVED</option><option>SOLD</option><option>UNDER_REPAIR</option></select></label>
      <label>Location<select name="locationId">${state.locations.map((l) => `<option value="${l.id}">${l.name}</option>`).join("")}</select></label>
      <div><button class="btn primary" type="submit">Create Piano</button></div>
    </form>

    <div id="pianoTable"></div>

    <div class="grid-2">
      <div>
        <h3>Stock Aging</h3>
        <div>${table(["Piano ID", "Brand", "Model", "Days In Stock"], aging.slice(0, 15).map((a) => [a.pianoId, a.brand, a.model, a.daysInStock]))}</div>
      </div>
      <div>
        <h3>Media Management (Local Browser Storage)</h3>
        <form id="mediaForm" class="grid-2">
          <label>Piano ID<input type="number" name="pianoId" required /></label>
          <label>Document Type
            <select name="type"><option>Image</option><option>Repair Document</option><option>Warranty Document</option></select>
          </label>
          <label>Upload File<input type="file" name="file" required /></label>
          <div><button class="btn" type="submit">Attach</button></div>
        </form>
        <ul id="mediaList" class="file-list"></ul>
        <p class="note">Backend has no media endpoint yet, so this keeps uploaded file names in local storage.</p>
      </div>
    </div>
  `;

  const renderPianoTable = async (filter = {}) => {
    const list = await api(`/api/pianos${qs(filter)}`);
    const q = (filter.q || "").toLowerCase();
    const filtered = q
      ? list.filter((p) => `${p.serialNumber || ""} ${p.model || ""} ${p.brand || ""}`.toLowerCase().includes(q))
      : list;

    $("#pianoTable").innerHTML = table(
      ["ID", "Code", "Brand", "Model", "Serial", "Type", "Condition", "Status", "Price", "Location"],
      filtered.map((p) => {
        const location = state.locations.find((l) => l.id === p.locationId)?.name || "-";
        return [p.id, p.pianoCode || "-", p.brand, p.model || "-", p.serialNumber || "-", p.pianoType || "-", p.conditionType || "-", p.status, toCurrency(p.expectedSellingPrice), location];
      })
    );
  };

  await renderPianoTable();

  $("#pianoFilterForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    await renderPianoTable(Object.fromEntries(fd.entries()));
  });

  $("#pianoCreateForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = Object.fromEntries(new FormData(e.target).entries());
    if (fd.manufactureYear) fd.manufactureYear = Number(fd.manufactureYear);
    if (fd.locationId) fd.locationId = Number(fd.locationId);
    if (fd.expectedSellingPrice) fd.expectedSellingPrice = Number(fd.expectedSellingPrice);
    await api("/api/pianos", { method: "POST", body: JSON.stringify(fd) });
    status("Piano created");
    await bootstrapCommonData();
    await loadPianos();
  });

  $("#mediaForm").addEventListener("submit", (e) => {
    e.preventDefault();
    const f = new FormData(e.target);
    const pianoId = Number(f.get("pianoId"));
    const file = f.get("file");
    if (!pianoId || !file || !file.name) return;

    const media = readMedia(pianoId);
    media.push({
      type: f.get("type"),
      name: file.name,
      addedAt: new Date().toISOString()
    });
    writeMedia(pianoId, media);
    $("#mediaList").innerHTML = media.map((m) => `<li>${m.type}: ${m.name} (${fmtDate(m.addedAt)})</li>`).join("");
    status(`Saved media metadata for piano ${pianoId}`);
  });
}

async function loadWorkers() {
  const section = $("#workers");
  const now = new Date();
  const payouts = await api(`/api/worker-payouts${qs({ year: now.getFullYear(), month: now.getMonth() + 1 })}`);

  section.innerHTML = `
    <div class="section-head"><h3>Worker Management</h3></div>
    <form id="workerCreateForm" class="grid-4">
      <label>Employee ID<input name="employeeCode" /></label>
      <label>Name<input name="fullName" required /></label>
      <label>Role
        <select name="roleId">${state.workerRoles.map((r) => `<option value="${r.id}">${r.roleName}</option>`).join("")}</select>
      </label>
      <label>Phone<input name="phone" /></label>
      <label>Email<input name="email" type="email" /></label>
      <label>Base Salary<input type="number" step="0.01" name="baseSalary" /></label>
      <label>Commission %<input type="number" step="0.01" name="commissionRate" /></label>
      <label>Date Joined<input type="date" name="joinedDate" /></label>
      <div><button class="btn primary" type="submit">Add Worker</button></div>
    </form>

    <h3>Employees</h3>
    ${table(
      ["ID", "Employee ID", "Name", "Role", "Contact", "Salary", "Commission %", "Joined", "Status"],
      state.workers.map((w) => [
        w.id,
        w.employeeCode || "-",
        w.fullName,
        state.workerRoles.find((r) => r.id === w.roleId)?.roleName || w.roleId,
        `${w.phone || ""} ${w.email || ""}`.trim() || "-",
        toCurrency(w.baseSalary),
        `${w.commissionRate || 0}%`,
        fmtDate(w.joinedDate),
        w.isActive ? "Active" : "Disabled"
      ])
    )}

    <h3>Payment Records (Current Month)</h3>
    ${table(["Worker ID", "Type", "Amount", "Date"], payouts.map((p) => [p.workerId, p.payoutType, toCurrency(p.amount), fmtDate(p.payoutDate)]))}

    <h3>Add Salary/Commission Payout</h3>
    <form id="workerPayoutForm" class="grid-4">
      <label>Worker<select name="workerId">${state.workers.map((w) => `<option value="${w.id}">${w.fullName}</option>`).join("")}</select></label>
      <label>Year<input type="number" name="periodYear" value="${now.getFullYear()}" /></label>
      <label>Month<input type="number" min="1" max="12" name="periodMonth" value="${now.getMonth() + 1}" /></label>
      <label>Type<select name="payoutType"><option>SALARY</option><option>COMMISSION</option><option>BONUS</option><option>DEDUCTION</option></select></label>
      <label>Amount<input type="number" step="0.01" name="amount" /></label>
      <label>Payout Date<input type="date" name="payoutDate" /></label>
      <div><button class="btn" type="submit">Record Payout</button></div>
    </form>
  `;

  $("#workerCreateForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = Object.fromEntries(new FormData(e.target).entries());
    fd.roleId = Number(fd.roleId);
    fd.baseSalary = Number(fd.baseSalary || 0);
    fd.commissionRate = Number(fd.commissionRate || 0);
    await api("/api/workers", { method: "POST", body: JSON.stringify(fd) });
    status("Worker added");
    await bootstrapCommonData();
    await loadWorkers();
  });

  $("#workerPayoutForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = Object.fromEntries(new FormData(e.target).entries());
    fd.workerId = Number(fd.workerId);
    fd.periodYear = Number(fd.periodYear);
    fd.periodMonth = Number(fd.periodMonth);
    fd.amount = Number(fd.amount || 0);
    await api("/api/worker-payouts", { method: "POST", body: JSON.stringify(fd) });
    status("Payout recorded");
    await loadWorkers();
  });
}

async function loadSuppliers() {
  const section = $("#suppliers");
  section.innerHTML = `
    <div class="section-head"><h3>Supplier Management</h3></div>
    <form id="supplierCreateForm" class="grid-4">
      <label>Name<input name="name" required /></label>
      <label>Phone<input name="phone" /></label>
      <label>Email<input name="email" type="email" /></label>
      <label>Address<input name="address" /></label>
      <label>Country<input name="country" placeholder="Sri Lanka / Japan" /></label>
      <label>Reliability Rating (1-10)<input type="number" min="1" max="10" name="reliabilityRating" /></label>
      <div><button class="btn primary" type="submit">Add Supplier</button></div>
    </form>
    ${table(["ID", "Name", "Country", "Phone", "Email", "Rating"], state.suppliers.map((s) => [s.id, s.name, s.country || "-", s.phone || "-", s.email || "-", s.reliabilityRating || "-"]))}
  `;

  $("#supplierCreateForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = Object.fromEntries(new FormData(e.target).entries());
    if (fd.reliabilityRating) fd.reliabilityRating = Number(fd.reliabilityRating);
    await api("/api/suppliers", { method: "POST", body: JSON.stringify(fd) });
    status("Supplier added");
    await bootstrapCommonData();
    await loadSuppliers();
  });
}

async function loadPurchases() {
  const section = $("#purchases");
  section.innerHTML = `
    <div class="section-head"><h3>Buying History & Purchases</h3></div>
    <form id="purchaseCreateForm" class="grid-4">
      <label>Purchase Invoice ID<input name="purchaseCode" /></label>
      <label>Supplier<select name="supplierId">${state.suppliers.map((s) => `<option value="${s.id}">${s.name}</option>`).join("")}</select></label>
      <label>Date of Purchase<input type="date" name="purchaseDate" required /></label>
      <label>Payment Status<select name="paymentStatus"><option>PENDING</option><option>PARTIAL</option><option>PAID</option></select></label>
      <div><button class="btn primary" type="submit">Create Purchase</button></div>
    </form>

    <h3>Purchases</h3>
    ${table(
      ["ID", "Invoice", "Supplier", "Date", "Payment"],
      state.purchases.map((p) => [p.id, p.purchaseCode || "-", state.suppliers.find((s) => s.id === p.supplierId)?.name || p.supplierId, fmtDate(p.purchaseDate), p.paymentStatus])
    )}

    <h3>Add Cost Breakdown Item</h3>
    <form id="purchaseItemForm" class="grid-4">
      <label>Purchase<select name="purchaseId">${state.purchases.map((p) => `<option value="${p.id}">${p.purchaseCode || `Purchase #${p.id}`}</option>`).join("")}</select></label>
      <label>Piano<select name="pianoId">${state.pianos.map((p) => `<option value="${p.id}">${p.id} - ${p.brand} ${p.model || ""}</option>`).join("")}</select></label>
      <label>Buying Price<input type="number" step="0.01" name="buyPrice" /></label>
      <label>Transport Cost<input type="number" step="0.01" name="shippingCost" /></label>
      <label>Repair Cost<input type="number" step="0.01" name="repairCost" /></label>
      <label>Other Cost<input type="number" step="0.01" name="otherCost" /></label>
      <label>Total Investment (Landed Cost)<input type="number" step="0.01" name="landedCost" /></label>
      <div><button class="btn" type="submit">Add Item</button></div>
    </form>

    <h3>Add Purchase Payment</h3>
    <form id="purchasePaymentForm" class="grid-4">
      <label>Purchase<select name="purchaseId">${state.purchases.map((p) => `<option value="${p.id}">${p.purchaseCode || `Purchase #${p.id}`}</option>`).join("")}</select></label>
      <label>Pay Date<input type="date" name="payDate" required /></label>
      <label>Amount<input type="number" step="0.01" name="amount" required /></label>
      <label>Method<select name="method"><option>Cash</option><option>Bank</option></select></label>
      <div><button class="btn" type="submit">Add Payment</button></div>
    </form>
  `;

  $("#purchaseCreateForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = Object.fromEntries(new FormData(e.target).entries());
    fd.supplierId = Number(fd.supplierId);
    await api("/api/purchases", { method: "POST", body: JSON.stringify(fd) });
    status("Purchase created");
    await bootstrapCommonData();
    await loadPurchases();
  });

  $("#purchaseItemForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = Object.fromEntries(new FormData(e.target).entries());
    const purchaseId = Number(fd.purchaseId);
    delete fd.purchaseId;
    fd.pianoId = Number(fd.pianoId);
    ["buyPrice", "shippingCost", "repairCost", "otherCost", "landedCost"].forEach((k) => fd[k] = Number(fd[k] || 0));
    await api(`/api/purchases/${purchaseId}/items`, { method: "POST", body: JSON.stringify(fd) });
    status("Purchase item added");
  });

  $("#purchasePaymentForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = Object.fromEntries(new FormData(e.target).entries());
    const purchaseId = Number(fd.purchaseId);
    delete fd.purchaseId;
    fd.amount = Number(fd.amount || 0);
    await api(`/api/purchases/${purchaseId}/payments`, { method: "POST", body: JSON.stringify(fd) });
    await api(`/api/purchases/${purchaseId}/payment-status`, { method: "PUT" });
    status("Payment added and status refreshed");
    await bootstrapCommonData();
    await loadPurchases();
  });
}

async function loadCustomers() {
  const section = $("#customers");
  section.innerHTML = `
    <div class="section-head"><h3>Sold Piano Customer Management</h3></div>
    <form id="customerCreateForm" class="grid-4">
      <label>Full Name<input name="fullName" required /></label>
      <label>Phone<input name="phone" required /></label>
      <label>Email<input type="email" name="email" /></label>
      <label>Address<input name="address" /></label>
      <label>How Found<select name="howFound"><option>Referral</option><option>Social Media</option><option>Walk-in</option><option>Other</option></select></label>
      <div><button class="btn primary" type="submit">Add Customer</button></div>
    </form>

    <h3>Customers</h3>
    ${table(["ID", "Name", "Phone", "Email", "Address", "How Found"], state.customers.map((c) => [c.id, c.fullName, c.phone, c.email || "-", c.address || "-", c.howFound || "-"]))}
  `;

  $("#customerCreateForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = Object.fromEntries(new FormData(e.target).entries());
    await api("/api/customers", { method: "POST", body: JSON.stringify(fd) });
    status("Customer added");
    await bootstrapCommonData();
    await loadCustomers();
  });
}

async function loadSales() {
  const section = $("#sales");
  section.innerHTML = `
    <div class="section-head"><h3>Sales Record</h3></div>
    <form id="saleCreateForm" class="grid-4">
      <label>Invoice ID<input name="invoiceNo" /></label>
      <label>Customer<select name="customerId">${state.customers.map((c) => `<option value="${c.id}">${c.fullName}</option>`).join("")}</select></label>
      <label>Sold Date<input type="date" name="soldDate" required /></label>
      <label>Salesperson<select name="salespersonId"><option value="">None</option>${state.workers.map((w) => `<option value="${w.id}">${w.fullName}</option>`).join("")}</select></label>
      <label>Payment Method<select name="paymentPlan"><option>FULL</option><option>INSTALLMENT</option></select></label>
      <label>Discount<input type="number" step="0.01" name="discount" /></label>
      <label>Warranty Months<input type="number" name="warrantyMonths" value="12" /></label>
      <label>Delivery Date<input type="date" name="deliveryDate" /></label>
      <div><button class="btn primary" type="submit">Create Sale</button></div>
    </form>

    <h3>Sales List</h3>
    ${table(
      ["ID", "Invoice", "Customer", "Sold Date", "Total", "Discount", "Plan", "Delivery"],
      state.sales.map((s) => [
        s.id,
        s.invoiceNo || "-",
        state.customers.find((c) => c.id === s.customerId)?.fullName || s.customerId,
        fmtDate(s.soldDate),
        toCurrency(s.total),
        toCurrency(s.discount),
        s.paymentPlan,
        fmtDate(s.deliveryDate)
      ])
    )}

    <h3>Add Sold Piano Item</h3>
    <form id="saleItemForm" class="grid-4">
      <label>Sale<select name="saleId">${state.sales.map((s) => `<option value="${s.id}">${s.invoiceNo || `Sale #${s.id}`}</option>`).join("")}</select></label>
      <label>Piano<select name="pianoId">${state.pianos.filter((p) => p.status !== "SOLD").map((p) => `<option value="${p.id}">${p.id} - ${p.brand} ${p.model || ""}</option>`).join("")}</select></label>
      <label>Selling Price<input type="number" step="0.01" name="unitPrice" /></label>
      <label>Line Discount<input type="number" step="0.01" name="lineDiscount" /></label>
      <label>Line Total<input type="number" step="0.01" name="lineTotal" /></label>
      <div><button class="btn" type="submit">Add Item</button></div>
    </form>

    <h3>Add Sale Payment</h3>
    <form id="salePaymentForm" class="grid-4">
      <label>Sale<select name="saleId">${state.sales.map((s) => `<option value="${s.id}">${s.invoiceNo || `Sale #${s.id}`}</option>`).join("")}</select></label>
      <label>Pay Date<input type="date" name="payDate" required /></label>
      <label>Amount<input type="number" step="0.01" name="amount" required /></label>
      <label>Method<select name="method"><option>Cash</option><option>Bank</option><option>Installment</option></select></label>
      <div><button class="btn" type="submit">Record Payment</button></div>
    </form>
  `;

  $("#saleCreateForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = Object.fromEntries(new FormData(e.target).entries());
    fd.customerId = Number(fd.customerId);
    fd.salespersonId = fd.salespersonId ? Number(fd.salespersonId) : null;
    fd.discount = Number(fd.discount || 0);
    fd.warrantyMonths = Number(fd.warrantyMonths || 0);
    await api("/api/sales", { method: "POST", body: JSON.stringify(fd) });
    status("Sale created");
    await bootstrapCommonData();
    await loadSales();
  });

  $("#saleItemForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = Object.fromEntries(new FormData(e.target).entries());
    const saleId = Number(fd.saleId);
    delete fd.saleId;
    fd.pianoId = Number(fd.pianoId);
    fd.unitPrice = Number(fd.unitPrice || 0);
    fd.lineDiscount = Number(fd.lineDiscount || 0);
    fd.lineTotal = Number(fd.lineTotal || 0);
    await api(`/api/sales/${saleId}/items`, { method: "POST", body: JSON.stringify(fd) });
    status("Sale item added");
    await bootstrapCommonData();
    await loadSales();
  });

  $("#salePaymentForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = Object.fromEntries(new FormData(e.target).entries());
    const saleId = Number(fd.saleId);
    delete fd.saleId;
    fd.amount = Number(fd.amount || 0);
    await api(`/api/sales/${saleId}/payments`, { method: "POST", body: JSON.stringify(fd) });
    status("Sale payment recorded");
  });
}

async function loadInstallments() {
  const section = $("#installments");
  const overdue = await api("/api/installments/overdue");
  const upcoming = await api("/api/installments/upcoming?days=30");

  section.innerHTML = `
    <div class="section-head"><h3>Installment Tracking</h3></div>
    <form id="planCreateForm" class="grid-4">
      <label>Sale<select name="saleId">${state.sales.map((s) => `<option value="${s.id}">${s.invoiceNo || `Sale #${s.id}`}</option>`).join("")}</select></label>
      <label>Total Amount<input type="number" step="0.01" name="totalAmount" /></label>
      <label>Down Payment<input type="number" step="0.01" name="downPayment" /></label>
      <label>Remaining Balance<input type="number" step="0.01" name="remainingBalance" /></label>
      <label>Months<input type="number" name="months" value="12" /></label>
      <label>Start Date<input type="date" name="startDate" /></label>
      <label>Due Day of Month<input type="number" min="1" max="28" name="dueDayOfMonth" value="1" /></label>
      <div><button class="btn primary" type="submit">Create Plan</button></div>
    </form>

    <h3>Generate Schedule</h3>
    <form id="scheduleGenerateForm" class="grid-3">
      <label>Plan ID<input type="number" name="planId" /></label>
      <div><button class="btn" type="submit">Generate</button></div>
    </form>

    <h3>Record Installment Payment</h3>
    <form id="installmentPayForm" class="grid-4">
      <label>Plan ID<input type="number" name="planId" /></label>
      <label>Schedule ID<input type="number" name="scheduleId" /></label>
      <label>Amount<input type="number" step="0.01" name="amount" /></label>
      <label>Method<select name="method"><option>Cash</option><option>Bank</option><option>Installment</option></select></label>
      <div><button class="btn" type="submit">Record Payment</button></div>
    </form>

    <div class="grid-2">
      <div><h3>Overdue Alerts</h3>${table(["Schedule ID", "Plan", "Due", "Amount", "Paid", "Status"], overdue.map((s) => [s.id, s.planId, fmtDate(s.dueDate), toCurrency(s.amount), toCurrency(s.paidAmount), s.status]))}</div>
      <div><h3>Upcoming (30 Days)</h3>${table(["Schedule ID", "Plan", "Due", "Amount", "Status"], upcoming.map((s) => [s.id, s.planId, fmtDate(s.dueDate), toCurrency(s.amount), s.status]))}</div>
    </div>
  `;

  $("#planCreateForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = Object.fromEntries(new FormData(e.target).entries());
    fd.saleId = Number(fd.saleId);
    fd.totalAmount = Number(fd.totalAmount || 0);
    fd.downPayment = Number(fd.downPayment || 0);
    fd.remainingBalance = Number(fd.remainingBalance || 0);
    fd.months = Number(fd.months || 1);
    fd.dueDayOfMonth = Number(fd.dueDayOfMonth || 1);
    await api("/api/installments/plans", { method: "POST", body: JSON.stringify(fd) });
    status("Installment plan created");
  });

  $("#scheduleGenerateForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const planId = Number(new FormData(e.target).get("planId"));
    await api(`/api/installments/plans/${planId}/schedule/generate`, { method: "POST" });
    status("Schedule generated");
    await loadInstallments();
  });

  $("#installmentPayForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = Object.fromEntries(new FormData(e.target).entries());
    const query = qs({ scheduleId: Number(fd.scheduleId), amount: Number(fd.amount), method: fd.method });
    await api(`/api/installments/plans/${Number(fd.planId)}/pay${query}`, { method: "POST" });
    status("Installment payment recorded");
    await loadInstallments();
  });
}

async function loadCommissions() {
  const section = $("#commissions");
  const pending = await api("/api/commissions/workers?status=PENDING");
  const paid = await api("/api/commissions/workers?status=PAID");

  section.innerHTML = `
    <div class="section-head"><h3>Commission Tracking</h3></div>
    <div class="metrics">
      <div class="metric">Pending Commissions<b>${pending.length}</b></div>
      <div class="metric">Paid Commissions<b>${paid.length}</b></div>
      <div class="metric">Total Pending Value<b>${toCurrency(pending.reduce((a, b) => a + Number(b.commissionAmount || 0), 0))}</b></div>
      <div class="metric">Total Paid Value<b>${toCurrency(paid.reduce((a, b) => a + Number(b.commissionAmount || 0), 0))}</b></div>
    </div>

    <form id="calcCommissionForm" class="grid-3">
      <label>Sale ID<input type="number" name="saleId" required /></label>
      <div><button class="btn" type="submit">Calculate Worker Commission</button></div>
    </form>

    ${table(
      ["Commission ID", "Sale", "Worker", "Rate", "Amount", "Status", "Action"],
      pending.map((c) => [
        c.id,
        c.saleId,
        state.workers.find((w) => w.id === c.workerId)?.fullName || c.workerId,
        `${c.commissionRate || 0}%`,
        toCurrency(c.commissionAmount),
        c.status,
        `<button class='btn mark-paid' data-id='${c.id}'>Mark Paid</button>`
      ])
    )}

    <h3>End-of-Month Summary (Printable)</h3>
    <button id="printCommissionBtn" class="btn">Print Commission Report</button>
  `;

  $("#calcCommissionForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const saleId = new FormData(e.target).get("saleId");
    await api(`/api/commissions/workers/calculate${qs({ saleId })}`, { method: "POST" });
    status("Commission calculated");
    await loadCommissions();
  });

  section.querySelectorAll(".mark-paid").forEach((btn) => {
    btn.addEventListener("click", async () => {
      await api(`/api/commissions/workers/${btn.dataset.id}/mark-paid`, { method: "PUT" });
      status("Commission marked as paid");
      await loadCommissions();
    });
  });

  $("#printCommissionBtn").addEventListener("click", () => window.print());
}

async function loadTasks() {
  const section = $("#tasks");
  const allTasks = await api("/api/tasks");

  section.innerHTML = `
    <div class="section-head"><h3>Attendance & Work Logs (Tasks)</h3></div>
    <form id="taskCreateForm" class="grid-4">
      <label>Task Type<select name="taskType"><option>REPAIR</option><option>DELIVERY</option><option>TUNING</option><option>OTHER</option></select></label>
      <label>Piano<select name="pianoId"><option value="">None</option>${state.pianos.map((p) => `<option value="${p.id}">${p.id} - ${p.brand}</option>`).join("")}</select></label>
      <label>Customer<select name="customerId"><option value="">None</option>${state.customers.map((c) => `<option value="${c.id}">${c.fullName}</option>`).join("")}</select></label>
      <label>Assigned Worker<select name="assignedWorkerId">${state.workers.map((w) => `<option value="${w.id}">${w.fullName}</option>`).join("")}</select></label>
      <label>Due Date<input type="date" name="dueDate" /></label>
      <label>Status<select name="status"><option>TODO</option><option>IN_PROGRESS</option><option>DONE</option></select></label>
      <div><button class="btn primary" type="submit">Assign Task</button></div>
    </form>

    ${table(
      ["Task ID", "Type", "Piano", "Customer", "Worker", "Due", "Status"],
      allTasks.map((t) => [
        t.id,
        t.taskType,
        t.pianoId || "-",
        t.customerId || "-",
        state.workers.find((w) => w.id === t.assignedWorkerId)?.fullName || t.assignedWorkerId || "-",
        fmtDate(t.dueDate),
        t.status
      ])
    )}
  `;

  $("#taskCreateForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = Object.fromEntries(new FormData(e.target).entries());
    fd.pianoId = fd.pianoId ? Number(fd.pianoId) : null;
    fd.customerId = fd.customerId ? Number(fd.customerId) : null;
    fd.assignedWorkerId = Number(fd.assignedWorkerId);
    await api("/api/tasks", { method: "POST", body: JSON.stringify(fd) });
    status("Task added");
    await loadTasks();
  });
}

async function loadRepairs() {
  const section = $("#repairs");
  const repairs = await api("/api/repairs");

  section.innerHTML = `
    <div class="section-head"><h3>Piano Repair History</h3></div>
    <form id="repairCreateForm" class="grid-4">
      <label>Piano<select name="pianoId">${state.pianos.map((p) => `<option value="${p.id}">${p.id} - ${p.brand} ${p.model || ""}</option>`).join("")}</select></label>
      <label>Opened Date<input type="date" name="openedDate" required /></label>
      <label>Issue<input name="issue" /></label>
      <label>Repair Cost<input type="number" step="0.01" name="repairCost" /></label>
      <label>Technician<select name="technicianId">${state.workers.map((w) => `<option value="${w.id}">${w.fullName}</option>`).join("")}</select></label>
      <label>Status<select name="status"><option>OPEN</option><option>IN_PROGRESS</option><option>DONE</option></select></label>
      <div><button class="btn primary" type="submit">Add Repair</button></div>
    </form>

    ${table(
      ["Repair ID", "Piano", "Issue", "Cost", "Technician", "Opened", "Closed", "Status"],
      repairs.map((r) => [
        r.id,
        r.pianoId,
        r.issue || "-",
        toCurrency(r.repairCost),
        state.workers.find((w) => w.id === r.technicianId)?.fullName || r.technicianId || "-",
        fmtDate(r.openedDate),
        fmtDate(r.closedDate),
        r.status
      ])
    )}
  `;

  $("#repairCreateForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = Object.fromEntries(new FormData(e.target).entries());
    fd.pianoId = Number(fd.pianoId);
    fd.technicianId = Number(fd.technicianId);
    fd.repairCost = Number(fd.repairCost || 0);
    await api("/api/repairs", { method: "POST", body: JSON.stringify(fd) });
    status("Repair entry created");
    await loadRepairs();
  });
}

async function onLogin(e) {
  e.preventDefault();
  const fd = Object.fromEntries(new FormData(e.target).entries());
  status("Signing in...");

  try {
    const res = await api("/api/auth/login", { method: "POST", body: JSON.stringify(fd), headers: {} });
    state.token = res.token;
    localStorage.setItem("phosl_token", res.token);

    state.user = await api("/api/auth/me");
    await bootstrapCommonData();

    $("#loginView").classList.add("hidden");
    $("#appView").classList.remove("hidden");

    $("#userInfo").textContent = `${state.user.username} (${state.user.email})`;
    $("#activeRole").textContent = `Role: ${state.user.role}`;

    renderNav();
    await setView("dashboard");
    status("Logged in successfully");
  } catch (err) {
    status(err.message, true);
  }
}

function onLogout() {
  state.token = "";
  state.user = null;
  localStorage.removeItem("phosl_token");
  $("#loginView").classList.remove("hidden");
  $("#appView").classList.add("hidden");
  status("Logged out");
}

async function init() {
  $("#loginForm").addEventListener("submit", onLogin);
  $("#logoutBtn").addEventListener("click", onLogout);

  if (state.token) {
    try {
      state.user = await api("/api/auth/me");
      await bootstrapCommonData();

      $("#loginView").classList.add("hidden");
      $("#appView").classList.remove("hidden");
      $("#userInfo").textContent = `${state.user.username} (${state.user.email})`;
      $("#activeRole").textContent = `Role: ${state.user.role}`;

      renderNav();
      await setView("dashboard");
      status("Session restored");
    } catch {
      onLogout();
    }
  }
}

init();
