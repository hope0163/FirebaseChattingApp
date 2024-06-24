package com.hope0163.chattingapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.hope0163.chattingapp.FullScreenActivity;
import com.hope0163.chattingapp.R;
import com.hope0163.chattingapp.model.Chatting;
import com.hope0163.chattingapp.model.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChattingAdapter extends RecyclerView.Adapter<ChattingAdapter.ViewHolder> {

    Context context;
    ArrayList<Chatting> chattingArrayList;

    final int MY_TYPE = 0;
    final int OTHER_TYPE = 1;

    Chatting chatting;

    public ChattingAdapter(Context context, ArrayList<Chatting> chattingArrayList) {
        this.context = context;
        this.chattingArrayList = chattingArrayList;
    }

    // getItemViewType 메소드로 타입에 따라 다른 xml 선택
    @Override
    public int getItemViewType(int position) {

        if (chattingArrayList.get(position).userName.equals(User.userName)) {
            return MY_TYPE;
        } else {
            return OTHER_TYPE;
        }

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 초기값
        View itemView = null;

        // 나의 채팅과 남의 채팅 구분
        if (viewType == MY_TYPE) {
            itemView = LayoutInflater.from(context).inflate(R.layout.my_chatting, parent, false);
        } else {
            itemView = LayoutInflater.from(context).inflate(R.layout.other_chatting, parent, false);
        }

        return new ChattingAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        chatting = chattingArrayList.get(position);

        holder.txtUserName.setText(chatting.userName);

        // 이미지를 보내는 경우와 텍스트를 보내는 경우를 나눠서
        // 이미지를 보낼때는 ImageView를
        // 텍스트를 보낼 때는 TextView를 홀더에 담아준다.


        // message 값에 https, chattingapp, photo 가 모두 포함되어 있다면 이미지를 보내는 경우임
        if (chatting.message.contains("https") && chatting.message.contains("chattingapp") && chatting.message.contains("/o/photo")) {
            // 숨겨놨던 imgPhoto 이미지뷰를 표시하고
            // txtMessage 텍스트뷰를 숨긴다.
            holder.txtMessage.setVisibility(View.GONE);
            holder.imgPhoto.setVisibility(View.VISIBLE);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.txtTime.getLayoutParams();

            // 기존 규칙 제거
            layoutParams.addRule(RelativeLayout.BELOW, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, 0);
            layoutParams.addRule(RelativeLayout.LEFT_OF, 0);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);

            // 내가 올린 사진과 상대가 올린 사진에 대해 txtTime 위치 다르게 설정
            if (chatting.userName.equals(User.userName)) {
                layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.imgPhoto);
                layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.imgPhoto);
            } else {
                layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.imgPhoto);
                layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.imgPhoto);
            }

            holder.txtTime.setLayoutParams(layoutParams); // 변경된 레이아웃 파라미터 적용

            MultiTransformation option = new MultiTransformation(new CenterCrop(), new RoundedCorners(50));
            Log.i("url 확인", chatting.message);
            Glide.with(context).load(chatting.message).apply(RequestOptions.bitmapTransform(option)).into(holder.imgPhoto);

        } else {
            // message 값에 텍스트가 들어온 경우
            holder.imgPhoto.setVisibility(View.GONE);
            holder.txtMessage.setVisibility(View.VISIBLE);
            holder.txtMessage.setText(chatting.message);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.txtTime.getLayoutParams();

            // 기존 규칙 제거
            layoutParams.addRule(RelativeLayout.BELOW, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, 0);
            layoutParams.addRule(RelativeLayout.LEFT_OF, 0);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);


            // 내가 올린 사진과 상대가 올린 사진에 대해 txtTime 위치 다르게 설정
            if (chatting.userName.equals(User.userName)) {
                layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.txtMessage);
                layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.txtMessage);
            } else {
                layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.txtMessage);
                layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.txtMessage);
            }


            holder.txtTime.setLayoutParams(layoutParams); // 변경된 레이아웃 파라미터 적용
        }
        holder.txtTime.setText(chatting.time);
        Glide.with(context).load(chatting.profileImgUrl).into(holder.circleImgProfile);

    }

    @Override
    public int getItemCount() {
        return chattingArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtUserName;
        CircleImageView circleImgProfile;
        TextView txtMessage;
        TextView txtTime;
        ImageView imgPhoto;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            circleImgProfile = itemView.findViewById(R.id.circleImgProfile);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);

            // 사진 눌렀을때 전체화면으로 사진 띄우기
            imgPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FullScreenActivity.class);
                    int index = getAdapterPosition();

                    Chatting chatting = chattingArrayList.get(index);

                    intent.putExtra("photoUrl", chatting.message);
                    context.startActivity(intent);
                }
            });


        }
    }

}
