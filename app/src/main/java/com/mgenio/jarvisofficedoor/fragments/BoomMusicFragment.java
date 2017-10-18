package com.mgenio.jarvisofficedoor.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.models.Track;
import com.mgenio.jarvisofficedoor.utils.Utils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link BoomMusicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BoomMusicFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    @BindView(R.id.iv_album_art) ImageView ivAlbumArt;
    @BindView(R.id.tv_track_title) TextView tvTrackTitle;
    @BindView(R.id.tv_artist_name) TextView tvArtistName;
    @BindView(R.id.tv_album_name) TextView tvAlbumName;

    private DatabaseReference mDatabase;
    private Track mTrack;

    public BoomMusicFragment() {
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
    public static BoomMusicFragment newInstance(String param1, String param2) {
        BoomMusicFragment fragment = new BoomMusicFragment();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_boom_music, container, false);
        ButterKnife.bind(this, view);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvTrackTitle.setText(Utils.getSetting("BOOM_MUSIC_TRACK_TITLE", "No Track Selected", getActivity()));
        Picasso.with(getActivity()).load(Utils.getSetting("BOOM_MUSIC_ALBUM_URL", "http://www.myscapp.eu/img/album_default.gif", getActivity()))
                .into(ivAlbumArt);

//        SpotifyService spotify = new SpotifyApi().getService();
//        spotify.getTrack(mParam1, new Callback<Track>() {
//            @Override public void success(Track track, Response response) {
//                mTrack = track;
//
//                Picasso.with(getActivity()).load(track.album.images.get(0).url)
//                        .into(ivAlbumArt);
//
//                tvTrackTitle.setText(track.name);
//                tvArtistName.setText(track.artists.get(0).name);
//                tvAlbumName.setText(track.album.name);
//            }
//
//            @Override public void failure(RetrofitError error) {
//
//            }
//        });

        ivAlbumArt.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                updateBoomMusic();
            }
        });

        return view;
    }

    @OnClick(R.id.fab_previous)
    public void previous() {
        MainFragment fragment = (MainFragment) getActivity().getSupportFragmentManager().findFragmentByTag("main_fragment");
        if (null != fragment) {
            if (fragment.pagerBoomMusic.getCurrentItem() > 0) {
                fragment.pagerBoomMusic.setCurrentItem(fragment.pagerBoomMusic.getCurrentItem() - 1, true);
            }
        }
    }

    @OnClick(R.id.fab_next)
    public void next() {
        MainFragment fragment = (MainFragment) getActivity().getSupportFragmentManager().findFragmentByTag("main_fragment");
        if (null != fragment) {
            if (fragment.pagerBoomMusic.getCurrentItem() < fragment.pagerBoomMusic.getChildCount()) {
                fragment.pagerBoomMusic.setCurrentItem(fragment.pagerBoomMusic.getCurrentItem() + 1, true);
            }
        }
    }

    /**
     *
     */
    private void updateBoomMusic() {
        MainFragment fragment = ((MainFragment) getParentFragment());
        if (fragment == null) {
            return;
        }

//        BoomMusic boomMusic = fragment.getBoomMusic();
//        boomMusic.setSpotifyUrl(mTrack.preview_url);
//
//        //this is stupid
//        BoomMusic temp = new BoomMusic(boomMusic.getAccessKey(), boomMusic.getSpotifyUrl());
//
//        mDatabase.child("boom-music").child(boomMusic.getKey()).setValue(temp).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override public void onSuccess(Void aVoid) {
//                Toast.makeText(getActivity(), "Track set as current boom track", Toast.LENGTH_SHORT).show();
//
//                Utils.storeSetting("BOOM_MUSIC_ALBUM_URL", mTrack.album.images.get(0).url, getActivity());
//                Utils.storeSetting("BOOM_MUSIC_PREVIEW_URL", mTrack.preview_url, getActivity());
//                Utils.storeSetting("BOOM_MUSIC_TRACK_TITLE", mTrack.name, getActivity());
//            }
//        });
    }

}
