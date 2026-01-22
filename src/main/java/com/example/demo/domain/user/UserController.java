package com.example.demo.domain.user;

import com.example.demo.domain.user.dto.UserDTO;
import com.example.demo.domain.user.dto.UserMapper;
import com.example.demo.domain.user.dto.UserRegisterDTO;

import java.util.List;
import java.util.UUID;

import com.example.demo.domain.userProfile.UserProfile;
import com.example.demo.domain.userProfile.dto.UserProfileDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;


@Validated
@RestController
@RequestMapping("/user")
public class UserController {

  private final UserService userService;
  private final UserMapper userMapper;
  private final UserServiceImpl userServiceImpl;

  @Autowired
  public UserController(UserService userService, UserMapper userMapper, UserServiceImpl userServiceImpl) {
    this.userService = userService;
    this.userMapper = userMapper;
    this.userServiceImpl = userServiceImpl;
  }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> retrieveById(@PathVariable UUID id) {
        User user = userService.findById(id);
        return new ResponseEntity<>(userMapper.toDTO(user), HttpStatus.OK);
    }

    @GetMapping({"", "/"})
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<List<UserDTO>> retrieveAll() {
        List<User> users = userService.findAll();
        return new ResponseEntity<>(userMapper.toDTOs(users), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
        User user = userService.register(userMapper.fromUserRegisterDTO(userRegisterDTO));
        return new ResponseEntity<>(userMapper.toDTO(user), HttpStatus.CREATED);
    }

    @PostMapping("/registerUser")
    public ResponseEntity<UserDTO> registerWithoutPassword(@Valid @RequestBody UserDTO userDTO) {
        User user = userService.registerUser(userMapper.fromDTO(userDTO));
        return new ResponseEntity<>(userMapper.toDTO(user), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MODIFY') && @userPermissionEvaluator.exampleEvaluator(authentication.principal.user,#id)")
    public ResponseEntity<UserDTO> updateById(@PathVariable UUID id, @RequestBody UserDTO userDTO) {
        User user = userService.updateById(id, userMapper.fromDTO(userDTO));
        return new ResponseEntity<>(userMapper.toDTO(user), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DEACTIVATE')")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        userService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RestController
    @RequestMapping("/roles")
    @PreAuthorize("hasAuthority('USER_READ')")
    public class RoleController {

        @GetMapping
        public List<String> getAllRoles() {
            return List.of("USER", "ADMIN");
        }
    }

// This is an admin only function 
    @GetMapping("/admin/search")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<Page<UserDTO>> filterUsers(
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "lastName", "firstName"));

        Page<User> usersPage =
                userService.getFilteredPaginatedAndSortedUsers(
                        minAge,
                        maxAge,
                        firstName,
                        lastName,
                        pageable
                );

        Page<UserDTO> dtoPage = usersPage.map(userMapper::toDTO);

        return ResponseEntity.ok(dtoPage);
    }

  @PostMapping("/edit")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<User> createProfile(
          @AuthenticationPrincipal User user,
          @Valid @RequestBody UserRegisterDTO userRegisterDTO
  ) {
    User profile = userServiceImpl.createProfile(userRegisterDTO);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(profile);
  }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('USER_READ_OWN_PROFILE')")
    public UserDTO getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return userServiceImpl.getOwnProfile(userDetails.getUsername());
    }

    @PutMapping("/editUser/{id}")
    @PreAuthorize("hasAuthority('USER_MODIFY')")
    public UserDTO updateOwnProfile(@Valid @RequestBody UserDTO dto, @PathVariable UUID id) {
        return userServiceImpl.updateOwnProfile(id, dto);
    }

// User can only delete own profile this is a user specific function
    @DeleteMapping("/me")
    @PreAuthorize("hasAuthority('USER_DELETE_OWN_PROFILE')")
    public ResponseEntity<Void> deleteOwnProfile(@Valid
            @AuthenticationPrincipal UserDetailsImpl principal) {

        UUID userId = principal.user().getId();
        userServiceImpl.deleteOwnProfileById(userId);

        return ResponseEntity.noContent().build();
    }
}
