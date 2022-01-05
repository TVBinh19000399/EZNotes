package com.mobile.eznote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SignUpActivity extends AppCompatActivity {
    private EditText msignupemail,msignuppassword;
    private RelativeLayout msignup;
    private TextView mgotologin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().hide();

        msignupemail = findViewById(R.id.signupemail);
        msignuppassword = findViewById(R.id.signuppassword);
        msignup = findViewById(R.id.dangky);
        mgotologin =findViewById(R.id.gotologin);

        mgotologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        msignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = msignuppassword.getText().toString().trim();
                String password =  msignuppassword.getText().toString().trim();
                if(mail.isEmpty() || password.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Tất cả đều bắt buộc :",Toast.LENGTH_SHORT).show();
                } else if (mail.length()<7|| password.length()<7){
                    Toast.makeText(getApplicationContext(),"Tài khoản, mật khẩu phải lớn hơn 6 kí tự!",Toast.LENGTH_SHORT).show();
                }
                else if(password.length()<7) {
                    Toast.makeText(getApplicationContext(),"Mật khẩu nên lớn hơn 7 nhé",Toast.LENGTH_SHORT).show();
                }
                else {
                    //đã đanghư ky vaofile base
                }


            }
        });
    }



}
