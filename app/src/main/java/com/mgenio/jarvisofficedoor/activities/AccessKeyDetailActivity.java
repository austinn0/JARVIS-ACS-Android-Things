package com.mgenio.jarvisofficedoor.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.joaquimley.faboptions.FabOptions;
import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.adapters.CardAdapter;
import com.mgenio.jarvisofficedoor.adapters.PinAdapter;
import com.mgenio.jarvisofficedoor.dialogs.CreatePinFragment;
import com.mgenio.jarvisofficedoor.interfaces.OnRecyclerViewItemLongClickListener;
import com.mgenio.jarvisofficedoor.models.AccessKey;
import com.mgenio.jarvisofficedoor.models.Card;
import com.mgenio.jarvisofficedoor.models.Pin;
import com.mgenio.jarvisofficedoor.utils.CircleTransform;
import com.mgenio.jarvisofficedoor.utils.Utils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AccessKeyDetailActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.iv_access_key_blocked) ImageView ivAccessKeyBlocked;
    @BindView(R.id.iv_user_access_key_image) ImageView ivUserAccessKeyImage;
    @BindView(R.id.iv_access_key_edit) ImageView ivAccessKeyEdit;
    @BindView(R.id.fab_options) FabOptions fabOptions;

    @BindView(R.id.layout_integrations) LinearLayout layoutIntegrations;
    @BindView(R.id.card_weather_integration) CardView cardWeatherIntegration;
    @BindView(R.id.card_gmail_integration) CardView cardGmailIntegration;
    @BindView(R.id.card_calendar_integration) CardView cardCalendarIntegration;
    @BindView(R.id.card_ifttt_integration) CardView cardIftttIntegration;

    @BindView(R.id.rv_pins) RecyclerView rvPins;
    private PinAdapter pinAdapter;

    @BindView(R.id.rv_cards) RecyclerView rvCards;
    private CardAdapter cardAdapter;

    private DatabaseReference mDatabase;
    private String accessKey;
    private String accessKeyName;
    private AccessKey mAccessKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_key_detail);
        ButterKnife.bind(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        accessKey = getIntent().getStringExtra("key");
        accessKeyName = getIntent().getStringExtra("name");

        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle(accessKeyName);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fabOptions.setButtonsMenu(R.menu.access_key_options);
        fabOptions.setOnClickListener(this);

        if (accessKey.equals(Utils.getSetting(getString(R.string.preferences_active_access_key), "", this))) {
            ivAccessKeyEdit.setVisibility(View.VISIBLE);
            layoutIntegrations.setVisibility(View.VISIBLE);
            setupEnabledIntegrations();
        }

        pinAdapter = new PinAdapter(this);
        rvPins.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPins.setAdapter(pinAdapter);
        pinAdapter.setOnItemLongClickListener(new OnRecyclerViewItemLongClickListener<Pin>() {
            @Override public void onItemLongClick(View view, Pin model) {
                DatabaseReference mReference = mDatabase.child("pins").child(model.getKey());
                showDeleteAlertDialog(mReference, "pin");
            }
        });

        cardAdapter = new CardAdapter(this);
        rvCards.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCards.setAdapter(cardAdapter);
        cardAdapter.setOnItemLongClickListener(new OnRecyclerViewItemLongClickListener<Card>() {
            @Override public void onItemLongClick(View view, Card model) {
                DatabaseReference mReference = mDatabase.child("cards").child(model.getKey());
                showDeleteAlertDialog(mReference, "card");
            }
        });

        getPins();
        getCards();
        getAccessKey();
    }

    @OnClick(R.id.iv_access_key_delete)
    public void delete() {
        DatabaseReference mReference = mDatabase.child("access-keys").child(accessKey);
        showDeleteAlertDialog(mReference, "access-key");
    }

    @OnClick(R.id.iv_access_key_blocked)
    public void block() {
        mAccessKey.setEnabled(!mAccessKey.isEnabled());
        mDatabase.child("access-keys").child(accessKey).setValue(mAccessKey).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override public void onSuccess(Void aVoid) {
                if (!mAccessKey.isEnabled()) {
                    ivAccessKeyBlocked.setColorFilter(Color.parseColor("#d50000"), PorterDuff.Mode.SRC_ATOP);
                } else {
                    ivAccessKeyBlocked.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                }
            }
        });
    }

    @OnClick(R.id.iv_access_key_edit)
    public void edit() {
        showEditAccessKeyDialog();
    }

    @OnClick(R.id.card_weather_integration)
    public void weather(View v) {
        toggleEnabledIntegrations(v, getString(R.string.preferences_weather_integration));
    }

    @OnClick(R.id.card_gmail_integration)
    public void gmail(View v) {
        toggleEnabledIntegrations(v, getString(R.string.preferences_gmail_integration));
    }

    @OnClick(R.id.card_calendar_integration)
    public void calendar(View v) {
        toggleEnabledIntegrations(v, getString(R.string.preferences_calendar_integration));
    }

    @OnClick(R.id.card_ifttt_integration)
    public void ifttt(View v) {
        toggleEnabledIntegrations(v, getString(R.string.preferences_ifttt_integration));
    }

    private void getAccessKey() {
        mDatabase.child("access-keys").child(accessKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                AccessKey accessKey = dataSnapshot.getValue(AccessKey.class);
                mAccessKey = accessKey;

                collapsingToolbarLayout.setTitle(mAccessKey.toString());

                if (!accessKey.isEnabled()) {
                    ivAccessKeyBlocked.setColorFilter(Color.parseColor("#d50000"), PorterDuff.Mode.SRC_ATOP);
                }

                Picasso.with(getApplicationContext()).load(mAccessKey.getImageUrl())
                        .error(R.drawable.error_image)
                        .placeholder(R.drawable.error_image)
                        .transform(new CircleTransform())
                        .into(ivUserAccessKeyImage);
            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPins() {
        mDatabase.child("pins").orderByChild("accessKey").startAt(accessKey).endAt(accessKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Pin pin = snapshot.getValue(Pin.class);
                    pin.setKey(snapshot.getKey());
                    pinAdapter.add(pin);
                }
            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getCards() {
        mDatabase.child("cards").orderByChild("accessKey").startAt(accessKey).endAt(accessKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Card card = snapshot.getValue(Card.class);
                    card.setKey(snapshot.getKey());
                    cardAdapter.add(card);
                }
            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupEnabledIntegrations() {
        if (!Utils.getSetting(getString(R.string.preferences_weather_integration), false, this)) {
            cardWeatherIntegration.setCardBackgroundColor(getResources().getColor(R.color.gray));
        }

        if (!Utils.getSetting(getString(R.string.preferences_gmail_integration), false, this)) {
            cardGmailIntegration.setCardBackgroundColor(getResources().getColor(R.color.gray));
        }

        if (!Utils.getSetting(getString(R.string.preferences_calendar_integration), false, this)) {
            cardCalendarIntegration.setCardBackgroundColor(getResources().getColor(R.color.gray));
        }

        if (!Utils.getSetting(getString(R.string.preferences_ifttt_integration), false, this)) {
            cardIftttIntegration.setCardBackgroundColor(getResources().getColor(R.color.gray));
        }
    }

    /**
     * @param v
     * @param key
     */
    private void toggleEnabledIntegrations(View v, String key) {
        if (Utils.getSetting(key, false, this)) {
            ((CardView) v).setCardBackgroundColor(getResources().getColor(R.color.gray));
            Utils.storeSetting(key, false, this);
        } else {
            ((CardView) v).setCardBackgroundColor(getResources().getColor(R.color.white));
            Utils.storeSetting(key, true, this);
        }
    }

    /**
     *
     */
    private void showEditAccessKeyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setText(mAccessKey.getName());
        input.setHint("name");
        builder.setView(input);
        builder.setTitle("Edit");
        builder.setMessage("Make your changes");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                collapsingToolbarLayout.setTitle(input.getText().toString());
                mAccessKey.setName(input.getText().toString());
                mDatabase.child("access-keys").child(accessKey).child("name").setValue(input.getText().toString());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     *
     */
    private void showDeleteAlertDialog(final DatabaseReference mReference, final String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete?");
        builder.setMessage("Are you sure?");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (type.equals("access-key")) {
                    mAccessKey.setDeleted(true);
                    mReference.setValue(mAccessKey);
                } else {
                    mReference.removeValue();
                }

                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     *
     */
    private void showCreatePinDialog() {
        DialogFragment newFragment = CreatePinFragment.newInstance(accessKey);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(newFragment, "dialog_create_pin");
        ft.commit();
    }

    /**
     *
     */
    public void addCard() {
        Intent intent = new Intent(this, ProvisionCardActivity.class);
        intent.putExtra("accessKey", accessKey);
        startActivity(intent);
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

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.faboptions_pin:
                showCreatePinDialog();
                break;
            case R.id.faboptions_card:
                addCard();
                break;
        }
    }
}
