package com.example.chatting_app.models;

import lombok.Data;

@Data
public class TextMessage extends Message{
    private String messageText;
}
