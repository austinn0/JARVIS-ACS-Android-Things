package com.mgenio.jarvisofficedoor.activities;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.models.BoomMusic;
import com.mgenio.jarvisofficedoor.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SpotifyTrackDetailActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.iv_album_art) ImageView ivAlbumArt;

    private String albumUrl;
    private String previewUrl;
    private String trackTitle;
    private String trackId;

    private MediaPlayer mediaPlayer;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_track_detail);
        ButterKnife.bind(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        trackId = getIntent().getStringExtra("track_id");
        albumUrl = getIntent().getStringExtra("album_url");
        previewUrl = getIntent().getStringExtra("preview_url");
        trackTitle = getIntent().getStringExtra("track_title");

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(trackTitle);

        mediaPlayer = MediaPlayer.create(this, Uri.parse(previewUrl));

        Picasso.with(this).load(albumUrl)
                .into(ivAlbumArt);
    }

    @OnClick(R.id.btn_preview_track)
    public void previewTrack() {
        try {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_set_boom_music)
    public void setBoomMusic() {
        final String activeAccessKey = Utils.getSetting(getString(R.string.preferences_active_access_key), "", getApplicationContext());

        mDatabase.child("boom-music")
                .orderByChild("accessKey")
                .startAt(activeAccessKey)
                .endAt(activeAccessKey)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                BoomMusic boomMusic = snapshot.getValue(BoomMusic.class);

                                if (null != boomMusic) {
                                    updateBoomMusic(snapshot.getKey(), boomMusic);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }

                        } else {
                            createNewBoomMusic(activeAccessKey);
                        }

                        finish();
                    }

                    @Override public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /**
     * @param boomMusicKey
     * @param boomMusic
     */
    private void updateBoomMusic(String boomMusicKey, BoomMusic boomMusic) {
        boomMusic.setSpotifyUrl(previewUrl);
        mDatabase.child("boom-music").child(boomMusicKey).setValue(boomMusic).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Track set as current boom track", Toast.LENGTH_SHORT).show();

                Utils.storeSetting("BOOM_MUSIC_ALBUM_URL", albumUrl, getApplicationContext());
                Utils.storeSetting("BOOM_MUSIC_PREVIEW_URL", previewUrl, getApplicationContext());
                Utils.storeSetting("BOOM_MUSIC_TRACK_TITLE", trackTitle, getApplicationContext());

                Set<String> boomIds = Utils.getSetting(getString(R.string.preferences_boom_music), new HashSet<String>(), getApplicationContext());
                boomIds.add(trackId);
                Utils.storeSetting(getString(R.string.preferences_boom_music), boomIds, getApplicationContext());

                finish();
            }
        });
    }

    /**
     * @param activeAccessKey
     */
    private void createNewBoomMusic(String activeAccessKey) {
        BoomMusic boomMusic = new BoomMusic(activeAccessKey, previewUrl);
        mDatabase.child("boom-music").push().setValue(boomMusic).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Track set as current boom track", Toast.LENGTH_SHORT).show();

                Utils.storeSetting("BOOM_MUSIC_ALBUM_URL", albumUrl, getApplicationContext());
                Utils.storeSetting("BOOM_MUSIC_PREVIEW_URL", previewUrl, getApplicationContext());
                Utils.storeSetting("BOOM_MUSIC_TRACK_TITLE", trackTitle, getApplicationContext());

                Set<String> boomIds = Utils.getSetting(getString(R.string.preferences_boom_music), new HashSet<String>(), getApplicationContext());
                boomIds.add(trackId);
                Utils.storeSetting(getString(R.string.preferences_boom_music), boomIds, getApplicationContext());

                finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mediaPlayer) {
            mediaPlayer.stop();
        }
    }
}
