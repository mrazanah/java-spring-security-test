package com.example.authentication.controller;

import com.example.authentication.domain.CustomUserDetails;
import com.example.authentication.domain.User;
import com.example.authentication.exception.AuthenticationException;
import com.example.authentication.exception.FormValidationException;
import com.example.authentication.form.LoginForm;
import com.example.authentication.form.UserCreationForm;
import com.example.authentication.form.UserUpdateForm;
import com.example.authentication.jwt.JwtTokenProvider;
import com.example.authentication.service.UserService;
import com.example.authentication.service.UserValidationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/authentication")
@Slf4j
public class UserRestController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserValidationService validationService;
    @Autowired(required = false)
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<User> showUserDetails(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userDetails.getUser());
    }
    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            log.info("Logging out " + username);

            new SecurityContextLogoutHandler().logout(request, response, authentication);
            SecurityContextHolder.getContext().setAuthentication(null);

            log.info("User " + username + ": logout successful");
        } catch (Exception e) {
            throw new AuthenticationException("Unable to logout the user", e);
        }
        return ResponseEntity.ok("logged out");
    }
    @PostMapping
    public ResponseEntity<User> signup(@RequestBody UserCreationForm form) {
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        validationService.validate(form, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new FormValidationException(bindingResult.getAllErrors()
                    .stream()
                    .map(ObjectError::getCode)
                    .collect(Collectors.joining("\n"))
            );
        }
        return ResponseEntity.ok(userService.register(form));
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginForm form, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword());
        Authentication auth = authenticationManager.authenticate(token);
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User user = userDetails.getUser();
        log.info("User {}: login successful", user.getUsername());
        user.setLastLoginDate(System.currentTimeMillis());
        userService.save(user);
        return ResponseEntity.ok(jwtTokenProvider.generateToken(auth));
    }
    @PutMapping
    public ResponseEntity<User> update(
            @AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UserUpdateForm form) {
        User user = userService.findByUserId(userDetails.getUser().getId());
        updateField(user::setFirstname, form.getFirstname());
        updateField(user::setLastname, form.getLastname());
        updateField(user::setPhone, form.getPhone());
        return ResponseEntity.ok(userService.save(user));
    }
    @DeleteMapping
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getUser().getId();
        User user = userService.findByUserId(id);
        userService.deleteUser(id);
        return ResponseEntity.ok("User ".concat(user.getUsername()).concat(" deleted"));
    }
    private <T> void updateField(Consumer<T> setter, T value) {
        Optional.ofNullable(value).ifPresent(setter);
    }
}
