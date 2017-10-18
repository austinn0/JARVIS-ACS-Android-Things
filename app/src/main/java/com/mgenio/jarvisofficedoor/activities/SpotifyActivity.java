package com.mgenio.jarvisofficedoor.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.adapters.SpotifyTrackAdapter;
import com.mgenio.jarvisofficedoor.interfaces.OnRecyclerViewItemClickListener;
import com.mgenio.jarvisofficedoor.models.Album;
import com.mgenio.jarvisofficedoor.models.Artist;
import com.mgenio.jarvisofficedoor.models.Track;
import com.mgenio.jarvisofficedoor.utils.Utils;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SpotifyActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1337;
    @SuppressWarnings("SpellCheckingInspection")
    private static final String CLIENT_ID = "029465a9598a4e8ca17a097551537808";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String REDIRECT_URI = "testschema://callback";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.input_song_search) EditText inputSongSearch;
    @BindView(R.id.rv_tracks) RecyclerView rvTracks;

    private SpotifyTrackAdapter spotifyTrackAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Boom Music");
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        spotifyTrackAdapter = new SpotifyTrackAdapter(this);
        rvTracks.setLayoutManager(new LinearLayoutManager(this));
        rvTracks.setItemAnimator(new DefaultItemAnimator());
        rvTracks.setAdapter(spotifyTrackAdapter);

        spotifyTrackAdapter.setOnItemClickListener(new OnRecyclerViewItemClickListener<Track>() {
            @Override public void onItemClick(View view, Track model) {
                Intent intent = new Intent(SpotifyActivity.this, SpotifyTrackDetailActivity.class);
                intent.putExtra("track_id", model.getId());
                intent.putExtra("track_title", model.getName());
                intent.putExtra("preview_url", model.getPreviewUrl());
                intent.putExtra("album_url", model.getAlbum().getImageUrl());
                startActivity(intent);
            }
        });
    }

    @OnClick(R.id.btn_search)
    public void spotifySearch() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Loading results...");
        dialog.show();

        spotifyTrackAdapter.clear();
        final ArrayList<Track> searchTracks = new ArrayList<>();
        final String authToken = Utils.getSetting("AUTH_TOKEN", "NO_TOKEN_FOUND", getApplicationContext());


        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, "https://api.spotify.com/v1/search?type=track&q=" + inputSongSearch.getText().toString().replace(" ", "%20"),
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                dialog.dismiss();
                Log.d("SpotifyActivity", response.toString());

                try {
                    JSONObject tracks = response.getJSONObject("tracks");
                    JSONArray items = tracks.getJSONArray("items");
                    for (int i = 0; i < items.length(); i++) {

                        String id = items.getJSONObject(i).getString("id");
                        String name = items.getJSONObject(i).getString("name");
                        String previewUrl = items.getJSONObject(i).getString("preview_url");

                        JSONObject albumObject = items.getJSONObject(i).getJSONObject("album");
                        String albumName = albumObject.getString("name");

                        JSONArray images = albumObject.getJSONArray("images");
                        String imageUrl = images.getJSONObject(0).getString("url");

                        JSONArray artists = albumObject.getJSONArray("artists");
                        String artistName = artists.getJSONObject(0).getString("name");

                        Artist artist = new Artist("", artistName);
                        Album album = new Album("", albumName, artist, imageUrl);
                        Track track = new Track(id, name, previewUrl, album);

                        if (null != previewUrl && previewUrl.startsWith("https")) {
                            searchTracks.add(track);
                        }

                        Log.d("PreviewUrl", track.getPreviewUrl());

                        //spotifyTrackAdapter.add(track, 0);
                    }
                    spotifyTrackAdapter.setTracks(searchTracks);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("SpotifyActivity", "Error: " + error.getMessage());
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                //headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };


        try {
            Set<String> keys = req.getHeaders().keySet();
            for(String key : keys) {
                Log.d(key, req.getHeaders().get(key));
            }
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
        queue.add(req);
    }

    private void openLoginWindow() {
        final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private", "streaming"})
                .build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    private boolean isLoggedIn() {
        //return mPlayer != null && mPlayer.isLoggedIn();
        return false;
    }

    @OnClick(R.id.btn_spotify_login)
    public void onLoginButtonClicked(View view) {
        if (!isLoggedIn()) {
            //logStatus("Logging in");
            openLoginWindow();
        } else {
            //mPlayer.logout();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    onAuthenticationComplete(response);
                    break;

                // Auth flow returned an error
                case ERROR:
                    Log.d("SpotifyActivity", "Auth error: " + response.getError());
                    //logStatus("Auth error: " + response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    Log.d("SpotifyActivity", "Auth result: " + response.getType());
                    //logStatus("Auth result: " + response.getType());
            }
        }
    }

    private void onAuthenticationComplete(AuthenticationResponse authResponse) {
        Log.d("SpotifyActivity", "Got authentication token: " + authResponse.getAccessToken());
        Snackbar.make(toolbar, "Successfully Logged In", Snackbar.LENGTH_SHORT).show();
        Utils.storeSetting("AUTH_TOKEN", authResponse.getAccessToken(), this);
        // Once we have obtained an authorization token, we can proceed with creating a Player.
        //logStatus("Got authentication token");
//        if (mPlayer == null) {
//            Config playerConfig = new Config(getApplicationContext(), authResponse.getAccessToken(), CLIENT_ID);
//            // Since the Player is a static singleton owned by the Spotify class, we pass "this" as
//            // the second argument in order to refcount it properly. Note that the method
//            // Spotify.destroyPlayer() also takes an Object argument, which must be the same as the
//            // one passed in here. If you pass different instances to Spotify.getPlayer() and
//            // Spotify.destroyPlayer(), that will definitely result in resource leaks.
//            mPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
//                @Override
//                public void onInitialized(SpotifyPlayer player) {
//                    //logStatus("-- Player initialized --");
//                    mPlayer.setConnectivityStatus(mOperationCallback, getNetworkConnectivity(DemoActivity.this));
//                    mPlayer.addNotificationCallback(DemoActivity.this);
//                    mPlayer.addConnectionStateCallback(DemoActivity.this);
//                    // Trigger UI refresh
//                    updateView();
//                }
//
//                @Override
//                public void onError(Throwable error) {
//                    //logStatus("Error in initialization: " + error.getMessage());
//                }
//            });
//        } else {
//            mPlayer.login(authResponse.getAccessToken());
//        }
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
