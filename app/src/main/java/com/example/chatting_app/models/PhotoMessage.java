package com.example.chatting_app.models;

import lombok.Data;

@Data
public class PhotoMessage extends Message {
    private String photoUrl;
}
