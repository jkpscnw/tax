package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.UserDto;

import com.example.demo.model.User;

public interface UserService {
    void saveUser(UserDto userDto);

    User findUserByEmail(String email);
    User findUserByName(String name);
}