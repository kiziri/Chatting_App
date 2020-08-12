package com.example.chatting_app.views;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatting_app.R;

public class FriendFragment extends Fragment {

    public FriendFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View FriendView = inflater.inflate(R.layout.fragment_friend, container, false);
        return FriendView;
    }
}