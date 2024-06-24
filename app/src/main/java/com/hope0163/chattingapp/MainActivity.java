package com.hope0163.chattingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hope0163.chattingapp.config.Config;
import com.hope0163.chattingapp.model.User;

public class MainActivity extends AppCompatActivity {

    EditText editEmail;
    EditText editPassword;
    Button btnLogin;
    TextView txtRegister;
    String email;
    String password;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.txtRegister);
        progressBar = findViewById(R.id.progressBar);

        SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        User.userName = sp.getString("userName", null);
        User.profileImgUrl = sp.getString("profileImgUrl", null);

        if (User.userName != null) {
            Intent intent = new Intent(MainActivity.this, ChattingActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        // 파이어스토어에 접근하기 위한 객체 생성
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // 파이어스토어의 user컬렉션 참조
        CollectionReference userRef = db.collection("user");


        progressBar.setVisibility(View.GONE);

        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);

                email = editEmail.getText().toString().trim();
                password = editPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "아이디와 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 파이어스토어 user 컬렉션의 문서 읽어오기
                userRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // 반복문을 이용하여
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                String fireStoreUserEmail = doc.getString("email");
                                String fireStoreUserPassword = doc.getString("password");
                                String fireStoreUserName = doc.getString("userName");
                                String fireStoreProfileImgUrl = doc.getString("profileImgUrl");
                                String userInputEmail = email;
                                String userInputPassword = password;

                                if (userInputEmail.equals(fireStoreUserEmail) && userInputPassword.equals(fireStoreUserPassword)) {
                                    Toast.makeText(MainActivity.this, "로그인 하였습니다.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, ChattingActivity.class);

                                    User.userName = fireStoreUserName;
                                    User.profileImgUrl = fireStoreProfileImgUrl;

                                    SharedPreferences sp = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("userName", fireStoreUserName);
                                    editor.putString("profileImgUrl", fireStoreProfileImgUrl);
                                    editor.commit();

                                    progressBar.setVisibility(View.GONE);

                                    startActivity(intent);
                                    finish();
                                    return;

                                }
                            }
                            Toast.makeText(MainActivity.this, "이메일 또는 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(MainActivity.this, "네트워크 에러", Toast.LENGTH_SHORT).show();
                            Log.i("MAIN_LOGIN", task.toString());
                        }
                    }
                });



            }
        });


    }
}