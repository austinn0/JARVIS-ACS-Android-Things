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
import com.mgenio.jarvisofficedoor.models.AccessKey;
import com.mgenio.jarvisofficedoor.models.Pin;
import com.mgenio.jarvisofficedoor.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TempPinsActivity extends AppCompatActivity {

    @BindView(R.id.textView2) TextView tvTempPins;
    @BindView(R.id.textView3) TextView tvPins;

    private DatabaseReference mDatabase;
    private String access;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_pins);
        ButterKnife.bind(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        access = getIntent().getStringExtra("access");

        getSupportActionBar().setTitle("MGenio's Door Codes");

        if (access.equals("admin")) {
            tvTempPins.setVisibility(View.GONE);
            tvPins.setVisibility(View.VISIBLE);
        } else {
            tvPins.setVisibility(View.GONE);
            tvTempPins.setVisibility(View.VISIBLE);
        }

        getTempPinsForLocation();
    }

    /**
     *
     */
    private void getTempPinsForLocation() {
        String activeLocation = Utils.getSetting(getString(R.string.preferences_active_location), "", getApplicationContext());
        if (activeLocation.equals("")) {
            finish();
        }

        mDatabase.child("access-keys")
                .orderByChild("location")
                .startAt(activeLocation)
                .endAt(activeLocation)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            final AccessKey accessKey = snapshot.getValue(AccessKey.class);
                            accessKey.setKey(snapshot.getKey());

                            mDatabase.child("pins")
                                    .orderByChild("accessKey")
                                    .startAt(accessKey.getKey())
                                    .endAt(accessKey.getKey())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                final Pin pin = snapshot.getValue(Pin.class);
                                                Date expirationDate = new Date(pin.getExpirationDate());
                                                SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy @ hh:mm:ss a");
                                                StringBuilder sb = new StringBuilder();
                                                sb.append("Access Key: " + accessKey.getKey())
                                                        .append("\n")
                                                        .append("Pin: " + pin.getPin())
                                                        .append("\n")
                                                        .append("Expiration Date: " + ((pin.getExpirationDate() == -1) ? "Never" : sdf.format(expirationDate)))
                                                        .append("\n\n\n");
                                                if (accessKey.getType().equals("temp")) {
                                                    tvTempPins.append(sb.toString());
                                                } else {
                                                    tvPins.append(sb.toString());
                                                }
                                            }
                                        }

                                        @Override public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                        }
                    }

                    @Override public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
