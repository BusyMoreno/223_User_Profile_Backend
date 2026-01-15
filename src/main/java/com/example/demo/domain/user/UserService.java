package com.example.demo.domain.user;

import com.example.demo.core.generic.AbstractService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService, AbstractService<User> {
    User register(User user);

    User registerUser(User user);

    List<User> getFilteredPaginatedAndSortedUsers(@RequestParam(required = false) Integer minAge,
                                                 @RequestParam(required = false) Integer maxAge,
                                                 @RequestParam(required = false) String firstName,
                                                 @RequestParam(required = false) String lastName,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "5") int size);

    void deleteUserById(UUID id);


}
