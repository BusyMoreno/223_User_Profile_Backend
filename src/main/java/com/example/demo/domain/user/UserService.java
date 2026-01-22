package com.example.demo.domain.user;

import com.example.demo.core.generic.AbstractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

public interface UserService extends UserDetailsService, AbstractService<User> {

    User register(User user);

    User registerUser(User user);

    Page<User> getFilteredPaginatedAndSortedUsers(
            Integer minAge,
            Integer maxAge,
            String firstName,
            String lastName,
            Pageable pageable
    );

    void deleteUserById(UUID id);

    void deleteOwnProfileById(UUID id);
}
