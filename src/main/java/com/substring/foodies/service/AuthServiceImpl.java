package com.substring.foodies.service;

import com.substring.foodies.dto.JwtResponse;
import com.substring.foodies.dto.LoginUserDto;
import com.substring.foodies.dto.RefreshTokenRequest;
import com.substring.foodies.dto.UserDto;
import com.substring.foodies.exception.BadRequestException;
import com.substring.foodies.repository.UserRepository;
import com.substring.foodies.security.JwtService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService{

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public JwtResponse login(LoginUserDto loginUserDto) {

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginUserDto.email(), loginUserDto.password());
        authenticationManager.authenticate(token);

        String jwtAccessToken = jwtService.generateToken(loginUserDto.email(), true);
        String jwtRefreshToken = jwtService.generateToken(loginUserDto.email(), false);

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginUserDto.email());
        UserDto userDto = modelMapper.map(userService.getUserByEmail(userDetails.getUsername()), UserDto.class);

        JwtResponse response = JwtResponse.builder()
                .accessToken(jwtAccessToken)
                .refreshToken(jwtRefreshToken)
                .user(userDto)
                .build();

        return response;
    }

    @Override
    public JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {

        if(!jwtService.isRefreshToken(refreshTokenRequest.getRefreshToken()))
        {
            throw new BadRequestException("Invalid Refresh Token");
        }

        if(jwtService.isTokenValid(refreshTokenRequest.getRefreshToken()) && jwtService.isRefreshToken(refreshTokenRequest.getRefreshToken()))
        {
            String username = jwtService.getUsername(refreshTokenRequest.getRefreshToken());
            UserDto userDto = modelMapper.map(
                    userRepository.findByEmail(username)
                            .orElseThrow(() -> new RuntimeException("User not found with username = "+username)),
                    UserDto.class
            );
            String jwtAccessToken = jwtService.generateToken(username, true);
            String jwtRefreshToken = jwtService.generateToken(username, false);

            JwtResponse response = JwtResponse.builder()
                    .accessToken(jwtAccessToken)
                    .refreshToken(jwtRefreshToken)
                    .user(userDto)
                    .build();
            return response;
        }

        else {
            throw new BadRequestException("Expired or invalid Refresh Token");
        }
    }
}
