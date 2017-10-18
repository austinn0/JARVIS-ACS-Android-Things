package com.mgenio.jarvisofficedoor.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mgenio.jarvisofficedoor.R;

import butterknife.ButterKnife;

/**
 * Created by Austin Nelson on 2/16/2017.
 */

public class ProvisionCardFragment extends DialogFragment {

    //@BindView(R.id.input_card_number) EditText inputCardNumber;

    private DatabaseReference mDatabase;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static ProvisionCardFragment newInstance(int num) {
        ProvisionCardFragment f = new ProvisionCardFragment();

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

        return v;
    }
}
