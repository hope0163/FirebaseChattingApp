package com.hope0163.chattingapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hope0163.chattingapp.adapter.ChattingAdapter;
import com.hope0163.chattingapp.model.Chatting;
import com.hope0163.chattingapp.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ChattingActivity extends AppCompatActivity {

    ImageView imgPhoto;
    EditText editMessage;
    ImageView imgSend;
    RecyclerView recyclerView;
    ArrayList<Chatting> chattingArrayList = new ArrayList<>();
    ChattingAdapter adapter;

    FirebaseFirestore firestore;
    CollectionReference chatRef;
    String chatName = "chat";
    Uri photoUri = null;
    public String photoUrl = null;

    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        imgPhoto = findViewById(R.id.imgPhoto);
        editMessage = findViewById(R.id.editMessage);
        imgSend = findViewById(R.id.imgSend);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ChattingActivity.this));

        progressBar = findViewById(R.id.progressBar);


        // editText에 문자를 입력하면 imgSend의 background 색깔이 바뀜
        editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Change the background color of imgSend based on the text length
                if (s.length() > 0) {
                    imgSend.setBackgroundColor(Color.parseColor("#fae100")); // Red color
                } else {
                    imgSend.setBackgroundColor(Color.TRANSPARENT); // Transparent
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //
            }
        });

        Log.i("유저정보확인", User.userName + " " + User.profileImgUrl);


        // adapter 연결
        adapter = new ChattingAdapter(ChattingActivity.this, chattingArrayList);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        // 추가된 코드: 초기 데이터 로드 시 최하단으로 스크롤
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        });


        //
        // Firestore 데이터베이스에서 chat 컬렉션을 참조하는 chatRef 변수를 설정
        firestore = FirebaseFirestore.getInstance();
        chatRef = firestore.collection(chatName);

        //  chat 컬렉션에 저장되어 있는 데이터 읽어오기
        // chatRef의 데이터가 변경될때마다 새롭게 필드값을 가져오는 리스너
        chatRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                // 데이터가 바뀔때마다 add하면 중복되어버림
                // 따라서 변경된 Document만 찾아와야됨
                List<DocumentChange> documentChanges = value.getDocumentChanges();

                for (DocumentChange documentChange : documentChanges) {
                    // 변경된 문서내역의 데이터를 촬영한 DocumentSnapShot얻어오기
                    DocumentSnapshot snapshot = documentChange.getDocument();

                    // Documnet에 있는 필드값 가져오기
                    Map<String, Object> chattingMsg = snapshot.getData();
                    String userName = chattingMsg.get("userName").toString();
                    String message = chattingMsg.get("message").toString();
                    String profileImgUrl = chattingMsg.get("profileImgUrl").toString();
                    String time = chattingMsg.get("time").toString();

                    // 읽어온 메세지를 리스트에 추가
                    chattingArrayList.add(new Chatting(message, profileImgUrl, time, userName));

                    // 어댑터에 추가된 내용 반영
                    adapter.notifyItemInserted(chattingArrayList.size()-1);

                    progressBar.setVisibility(View.GONE);

                }

            }
        });



        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSend();
            }
        });

        imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPhoto();
            }
        });


    }

    private void loadPhoto() {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }



    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_CANCELED) return;
        progressBar.setVisibility(View.VISIBLE);
        if (result.getData() != null) {
            photoUri = result.getData().getData();
            if (photoUri != null) {
                FirebaseStorage storage = FirebaseStorage.getInstance();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                StorageReference photoRef = storage.getReference().child("photo/IMG_" + sdf.format(new Date()));

                Log.i("채팅 액티비티 확인", "photoUri : " + photoUri);

                photoRef.putFile(photoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                photoUrl = uri.toString().trim();
                                Toast.makeText(ChattingActivity.this, "사진 업로드 성공", Toast.LENGTH_SHORT).show();
                                Log.i("채팅 액티비티 확인", "photoUrl : " + photoUrl);

                                String profileImgUrl = User.profileImgUrl;
                                String userName = User.userName;
                                Calendar calendar = Calendar.getInstance();
                                String time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
                                String message = photoUrl;


                                Chatting chatting = new Chatting(message, profileImgUrl, time, userName);

                                chatRef.document("CHAT_" + System.currentTimeMillis()).set(chatting);
                                progressBar.setVisibility(View.GONE);


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("채팅 액티비티 확인", "사진 URL 가져오기 실패", e);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("채팅 액티비티 확인", "사진 업로드 실패", e);
                    }
                });
            } else {
                Log.e("채팅 액티비티 확인", "사진 Uri가 null입니다.");
            }
        } else {
            Log.e("채팅 액티비티 확인", "Intent 데이터가 null입니다.");
        }
    });


    private void clickSend() {
        // fireStore에 저장할 데이터 준비
        String message = editMessage.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "메세지를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            message = editMessage.getText().toString().trim();
        }

        String profileImgUrl = User.profileImgUrl;
        String userName = User.userName;
        Calendar calendar = Calendar.getInstance();
        String time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);

        // 필드에 넣을 값을 묶어서 Chatting 객체로 저장
        Chatting chatting = new Chatting(message, profileImgUrl, time, userName);

        // chat 컬렉션에 메세지 저장
        // document 이름을 현재시간으로 지정하여 시간순으로 정렬
        chatRef.document("CHAT_" + System.currentTimeMillis()).set(chatting);

        // 다음 메세지를 입력을 위해 EditText에 글씨 삭제
        editMessage.setText("");

    }


    // 외부영역 터치시 키보드 내려가게 하기
    // 대신 imgSend를 터치했을때는 내리지 않음
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View focusView = getCurrentFocus();
        if (focusView != null) {
            Rect rect = new Rect();
            focusView.getGlobalVisibleRect(rect);
            int x = (int) ev.getX(), y = (int) ev.getY();

            View imageView = findViewById(R.id.imgSend);
            if (imageView != null) {
                Rect imageRect = new Rect();
                imageView.getGlobalVisibleRect(imageRect);

                // 특정 뷰를 터치한 경우 키보드를 내리지 않음
                if (imageRect.contains(x, y)) {
                    return super.dispatchTouchEvent(ev);
                }
            }

            if (!rect.contains(x, y)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                }
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}