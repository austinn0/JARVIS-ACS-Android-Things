package com.mgenio.jarvisofficedoor.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.models.Pin;
import com.mgenio.jarvisofficedoor.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyPinsActivity extends AppCompatActivity {

    @BindView(R.id.textView3) TextView tvPins;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_pins);
        ButterKnife.bind(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        getSupportActionBar().setTitle("My Door Codes");
        tvPins = (TextView) findViewById(R.id.textView3);
        tvPins.setVisibility(View.VISIBLE);

        getTempPinsForLocation();
    }

    /**
     *
     */
    private void getTempPinsForLocation() {
        final String activeKey = Utils.getSetting(getString(R.string.preferences_active_access_key), "", getApplicationContext());
        if (activeKey.equals("")) {
            finish();
        }

        mDatabase.child("pins")
                .orderByChild("accessKey")
                .startAt(activeKey)
                .endAt(activeKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            final Pin pin = snapshot.getValue(Pin.class);
                            Date expirationDate = new Date(pin.getExpirationDate());
                            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy @ hh:mm:ss a");
                            StringBuilder sb = new StringBuilder();
                            sb.append("Access Key: " + activeKey)
                                    .append("\n")
                                    .append("Pin: " + pin.getPin())
                                    .append("\n")
                                    .append("Expiration Date: " + ((pin.getExpirationDate() == -1) ? "Never" : sdf.format(expirationDate)))
                                    .append("\n\n\n");

                            tvPins.append(sb.toString());
                        }
                    }

                    @Override public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
