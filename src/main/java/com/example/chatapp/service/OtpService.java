package com.example.chatapp.service;

import com.example.chatapp.model.User;
import com.example.chatapp.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    private static final long OTP_EXPIRATION_TIME = 10 * 60;


    public void sendOtpToEmail(String email) {

        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        redisTemplate.opsForValue().set(email, otp, OTP_EXPIRATION_TIME, TimeUnit.SECONDS);
        sendEmail(email, otp);
    }

    // Email sending logic
    private void sendEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setTo(toEmail);
            helper.setSubject("Email Verification OTP");
            helper.setText("Your OTP for email verification is: " + otp);
            mailSender.send(message);
            System.out.println("Email sent to: " + toEmail);
        } catch (Exception e) {
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }
    }


    public boolean verifyOtp(String email, String inputOtp) {
        // Retrieve OTP from Redis
        String cachedOtp = redisTemplate.opsForValue().get(email);

        if (cachedOtp != null && cachedOtp.equals(inputOtp)) {
            // OTP is valid, mark the user as verified
            User user = userRepository.findByEmail(email);
            if (user != null) {
                user.setVerified(true);
                userRepository.save(user);
                return true;
            }
        }

        return false;
    }
}
