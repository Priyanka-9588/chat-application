package com.example.chatapp.repository;



import com.example.chatapp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByFromUser(String fromUser);
    // Method to find messages by recipient
    List<Message> findByToUser(String toUser);
}

