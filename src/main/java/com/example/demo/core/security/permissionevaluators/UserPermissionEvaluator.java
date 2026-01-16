package com.example.demo.core.security.permissionevaluators;

import com.example.demo.domain.user.User;
import com.example.demo.domain.user.UserRepository;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserPermissionEvaluator {

  private final UserRepository userRepository;

  public UserPermissionEvaluator(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public boolean isOwner(User principal, UUID elementId) {
    return userRepository.findById(elementId)
            .map(user -> user.getId().equals(principal.getId()))
            .orElse(false);
  }

}




