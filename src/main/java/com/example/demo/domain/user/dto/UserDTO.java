package com.example.demo.domain.user.dto;

import com.example.demo.core.generic.AbstractDTO;
import com.example.demo.domain.role.dto.RoleDTO;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class UserDTO extends AbstractDTO {

  private String firstName;

  private String lastName;

  @Email
  private String email;

  @Valid
  private Set<RoleDTO> roles;

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

  public UserDTO(UUID id, String firstName, String lastName, String email, Set<RoleDTO> roles, String address, LocalDate birthDate, String profileImageUrl) {
    super(id);
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.roles = roles;
    this.address = address;
    this.birthDate = birthDate;
    this.profileImageUrl = profileImageUrl;
  }

}
