package com.substring.foodies.dto;

import com.substring.foodies.dto.enums.PaymentMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPlaceRequest {

    @NotBlank(message = "Please provide the user Id.")
    private String userId;

    @NotNull(message = "Please provide the Address.")
    @Valid
    private AddressDto address;

    @NotNull(message = "Please provide the payment mode.")
    private PaymentMode paymentMode;

    private String aboutThisOrder;
}
