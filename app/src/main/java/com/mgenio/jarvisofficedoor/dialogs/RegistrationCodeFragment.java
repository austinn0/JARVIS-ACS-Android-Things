package com.mgenio.jarvisofficedoor.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.activities.DrawerActivity;
import com.mgenio.jarvisofficedoor.activities.SecurityActivity;
import com.mgenio.jarvisofficedoor.models.AccessKey;
import com.mgenio.jarvisofficedoor.utils.Utils;

import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Austin Nelson on 2/16/2017.
 */

public class RegistrationCodeFragment extends DialogFragment {

    @BindView(R.id.input_layout_access_key) TextInputLayout inputLayoutAccessKey;
    @BindView(R.id.input_access_key) EditText inputAccessKey;
    @BindView(R.id.input_layout_name) TextInputLayout inputLayoutName;
    @BindView(R.id.input_name) EditText inputName;

    private DatabaseReference mDatabase;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static RegistrationCodeFragment newInstance(int num) {
        RegistrationCodeFragment f = new RegistrationCodeFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_registration_code, container, false);
        ButterKnife.bind(this, v);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        return v;
    }

    @OnClick(R.id.btn_verify_access_key)
    public void verify() {
        final String accessKeyQuery = inputAccessKey.getText().toString();
        final String accessKeyName = inputName.getText().toString();

        if (accessKeyName.length() <= 0) {
            inputLayoutName.setError("Please enter a name for this access key");
            return;
        }

        if (accessKeyQuery.length() <= 0) {
            inputLayoutAccessKey.setError("Please enter an access key");
            return;
        }

        mDatabase.child("access-keys")
                .child(accessKeyQuery)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot dataSnapshot) {
                        AccessKey accessKey = dataSnapshot.getValue(AccessKey.class);
                        if (null != accessKey) {
                            accessKey.setName(accessKeyName);

                            if (accessKey.isRegistered()) {
                                inputLayoutAccessKey.setError("Access key already registered");
                            } else if (!accessKey.isEnabled()) {
                                inputLayoutAccessKey.setError("Access key has been disabled");
                            } else {
                                registerAccessKey(accessKeyQuery, accessKey);
                            }
                        } else {
                            //invalid access code
                            inputLayoutAccessKey.setError("Access key is invalid");
                        }
                    }

                    @Override public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /**
     * We found a valid access key for the first time
     * Setting registered to false
     *
     * @param accessKey
     */
    private void registerAccessKey(final String accessKeyQuery, final AccessKey accessKey) {
        accessKey.setRegistered(true);
        mDatabase.child("access-keys").child(accessKeyQuery).setValue(accessKey).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override public void onSuccess(Void aVoid) {
                Set<String> deviceAccessKeys = Utils.getSetting(getString(R.string.preferences_device_access_keys), new HashSet<String>(), getActivity());
                deviceAccessKeys.add(accessKeyQuery);
                Utils.storeSetting(getString(R.string.preferences_device_access_keys), deviceAccessKeys, getActivity());
                Utils.storeSetting(getString(R.string.preferences_active_access_key_name), inputName.getText().toString(), getActivity());
                Utils.storeSetting(getString(R.string.preferences_active_access_key), accessKeyQuery, getActivity());
                Utils.storeSetting(getString(R.string.preferences_active_location), accessKey.getLocation(), getActivity());
                getDialog().dismiss();

                Toast.makeText(getActivity(), "Validated access key: " + accessKeyQuery, Toast.LENGTH_SHORT).show();
                if (Utils.getSetting(getString(R.string.preferences_security), false, getActivity())) {
                    startActivity(new Intent(getActivity(), SecurityActivity.class));
                } else {
                    startActivity(new Intent(getActivity(), DrawerActivity.class));
                }
            }
        });
    }
}
