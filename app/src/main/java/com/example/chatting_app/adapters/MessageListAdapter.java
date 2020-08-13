package com.example.chatting_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatting_app.R;
import com.example.chatting_app.customviews.RoundedImageView;
import com.example.chatting_app.models.Message;
import com.example.chatting_app.models.PhotoMessage;
import com.example.chatting_app.models.TextMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {

    private ArrayList<Message> mMessageList;
    private SimpleDateFormat messageDateFormat = new SimpleDateFormat("MM/dd a\n hh:mm");
    private String userId;

    public MessageListAdapter() {
        mMessageList = new ArrayList<>();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void addItem(Message item) {
        mMessageList.add(item);
        notifyDataSetChanged();
    }

    public Message getItem(int position) {
        return mMessageList.get(position);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // View를 이용한 뷰홀더 리턴
        View messageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_message_item, parent, false);
        return new MessageViewHolder(messageView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        // 전달받은 뷰 홀더를 이용한 뷰 구현
        Message item = getItem(position);

        TextMessage textMessage = null;
        PhotoMessage photoMessage = null;

        if (item instanceof TextMessage) {
            textMessage = (TextMessage) item;
        } else if (item instanceof PhotoMessage) {
            photoMessage = (PhotoMessage) item;
        }

        // 메시지의 송수신의 차이 구별
        if (userId.equals(item.getMessageUser().getUid())) {
            // 내가 보냄
            // 텍스트 또는 포토인지 메시지 인지 구별
            if (item.getMessageType() == Message.MessageType.TEXT) {
                holder.sendTxt.setText(textMessage.getMessageText());
                holder.sendTxt.setVisibility(View.VISIBLE);
                holder.sendImage.setVisibility(View.GONE);

            } else if (item.getMessageType() == Message.MessageType.PHOTO) {

                Glide.with(holder.sendArea).load(photoMessage.getPhotoUrl()).into(holder.sendImage);
                holder.sendTxt.setVisibility(View.GONE);
                holder.sendImage.setVisibility(View.VISIBLE);
            }

            if (item.getUnreadCount() > 0) {
                holder.sendUnreadCount.setText(String.valueOf(item.getUnreadCount()));
            }
            holder.sendDate.setText(messageDateFormat.format(item.getMessageDate()));
            holder.yourArea.setVisibility(View.GONE);
            holder.sendArea.setVisibility(View.VISIBLE);
            holder.exitArea.setVisibility(View.GONE);
        } else {
            // 상대가 보냄
            if (item.getMessageType() == Message.MessageType.TEXT) {

                holder.rcvTextView.setText(textMessage.getMessageText());

                holder.rcvTextView.setVisibility(View.VISIBLE);
                holder.sendImage.setVisibility(View.GONE);

            } else if (item.getMessageType() == Message.MessageType.PHOTO) {

                Glide.with(holder.yourArea).load(photoMessage.getPhotoUrl()).into(holder.rcvImage);

                holder.rcvTextView.setVisibility(View.GONE);
                holder.sendImage.setVisibility(View.VISIBLE);

            }
            if (item.getUnreadCount() > 0) {
                holder.rcvUnreadCount.setText(String.valueOf(item.getUnreadCount()));
            }
            if (item.getMessageUser().getProfileUrl() != null) {
                Glide.with(holder.yourArea).load(item.getMessageUser().getProfileUrl()).into(holder.rcvProfileView);
            }
            holder.rcvDate.setText(messageDateFormat.format(item.getMessageDate()));
            holder.yourArea.setVisibility(View.VISIBLE);
            holder.sendArea.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.yourChatArea)
        LinearLayout yourArea;
        @BindView(R.id.myChatArea)
        LinearLayout sendArea;
        @BindView(R.id.exitArea)
        LinearLayout exitArea;
        @BindView(R.id.rcvProfile)
        RoundedImageView rcvProfileView;
        @BindView(R.id.rcvTxt)
        TextView rcvTextView;
        @BindView(R.id.exitTxt)
        TextView exitTextView;
        @BindView(R.id.rcvImage)
        ImageView rcvImage;
        @BindView(R.id.rcvUnreadCount)
        TextView rcvUnreadCount;
        @BindView(R.id.rcvDate)
        TextView rcvDate;
        @BindView(R.id.sendUnreadCount)
        TextView sendUnreadCount;
        @BindView(R.id.sendDate)
        TextView sendDate;
        @BindView(R.id.sendTxt)
        TextView sendTxt;
        @BindView(R.id.sendImage)
        ImageView sendImage;


        public MessageViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
