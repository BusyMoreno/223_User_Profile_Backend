package com.example.demo.domain.user;

import com.example.demo.core.generic.AbstractRepository;

import java.util.List;
import java.util.Optional;


import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends AbstractRepository<User> {
  Optional<User> findByEmail(String email);
  List<User> filterUsers(Integer minAge, Integer maxAge, String firstName, String lastName, Pageable pageable);


}
