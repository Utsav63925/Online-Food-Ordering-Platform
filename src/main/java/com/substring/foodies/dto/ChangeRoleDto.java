package com.substring.foodies.dto;

import com.substring.foodies.dto.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeRoleDto {

    @NotNull(message = "Role cannot be null. Please provide a valid role.")
    private Role role;
}
