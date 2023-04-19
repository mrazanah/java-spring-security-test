package com.example.authentication.service;

import com.example.authentication.form.UserCreationForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;
import java.util.Optional;

@Service
public class UserValidationService implements Validator {
    @Autowired
    private UserService userService;
    @Override
    public boolean supports(Class<?> clazz) {
        return UserCreationForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Optional.ofNullable(target).map(obj -> (UserCreationForm) obj).ifPresentOrElse(
                form -> validateFields(form, errors), () -> errors.reject("The form is null")
        );
    }

    private void validateFields(UserCreationForm form, Errors errors) {
        if(!StringUtils.hasText(form.getEmail())) {
            errors.rejectValue("email", "Empty value");
        } else {
            checkUsernameUnique(form.getEmail(), errors);
        }
        if(Objects.isNull(form.getPassword()) || !form.getPassword().equals(form.getPasswordCheck())) {
            errors.rejectValue("password", "Value empty or doesn't match");
        }
    }
    private void checkUsernameUnique(String email, Errors errors) {
        if(userService.exists(email)) {
            errors.rejectValue("email", "The user already exists");
        }
    }
}
