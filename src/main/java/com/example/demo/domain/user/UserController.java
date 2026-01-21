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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<UserDTO> updateById(@PathVariable UUID id, @Valid @RequestBody UserDTO userDTO) {
        User user = userService.updateById(id, userMapper.fromDTO(userDTO));
        return new ResponseEntity<>(userMapper.toDTO(user), HttpStatus.OK);
    }

    //This function is an admin only function it is able to delete any user by ID
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('USER_DEACTIVATE')")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        userService.deleteUserById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //This function is an admin only function it is able to filter users based on age and name
    //The results are paginated and also sorted
    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> filterUsers(
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<User> users = userService.getFilteredPaginatedAndSortedUsers(
                minAge, maxAge, firstName, lastName, page,size
        );

        return ResponseEntity.ok(userMapper.toDTOs(users));
    }

  @PostMapping("/edit")
  @PreAuthorize("hasAuthority('USER_CREATE')")
  public ResponseEntity<User> createProfile(
          @AuthenticationPrincipal User user,
          @Valid @RequestBody UserDTO dto
  ) {
    User profile = userServiceImpl.createProfile(dto);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(profile);
  }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public UserDTO getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return userServiceImpl.getOwnProfile(userDetails.getUsername());
    }

    @PutMapping("/editUser/{id}")
    @PreAuthorize("isAuthenticated()")
    public UserDTO updateOwnProfile(@Valid @RequestBody UserDTO dto, @PathVariable UUID id) {
        return userServiceImpl.updateOwnProfile(id, dto);
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteOwnProfile(
            @AuthenticationPrincipal UserDetailsImpl principal) {

        UUID userId = principal.user().getId();
        userServiceImpl.deleteOwnProfileById(userId);

        return ResponseEntity.noContent().build();
    }
}
