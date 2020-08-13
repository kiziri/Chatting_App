package com.example.chatting_app.views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.chatting_app.R;
import com.example.chatting_app.models.Chat;
import com.example.chatting_app.models.Message;
import com.example.chatting_app.models.TextMessage;
import com.example.chatting_app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.sendBtn)
    ImageView mSendBtn;
    @BindView(R.id.editContent)
    EditText mEditMsgText;


    private String mChatId;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mChatRef;
    private DatabaseReference mChatMemberRef;
    private DatabaseReference mChatMessageRef;
    private DatabaseReference mUserRef;
    private FirebaseUser fUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        mChatId = getIntent().getStringExtra("chat_id");
        mDatabase = FirebaseDatabase.getInstance();
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserRef = mDatabase.getReference("Users");


    }

    @OnClick(R.id.sendBtn)
    public void onSendEvent(View view) {
        if (mChatId != null) {
             sendMessage();
        } else {
            createChat();
        }
    }

    private void sendMessage() {
        // 메시지 키 생성
        mChatMessageRef = mDatabase.getReference("chat_message").child(mChatId);
        String messageId = mChatMessageRef.push().getKey(); // chat_message > {chat_id} > {message_id} > messageInfo
        String messageText = mEditMsgText.getText().toString();

        if ( messageText.isEmpty()) {
            return;
        }
        TextMessage textMessage = new TextMessage();
        textMessage.setMessageText(messageText);
        textMessage.setMessageDate(new Date());
        textMessage.setChatId(mChatId);
        textMessage.setMessageId(messageId);
        textMessage.setMessageType(Message.MessageType.TEXT);
        textMessage.setReadUserLsit(Arrays.asList(new String[]{fUser.getUid()}));
        String[] uids = getIntent().getStringArrayExtra("uids");
        if (uids != null) {
            textMessage.setUnreadCount(uids.length-1);
        }
        mEditMsgText.setText("");     // 메시지 출력 처리
        mChatMemberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // unreadCount 셋팅하기 위한 대화 상대 수 가져옴.
                long memberCount = dataSnapshot.getChildrenCount();
                textMessage.setUnreadCount((int) (memberCount-1));
                // 메시지 저장
                mChatMessageRef.setValue(textMessage, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        Iterator<DataSnapshot> memberIterator = dataSnapshot.getChildren().iterator();
                        while(memberIterator.hasNext()) {
                            User chatMember = memberIterator.next().getValue(User.class);
                            mUserRef.child(chatMember.getUid()).child("chats").child(mChatId).child("lastMessage").setValue(textMessage);

                            if ( !chatMember.getUid().equals(fUser.getUid())) {
                                mUserRef.child(chatMember.getUid()).child("chats").child(mChatId).child("totalUnreadCount").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        long totalUnreadCount = dataSnapshot.getValue(long.class);
                                        dataSnapshot.getRef().setValue(totalUnreadCount+1);
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
                            }
                        }
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    }

    private boolean isSentMessage = false;
    private void createChat() {
        // 0. 방 생성
        // 1. 방 정보 설정 <- 기본 방이여야 가능함.
        // 1-1. 대화 상대에 내가 선택한 사람 추가
        // 2. 각 상대별 chats에 방 추가
        // 3. 메시지 정보 중 읽은 사람에 내 정보 추가

        // 4. 첫 메시지 전송
        Chat chat = new Chat();
        mChatRef = mDatabase.getReference("Users").child(fUser.getUid()).child("chats");
        mChatId = mChatRef.push().getKey();  //Users > {uid} > chats > {chat_id}
        mChatMemberRef = mDatabase.getReference("chat_members").child(mChatId);

        chat.setChatId(mChatId);
        chat.setCreateData(new Date());

        String uid = getIntent().getStringExtra("uid");
        String[] uids = getIntent().getStringArrayExtra("uids");
        if (uid != null) {
            // 1 : 1
            uids = new String[] {uid};
        }

        List<String> uidList = new ArrayList<>(Arrays.asList(uids));
        uidList.add(fUser.getUid());

        // uid 등록 로직
        for ( String userId : uidList) {
            // uid > userInfo
            mUserRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User member = dataSnapshot.getValue(User.class);

                    mChatMemberRef.child(member.getUid()).setValue(member, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            // Users > uid > chats > {chat_id} > chatInfo
                            dataSnapshot.getRef().child("chats").child(mChatId).setValue(chat); // 원하는 유저id까 chat에 charid에 chat정보
                            if ( !isSentMessage ) {
                                sendMessage();
                                isSentMessage = true;
                            }
                        }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }
}