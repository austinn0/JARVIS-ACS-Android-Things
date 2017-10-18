package com.mgenio.jarvisofficedoor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.dialogs.CreatePinFragment;
import com.mgenio.jarvisofficedoor.fragments.MainFragment;
import com.mgenio.jarvisofficedoor.models.Door;
import com.mgenio.jarvisofficedoor.models.Location;
import com.mgenio.jarvisofficedoor.utils.CircleTransform;
import com.mgenio.jarvisofficedoor.utils.Utils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String ACTION_OPEN_DOOR = "com.mgenio.jarvisofficedoor.OPEN_DOOR";
    private static final String ACTION_CREATE_PIN = "com.mgenio.jarvisofficedoor.CREATE_PIN";
    private static final String ACTION_PROVISION_CARD = "com.mgenio.jarvisofficedoor.PROVISION_CARD";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.navigation) NavigationView navigationView;
    @BindView(R.id.content_frame) FrameLayout contentFrame;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        String accessKey = Utils.getSetting(getString(R.string.preferences_active_access_key), "", this);
        final String accessKeyName = Utils.getSetting(getString(R.string.preferences_active_access_key_name), "", this);
        TextView tvUserAccessKeyName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.tv_user_access_key_name);
        TextView tvUserAccessKey = (TextView) navigationView.getHeaderView(0).findViewById(R.id.tv_user_access_key);
        ImageView ivUserProfile = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.iv_user_profile);
        tvUserAccessKey.setText(accessKey);
        tvUserAccessKeyName.setText(accessKeyName);
        ivUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String accessKey = Utils.getSetting(getString(R.string.preferences_active_access_key), "", getApplicationContext());
                Intent intent = new Intent(DrawerActivity.this, AccessKeyDetailActivity.class);
                intent.putExtra("key", accessKey);
                intent.putExtra("name", accessKeyName);
                startActivity(intent);
            }
        });

        Picasso.with(this).load("https://assets.materialup.com/uploads/af36a613-8448-452d-bee1-d70b2bb839ed/preview.png")
                .transform(new CircleTransform())
                .into(ivUserProfile);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        getLocationInfo();

        //Load the initial fragment
        Fragment fragment = MainFragment.newInstance("", "");
        getSupportFragmentManager().beginTransaction().add(R.id.content_frame, fragment, "main_fragment").commit();

        if (ACTION_OPEN_DOOR.equals(getIntent().getAction())) {
            openDoor();
        } else if (ACTION_CREATE_PIN.equals(getIntent().getAction())) {
            showPinDialog();
        } else if (ACTION_PROVISION_CARD.equals(getIntent().getAction())) {
            addCard();
        }
    }

    public void openDoor() {
        String accessKeyName = Utils.getSetting(getString(R.string.preferences_active_access_key_name), "", this);
        String activeLocation = Utils.getSetting(getString(R.string.preferences_active_location), "", this);
        String activeAccessKey = Utils.getSetting(getString(R.string.preferences_active_access_key), "", this);

        Door door = new Door(activeAccessKey, accessKeyName, activeLocation);
        mDatabase.child("doors").push().setValue(door);
    }

    public void addCard() {
        startActivity(new Intent(DrawerActivity.this, ProvisionCardActivity.class));
    }

    public void showPinDialog() {
        DialogFragment newFragment = CreatePinFragment.newInstance(null);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(newFragment, "dialog_create_pin");
        ft.commit();
    }

    /**
     *
     */
    private void getLocationInfo() {
        String activeLocation = Utils.getSetting(getString(R.string.preferences_active_location), "", this);
        if (activeLocation.equals("")) {
            Toast.makeText(getApplicationContext(), "Could not retrieve location information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDatabase.child("locations")
                .child(activeLocation)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot dataSnapshot) {
                        Location location = dataSnapshot.getValue(Location.class);
                        if (null != location) {
                            Toast.makeText(getApplicationContext(), "Welcome to " + location.getName(), Toast.LENGTH_SHORT).show();
                            location.setKey(dataSnapshot.getKey());
                            getSupportActionBar().setTitle(location.getName());
                        } else {
                            Toast.makeText(getApplicationContext(), "Could not retrieve location information", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings) {
            startActivity(new Intent(DrawerActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_spotify) {
            startActivity(new Intent(this, SpotifyActivity.class));
        } else if (id == R.id.nav_manage_access_keys) {
            startActivity(new Intent(this, ManageAccessKeyActivity.class));
        } else {
            Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
