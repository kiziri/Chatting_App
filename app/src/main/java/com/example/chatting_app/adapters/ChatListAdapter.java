package com.example.chatting_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatting_app.R;
import com.example.chatting_app.customviews.RoundedImageView;
import com.example.chatting_app.models.Chat;
import com.example.chatting_app.views.ChatFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatHolder> {

    private ArrayList<Chat> mChatList;
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd\naa hh:mm");
    private ChatFragment mChatFragment;

    public ChatListAdapter() {
        mChatList = new ArrayList<>();
    }

    public void addItem(Chat chat) {
        mChatList.add(chat);
        notifyDataSetChanged();
    }

    public Chat getItem(int position) {
        return this.mChatList.get(position);
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_chat_item, parent, false);
        return new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
        final Chat item = getItem(position);

        holder.lastMessageView.setText(item.getLastMessage().getMessageText());
        holder.titleView.setText(item.getTitle());
        holder.lastMessageDateView.setText(sdf.format(item.getCreateData()));
        if ( item.getTotalUnreadCount() > 0) {
            holder.totalUnreadCountView.setText(String.valueOf(item.getTotalUnreadCount()));
            holder.totalUnreadCountView.setVisibility(View.VISIBLE);
        } else {
            holder.totalUnreadCountView.setVisibility(View.GONE);
            holder.totalUnreadCountView.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    public static class ChatHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.thumb)
        RoundedImageView chatThumbnailView;
        @BindView(R.id.title)
        TextView titleView;
        @BindView(R.id.lastMessage)
        TextView lastMessageView;
        @BindView(R.id.totalUnreadCount)
        TextView totalUnreadCountView;
        @BindView(R.id.lastMsgDate)
        TextView lastMessageDateView;
        @BindView(R.id.rootView)
        LinearLayout rootView;

        public ChatHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
