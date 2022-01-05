package com.mobile.eznote;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class CreateNoteActivity extends AppCompatActivity {
private TextView datetime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note_actitvity);

        datetime= findViewById(R.id.textDateTime);
        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        datetime.setText(String.valueOf(Calendar.getInstance().getTime() ));
    }
}