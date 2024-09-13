package com.example.chatapp.controller;

import com.example.chatapp.dto.UserDto;
import com.example.chatapp.model.Message;
import com.example.chatapp.repository.MessageRepository;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.service.OpenfireService;
import com.example.chatapp.service.OtpService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OtpService otpService;

    private final OpenfireService openfireService;
    @Autowired
    public UserController(OpenfireService openfireService) {
        this.openfireService = openfireService;
    }

    @PostMapping("/authenticateUser")
    public String authenticateUser(@RequestBody UserDto dto) {

        boolean isAuthenticated = openfireService.authenticateUser(dto);

        if (isAuthenticated) {
            return "User authenticated successfully.";
        } else {
            return "Authentication failed.";
        }
    }

    @PostMapping(value = "/createUser")
    public String createUser(@RequestBody UserDto userDto) {
        try {
            if (openfireService.userExists(userDto.getUsername())) {
                return "Failed to create user: User already exists.";
            }
            openfireService.createUser(userDto);
            return "User created successfully.";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Failed to create user: " + e.getMessage();
        }
    }

    @PostMapping("/createUserInOpenfire")
    public String createUserInOpenfire(@RequestBody UserDto userDto) {
        try {
            boolean userCreated = openfireService.createUserInOpenfire(userDto);
            if (userCreated) {
                return "User created in Openfire successfully.";
            } else {
                return "Failed to create user in Openfire.";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @PutMapping(value = "/updateUser")
    public String updateUser(@RequestBody UserDto userDto) {
        try {
            openfireService.updateUser(userDto);
            return "User updated successfully.";
        } catch (Exception e) {
            return "Failed to update user: " + e.getMessage();
        }
    }

    @DeleteMapping(value = "/deleteUser")
    public String deleteUser(@RequestBody UserDto userDto) {
        openfireService.deleteUser(userDto);
        return "User deleted successfully.";
    }


    @PostMapping("/sendMessage")
    public String sendMessage(@RequestBody UserDto dto) {
        try {
            openfireService.sendMessage(dto);
            return "Message sent successfully.";
        } catch (Exception e) {
            return "Failed to send message: " + e.getMessage();
        }
    }

    @GetMapping("/messages/history")
    public Map<String, List<Message>> getMessageHistory(@RequestParam String loggedInUser) {
        return openfireService.getMessageHistory(loggedInUser);
    }

    @GetMapping("/getAllUsers")
    public List<UserDto> getAllUsers() {
        return openfireService.getAllUsers();
    }

    @PostMapping("/signup")
    public String signUpUser(@Valid @RequestBody UserDto userDto, BindingResult result) {
        return openfireService.signUpUser(userDto, result);
    }

    @PostMapping("/verify")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp) {
        boolean isVerified = otpService.verifyOtp(email, otp);
        return isVerified ? "Email verified successfully!" : "Invalid OTP or email.";
    }


}