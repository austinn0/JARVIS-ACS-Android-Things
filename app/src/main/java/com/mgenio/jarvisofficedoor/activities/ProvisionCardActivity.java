package com.mgenio.jarvisofficedoor.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kofigyan.stateprogressbar.StateProgressBar;
import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.adapters.AccessKeyAdapter;
import com.mgenio.jarvisofficedoor.interfaces.OnRecyclerViewItemClickListener;
import com.mgenio.jarvisofficedoor.models.AccessKey;
import com.mgenio.jarvisofficedoor.models.Card;
import com.mgenio.jarvisofficedoor.models.Location;
import com.mgenio.jarvisofficedoor.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProvisionCardActivity extends AppCompatActivity implements ValueEventListener, ChildEventListener, DialogInterface.OnDismissListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.layout_cards) LinearLayout layoutCards;
    @BindView(R.id.rv_access_keys) RecyclerView rvAccessKeys;
    @BindView(R.id.indicator) StateProgressBar indicator;

    private Handler handler = new Handler();
    private DatabaseReference mDatabase;
    private Location location;
    private Card card1 = null;
    private Card card2 = null;
    private String mAccessKey;
    private String selectedAccessKey;

    private String[] descriptionData = {"Card", "Confirm", "Add Card"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provision_card);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Card Provisioning");
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String activeLocation = Utils.getSetting(getString(R.string.preferences_active_location), "", this);
        if (getIntent().hasExtra("accessKey")) {
            mAccessKey = getIntent().getStringExtra("accessKey");
        }

        indicator.setStateDescriptionData(descriptionData);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("locations").child(activeLocation).addListenerForSingleValueEvent(this);
        mDatabase.child("locations").child(activeLocation).child("card-provisioning").addChildEventListener(this);

        handler.postDelayed(mReaderMode, 30000);

        getAccessKeysForMyLocation();
    }

    /**
     *
     */
    private void getAccessKeysForMyLocation() {
        final AccessKey me = new AccessKey("admin", null, "Myself", true, true, -1);
        me.setKey(Utils.getSetting(getString(R.string.preferences_active_access_key), "", this));

        final ArrayList<AccessKey> accessKeys = new ArrayList<>();
        //accessKeys.add(me);

        String activeLocation = Utils.getSetting(getString(R.string.preferences_active_location), "", this);
        mDatabase.child("access-keys").orderByChild("location").startAt(activeLocation).endAt(activeLocation).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final AccessKey accessKey = snapshot.getValue(AccessKey.class);
                    accessKey.setKey(snapshot.getKey());
                    accessKeys.add(accessKey);
                }

                rvAccessKeys.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                final AccessKeyAdapter adapter = new AccessKeyAdapter(ProvisionCardActivity.this, R.layout.list_row_item_access_key_user);
                rvAccessKeys.setAdapter(adapter);
                adapter.setModels(accessKeys);

                adapter.setOnItemClickListener(new OnRecyclerViewItemClickListener<AccessKey>() {
                    @Override public void onItemClick(View view, AccessKey model) {
                        for (AccessKey key : accessKeys) {
                            if (model.getKey().equals(key.getKey())) {
                                key.setSelected(true);
                                selectedAccessKey = key.getKey();
                            } else {
                                key.setSelected(false);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }
                });

            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addCard() {
        String activeLocation = Utils.getSetting(getString(R.string.preferences_active_location), "", this);
        card1.setAccessKey(selectedAccessKey);
        card1.setLocation(activeLocation);
        mDatabase.child("cards").push().setValue(card1).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override public void onSuccess(Void aVoid) {
                finish();
            }
        });
    }

    /**
     *
     */
    private void showConfirmAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProvisionCardActivity.this);
        builder.setTitle("Confirm");
        builder.setMessage("Would you like to add this card?");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                addCard();
                indicator.setAllStatesCompleted(true);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Turns off the LED after a set amount of time
     */
    private Runnable mReaderMode = new Runnable() {
        @Override
        public void run() {
            String activeLocation = Utils.getSetting(getString(R.string.preferences_active_location), "", getApplicationContext());
            location.setMode("reader");
            mDatabase.child("locations").child(activeLocation).setValue(location);
            finish();
        }
    };

    @Override public void onDataChange(DataSnapshot dataSnapshot) {
        location = dataSnapshot.getValue(Location.class);
        location.setMode("provisioning");
        dataSnapshot.getRef().setValue(location);

    }

    @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        View card = getLayoutInflater().inflate(R.layout.list_row_item_card, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(24, 12, 24, 12);
        if (null == card1) {
            card1 = dataSnapshot.getValue(Card.class);
            ((TextView) card.findViewById(R.id.tv_card_number)).setText(card1.getFacility() + card1.getCard());
            layoutCards.addView(card);
            indicator.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
        } else {
            card2 = dataSnapshot.getValue(Card.class);
            ((TextView) card.findViewById(R.id.tv_card_number)).setText(card1.getFacility() + card1.getCard());
            layoutCards.addView(card);
            indicator.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);

            if (card1.getFacility().equals(card2.getFacility()) && card1.getCard().equals(card2.getCard())) {
                showConfirmAlertDialog();
            } else {
                Toast.makeText(getApplicationContext(), "Cards were different. Try again", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        String activeLocation = Utils.getSetting(getString(R.string.preferences_active_location), "", getApplicationContext());
        location.setMode("reader");
        mDatabase.child("locations").child(activeLocation).setValue(location);

        if (null != mReaderMode) {
            handler.removeCallbacks(mReaderMode);
        }
    }

    @Override public void onDismiss(DialogInterface dialog) {

    }
}
