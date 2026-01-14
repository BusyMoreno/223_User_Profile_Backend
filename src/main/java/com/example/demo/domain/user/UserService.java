package com.example.demo.domain.user;

import com.example.demo.core.generic.AbstractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService, AbstractService<User> {
  User register(User user);

  User registerUser(User user);

  List<User> filterUsers(Integer minAge, Integer maxAge, String firstName, String lastName);

}
