package com.project.covidguard;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void clickRegistrationHandler(View view) {

        final String uuid = UUID.randomUUID().toString().replace("-", "");
        Toast.makeText(this, uuid, Toast.LENGTH_LONG).show();

        setContentView(R.layout.diagnose_fragment);


    }

    public void termsConditionsLink(View view) {
        TextView termsConditions = findViewById(R.id.textView7);
        termsConditions.setMovementMethod(LinkMovementMethod.getInstance());
    }

}