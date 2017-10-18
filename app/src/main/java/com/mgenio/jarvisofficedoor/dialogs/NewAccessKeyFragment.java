package com.mgenio.jarvisofficedoor.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.activities.ManageAccessKeyActivity;
import com.mgenio.jarvisofficedoor.models.AccessKey;
import com.mgenio.jarvisofficedoor.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Austin Nelson on 2/16/2017.
 */

public class NewAccessKeyFragment extends DialogFragment {

    @BindView(R.id.spinner_type) Spinner spinnerType;
    @BindView(R.id.input_name) EditText inputName;

    private DatabaseReference mDatabase;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static NewAccessKeyFragment newInstance(int num) {
        NewAccessKeyFragment f = new NewAccessKeyFragment();

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
        View v = inflater.inflate(R.layout.dialog_new_access_key, container, false);
        ButterKnife.bind(this, v);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        ArrayAdapter<String> types = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.types));
        spinnerType.setAdapter(types);
        spinnerType.setSelection(0);

        return v;
    }

    @OnClick(R.id.btn_create_access_key)
    public void createAccessKey() {
        String activeLocation = Utils.getSetting(getString(R.string.preferences_active_location), "", getActivity());
        String name = inputName.getText().toString();
        if (name.length() == 0) {
            name = null;
        }
        AccessKey accessKey = new AccessKey(spinnerType.getSelectedItem().toString().toLowerCase(), activeLocation, name, true, false, -1);
        mDatabase.child("access-keys").push().setValue(accessKey).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override public void onSuccess(Void aVoid) {
                ((ManageAccessKeyActivity) getActivity()).getAccessKeysForLocation();
                getDialog().dismiss();
            }
        });
    }
}
