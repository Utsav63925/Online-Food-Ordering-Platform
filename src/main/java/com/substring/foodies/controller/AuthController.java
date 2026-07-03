package com.substring.foodies.controller;

import com.substring.foodies.dto.*;
import com.substring.foodies.service.AuthService;
import com.substring.foodies.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // We use SLF4j logger, LoggerFactory is the static method and getLogger is its method
    // We then have to provide our class name to get to know where the logger is used.
    private Logger logger=LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginUserDto loginUserDto)
    {
        JwtResponse response = authService.login(loginUserDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> generateNewAccessToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest)
    {
        JwtResponse response = authService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@Valid @RequestBody UserDto signUpUserDto) {
        UserDto user = userService.signUpUser(signUpUserDto);
        return ResponseEntity.ok(user);  // returning the created user, not the request again
    }

}
