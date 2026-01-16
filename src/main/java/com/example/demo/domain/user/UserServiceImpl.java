package com.example.demo.domain.user;

import com.example.demo.core.generic.AbstractServiceImpl;
import com.example.demo.domain.role.Role;
import com.example.demo.domain.role.RoleService;
import com.example.demo.domain.user.dto.UserMapper;
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

import java.util.*;
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
    Role defaultRole = roleService.findById(
            UUID.fromString("d29e709c-0ff1-4f4c-a7ef-09f656c390f1")
    );
    user.setRoles(Set.of(defaultRole));
    if (user.getProfile() != null) {
      user.getProfile().setUser(user);
    }
    return userRepository.save(user);
  }

  @Override
  @Transactional
  //This Method can be used for development and testing. the Password for the user will be set to "1234"
  public User registerUser(User user){
    user.setPassword(passwordEncoder.encode("1234"));
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

    public Page<User> getPaginatedUsers(int page, int size, List<User> users) {
        int start = page * size;
        int end = Math.min(start + size, users.size());
        List<User> pagedList = users.subList(start, end);
        return new PageImpl<>(pagedList, PageRequest.of(page, size), users.size());
    }

  public List<User> getFilteredPaginatedAndSortedUsers(
          Integer minAge,
          Integer maxAge,
          String firstName,
          String lastName,
          int page,
          int size
  ) {
    String fName = (firstName == null || firstName.isBlank()) ? null : firstName.toLowerCase();
    String lName = (lastName == null || lastName.isBlank()) ? null : lastName.toLowerCase();

    List<User> filteredList = repository.findAll()
            .stream()
            .filter(u -> fName == null || u.getFirstName().toLowerCase().startsWith(fName))
            .filter(u -> lName == null || u.getLastName().toLowerCase().startsWith(lName))
            .filter(u -> minAge == null || calculateAge(u.getProfile().getBirthDate()) >= minAge)
            .filter(u -> maxAge == null || calculateAge(u.getProfile().getBirthDate()) <= maxAge)
            .sorted(Comparator
                    .comparing((User u) -> u.getProfile().getBirthDate())
                    .thenComparing(User::getFirstName))
            .toList();

    int start = page * size;
    int end = Math.min(start + size, filteredList.size());

    if (start >= filteredList.size()) {
      return List.of();
    }

    return filteredList.subList(start, end);
  }

    public void deleteUserById(UUID id) {
        userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("There is no user with this id: " + id.toString()));
        userRepository.deleteById(id);
    }

  @Transactional
  public User createProfile(UserDTO userDTO){

    validateAge(userDTO.getProfile().getBirthDate());

    User user = new User();
    user.setEmail(userDTO.getEmail());
    user.setFirstName(userDTO.getFirstName());
    user.setLastName(userDTO.getLastName());
    user.setPassword(passwordEncoder.encode("1234"));

    UserProfile profile = new UserProfile();
    profile.setAddress(userDTO.getProfile().getAddress());
    profile.setBirthDate(userDTO.getProfile().getBirthDate());
    profile.setProfileImageUrl(userDTO.getProfile().getProfileImageUrl());

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

    userRepository.save(user);

    return userMapper.toDTO(user);
  }

  @Override
  public void deleteOwnProfileById(UUID id){
    User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    userRepository.delete(user);
  }
}
