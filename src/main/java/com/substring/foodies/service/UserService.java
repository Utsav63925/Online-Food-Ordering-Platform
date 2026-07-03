package com.substring.foodies.service;

import com.substring.foodies.dto.ChangePasswordDto;
import com.substring.foodies.dto.ChangeRoleDto;
import com.substring.foodies.dto.UserDto;
import com.substring.foodies.dto.UserPutDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;


public interface UserService {

    UserDto updateUser(String id, UserPutDto userDto);

    Page<UserDto> getAllUsers(Pageable pageable);

    List<UserDto> getUserByName(String userName);

    UserDto getUserByEmail(String userEmail);

    UserDto getUserById (String userId);

    UserDto patchUser(String userId, UserDto patchDto);

    void deleteUser(String userId);

    UserDto signUpUser(UserDto signUpUserDto);

    void changeUserRole(String userId, ChangeRoleDto dto);

    void forgotPassword(String email);

    void resetPassword(String email, String otp, String newPassword, String confirmPassword);

    void changePassword(String userId, ChangePasswordDto dto);

    void changeAvailability(String userId);
}
