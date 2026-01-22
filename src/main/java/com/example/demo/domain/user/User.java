package com.example.demo.domain.user;

import com.example.demo.core.generic.AbstractEntity;
import com.example.demo.domain.role.Role;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.example.demo.domain.userProfile.UserProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class User extends AbstractEntity {

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "email", unique = true, nullable = false)
  private String email;

  @Column(name = "password")
  private String password;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "users_role", joinColumns = @JoinColumn(name = "users_id", referencedColumnName = "id"),
             inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
  private Set<Role> roles = new HashSet<>();

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private UserProfile profile;

  @PrePersist
  public void logNewUserAttempt() {
    System.out.println("Attempting to add new user with username: " + getEmail());
  }

  @PostPersist
  public void logNewUserAdded() {
    System.out.println("Added user: " + getEmail());
  }

  @PreUpdate
  public void logUpdate() {
    System.out.println("User is being updated: " + getId());
  }

  @PreRemove
  public void logDelete() {
    System.out.println("User is being deleted: " + getId());
  }

  public User(UUID id, String firstName, String lastName, String email, String password, Set<Role> roles, UserProfile profile) {
    super(id);
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
    this.roles = roles;
    this.profile = profile;
  }

}
