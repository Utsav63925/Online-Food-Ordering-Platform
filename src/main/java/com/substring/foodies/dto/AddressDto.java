package com.substring.foodies.dto;

import com.substring.foodies.dto.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDto {

    @NotBlank(message = "Please provide the id.")
    private String id;

    @NotBlank(message = "Address line is required")
    private String addressLine;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "Pincode must be a valid 6-digit number"
    )
    private String pincode;

    @NotBlank(message = "Country is required")
    private String country;

    @NotNull(message = "Address type is required")
    private AddressType addressType;
}
