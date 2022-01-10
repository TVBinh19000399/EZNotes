package com.mobile.eznote.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.mobile.eznote.R;

public class SignUpActivity extends AppCompatActivity {
    private EditText msignupemail, msignuppassword;
    private RelativeLayout msignup;
    private TextView mgotologin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().hide();
        mAuth = FirebaseAuth.getInstance();
        msignupemail = findViewById(R.id.signupemail);
        msignuppassword = findViewById(R.id.signuppassword);
        msignup = findViewById(R.id.dangky);
        mgotologin = findViewById(R.id.gotologin);

        mgotologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        msignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = msignupemail.getText().toString().trim();
                String password = msignuppassword.getText().toString().trim();
                if (mail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Tất cả đều bắt buộc:", Toast.LENGTH_SHORT).show();

                } else if (password.length() < 7) {
                    Toast.makeText(getApplicationContext(), "Mật khẩu nên lớn hơn 7 nhé!", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(mail, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignUpActivity.this, "Đã đăng kí thành công!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(SignUpActivity.this, "Đã có lỗi xảy ra: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }


            }
        });
    }


}
