package com.example.chatapp.service;

import com.example.chatapp.dto.UserDto;
import com.example.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public void validateUserDto(UserDto userDto) {
        if (userDto.getUsername() == null || userDto.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required and cannot be blank.");
        }

        if (userDto.getPassword() == null || userDto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required and cannot be blank.");
        }

        if (userDto.getName() == null || userDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required and cannot be blank.");
        }

        if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty() || !userDto.getEmail().contains("@")) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }

        if (userRepository.findByEmail(userDto.getEmail()) != null) {
            throw new IllegalArgumentException("Email is already taken.");
        }
    }

    public void validateUserUpdate(UserDto userDto) {
        if (userDto.getUsername() == null || userDto.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required and cannot be blank.");
        }

        if (userDto.getName() == null || userDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required and cannot be blank.");
        }


    }

}


