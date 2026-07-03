package com.substring.foodies.service;

import com.substring.foodies.dto.JwtResponse;
import com.substring.foodies.dto.LoginUserDto;
import com.substring.foodies.dto.RefreshTokenRequest;
import com.substring.foodies.dto.UserDto;

public interface AuthService {

    JwtResponse login(LoginUserDto loginUserDto);

    JwtResponse refreshToken(RefreshTokenRequest refreshToken);
}
