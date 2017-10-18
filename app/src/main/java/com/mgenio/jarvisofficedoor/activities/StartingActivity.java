package com.mgenio.jarvisofficedoor.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.dialogs.RegistrationCodeFragment;
import com.mgenio.jarvisofficedoor.models.AccessKey;
import com.mgenio.jarvisofficedoor.utils.Utils;

public class StartingActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        String activeAccessKey = Utils.getSetting(getString(R.string.preferences_active_access_key), "", this);
        if (activeAccessKey.equals("")) {
            showRegistrationCodeDialog();
        } else {
            verify(activeAccessKey);
        }
    }

    /**
     *
     */
    private void showRegistrationCodeDialog() {
        DialogFragment newFragment = RegistrationCodeFragment.newInstance(0);
        newFragment.show(getSupportFragmentManager(), "dialog_registration_code");
    }

    /**
     * @param activeAccessKey
     */
    private void verify(String activeAccessKey) {
        mDatabase.child("access-keys")
                .child(activeAccessKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot dataSnapshot) {
                        AccessKey accessKey = dataSnapshot.getValue(AccessKey.class);
                        if (null != accessKey) {
                            accessKey.setKey(dataSnapshot.getKey());

                            if (!accessKey.isEnabled()) {
                                showAlertDialog("This access key has been disabled");
                            } else {
                                Toast.makeText(getApplicationContext(), "Validated access key: " + accessKey.getKey(), Toast.LENGTH_SHORT).show();
                                if (Utils.getSetting(getString(R.string.preferences_security), false, getApplicationContext())) {
                                    startActivity(new Intent(StartingActivity.this, SecurityActivity.class));
                                } else {
                                    startActivity(new Intent(StartingActivity.this, DrawerActivity.class));
                                }
                                finish();
                            }
                        } else {
                            //invalid access code
                            showAlertDialog("Your access code is invalid");
                        }
                    }

                    @Override public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /**
     * @param message
     */
    private void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¯\\_(ツ)_/¯");
        builder.setMessage(message);

        builder.setPositiveButton("Enter New Access Key", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                showRegistrationCodeDialog();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
