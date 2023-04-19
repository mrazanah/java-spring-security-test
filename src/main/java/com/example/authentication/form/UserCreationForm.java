package com.example.authentication.form;

import lombok.Data;

@Data
public class UserCreationForm {
    private String firstname;
    private String lastname;
    private String password;
    private String passwordCheck;
    private String email;
    private String phone;
}
