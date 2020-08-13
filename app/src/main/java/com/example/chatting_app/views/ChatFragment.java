package com.example.chatting_app.views;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatting_app.R;
import com.example.chatting_app.adapters.ChatListAdapter;
import com.example.chatting_app.customviews.RecyclerViewItemClickListener;
import com.example.chatting_app.models.Chat;
import com.example.chatting_app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatFragment extends Fragment {
    @BindView(R.id.chatRecyclerView)
    RecyclerView mChatRecyclerView;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mChatRef;
    private DatabaseReference mChatMemberRef;
    private FirebaseUser fUser;
    private ChatListAdapter mChatListAdapter;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View chatView = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, chatView);

        // 채팅방 리스너 부착
        // Users > {나의 uid} > chats
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mChatRef = mDatabase.getReference("Users").child(fUser.getUid()).child("chats");    // 채팅
        mChatMemberRef = mDatabase.getReference("chat_members");


        mChatListAdapter = new ChatListAdapter();
        mChatRecyclerView.setAdapter(mChatListAdapter);
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mChatRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(), new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Chat chat = mChatListAdapter.getItem(position);

                Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                chatIntent.putExtra("chat_id", chat.getChatId());
                startActivity(chatIntent);
            }
        }));

        addChatListener();
        return chatView;
    }

    private void addChatListener() {
        mChatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot chatDataSnapshot, @Nullable String s) {
                // 방에 대한 정보 얻어 와 UI갱신 시켜 정보 전달
                final Chat chatRoom = chatDataSnapshot.getValue(Chat.class);
                mChatMemberRef.child(chatRoom.getChatId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long memberCount = dataSnapshot.getChildrenCount();
                        Iterator<DataSnapshot> memberIterator = dataSnapshot.getChildren().iterator();
                        StringBuffer memberStringBuffer = new StringBuffer();

                        int loopCount = 1;
                        while (memberIterator.hasNext()) {
                            User member = memberIterator.next().getValue(User.class);

                            if ( !fUser.getUid().equals(member.getUid())) {
                                memberStringBuffer.append(member.getName());

                                if (memberCount - loopCount >= 1) {
                                    memberStringBuffer.append(", ");
                                }
                            }
                            if (loopCount == memberCount) {
                                // User > uid > chats > {chat_id} > title
                                String title = memberStringBuffer.toString();
                                // 항상의 업뎃이 아닌, 기존과 변경사항일 시에만
                                if (chatRoom.getTitle() == null) {
                                    chatDataSnapshot.getRef().child("title").setValue(title);
                                } else if (!chatRoom.getTitle().equals(title)){
                                    chatDataSnapshot.getRef().child("title").setValue(title);
                                }
                                chatRoom.setTitle(title);
                                drawUI(chatRoom);
                            }
                            loopCount++;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
                // 기존의 방 제목과 방 멤버의 이름들을 가져와 타이틀화 시켰을 때, 같이 않은 경우 방제목을 업데이트 시킴

            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // 변경된 방의 정보 수신
                // 나의 내가 보낸 메시지가 아닌경우와 마지막 메시지가 수정되었을 시, -> 노티출력
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void drawUI(Chat chat) {
        mChatListAdapter.addItem(chat);
    }
}