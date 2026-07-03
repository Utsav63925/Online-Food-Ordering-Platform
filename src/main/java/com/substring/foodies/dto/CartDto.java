package com.substring.foodies.dto;
import com.substring.foodies.entity.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartDto {

    private String id;
    private LocalDateTime createdAt;
    private String creatorId;
    private List<CartItemsDto> cartItems;

}
