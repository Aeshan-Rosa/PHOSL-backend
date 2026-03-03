package com.phosl.backend.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordUpdateRequest {
    @NotBlank
    private String newPassword;
}
