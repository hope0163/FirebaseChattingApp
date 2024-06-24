package com.hope0163.chattingapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hope0163.chattingapp.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    CircleImageView circleImgProfile;
    EditText editUserName;
    EditText editEmail;
    EditText editPassword;
    Button btnRegister;
    TextView txtLogin;
    Uri profileUri;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        circleImgProfile = findViewById(R.id.circleImgProfile);
        editUserName = findViewById(R.id.editUserName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);
        circleImgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clikcImg();
            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String userInputName = editUserName.getText().toString().trim();
                String userInputEmail = editEmail.getText().toString().trim();
                String userInputPassword = editPassword.getText().toString().trim();

                if (userInputEmail.contains("@") == false || userInputEmail.contains(".") == false) {
                    Toast.makeText(RegisterActivity.this, "이메일을 제대로 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 회원가입 항목 모두 입력했는지 검사
                if (userInputName.isEmpty() || userInputEmail.isEmpty() | userInputPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "필수항목입니다. 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (profileUri == null) {
                    Toast.makeText(RegisterActivity.this, "프로필 이미지를 설정해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // FirebaseStorage클래스는 firebase storage를 사용할 수 있게 해주는 클래스
                // getInstance()는 FirebaseStorage의 인스턴스를 반환
                // 즉, Firebase Storage를 사용하기 위해 필요한 초기화 과정
                // FirebaseStorage.getInstance() 메서드를 호출하여 인스턴스를 얻고
                // 인스턴스를 통해 storage 서비스에 접근하여 파일 관리 작업을 수행할 수 있음.
                FirebaseStorage storage = FirebaseStorage.getInstance();
                // 이미지 이름을 (유저이름 + 업로드 시간)으로 유니크하게 저장하기 위해 SimpleDataFormat 사용
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
                StorageReference imgRef = storage.getReference("profileImage/" + userInputName +sdf.format(new Date()) + ".jpeg");

                imgRef.putFile(profileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                User.profileImgUrl = uri.toString();
                                Log.i("REGISTER URI", uri.toString());
                                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                DocumentReference ref = firestore.collection("user").document();

                                ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                        if (documentSnapshot.exists()) {
                                            Toast.makeText(RegisterActivity.this, "이미 등록하신 사용자입니다.", Toast.LENGTH_SHORT).show();
                                            return;
                                        } else {
                                            Map<String, Object> user = new HashMap<>();
                                            user.put("email", userInputEmail);
                                            user.put("password", userInputPassword);
                                            user.put("profileImgUrl", User.profileImgUrl);
                                            user.put("userName", userInputName);

                                            firestore.collection("user").document("user-"+userInputEmail).set(user)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            progressBar.setVisibility(View.GONE);
                                                            Toast.makeText(RegisterActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                            startActivity(intent);

                                                            finish();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("Error", e.getMessage());
                                                        }
                                                    });



                                        }

                                    }
                                });
                            }
                        });
                    }
                });

//                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//                DocumentReference ref = firestore.collection("user").document();
//
//                ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//
//                        if (documentSnapshot.exists()) {
//                            Toast.makeText(RegisterActivity.this, "이미 등록하신 사용자입니다.", Toast.LENGTH_SHORT).show();
//                        } else {
//
//
//                            Map<String, Object> user = new HashMap<>();
//                            user.put("email", userInputEmail);
//                            user.put("password", userInputPassword);
//                            user.put("profileImgUrl", User.profileImgUrl);
//                            user.put("userName", userInputName);
//
//                            firestore.collection("user").document("user-"+userInputEmail).set(user)
//                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void unused) {
//                                            Toast.makeText(RegisterActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
//                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
//                                            startActivity(intent);
//
//                                            finish();
//                                        }
//                                    }).addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Log.d("Error", e.getMessage());
//                                        }
//                                    });
//
//
//
//                        }
//
//                    }
//                });

            }
        });




        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });



    }

    private void clikcImg() {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == RESULT_CANCELED) return;
        progressBar.setVisibility(View.VISIBLE);

        profileUri = result.getData().getData();
        Glide.with(RegisterActivity.this).load(profileUri).into(circleImgProfile);
        progressBar.setVisibility(View.GONE);
    });
}