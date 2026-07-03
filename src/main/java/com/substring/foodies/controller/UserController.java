package com.substring.foodies.controller;

import com.substring.foodies.dto.*;
import com.substring.foodies.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(@RequestParam(value="page", required = false, defaultValue = "0") int page,
                                                     @RequestParam(value="size", required = false, defaultValue = "10") int size,
                                                     @RequestParam(value="sortBy", required = false, defaultValue = "id") String sortBy,
                                                     @RequestParam(value="sortDir", required = false, defaultValue = "asc") String sortDir)
    {
        Sort sort=sortDir.equalsIgnoreCase("asc")?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageable= PageRequest.of(page, size, sort);

        Page<UserDto> allUsers = userService.getAllUsers(pageable);
        return new ResponseEntity<>(allUsers,HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id)
    {
        UserDto user = userService.getUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getUsersByName(
            @RequestParam("name") String name
    ) {
        List<UserDto> users = userService.getUserByName(name);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PatchMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable("id") String userId,
            @Valid @RequestBody ChangePasswordDto dto
    ) {
        userService.changePassword(userId, dto);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUserById(@PathVariable String id, @Valid @RequestBody UserPutDto userDto)
    {
        UserDto user = userService.updateUser(id, userDto);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PatchMapping("/patch/{id}")
    public ResponseEntity<?> patchUser(
            @PathVariable String id,
            @RequestBody UserDto userDto
    ) {

        UserDto updatedUser = userService.patchUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}/role-change")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeRole(@PathVariable String id, @Valid @RequestBody ChangeRoleDto dto)
    {
        userService.changeUserRole(id, dto);
        return new ResponseEntity<>("Role changed successfully", HttpStatus.OK);
    }

    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('DELIVERY_BOY') or hasRole('ADMIN')")
    public ResponseEntity<?> changeAvailability(@PathVariable String id) {

        userService.changeAvailability(id);

        return ResponseEntity.ok(
                "Availability status updated successfully."
        );
    }


    @DeleteMapping("/deleteMyAccount/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable String id)
    {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-credential/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request) {

        userService.forgotPassword(request.getEmail());

        // Always return same response (prevents email enumeration)
        return ResponseEntity.ok(
                Map.of("message", "If the email exists, an OTP has been sent.")
        );
    }

    @PostMapping("/change-credential/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request) {

        userService.resetPassword(
                request.getEmail(),
                request.getOtp(),
                request.getNewPassword(),
                request.getConfirmPassword()
        );

        return ResponseEntity.ok(
                Map.of("message", "Password reset successful")
        );
    }

}
