package com.example.demo.domain.user;

import com.example.demo.core.generic.AbstractServiceImpl;
import com.example.demo.domain.role.Role;
import com.example.demo.domain.role.RoleService;
import com.example.demo.domain.user.dto.UserMapper;
import com.example.demo.domain.user.dto.UserRegisterDTO;
import com.example.demo.domain.userProfile.UserProfile;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import com.example.demo.domain.user.dto.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

import java.util.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl extends AbstractServiceImpl<User> implements UserService {

  private final PasswordEncoder passwordEncoder;
  private final RoleService roleService;
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Autowired
  public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder, RoleService roleService, UserRepository userRepository, UserMapper userMapper) {
    super(repository);
    this.passwordEncoder = passwordEncoder;
      this.roleService = roleService;
      this.userRepository = userRepository;
      this.userMapper = userMapper;
  }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return ((UserRepository) repository).findByEmail(email)
                .map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException(email));
    }

  @Override
  @Transactional
  public User register(User user) {
    validateAge(user.getProfile().getBirthDate());
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    Role userRole = roleService.findById(
            UUID.fromString("c6aee32d-8c35-4481-8b3e-a876a39b0c02")
    );
    user.setRoles(Set.of(userRole));
    if (user.getProfile() != null) {
      user.getProfile().setUser(user);
    }
    if (user.getProfile().getProfileImageUrl() != null && user.getProfile().getProfileImageUrl().isBlank()) {
      user.getProfile().setProfileImageUrl(null);
    }

    return userRepository.save(user);
  }

  @Override
  @Transactional
  public User registerUser(User user){
    if (user.getPassword() != null && !user.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(user.getPassword()));
    }
    if (user.getProfile() != null) {
      user.getProfile().setUser(user);
    }
    return userRepository.save(user);
  }

    //Show all users without any filter
    public List<User> findAll() {
        return repository.findAll();
    }

    public Integer calculateAge(LocalDate birthDate) {
        LocalDate today = LocalDate.now();
        Integer age = today.getYear() - birthDate.getYear();
        return age;
    }

  private int extractNumber(String value) {
    Matcher m = Pattern.compile("(\\d+)$").matcher(value);
    return m.find() ? Integer.parseInt(m.group(1)) : 0;
  }

    //This function is an admin only function 
    // The method is able to filter users based on age and name
    //The results are paginated and also sorted
  @Override
  public Page<User> getFilteredPaginatedAndSortedUsers(
          Integer minAge,
          Integer maxAge,
          String firstName,
          String lastName,
          Pageable pageable
  ) {

    // Convert firstName and lastName to lowercase for case-insensitive comparison
    String fName = (firstName == null || firstName.isBlank())
            ? null
            : firstName.toLowerCase();

    String lName = (lastName == null || lastName.isBlank())
            ? null
            : lastName.toLowerCase();

    List<User> filteredList = repository.findAll()
            .stream()
            .filter(u -> fName == null || u.getFirstName().toLowerCase().startsWith(fName))
            .filter(u -> lName == null || u.getLastName().toLowerCase().startsWith(lName))
            .filter(u -> minAge == null || calculateAge(u.getProfile().getBirthDate()) >= minAge)
            .filter(u -> maxAge == null || calculateAge(u.getProfile().getBirthDate()) <= maxAge)
            .sorted(Comparator
                    .comparing(
                            (User u) -> extractNumber(u.getLastName())
                    )
            )
            .toList();

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), filteredList.size());

    List<User> pageContent =
            start >= filteredList.size()
                    ? List.of()
                    : filteredList.subList(start, end);

    return new PageImpl<>(pageContent, pageable, filteredList.size());
  }

  public void deleteUserById(UUID id) {
        userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("There is no user with this id: " + id.toString()));
        userRepository.deleteById(id);
    }

  @Transactional
  public User createProfile(UserRegisterDTO userRegisterDTO) {

// Validate age user has to be at least 13 years old
    validateAge(userRegisterDTO.getProfile().getBirthDate());

    User user = new User();
    user.setEmail(userRegisterDTO.getEmail());
    user.setFirstName(userRegisterDTO.getFirstName());
    user.setLastName(userRegisterDTO.getLastName());
    user.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));

    // Assign USER role "User" to new registered users 
    Role userRole = roleService.findById(
            UUID.fromString("c6aee32d-8c35-4481-8b3e-a876a39b0c02")
    );
    user.setRoles(Set.of(userRole));

    UserProfile profile = new UserProfile();
    profile.setAddress(userRegisterDTO.getProfile().getAddress());
    profile.setBirthDate(userRegisterDTO.getProfile().getBirthDate());
    profile.setProfileImageUrl(userRegisterDTO.getProfile().getProfileImageUrl());

    // If profile image URL is blank, set it to null to avoid storing empty strings
    // PP is optional
    if (profile.getProfileImageUrl() != null && profile.getProfileImageUrl().isBlank()) {
      profile.setProfileImageUrl(null);
    }

    profile.setUser(user);
    user.setProfile(profile);

    return userRepository.save(user);
  }

  private void validateAge(LocalDate birthDate) {
    int age = Period.between(birthDate, LocalDate.now()).getYears();

    // validates if the user is at least 13
    if (age < 13) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Age must be at least 13");
    }
  }

  public UserDTO getOwnProfile(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    return userMapper.toDTO(user);
  }

  @Transactional
  public UserDTO updateOwnProfile(UUID id, UserDTO userDTO){
    User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

    // User cannot update age to be less than 13
    validateAge(userDTO.getProfile().getBirthDate());
    user.setFirstName(userDTO.getFirstName());
    user.setLastName(userDTO.getLastName());
    user.setEmail(userDTO.getEmail());

    UserProfile profile = user.getProfile();
    if (profile == null) {
      profile = new UserProfile();
      profile.setUser(user);
      user.setProfile(profile);
    }

    profile.setAddress(userDTO.getProfile().getAddress());
    profile.setBirthDate(userDTO.getProfile().getBirthDate());
    profile.setProfileImageUrl(userDTO.getProfile().getProfileImageUrl());
    profile.setProfileImageUrl(userDTO.getProfile().getProfileImageUrl());

    if (profile.getProfileImageUrl() != null && profile.getProfileImageUrl().isBlank()) {
      profile.setProfileImageUrl(null);
    }


    userRepository.save(user);

    return userMapper.toDTO(user);
  }

  @Override
  public void deleteOwnProfileById(UUID id){
    User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    userRepository.delete(user);
  }
}
