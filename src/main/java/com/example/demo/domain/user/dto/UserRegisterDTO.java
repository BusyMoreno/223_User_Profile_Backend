package com.example.demo.domain.user.dto;

import com.example.demo.core.generic.AbstractDTO;

import java.time.LocalDate;
import java.util.UUID;

import com.example.demo.domain.userProfile.dto.UserProfileDTO;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class UserRegisterDTO extends AbstractDTO {

  private String firstName;

  private String lastName;

  @Email
  private String email;

  private String password;

  private UserProfileDTO profile;

  public UserRegisterDTO(UUID id, String firstName, String lastName, String email, String password, UserProfileDTO profile) {
    super(id);
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
    this.profile = profile;
  }

}
