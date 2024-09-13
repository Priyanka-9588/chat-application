//package com.example.chatapp.config;
//
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Base64;
//import java.util.List;
//
//@Configuration
//public class LoginConfig {
//    private final String adminUsername = "admin";
//    private final String adminPassword = "decipher@123";
//
//    @Bean("customRestTemplate")
//    public RestTemplate customRestTemplate() {
//        return new RestTemplate();
//    }
//
//    // Dynamically get session ID (JSESSIONID) from Openfire admin login
//    public String getAdminSessionId(@Qualifier("customRestTemplate")RestTemplate restTemplate) {
//        try {
//            String loginUrl = "http://localhost:9090/login.jsp";
//            String loginData = "username=admin&password=" + adminPassword + "&login=true";
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//            HttpEntity<String> request = new HttpEntity<>(loginData, headers);
//
//            // Send POST request to login and retrieve headers
//            ResponseEntity<String> response = restTemplate.postForEntity(loginUrl, request, String.class);
//
//            // Extract JSESSIONID from response cookies
//            List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
//            if (cookies != null) {
//                for (String cookie : cookies) {
//                    if (cookie.startsWith("JSESSIONID")) {
//                        String sessionId = cookie.split(";")[0];
//                        return sessionId;
//                    }
//                }
//            }
//            throw new RuntimeException("Failed to retrieve session ID.");
//        } catch (Exception e) {
//            throw new RuntimeException("Error logging into Openfire admin: " + e.getMessage(), e);
//        }
//    }
//
//    @Bean
//    public HttpHeaders createHeaders(@Qualifier("customRestTemplate")RestTemplate customRestTemplate) {
//        String sessionId = getAdminSessionId(customRestTemplate);
//        String auth = adminUsername + ":" + adminPassword;
//        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
//        String authHeader = "Basic " + new String(encodedAuth);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", authHeader);
//        headers.add("Content-Type", "application/json");
//        headers.add("accept", "*/*");
//        headers.add("Cookie", sessionId);
//        return headers;
//    }
//
//


//    public HttpHeaders createHeaders() {
//        String auth = adminUsername + ":" + adminPassword;
//        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
//        String authHeader = "Basic " + new String(encodedAuth);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", authHeader);
//        headers.add("Content-Type", "application/json");
//        headers.add("accept", "*/*");
//        headers.add("Cookie", "JSESSIONID=node0mqa7q4z6ndeupc59s6t3k98l9.node0");
//        System.out.println("headers: " + headers);
//        return headers;
//    }


