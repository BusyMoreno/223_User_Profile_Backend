package com.example.demo.domain.user.dto;

import com.example.demo.core.generic.AbstractDTO;
import com.example.demo.domain.role.dto.RoleDTO;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import com.example.demo.domain.userProfile.dto.UserProfileDTO;
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

  private UserProfileDTO profile;


  public UserDTO(UUID id, String firstName, String lastName, String email, Set<RoleDTO> roles, UserProfileDTO profile) {
    super(id);
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.roles = roles;
    this.profile = profile;
  }

}
