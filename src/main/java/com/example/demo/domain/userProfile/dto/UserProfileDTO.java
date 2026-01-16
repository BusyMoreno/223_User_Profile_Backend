package com.example.demo.domain.userProfile.dto;

import com.example.demo.domain.role.dto.RoleDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class UserProfileDTO {

    @NotBlank(message = "Address is mandatory")
    private String address;

    @NotNull(message = "Birth date is mandatory")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @NotBlank(message = "Profile picture is mandatory")
    @Pattern(
            regexp = "https?://.*",
            message = "Invalid URL"
    )
    private String profileImageUrl;

    public UserProfileDTO(String address, LocalDate birthDate, String profileImageUrl) {
        this.address = address;
        this.birthDate = birthDate;
        this.profileImageUrl = profileImageUrl;
    }

}
