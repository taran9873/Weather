package com.tarandeepsingh.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class dev_info extends AppCompatActivity implements View.OnClickListener {

    ImageView github,whatsapp,linkedin,gmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_info);
        github = findViewById(R.id.github);
        github.setOnClickListener(this);
        whatsapp = findViewById(R.id.whatsapp);
        whatsapp.setOnClickListener(this);
        linkedin = findViewById(R.id.linkedin);
        linkedin.setOnClickListener(this);
        gmail= findViewById(R.id.gmail);
        gmail.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.whatsapp:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://api.whatsapp.com/send?phone=+919315314510"));
                startActivity(i);
                break;
            case R.id.linkedin:
                Intent i1 = new Intent(Intent.ACTION_VIEW);
                i1.setData(Uri.parse("https://www.linkedin.com/in/tarandeep-singh-b85425190/"));
                startActivity(i1);
                break;
            case R.id.github:
                Intent i2 = new Intent(Intent.ACTION_VIEW);
                i2.setData(Uri.parse("https://github.com/taran9873"));
                startActivity(i2);
                break;
            case R.id.gmail:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "tarandeepsingh.tannu@gmail.com", null));
                this.startActivity(Intent.createChooser(emailIntent, null));
                break;

        }
    }
}
