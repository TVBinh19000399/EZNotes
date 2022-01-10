package com.mobile.eznote.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.eznote.R;

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
                Intent intent = new Intent(ForgetPasswodActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        mpasswordrecoverbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String mail = mforgotpassword.getText().toString().trim();
                Toast.makeText(getApplicationContext(), "Tính năng này đang được phát triển!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
