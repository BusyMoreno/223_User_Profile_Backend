package com.example.demo.domain.user;

import com.example.demo.core.generic.AbstractServiceImpl;
import com.example.demo.domain.role.Role;
import com.example.demo.domain.role.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends AbstractServiceImpl<User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Autowired
    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder, RoleService roleService) {
        super(repository);
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return ((UserRepository) repository).findByEmail(email)
                .map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException(email));
    }

    @Override
    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role defaultRole = roleService.findById(UUID.fromString("d29e709c-0ff1-4f4c-a7ef-09f656c390f1"));//Default role
        user.setRoles(Set.of(defaultRole));
        return save(user);
    }

    @Override
    //This Method can be used for development and testing. the Password for the user will be set to "1234"
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode("1234"));
        return save(user);
    }

    //Show all users without any filter omn
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


    //This method is going to filter users based on age and/or name
    //This function is an admin only function
    public List<User> getFilteredPaginatedAndSortedUsers(
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
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


}
