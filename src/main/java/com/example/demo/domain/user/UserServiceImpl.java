package com.example.demo.domain.user;

import com.example.demo.core.generic.AbstractServiceImpl;
import com.example.demo.domain.role.Role;
import com.example.demo.domain.role.RoleService;
import com.example.demo.domain.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.util.Set;
import java.util.UUID;

@Service
public class UserServiceImpl extends AbstractServiceImpl<User> implements UserService {

  private final PasswordEncoder passwordEncoder;
  private final RoleService roleService;
  private final UserRepository userRepository;

  @Autowired
  public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder, RoleService roleService, UserRepository userRepository) {
    super(repository);
    this.passwordEncoder = passwordEncoder;
      this.roleService = roleService;
      this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return ((UserRepository) repository).findByEmail(email)
                                        .map(UserDetailsImpl::new)
                                        .orElseThrow(() -> new UsernameNotFoundException(email));
  }

  @Override
  public User register(User user) {
    validateAge(user.getBirthDate());
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    Role defaultRole=roleService.findById(UUID.fromString("d29e709c-0ff1-4f4c-a7ef-09f656c390f1"));//Default role
    user.setRoles(Set.of(defaultRole));
    return save(user);
  }
  @Override
  //This Method can be used for development and testing. the Password for the user will be set to "1234"
  public User registerUser(User user){
    user.setPassword(passwordEncoder.encode("1234"));
    return save(user);
  }

  public User createProfile(User user, UserDTO userDTO){
    if (userRepository.findByEmail(userDTO.getEmail()).isPresent()){
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Profile with this email already exists");
    }

    validateAge(userDTO.getBirthDate());

    User profile = new User();
    profile.setEmail(userDTO.getEmail());
    profile.setFirstName(userDTO.getFirstName());
    profile.setLastName(userDTO.getLastName());
    profile.setPassword(passwordEncoder.encode(user.getPassword()));
    profile.setAddress(userDTO.getAddress());
    profile.setBirthDate(userDTO.getBirthDate());
    profile.setProfileImageUrl(userDTO.getProfileImageUrl());

    return save(profile);
  }

  private void validateAge(LocalDate birthDate) {
    int age = Period.between(birthDate, LocalDate.now()).getYears();

    // validates if the user is at least 13
    if (age < 13) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Age must be at least 13");
    }
  }
}
