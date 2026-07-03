package com.substring.foodies.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.substring.foodies.Utility.ValidGender;
import com.substring.foodies.dto.enums.Role;
import jakarta.persistence.Id;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    @NotBlank(message = "Please provide the id.")
    private String id;

    @NotEmpty(message = "Please provide your Name.")
    @Size(min=2, max=20, message = "Name must be between 2 and 20 characters.")
    private String name;

    @Email(message = "Email should be valid")
    @NotEmpty(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one letter, one number, and one special character")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotBlank(message = "ConfirmPassword cannot be empty")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String confirmPassword;

    @ValidGender
    // We can also give our custom message.
    private String gender;

    @Valid
    @NotNull(message = "Address is required")
    private AddressDto address;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Phone number must be a valid 10-digit Indian mobile number"
    )
    private String phoneNumber;

    private Role role;

    private boolean isAvailable=true;

    private boolean isEnabled=true;
}
