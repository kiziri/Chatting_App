package com.example.chatting_app.models;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RemoteService {
    public static final String BASE_URL = "http://192.168.1.6:8080/user/";

    @GET("insert.jsp")
    Call<List<User>> insertUser(@Query("useremail") String email, @Query("userpassword") String password, @Query("userprofileurl") String profileurl, @Query("useruid") String uid);
}
