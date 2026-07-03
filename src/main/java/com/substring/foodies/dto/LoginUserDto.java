package com.substring.foodies.dto;


import lombok.*;

@Builder
public record LoginUserDto(
        String email,
        String password)
{

}
