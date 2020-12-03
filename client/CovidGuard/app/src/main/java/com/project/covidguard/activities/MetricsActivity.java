
package com.project.covidguard.activities;

import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.project.covidguard.R;
import com.project.covidguard.data.entities.RPI;
import com.project.covidguard.data.entities.TEK;
import com.project.covidguard.data.repositories.RPIRepository;
import com.project.covidguard.data.repositories.TEKRepository;

import org.altbeacon.beacon.Identifier;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class MetricsActivity extends AppCompatActivity {

    private static final String LOG_TAG = MetricsActivity.class.getName();
    Toolbar mTopToolbar;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.metrics);
        mTopToolbar = findViewById(R.id.toolbar2);
        mTopToolbar.setTitleTextColor(getColor(R.color.white));
        mTopToolbar.setTitle(getString(R.string.metrics));
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void clickTEKMetric(View view) {
        TEKRepository repo = new TEKRepository(getApplicationContext());
        TEK currentTEK = repo.getLastTek();
        byte[] currentTEKByteArray = Base64.decode(currentTEK.getTekId(), Base64.DEFAULT);
        Toast.makeText(getApplicationContext(), "Current RPIs are derived from the TEK: " + Arrays.toString(currentTEKByteArray), Toast.LENGTH_SHORT).show();
    }

    public void clickRPIMetric(View view) {
        RPIRepository repo = new RPIRepository(getApplicationContext());
        RPI rpi = repo.getLastRPI();

        if (rpi == null) {
            Toast.makeText(getApplicationContext(), "No RPI is currently being received", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(
                getApplicationContext(), "Current anonymised RPI being received is: " +
                    Arrays.toString(Identifier.parse(rpi.getRpi(), 16).toByteArray()),
                Toast.LENGTH_SHORT).show();
        }
    }

    public void clickENINMetric(View view) {
        TEKRepository repo = new TEKRepository(getApplicationContext());
        TEK currentENIN = repo.getLastTek();

        Toast.makeText(getApplicationContext(), "Current TEK was derived at the ENIntervalNumber: " + currentENIN.getEnIntervalNumber(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish() ;// close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }
}
