package com.mgenio.jarvisofficedoor.fragments.pin;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.dialogs.CreatePinFragment;
import com.mgenio.jarvisofficedoor.models.AccessKey;
import com.mgenio.jarvisofficedoor.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link PinFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PinFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mAccessKey;
    private String mParam2;

    @BindView(R.id.input_layout_pin) TextInputLayout inputLayoutPin;
    @BindView(R.id.layout_assign_access_key) LinearLayout layoutAssignAccessKey;
    @BindView(R.id.input_pin) EditText inputPin;
    @BindView(R.id.spinner_assign_access_key) Spinner spinnerAssignAccessKey;
    @BindView(R.id.switch_temp) Switch switchTemp;

    private DatabaseReference mDatabase;
    private String type;

    public PinFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BoomMusicFragment.
     */
    public static PinFragment newInstance(String param1, String param2) {
        PinFragment fragment = new PinFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAccessKey = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_pin, container, false);
        ButterKnife.bind(this, view);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        getAccessKeysForMyLocation();

        switchTemp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layoutAssignAccessKey.setVisibility(View.GONE);
                    CreatePinFragment fragment = ((CreatePinFragment) getParentFragment());
                    if (null != fragment) {
                        fragment.addDateTime();
                    }
                } else {
                    layoutAssignAccessKey.setVisibility(View.VISIBLE);
                    CreatePinFragment fragment = ((CreatePinFragment) getParentFragment());
                    if (null != fragment) {
                        fragment.removeDateTime();
                    }
                }
            }
        });

        return view;
    }

    /**
     *
     */
    private void getAccessKeysForMyLocation() {
        final AccessKey me = new AccessKey("admin", null, "Myself", true, true, -1);
        me.setKey(Utils.getSetting(getString(R.string.preferences_active_access_key), "", getActivity()));

        final ArrayList<AccessKey> accessKeys = new ArrayList<>();
        accessKeys.add(me);

        String activeLocation = Utils.getSetting(getString(R.string.preferences_active_location), "", getActivity());
        mDatabase.child("access-keys").orderByChild("location").startAt(activeLocation).endAt(activeLocation).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AccessKey accessKey = snapshot.getValue(AccessKey.class);
                    accessKey.setKey(snapshot.getKey());

                    if (!snapshot.getKey().equals(me.getKey())) {
                        accessKeys.add(accessKey);
                    }
                }

                ArrayAdapter<AccessKey> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, accessKeys);
                spinnerAssignAccessKey.setAdapter(dataAdapter);
                spinnerAssignAccessKey.setSelection(0, false);

                if (null != mAccessKey) {
                    for (int i = 0; i < accessKeys.size(); i++) {
                        if (accessKeys.get(i).getKey().equals(mAccessKey)) {
                            spinnerAssignAccessKey.setSelection(i, false);
                        }
                    }
                }
            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     *
     */
    private void dismissDialog() {
        CreatePinFragment dialog = ((CreatePinFragment) getParentFragment());
        if (null != dialog) {
            dialog.dismiss();
        }
    }

    public boolean isTempPin() {
        return switchTemp.isChecked();
    }

    public AccessKey getSelectedAccessKey() {
        return (AccessKey) spinnerAssignAccessKey.getSelectedItem();
    }

    public String getPin() {
        return inputPin.getText().toString();
    }
}
