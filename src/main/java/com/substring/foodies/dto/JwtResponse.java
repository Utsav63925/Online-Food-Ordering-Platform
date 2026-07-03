package com.substring.foodies.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private UserDto user;
}
