package com.mgenio.jarvisofficedoor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.adapters.AccessKeyAdapter;
import com.mgenio.jarvisofficedoor.dialogs.NewAccessKeyFragment;
import com.mgenio.jarvisofficedoor.interfaces.OnRecyclerViewItemClickListener;
import com.mgenio.jarvisofficedoor.models.AccessKey;
import com.mgenio.jarvisofficedoor.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ManageAccessKeyActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rv_access_keys) RecyclerView rvAccessKeys;
    private AccessKeyAdapter accessKeyAdapter;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_access_key);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("MGenio's Access Keys");
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        accessKeyAdapter = new AccessKeyAdapter(this, R.layout.list_row_item_access_key);
        rvAccessKeys.setLayoutManager(new GridLayoutManager(this, 2));
        rvAccessKeys.setAdapter(accessKeyAdapter);
        getAccessKeysForLocation();

        accessKeyAdapter.setOnItemClickListener(new OnRecyclerViewItemClickListener<AccessKey>() {
            @Override public void onItemClick(View view, AccessKey model) {
                Intent intent = new Intent(ManageAccessKeyActivity.this, AccessKeyDetailActivity.class);
                intent.putExtra("key", model.getKey());
                intent.putExtra("name", model.getName());
                startActivity(intent);
            }
        });
    }

    @OnClick(R.id.fab_add_access_key)
    public void addAccessKey() {
        DialogFragment newFragment = NewAccessKeyFragment.newInstance(0);
        newFragment.show(getSupportFragmentManager(), "dialog_new_access_key");
    }

    /**
     *
     */
    public void getAccessKeysForLocation() {
        final ArrayList<AccessKey> accessKeys = new ArrayList<>();
        final String activeAccessKey = Utils.getSetting(getString(R.string.preferences_active_access_key), "", this);
        String activeLocation = Utils.getSetting(getString(R.string.preferences_active_location), "", this);
        mDatabase.child("access-keys").orderByChild("location").startAt(activeLocation).endAt(activeLocation).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AccessKey accessKey = snapshot.getValue(AccessKey.class);
                    accessKey.setKey(snapshot.getKey());

                    if (!snapshot.getKey().equals(activeAccessKey)) {
                        if (!accessKey.isDeleted()) {
                            accessKeys.add(accessKey);
                        }
                    }
                }

                accessKeyAdapter.setModels(accessKeys);
            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });
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
}
