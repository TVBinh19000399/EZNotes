package com.mobile.eznote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ForgetPasswodActivity extends AppCompatActivity {
    private EditText mforgotpassword;
    private Button mpasswordrecoverbutton;
    private TextView mgobacktologin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_passwod);

        getSupportActionBar().hide();
        mforgotpassword = findViewById(R.id.quenmatkhau);
        mpasswordrecoverbutton = findViewById(R.id.phuchoi_password);
        mgobacktologin = findViewById(R.id.goto_screen_login);
        mgobacktologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgetPasswodActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        mpasswordrecoverbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = mforgotpassword.getText().toString().trim();
                if (mail.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Nhập mail của bạn trước đã", Toast.LENGTH_SHORT).show();

                } else {
                    //c ta có 2 mkhau khôi phục email
                }


            }
        });
    }


}