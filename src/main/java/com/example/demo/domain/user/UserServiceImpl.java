package com.example.demo.domain.user;

import com.example.demo.core.generic.AbstractServiceImpl;
import com.example.demo.domain.role.Role;
import com.example.demo.domain.role.RoleService;
import com.example.demo.domain.user.dto.UserMapper;
import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.web.bind.annotation.RequestParam;

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
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<User> filteredList = repository.findAll()
                .stream()
                .filter(u -> firstName == null || u.getFirstName().equalsIgnoreCase(firstName))
                .filter(u -> lastName == null || u.getLastName().equalsIgnoreCase(lastName))
                .filter(u -> minAge == null || calculateAge(u.getBirthDate()) >= minAge)
                .filter(u -> maxAge == null || calculateAge(u.getBirthDate()) <= maxAge).sorted(Comparator.comparing(User::getBirthDate).thenComparing(User::getFirstName)).toList();

        Page<User> myPage = getPaginatedUsers(page, size, filteredList);

        return myPage.getContent();
    }


    public void deleteUserById(UUID id) {
        userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("There is no user with this id: " + id.toString()));
        userRepository.deleteById(id);
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

  public UserDTO getOwnProfile(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    return userMapper.toDTO(user);
  }

  public UserDTO updateOwnProfile(String email, UserDTO userDTO){
    User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    validateAge(userDTO.getBirthDate());
    user.setFirstName(userDTO.getFirstName());
    user.setLastName(userDTO.getLastName());
    user.setEmail(userDTO.getEmail());
    user.setAddress(userDTO.getAddress());
    user.setProfileImageUrl(userDTO.getProfileImageUrl());
    return userMapper.toDTO(user);
  }
}
