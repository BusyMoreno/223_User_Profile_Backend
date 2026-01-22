package com.example.demo.domain.userProfile;

import com.example.demo.core.generic.AbstractEntity;
import com.example.demo.domain.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name= "user_profiles")
@Getter
@Setter
@NoArgsConstructor
public class UserProfile extends AbstractEntity {
    @Column(name="address")
    private String address;

    @Column(name="birth_date")
    private LocalDate birthDate;

    @Column(name="profile_image_url")
    @Pattern(
            regexp = "https?://.*",
            message = "Invalid URL"
    )
    private String profileImageUrl;

    @OneToOne
    @JoinColumn(name="user_id", nullable=false, unique=true)
    private User user;
}
