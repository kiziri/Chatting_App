package com.example.chatting_app.models;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class Message {
    private String messageId;
    private User messageUser;
    private String chatId;
    private int unreadCount;
    private Date messageDate;
    private MessageType messageType;
    private List<String> readUserList;

    public enum MessageType {
        TEXT, PHOTO, EXIT
    }
}
