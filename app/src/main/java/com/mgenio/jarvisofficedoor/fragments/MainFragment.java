package com.mgenio.jarvisofficedoor.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.activities.DrawerActivity;
import com.mgenio.jarvisofficedoor.adapters.AccessKeyAdapter;
import com.mgenio.jarvisofficedoor.adapters.LogsAdapter;
import com.mgenio.jarvisofficedoor.adapters.PagerAdapter;
import com.mgenio.jarvisofficedoor.models.AccessKey;
import com.mgenio.jarvisofficedoor.models.BoomMusic;
import com.mgenio.jarvisofficedoor.models.Logs;
import com.mgenio.jarvisofficedoor.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements ChildEventListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    @BindView(R.id.rv_logs) RecyclerView rvLogs;
    private LogsAdapter logsAdapter;

    @BindView(R.id.pager_boom_music) public ViewPager pagerBoomMusic;
    private PagerAdapter pagerAdapter;

    @BindView(R.id.rv_access_keys) RecyclerView rvAccessKeys;
    private AccessKeyAdapter accessKeyAdapter;

    private DatabaseReference mDatabase;
    private BoomMusic mBoomMusic;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
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
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        String activeLocation = Utils.getSetting(getString(R.string.preferences_active_location), "", getActivity());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("logs").orderByChild("location").startAt(activeLocation).endAt(activeLocation).limitToLast(25).addChildEventListener(this);
        getLogs(activeLocation);

        logsAdapter = new LogsAdapter(getActivity());
        rvLogs.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvLogs.setItemAnimator(new DefaultItemAnimator());
        rvLogs.setAdapter(logsAdapter);

        accessKeyAdapter = new AccessKeyAdapter(getActivity(), R.layout.list_row_item_access_key_user);
        rvAccessKeys.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        rvAccessKeys.setItemAnimator(new DefaultItemAnimator());
        rvAccessKeys.setAdapter(accessKeyAdapter);
        getAccessKeyUsers();

        pagerAdapter = new PagerAdapter(getChildFragmentManager());
        pagerBoomMusic.setAdapter(pagerAdapter);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Utils.getScreenWidth(getActivity()), Utils.getScreenWidth(getActivity()) + 300);
        pagerBoomMusic.setLayoutParams(lp);
        setBoomFragments();
        setBoomMusic();

        return view;
    }

    /**
     * Initial get of all Logs
     *
     * @param activeLocation
     */
    private void getLogs(String activeLocation) {
        final ArrayList<Logs> logs = new ArrayList<>();
        mDatabase.child("logs").orderByChild("location").startAt(activeLocation).endAt(activeLocation).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Logs log = snapshot.getValue(Logs.class);
                    log.setKey(snapshot.getKey());

                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
                    String today = sdf.format(new Date());
                    String logDay = sdf.format(new Date(log.getTimestamp()));

                    Log.d(today, logDay);

                    if (today.equals(logDay)) {
                        logs.add(0, log);
                    }
                }

                logsAdapter.setLogs(logs);
            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     *
     */
    private void getAccessKeyUsers() {
        final ArrayList<AccessKey> accessKeys = new ArrayList<>();
        final String activeAccessKey = Utils.getSetting(getString(R.string.preferences_active_access_key), "", getActivity());
        String activeLocation = Utils.getSetting(getString(R.string.preferences_active_location), "", getActivity());
        mDatabase.child("access-keys").orderByChild("location").startAt(activeLocation).endAt(activeLocation).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AccessKey accessKey = snapshot.getValue(AccessKey.class);
                    accessKey.setKey(snapshot.getKey());

                    if (!snapshot.getKey().equals(activeAccessKey)) {
                        accessKeys.add(accessKey);
                    }
                }

                accessKeyAdapter.setModels(accessKeys);
            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setBoomFragments() {
        ArrayList<Fragment> boomFragments = new ArrayList<>();
//        Set<String> boomIds = Utils.getSetting(getString(R.string.preferences_boom_music), new HashSet<String>(), getActivity());
//        Log.d("Boom Music Size: ", boomIds.size() + "");
//        for (String id : boomIds) {
//            boomFragments.add(BoomMusicFragment.newInstance(id, ""));
//        }
        boomFragments.add(BoomMusicFragment.newInstance("", ""));
        pagerAdapter.setFragments(boomFragments, new ArrayList<String>(boomFragments.size()));
    }

    /**
     *
     */
    private void setBoomMusic() {

        final String activeAccessKey = Utils.getSetting(getString(R.string.preferences_active_access_key), "", getActivity());

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
                                    mBoomMusic = boomMusic;
                                    mBoomMusic.setKey(snapshot.getKey());
                                }
                            }
                        }
                    }

                    @Override public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public BoomMusic getBoomMusic() {
        return mBoomMusic;
    }

    @OnClick(R.id.card_open_door)
    public void openDoor() {
        ((DrawerActivity) getActivity()).openDoor();
    }

    @OnClick(R.id.card_add_card)
    public void addCard() {
        ((DrawerActivity) getActivity()).addCard();
//        startActivity(new Intent(getActivity(), ProvisionCardActivity.class));
    }

    @OnClick(R.id.card_create_pin)
    public void showCreatePinDialog() {
        ((DrawerActivity) getActivity()).showPinDialog();
//        DialogFragment newFragment = CreatePinFragment.newInstance(null);
//        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
//        ft.add(newFragment, "dialog_create_pin");
//        ft.commit();
    }

    @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {

    }

    @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Logs log = dataSnapshot.getValue(Logs.class);
        log.setKey(dataSnapshot.getKey());

        SimpleDateFormat sdf = new SimpleDateFormat("dd yyyy");
        String today = sdf.format(new Date());
        String logDay = sdf.format(new Date(log.getTimestamp()));

        Log.d(today, logDay);

        if (today.equals(logDay)) {
            logsAdapter.add(log);
            rvLogs.scrollToPosition(0);
        }
    }

    @Override public void onChildRemoved(DataSnapshot dataSnapshot) {
        Logs log = dataSnapshot.getValue(Logs.class);
        log.setKey(dataSnapshot.getKey());

        logsAdapter.remove(log);
    }

    @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override public void onCancelled(DatabaseError databaseError) {

    }
}
