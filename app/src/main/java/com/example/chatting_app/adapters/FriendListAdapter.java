package com.example.chatting_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatting_app.R;
import com.example.chatting_app.customviews.RoundedImageView;
import com.example.chatting_app.models.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendHolder> {
    public static final int UNSELECTION_MODE = 1;
    public static final int SELECTION_MODE = 2;
    int selectionMode = UNSELECTION_MODE;

    private ArrayList<User> friendList;

    public FriendListAdapter() {
        friendList = new ArrayList<>();
    }

    public void addItem(User friend) {
        friendList.add(friend);
        notifyDataSetChanged();
    }
    public User getItem(int position) { return this.friendList.get(position); }

    public void setSelectionMode(int selectionMode) {
        this.selectionMode = selectionMode;
        notifyDataSetChanged();
    }
    public int getSelectionMode() { return this.selectionMode; }

    public int getSelectionUserCount() {
        int selectedCount = 0;
        for (User user : friendList) {
            if (user.isSelection()) {
                selectedCount++;
            }
        }
        return selectedCount;
    }

    public String[] getSelectedUids() {
        String[] selectedUids = new String[getSelectionUserCount()];
        int i=0;
        for (User user : friendList) {
            if (user.isSelection()) {
                selectedUids[i++] = user.getUid();
            }
        }
        return selectedUids;
    }


    @NonNull
    @Override
    public FriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_friend_item, parent, false);
        FriendHolder friendHolder = new FriendHolder(view);
        return friendHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendHolder holder, int position) {
        User friend = getItem(position);
        holder.mEmailView.setText(friend.getEmail());
        holder.mNameView.setText(friend.getName());

        if (getSelectionMode() == UNSELECTION_MODE) {
            holder.friendSelectedView.setVisibility(View.GONE);
        } else {
            holder.friendSelectedView.setVisibility(View.VISIBLE);
        }

        if (friend.getProfileUrl() != null) {
            Glide.with(holder.itemView).load(friend.getProfileUrl()).into(holder.mProfileView);
        }
    }

    @Override
    public int getItemCount() { return friendList.size(); }

    public static class FriendHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.thumb)
        RoundedImageView mProfileView;
        @BindView(R.id.name)
        TextView mNameView;
        @BindView(R.id.email)
        TextView mEmailView;
        @BindView(R.id.checkbox)
        CheckBox friendSelectedView;

        private FriendHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
