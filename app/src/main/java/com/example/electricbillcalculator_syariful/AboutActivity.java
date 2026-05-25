package com.example.electricbillcalculator_syariful;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    TextView textViewGithubUrl;
    Button buttonBackAbout;

    String githubUrl = "github.url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        textViewGithubUrl = findViewById(R.id.textViewGithubUrl);
        buttonBackAbout = findViewById(R.id.buttonBackAbout);

        textViewGithubUrl.setText(githubUrl);

        textViewGithubUrl.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl));
            startActivity(intent);
        });

        buttonBackAbout.setOnClickListener(v -> finish());
    }
}