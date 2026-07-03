package com.substring.foodies.dto;

import com.substring.foodies.Utility.ValidGender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPutDto {

    @NotEmpty(message = "Please provide your Name.")
    @Size(min=2, max=20, message = "Name must be between 2 and 20 characters.")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Phone number must be a valid 10-digit Indian mobile number"
    )
    private String phoneNumber;

    @ValidGender
    // We can also give our custom message.
    private String gender;

    @Valid
    @NotNull(message = "Address is required")
    private AddressDto address;
}
