package com.substring.foodies.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RestaurantDto {

    @NotBlank(message = "Please provide the id.")
    private String id;

    @NotBlank(message = "Please provide the name of the Restaurant")
    private String name;

    private String description;

    private Set<AddressDto> addresses = new HashSet<>();

    private LocalTime openTime;

    private LocalTime closeTime;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean isOpen = true;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean isActive = true;

    @NotBlank(message = "Please provide the Owner Id")
    private String ownerId;

    private String banner;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private double rating;
}
