package com.example.chatting_app.views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.chatting_app.R;
import com.example.chatting_app.adapters.MessageListAdapter;
import com.example.chatting_app.models.Chat;
import com.example.chatting_app.models.Message;
import com.example.chatting_app.models.PhotoMessage;
import com.example.chatting_app.models.TextMessage;
import com.example.chatting_app.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.sendBtn)
    ImageView mSendBtn;
    @BindView(R.id.editContent)
    EditText mEditMsgText;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.chat_rec_view)
    RecyclerView mChatLogRecyclerView;

    private String mChatId;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mChatRef;
    private DatabaseReference mChatMemberRef;
    private DatabaseReference mChatMessageRef;
    private DatabaseReference mUserRef;
    private FirebaseUser fUser;
    private static final int TAKE_PHOTO_REQUEST_CODE = 201;
    private StorageReference mImageStorageRef;
    private FirebaseAnalytics mFirebaseAnalytics;
    private MessageListAdapter mMessageListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        mChatId = getIntent().getStringExtra("chat_id");
        mDatabase = FirebaseDatabase.getInstance();
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserRef = mDatabase.getReference("Users");

        mToolbar.setTitleTextColor(Color.WHITE);
        if (mChatRef != null) {
            mChatRef = mDatabase.getReference("Users").child(fUser.getUid()).child("chats").child(mChatId);
            mChatMessageRef = mDatabase.getReference("chat_message").child(mChatId);
            mChatMemberRef = mDatabase.getReference("chat_members").child(mChatId);
            ChatFragment.JOINED_ROOM = mChatId;
            initTotalUnreadCount();
        } else {
            mChatRef = mDatabase.getReference("Users").child(fUser.getUid()).child("chats");
        }
        mMessageListAdapter = new MessageListAdapter();
        mChatLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mChatLogRecyclerView.setAdapter(mMessageListAdapter);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mChatId != null) {
            removeMessageListener();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mChatId != null) {

            // 총 메세지의 카운터를 가져온다.
            // onchildadded 호출한 변수의 값이 총메세지의 값과 크거나 같다면, 포커스를 맨아래로 내려줍니다.
            mChatMessageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long totalMessageCount =  dataSnapshot.getChildrenCount();
                    mMessageEventListener.setTotalMessageCount(totalMessageCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            mMessageListAdapter.clearItem();
            addChatListener();
            addMessageListener();
        }
    }
    private void initTotalUnreadCount() {
        mChatRef.child("totalUnreadCount").setValue(0);
    }

    MessageEventListener mMessageEventListener = new MessageEventListener();

    private void addChatListener(){
        mChatRef.child("title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title = dataSnapshot.getValue(String.class);
                if ( title != null ) {
                    mToolbar.setTitle(title);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addMessageListener() {
        mChatMessageRef.addChildEventListener(mMessageEventListener);
    }

    private void removeMessageListener() {
        mChatMessageRef.removeEventListener(mMessageEventListener);
    }

    private class MessageEventListener implements ChildEventListener {

        private long totalMessageCount;
        private long callCount = 1;

        public void setTotalMessageCount(long totalMessageCount) {
            this.totalMessageCount = totalMessageCount;
        }

        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            // 신규 메시지
            Message item = dataSnapshot.getValue(Message.class);

            // 읽음 처리
            // chat_messages > {chat_id}< {message_id} > readUserList
            // 내가 존재 하는지 확인
            // 존재시, 미존재 시,
            // chat_messages > {chat_id}< {message_id} > readUserList -= 1
            // readUserList에 내 uid 추가
            List<String> readUserUIDList = item.getReadUserList();
            if ( readUserUIDList != null ) {
                if ( !readUserUIDList.contains(fUser.getUid())) {
                    // chat_messages > {chat_id} > {message_id} >  unreadCount -= 1

                    // messageRef.setValue();
                    dataSnapshot.getRef().runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            Message mutableMessage = mutableData.getValue(Message.class);
                            // readUserList에 내 uid 추가
                            // unreadCount -= 1

                            List<String> mutabledReadUserList = mutableMessage.getReadUserList();
                            mutabledReadUserList.add(fUser.getUid());
                            int mutableUnreadCount = mutableMessage.getUnreadCount() - 1;

                            if ( mutableMessage.getMessageType() == Message.MessageType.PHOTO) {
                                PhotoMessage mutablePhotoMessage = mutableData.getValue(PhotoMessage .class);
                                mutablePhotoMessage.setReadUserList(mutabledReadUserList);
                                mutablePhotoMessage.setUnreadCount(mutableUnreadCount);
                                mutableData.setValue(mutablePhotoMessage);
                            } else {
                                TextMessage mutableTextMessage = mutableData.getValue(TextMessage.class);
                                mutableTextMessage.setReadUserList(mutabledReadUserList);
                                mutableTextMessage.setUnreadCount(mutableUnreadCount);
                                mutableData.setValue(mutableTextMessage);
                            }
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                            //0.5 초 정도 후에 언리드카운트의 값을 초기화.
                            // Timer // TimeTask
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    initTotalUnreadCount();
                                }
                            }, 500);
                        }
                    });
                }
            }

            //ui
            if (item.getMessageType() == Message.MessageType.TEXT) {
                TextMessage textMessage = dataSnapshot.getValue(TextMessage.class);
                mMessageListAdapter.addItem(textMessage);
            } else if (item.getMessageType() == Message.MessageType.PHOTO) {
                PhotoMessage photoMessage = dataSnapshot.getValue(PhotoMessage.class);
                mMessageListAdapter.addItem(photoMessage);
            }
            if ( callCount >= totalMessageCount) {
                // 스크롤을 맨 마지막으로 내린다.
                mChatLogRecyclerView.scrollToPosition(mMessageListAdapter.getItemCount() - 1);
            }
            callCount++;
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            // 변경된 메시지
            // 변경된 메시지 데이터 어댑터로 전달
            // 메시지 아이디 번호로 해당 메세지의 위치를 알아냄
            Message item = dataSnapshot.getValue(Message.class);

            //ui
            if (item.getMessageType() == Message.MessageType.TEXT) {
                TextMessage textMessage = dataSnapshot.getValue(TextMessage.class);
                mMessageListAdapter.updateItem(textMessage);
            } else if (item.getMessageType() == Message.MessageType.PHOTO) {
                PhotoMessage photoMessage = dataSnapshot.getValue(PhotoMessage.class);
                mMessageListAdapter.updateItem(photoMessage);
            }
        }
        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            // 삭제
        }
        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            // 이동되었을 때
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // 취소 되었을 때,
        }
    }

    @OnClick(R.id.sendBtn)
    public void onSendEvent(View v){

        if ( mChatId != null ) {
            sendMessage();
        } else {
            createChat();
        }
    }

    @OnClick(R.id.photoSend)
    public void onPhotoSendEvent(View v) {
        // 안드로이드 파일창 오픈 (갤러리 오픈)
        // requestcode = 201
        //TAKE_PHOTO_REQUEST_CODE

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == TAKE_PHOTO_REQUEST_CODE ) {
            if ( data != null ) {

                // 업로드 이미지를 처리 합니다.
                // 이미지 업로드가 완료된 경우
                // 실제 web 에 업로드 된 주소를 받아서 photoUrl로 저장
                // 그다음 포토메세지 발
                uploadImage(data.getData());
            }
        }
    }
    private String mPhotoUrl = null;
    private Message.MessageType mMessageType = Message.MessageType.TEXT;
    private void uploadImage(Uri data){
        if ( mImageStorageRef == null ) {
            mImageStorageRef = FirebaseStorage.getInstance().getReference("/chats/").child(mChatId);
        }
        mImageStorageRef.putFile(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if ( task.isSuccessful() ) {
                    //mPhotoUrl = task.getResult().getDownloadUrl().toString();
                    mMessageType = Message.MessageType.PHOTO;
                    sendMessage();
                }
            }
        });
        //firebase Storage
    }


    private Message message = new Message();

    private void sendMessage() {
        // 메시지 키 생성
        mChatMessageRef = mDatabase.getReference("chat_messages").child(mChatId);
        String messageId = mChatMessageRef.push().getKey(); // chat_message > {chat_id} > {message_id} > messageInfo
        String messageText = mEditMsgText.getText().toString();

        final Bundle bundle = new Bundle();
        bundle.putString("me", fUser.getEmail());
        bundle.putString("roomId", mChatId);

        if ( mMessageType == Message.MessageType.TEXT ) {
            if ( messageText.isEmpty()) {
                return;
            }
            message = new TextMessage();
            ((TextMessage)message).setMessageText(messageText.trim());
            bundle.putString("messageType", Message.MessageType.TEXT.toString());
        } else if ( mMessageType == Message.MessageType.PHOTO ){
            message = new PhotoMessage();
            ((PhotoMessage)message).setPhotoUrl(mPhotoUrl);
            bundle.putString("messageType", Message.MessageType.PHOTO.toString());
        }

        TextMessage textMessage = new TextMessage();
        textMessage.setMessageText(messageText);
        textMessage.setMessageDate(new Date());
        textMessage.setChatId(mChatId);
        textMessage.setMessageId(messageId);
        textMessage.setMessageType(Message.MessageType.TEXT);
        textMessage.setMessageUser(new User(fUser.getUid(), fUser.getEmail(), fUser.getDisplayName(), fUser.getPhotoUrl().toString()));
        textMessage.setReadUserList(Arrays.asList(new String[]{fUser.getUid()}));
        String[] uids = getIntent().getStringArrayExtra("uids");
        if (uids != null) {
            textMessage.setUnreadCount(uids.length-1);
        }
        mEditMsgText.setText("");     // 메시지 출력 처리
        mMessageType = Message.MessageType.TEXT;
        mChatMemberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // unreadCount 셋팅하기 위한 대화 상대 수 가져옴.
                long memberCount = dataSnapshot.getChildrenCount();
                textMessage.setUnreadCount((int) (memberCount-1));
                // 메시지 저장
                mChatMessageRef.child(textMessage.getMessageId()).setValue(textMessage, new DatabaseReference.CompletionListener() {
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

                                Bundle bundle = new Bundle();
                                bundle.putString("me", fUser.getEmail());
                                bundle.putString("roomId", mChatId);
                                mFirebaseAnalytics.logEvent("createChat", bundle);
                                ChatFragment.JOINED_ROOM = mChatId;
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