package com.substring.foodies.service;

import com.substring.foodies.dto.*;
import com.substring.foodies.dto.enums.AddressType;
import com.substring.foodies.dto.enums.Role;
import com.substring.foodies.entity.Address;
import com.substring.foodies.entity.User;
import com.substring.foodies.exception.BadRequestException;
import com.substring.foodies.exception.ResourceNotFound;
import com.substring.foodies.repository.RestaurantRepository;
import com.substring.foodies.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ModelMapper modelMapper;

    private Logger logger= LoggerFactory.getLogger(UserServiceImpl.class);

    private User getLoggedInUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Invalid session"));
    }

    private User findAndValidate(String id)
    {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("User not found with id = "+id));

        return user;
    }

    @Override
    public UserDto updateUser(String userId, UserPutDto userDto) {

        User loggedInUser = getLoggedInUser();
        User existingUser = findAndValidate(userId);

        boolean isAdmin = loggedInUser.getRole() == Role.ROLE_ADMIN;
        boolean isSelf = loggedInUser.getId().equals(userId);

        // 🔐 Access control
        if (!isAdmin && !isSelf) {
            throw new AccessDeniedException(
                    "You can update only your own profile"
            );
        }

        // 🚫 Admin cannot update another admin
        if (isAdmin && existingUser.getRole() == Role.ROLE_ADMIN && !isSelf) {
            throw new AccessDeniedException(
                    "You cannot update another administrator's profile"
            );
        }

        // ✅ Allowed profile fields
        existingUser.setName(userDto.getName());
        existingUser.setPhoneNumber(userDto.getPhoneNumber());
        existingUser.setGender(userDto.getGender());

        // ✅ Address update (upsert by design)
        if (userDto.getAddress() != null) {
            Address address = existingUser.getAddress();
            if (address == null) {
                address = new Address();
                address.setUser(existingUser);
            }

            AddressDto a = userDto.getAddress();
            address.setAddressLine(a.getAddressLine());
            address.setCity(a.getCity());
            address.setState(a.getState());
            address.setPincode(a.getPincode());
            address.setCountry(a.getCountry());

            existingUser.setAddress(address);
        }

        return modelMapper.map(userRepository.save(existingUser), UserDto.class);
    }

    @Override
    public Page<UserDto> getAllUsers(Pageable page) {
        Page<User> userPage = userRepository.findAll(page);
        return userPage.map(user -> modelMapper.map(user, UserDto.class));
    }

    @Override
    public List<UserDto> getUserByName(String userName) {
        List<User> users = userRepository.findByName(userName);
        return users.stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserByEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFound(
                                "User not found with email = "+userEmail
                        )
                );
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto getUserById(String userId) {

        User user = findAndValidate(userId);
        UserDto userDto=modelMapper.map(user, UserDto.class);
        return userDto;
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {

        User loggedInUser = getLoggedInUser();
        User user = findAndValidate(userId);

        boolean isAdmin = loggedInUser.getRole() == Role.ROLE_ADMIN;
        boolean isSelf = loggedInUser.getId().equals(userId);

        // 🔐 Access control
        if (!isAdmin && !isSelf) {
            throw new AccessDeniedException(
                    "You are not allowed to delete this account."
            );
        }

        // 🚫 Admin cannot delete another admin
        if (isAdmin && user.getRole() == Role.ROLE_ADMIN && !isSelf) {
            throw new AccessDeniedException(
                    "Administrators cannot delete other administrator accounts."
            );
        }

        // 🚨 Prevent deleting the last admin
        if (user.getRole() == Role.ROLE_ADMIN &&
                userRepository.countByRole(Role.ROLE_ADMIN) == 1) {

            throw new BadRequestException(
                    "Cannot delete the last administrator account."
            );
        }

        // 🚨 Restaurant owner protection
        if (user.getRole() == Role.ROLE_RESTAURANT_ADMIN &&
                restaurantRepository.existsByOwnerId(user.getId())) {

            throw new BadRequestException(
                    "This account owns one or more restaurants. Transfer or remove the restaurants before deleting the account."
            );
        }

        userRepository.delete(user);
    }


    @Override
    public UserDto signUpUser(UserDto dto) {

        if (userRepository.existsById(dto.getId())) {
            throw new IllegalStateException(
                    "User already exists with id = " + dto.getId()
            );
        }

        // 1️⃣ Validate address
        if (dto.getAddress() == null) {
            throw new BadRequestException("Address is required");
        }


        // 2️⃣ Map & save user
        User user = modelMapper.map(dto, User.class);

        Address address = modelMapper.map(dto.getAddress(), Address.class);
        address.setUser(user);
        address.setAddressType(AddressType.USER);
        user.setAddress(address);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    @Transactional
    public UserDto patchUser(String userId, UserDto patchDto) {

        User loggedInUser = getLoggedInUser();
        User user = findAndValidate(userId);

        boolean isAdmin = loggedInUser.getRole() == Role.ROLE_ADMIN;
        boolean isSelf = loggedInUser.getId().equals(userId);

        if (!isAdmin && !isSelf) {
            throw new AccessDeniedException("You can patch only your own profile.");
        }

        if (isAdmin && user.getRole() == Role.ROLE_ADMIN && !isSelf) {
            throw new AccessDeniedException("You cannot patch another admin.");
        }

        if (patchDto.getName() != null) {
            user.setName(patchDto.getName());
        }

        if (patchDto.getPhoneNumber() != null) {
            user.setPhoneNumber(patchDto.getPhoneNumber());
        }

        // Address patch (NO addressId)
        if (patchDto.getAddress() != null) {
            Address address = user.getAddress();
            if (address == null) {
                address = new Address();
                address.setUser(user);
            }

            AddressDto a = patchDto.getAddress();
            if (a.getAddressLine() != null) address.setAddressLine(a.getAddressLine());
            if (a.getCity() != null) address.setCity(a.getCity());
            if (a.getState() != null) address.setState(a.getState());
            if (a.getPincode() != null) address.setPincode(a.getPincode());
            if (a.getCountry() != null) address.setCountry(a.getCountry());

            user.setAddress(address);
        }

        return modelMapper.map(userRepository.save(user), UserDto.class);
    }


    @Override
    @Transactional
    public void changeUserRole(String userId, ChangeRoleDto dto) {

        User admin = getLoggedInUser();

        // 1️⃣ Only ADMIN can change roles
        if (admin.getRole() != Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Admin access required");
        }

        // 2️⃣ Role must be provided
        if (dto.getRole() == null) {
            throw new BadRequestException("Target role must be specified.");
        }

        User user = findAndValidate(userId);

        // 3️⃣ Cannot modify another ADMIN
        if (user.getRole() == Role.ROLE_ADMIN && !admin.getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to modify another administrator's role.");
        }

        // 4️⃣ Null-safe restaurant ownership check
        boolean ownsRestaurant =
                user.getRestaurantList() != null &&
                        !user.getRestaurantList().isEmpty();

        // 5️⃣ Restaurant owner downgrade protection
        if (ownsRestaurant &&
                (dto.getRole() == Role.ROLE_USER ||
                        dto.getRole() == Role.ROLE_DELIVERY_BOY)) {

            throw new BadRequestException(
                    "This user owns one or more restaurants. Transfer ownership or delete the restaurants before downgrading the role."
            );
        }

        // 6️⃣ Apply role change
        user.setRole(dto.getRole());
        userRepository.save(user);
    }


    @Override
    public void changePassword(String userId, ChangePasswordDto dto) {

        User user = findAndValidate(userId);
        User loggedInUser = getLoggedInUser();

        boolean isAdmin = loggedInUser.getRole() == Role.ROLE_ADMIN;
        boolean isSelf = loggedInUser.getId().equals(userId);

        if (!isAdmin && !isSelf) {
            throw new AccessDeniedException("Only account owners or admins can change a password.");
        }

        if (isAdmin && user.getRole() == Role.ROLE_ADMIN && !isSelf) {
            throw new AccessDeniedException("You cannot change password for another admin.");
        }

        if (dto.getNewPassword().equals(dto.getOldPassword())) {
            throw new BadRequestException("Please choose a new password.");
        }
        // validate new password first
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BadRequestException("Password and Confirm Password must match.");
        }

        // old password only required for self-change
        if (!isAdmin &&
                !passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new AccessDeniedException("Old password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFound("User not found with email = " + email));

        String otp = String.valueOf(
                new SecureRandom().nextInt(900000) + 100000
        );

        user.setResetOtp(otp);
        user.setOtpExpiry(
                LocalDateTime.now(ZoneId.of("Asia/Kolkata")).plusMinutes(10)
        );

        userRepository.save(user);

        emailService.sendSimpleMail(
                user.getEmail(),
                "Password Reset OTP",
                "Your OTP is: " + otp + "\nValid for 10 minutes."
        );
    }


    @Override
    @Transactional
    public void resetPassword(String email, String otp, String newPassword, String confirmPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFound("User not found with email"));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(otp)) {
            throw new IllegalStateException("Invalid OTP");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))) {
            throw new IllegalStateException("OTP expired");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new BadRequestException("Password and Confirm Password must match.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetOtp(null);
        user.setOtpExpiry(null);

        userRepository.save(user);
    }



    @Override
    @Transactional
    public void changeAvailability(String userId) {

        User loggedInUser = getLoggedInUser();
        User user = findAndValidate(userId);

        boolean isAdmin = loggedInUser.getRole() == Role.ROLE_ADMIN;
        boolean isSelf = loggedInUser.getId().equals(userId);

        // 🔐 Only self or ADMIN
        if (!isAdmin && !isSelf) {
            throw new AccessDeniedException("You are not allowed to change this availability.");
        }

        // 🚫 Only DELIVERY_BOY
        if (user.getRole() != Role.ROLE_DELIVERY_BOY) {
            throw new BadRequestException("Availability can be changed only for delivery partners.");
        }

        // 🔄 Toggle availability
        user.setAvailable(!user.isAvailable());

        userRepository.save(user);
    }

}
