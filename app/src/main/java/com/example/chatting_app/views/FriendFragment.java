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
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.chatting_app.R;
import com.example.chatting_app.adapters.FriendListAdapter;
import com.example.chatting_app.customviews.RecyclerViewItemClickListener;
import com.example.chatting_app.models.User;
import com.google.android.material.snackbar.Snackbar;
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
import butterknife.OnClick;

public class FriendFragment extends Fragment {

    @BindView(R.id.searchBar)
    LinearLayout mSearchBar;
    @BindView(R.id.editContent)
    EditText edtEmail;
    @BindView(R.id.friendRecyclerView)
    RecyclerView mRecyclerView;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser fUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserDBRef;
    private DatabaseReference mFriendsDBRef;
    private FriendListAdapter friendListAdapter;

    public FriendFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View friendView = inflater.inflate(R.layout.fragment_friend, container, false);
        ButterKnife.bind(this, friendView);

        mFirebaseAuth = FirebaseAuth.getInstance();
        fUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mFriendsDBRef = mDatabase.getReference("Users").child(fUser.getUid()).child("friends");
        mUserDBRef = mDatabase.getReference("Users");

        // 1. 리얼 타임 디비에서 나의 친구목록을 리스너를 통해 데이터 가져오기
        addFriendListener();
        // 2. 가져온 데이터를 통해 recylerView의 어댑터로 추가
        friendListAdapter = new FriendListAdapter();
        mRecyclerView.setAdapter(friendListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // 3. 아이템별로 이벤트를 주어 선택 친구와의 대화를 가능하도록 함.
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(), new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final User friend = friendListAdapter.getItem(position);

                if (friendListAdapter.getSelectionMode() == FriendListAdapter.UNSELECTION_MODE) {
                    Snackbar.make(view, friend.getName() + "님과 대화를 하시겠습니까?", Snackbar.LENGTH_LONG).setAction("예", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                            chatIntent.putExtra("uid", friend.getUid());
                            startActivity(chatIntent);
                        }
                    }).show();
                } else {
                    friend.setSelection(friend.isSelection() ? false : true);
                    int selectedUserCount = friendListAdapter.getSelectionUserCount();
                    Snackbar.make(view, selectedUserCount +"명의 친구들과 대화를 하시겠습니까?", Snackbar.LENGTH_LONG).setAction("예", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                            chatIntent.putExtra("uids", friendListAdapter.getSelectedUids());
                            startActivity(chatIntent);
                        }
                    }).show();
                }
            }
        }));

        return friendView;
    }

    public void togglesearchBar() {
        mSearchBar.setVisibility(mSearchBar.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    public void toggleSelectionMode() {
        friendListAdapter.setSelectionMode(friendListAdapter.getSelectionMode() == FriendListAdapter.SELECTION_MODE ? FriendListAdapter.UNSELECTION_MODE : FriendListAdapter.SELECTION_MODE);
    }

    @OnClick(R.id.findBtn)//  찾기 버튼의 이벤트 연결
    public void addFriend() {
        // 1. editContent의 입력된 이메일 가져옴.
        final String getEmail = edtEmail.getText().toString();
        // 2. 이메일 미입력 상태일 시, 이메일 입력 메시지 출력
        if (getEmail.isEmpty()) {
            Snackbar.make(mSearchBar, "이메일을 입력하여 주세요.", Snackbar.LENGTH_LONG).show();
            return;
        }
        // 3. 자기 자진을 친구로 등록 불가이므로, 유저와 같은 이메일이라면, 자기 자신 이메일이란 결과 메시지 출력
        else if (getEmail.equals(fUser.getEmail())) {
            Snackbar.make(mSearchBar, "자기 자신은 친구로 등록할 수 없습니다.", Snackbar.LENGTH_LONG).show();
            return;
        }
        // 4. 정상적인 이메일일 시, 정보를 조회하여 등록 상태 여부 조회
        mFriendsDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> friendsIterable = dataSnapshot.getChildren();  // users/my uid/friends/ + 1 \n 2 \n
                Iterator<DataSnapshot> friendsIterator = friendsIterable.iterator();

                while(friendsIterator.hasNext()) {
                    User user = friendsIterator.next().getValue(User.class);

                    if (user.getEmail().equals(getEmail)) {
                        Snackbar.make(mSearchBar, "이미 등록된 친구입니다.", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                }

                mUserDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> userIterator = dataSnapshot.getChildren().iterator();
                        int userCount = (int)dataSnapshot.getChildrenCount();
                        int loopCount = 1;

                        while(userIterator.hasNext()) {
                            final User currentUser = userIterator.next().getValue(User.class);

                            if (getEmail.equals(currentUser.getEmail())) {
                                // 친구 등록 로직
                                // 6. 나의 계정 정보 디비 아래에, 상대 정보를 등록
                                mFriendsDBRef.push().setValue(currentUser, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        // 나의 정보 가져오기
                                        mUserDBRef.child(fUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                // 정보 가져오기
                                                User user1 = dataSnapshot.getValue(User.class);
                                                // 7. 상대 계정 정보 디비 아래에, 나의 정보를 등록
                                                mUserDBRef.child(currentUser.getUid()).child("friends").push().setValue(user1);
                                                Snackbar.make(mSearchBar, "친구 등록이 완료되었습니다.", Snackbar.LENGTH_LONG).show();
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                                        });
                                    }
                                });
                            } else {
                                if (loopCount++ >= userCount) { // 5. 디비에 미존재 시, 가입하지 않는 친구라는 메시지를 출력
                                    // 총 사용자 명수 == loopCount
                                    // -> 등록된 사용자가 없다는 메세지를 출력합니다.
                                    Snackbar.make(mSearchBar, "가입되지 않은 친구입니다.", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void addFriendListener() {
         mFriendsDBRef.addChildEventListener(new ChildEventListener() {
             @Override
             public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                 // 친구가 추가되었을 때,
                 User friend = dataSnapshot.getValue(User.class);
                 // 2. 가져온 데이터를 통해 recylerView의 어댑터로 추가
                 drawUI(friend);
             }
             @Override
             public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
             @Override
             public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
             @Override
             public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) { }
         });
    }

    private void drawUI(User getFriend) {
        friendListAdapter.addItem(getFriend);
    }
}