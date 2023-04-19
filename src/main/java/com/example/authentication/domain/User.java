package com.example.authentication.domain;

import com.example.authentication.model.UserEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Data
@NoArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String phone;
    private Long creationDate;
    private Long lastLoginDate;

    public User(UserEntity entity) {
        this.id = entity.getId();
        this.username = entity.getUsername();
        this.password = entity.getPassword();
        this.firstname = entity.getFirstname();
        this.lastname = entity.getLastname();
        this.creationDate = entity.getCreationDate();
        this.lastLoginDate = entity.getLastLoginDate();
    }
}
