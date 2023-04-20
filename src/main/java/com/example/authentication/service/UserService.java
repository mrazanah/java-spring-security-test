package com.example.authentication.service;

import com.example.authentication.form.UserCreationForm;
import com.example.authentication.domain.User;
import com.example.authentication.model.UserEntity;
import com.example.authentication.persistence.UserRepository;
import com.example.authentication.security.CustomPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;
    @Autowired
    private CustomPasswordEncoder passwordEncoder;

    public User findByUserId(Long id) {
        return repository.findById(id).map(User::new).orElseThrow(
                () -> new UsernameNotFoundException("No user with id " + id + " found"));
    }
    public User findByUsername(String username) {
        return repository.findByUsername(username).map(User::new).orElseThrow(
                () -> new UsernameNotFoundException("No user ".concat(username).concat(" found")));
    }

    public User register(UserCreationForm form) {
        User user = new User();
        user.setCreationDate(System.currentTimeMillis());
        user.setUsername(form.getEmail());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setFirstname(form.getFirstname());
        user.setLastname(form.getLastname());
        user.setPhone(form.getPhone());
        return save(user);
    }

    public boolean exists(String username) {
        return repository.existsByUsername(username);
    }

    public User save(User user) {
        return new User(repository.save(new UserEntity(user)));
    }

    public void deleteUser(Long id) {
        repository.deleteById(id);
    }
}
