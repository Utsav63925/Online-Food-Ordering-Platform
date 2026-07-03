package com.substring.foodies.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {

    @Email
    @NotBlank(message = "Please provide your email.")
    private String email;
}
