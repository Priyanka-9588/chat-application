package com.example.chatapp.service;

import com.example.chatapp.dto.UserDto;
import com.example.chatapp.model.Message;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.MessageRepository;
import com.example.chatapp.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.impl.JidCreate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.*;


@Service
public class OpenfireService {
    private final MessageRepository messageRepository;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final UserService userService;
    @Autowired
    private OtpService otpService;

    private final String domain = "desktop-6mhmd91.dlink";
    private final String host = "127.0.0.1";
    private final int port = 5222;

    private final String loginUrl = "http://localhost:9090/login.jsp";
    private final String openfireUrl = "http://localhost:9090/plugins/restapi/v1";
    private final String adminUsername = "admin";
    private final String adminPassword = "decipher@123";
    private String sessionId;
   // @Autowired
    //private HttpHeaders createHeaders;

    public OpenfireService(MessageRepository messageRepository, RestTemplate restTemplate, UserRepository userRepository, UserService userService) {
        this.messageRepository = messageRepository;
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public static HttpSession session() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true); // true == allow create

    }
    public String loginAndGetSessionId() {
        try {
            // Form the login data (like username and password)
            String loginData = "username=" + adminUsername + "&password=" + adminPassword + "&login=true";

            // Set up headers for login
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> request = new HttpEntity<>(loginData, headers);
 System.out.println(session().getId());

            // Make the POST request to login and get the response
            ResponseEntity<String> response = restTemplate.postForEntity(loginUrl, request, String.class);

            // Extract JSESSIONID from cookies
            List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (cookies != null) {
                for (String cookie : cookies) {
                    if (cookie.startsWith("JSESSIONID")) {
                     sessionId = cookie.split(";")[0];  // Extract the JSESSIONID value
                        System.out.println("Logged in. Session ID: " + cookie);
                        return sessionId;
                    }
                }
            }
            throw new RuntimeException("Failed to retrieve session ID.");
        } catch (Exception e) {
            throw new RuntimeException("Error logging into Openfire admin: " + e.getMessage(), e);
        }
    }


    // Step 2: Create headers with session ID for API requests
    public HttpHeaders createHeadersWithSession() {

        if (sessionId == null) {
            sessionId = loginAndGetSessionId();
        }
        String auth = adminUsername + ":" + adminPassword;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authHeader);
        headers.add("Content-Type", "application/json");
        headers.add("accept", "*/*");
        headers.add("Cookie", sessionId);  // Use the dynamically fetched session ID
        return headers;
    }
    public boolean authenticateUser(UserDto dto) {
        User user = userRepository.findByUsername(dto.getUsername());

        if (user == null) {
            throw new RuntimeException("User not found.");
        }

        if (!user.isVerified()) {
            throw new RuntimeException("Email is not verified. Please verify your email first.");
        }

        XMPPTCPConnection connection = null;

        try {
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setXmppDomain(domain)
                    .setHost(host)
                    .setPort(port)
                    .setUsernameAndPassword(dto.getUsername(), dto.getPassword())
                    .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled)
                    .setCompressionEnabled(false)
                    .build();

            connection = new XMPPTCPConnection(config);
            connection.connect();
            connection.login();
            return true;

        } catch (Exception e) {
            System.out.println("An error occurred during authentication. Please try again.");

            return false;
        } finally {
            if (connection != null && connection.isConnected()) {
                connection.disconnect();
            }
        }
    }


    public void createUser(UserDto userDto) {
        userService.validateUserDto(userDto);
        if (userRepository.findByEmail(userDto.getEmail()) != null) {
            throw new RuntimeException("Email is already taken.");
        }


        boolean isCreatedInOpenfire = createUserInOpenfire(userDto);
        if (isCreatedInOpenfire) {
            createUserInDb(userDto);
        } else {
            throw new RuntimeException("Failed to create user in Openfire.");
        }
    }

    public void createUserInDb(UserDto userDto) {

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        userRepository.save(user);
    }


    public boolean createUserInOpenfire(UserDto userDto) {
        String url = openfireUrl + "/users";
        String userJson = String.format("{\"username\":\"%s\",\"password\":\"%s\",\"name\":\"%s\",\"email\":\"%s\"}",
                userDto.getUsername(), userDto.getPassword(), userDto.getName(), userDto.getEmail());
        System.out.println("userJson:" + userJson);
        System.out.println("Sending POST request to Openfire: " + url);
        System.out.println("Request: " + userJson);

        HttpHeaders headers = createHeadersWithSession();
        HttpEntity<String> request = new HttpEntity<>(userJson, headers);
        try {
            System.out.println("Sending POST request to Openfire: " + url);
            System.out.println("Headers: " + headers);
            System.out.println("Request: " + request);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("Response status: " + response.getStatusCode());
            System.out.println("Response body: " + response.getBody());
            return response.getStatusCode() == HttpStatus.CREATED;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user in Openfire: " + e.getMessage(), e);
        }

    }

    public void updateUser(UserDto userDto) {
        userService.validateUserUpdate(userDto);


        boolean isUpdatedInOpenfire = updateUserInOpenfire(userDto);
        if (isUpdatedInOpenfire) {
            updateUserInDb(userDto);
        }
    }

    public void updateUserInDb(UserDto userDto) {
        User user = userRepository.findByUsername(userDto.getUsername());
        if (user != null) {
            user.setName(userDto.getName());
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found.");
        }

    }

    public boolean updateUserInOpenfire(UserDto userDto) {
        String url = openfireUrl + "/users/" + userDto.getUsername();
        String userJson = String.format("{\"username\":\"%s\",\"name\":\"%s\"}",
                userDto.getUsername(), userDto.getName());
        HttpHeaders headers = createHeadersWithSession();

        HttpEntity<Object> request = new HttpEntity<>(userJson,headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            //ResponseEntity<HttpStatus> response = restTemplate.exchange(url, HttpMethod.PUT, request, HttpStatus.class);
            return response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.NO_CONTENT;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user in Openfire: " + e.getMessage(), e);
        }
    }

    public boolean userExists(String username) {
        try {

            String url = openfireUrl + "/users/" + username;
            HttpHeaders headers = createHeadersWithSession();
            restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            return true;

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            return false;
        }
    }


    public void deleteUser(UserDto userDto) {
        String url = openfireUrl + "/users/" + userDto.getUsername();
        HttpHeaders headers = createHeadersWithSession();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<HttpStatus> response = restTemplate.exchange(url, HttpMethod.DELETE, request, HttpStatus.class);
        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.NO_CONTENT) {
            System.out.println("User deleted from Openfire successfully.");


            User user = userRepository.findByUsername(userDto.getUsername());
            if (user != null) {
                userRepository.delete(user);
                System.out.println("User deleted from the database successfully.");
            }
        } else {
            throw new RuntimeException("Failed to delete user from Openfire. Status code: " + response.getStatusCode());
        }
    }

    public String signUpUser(@Valid UserDto userDto, BindingResult result) {

        // Check for validation errors
        if (result.hasErrors()) {
            return result.getFieldError().getDefaultMessage(); // Return the first validation error message
        }

        // Check if username already exists
        if (userRepository.findByUsername(userDto.getUsername()) != null) {
            return "Username is already taken!";
        }

        // Check if email is already taken
        if (userRepository.findByEmail(userDto.getEmail()) != null) {
            return "Email is already taken!";
        }

        // Create a new user, setting verified to false (user must verify via OTP)
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword()); // Hash password (consider using bcrypt or other hashing mechanisms)
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setVerified(false); // Mark as not verified

        // Save user to the database
        userRepository.save(user);

        // Send OTP to the user's email for verification
        otpService.sendOtpToEmail(userDto.getEmail());

        // Return success message
        return "User created successfully. Please verify your email.";
    }


    public void sendMessage(UserDto dto) throws Exception {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(domain)
                .setHost(host)
                .setPort(port)
                .setUsernameAndPassword(dto.getUsername(), dto.getPassword())
                .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled)
                .setCompressionEnabled(false)
                .build();

        XMPPTCPConnection connection = new XMPPTCPConnection(config);
        connection.connect();
        connection.login();

        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        Chat chat = chatManager.chatWith(JidCreate.entityBareFrom(dto.getToUsername() + "@" + domain));
        org.jivesoftware.smack.packet.Message smackMessage = new org.jivesoftware.smack.packet.Message();
        smackMessage.setBody(dto.getMessageBody());
        chat.send(smackMessage);
        com.example.chatapp.model.Message sentMessage = new com.example.chatapp.model.Message();
        sentMessage.setFromUser(dto.getUsername() + "@" + domain);
        sentMessage.setToUser(dto.getToUsername() + "@" + domain);
        sentMessage.setBody(dto.getMessageBody());
        sentMessage.setTimestamp(LocalDateTime.now());

        messageRepository.save(sentMessage);
        connection.disconnect();
    }

    public Map<String, List<Message>> getMessageHistory(String loggedInUser) {
        String fullUser = loggedInUser + "@" + domain;


        List<Message> sentMessages = messageRepository.findByFromUser(fullUser);
        List<Message> receivedMessages = messageRepository.findByToUser(fullUser);


        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(sentMessages);
        allMessages.addAll(receivedMessages);
        Map<String, List<Message>> messageHistory = new HashMap<>();

        for (Message message : allMessages) {
            String otherUser;
            if (message.getFromUser().equals(fullUser)) {
                otherUser = message.getToUser().split("@")[0];  // Group by recipient's username
            } else {
                otherUser = message.getFromUser().split("@")[0];  // Group by sender's username
            }


            if (!messageHistory.containsKey(otherUser)) {
                messageHistory.put(otherUser, new ArrayList<>());
            }
            messageHistory.get(otherUser).add(message);
        }

        return messageHistory;

    }

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> userDtos = new ArrayList<>();

        if (!users.isEmpty()) {
            for (User user : users) {
                UserDto dto = new UserDto();
                dto.setUsername(user.getUsername());
                dto.setName(user.getName());
                dto.setEmail(user.getEmail());
                userDtos.add(dto);
            }
        }

        return userDtos;
    }


}

