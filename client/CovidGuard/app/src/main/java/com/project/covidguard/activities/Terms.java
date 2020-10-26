package com.project.covidguard.activities;

import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;

import com.project.covidguard.R;

public class Terms extends AppCompatActivity {
    Toolbar mTopToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms);
        mTopToolbar = findViewById(R.id.toolbar2);
        mTopToolbar.setTitleTextColor(getColor(R.color.white));
        mTopToolbar.setTitle("Terms & Conditions");
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    public void clickBarcode(View view) {
        Intent localIntent = new Intent(Terms.this, Barcode.class);
        startActivity(localIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish() ;// close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }
}
