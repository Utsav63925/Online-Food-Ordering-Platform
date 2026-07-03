package com.substring.foodies.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @Email
    @NotBlank(message = "Please provide the email.")
    private String email;

    @NotBlank(message = "Please provide the otp.")
    private String otp;

    @NotBlank(message = "Password cannot be empty")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one letter, one number, and one special character")
    private String newPassword;

    @NotBlank(message = "Please confirm your password.")
    private String confirmPassword;
}
