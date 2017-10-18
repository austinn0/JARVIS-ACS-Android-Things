package com.mgenio.jarvisofficedoor.dialogs;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.adapters.PagerAdapter;
import com.mgenio.jarvisofficedoor.fragments.pin.DatePickerFragment;
import com.mgenio.jarvisofficedoor.fragments.pin.PinFragment;
import com.mgenio.jarvisofficedoor.fragments.pin.TimePickerFragment;
import com.mgenio.jarvisofficedoor.models.AccessKey;
import com.mgenio.jarvisofficedoor.models.Pin;
import com.mgenio.jarvisofficedoor.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Austin Nelson on 2/16/2017.
 */

public class CreatePinFragment extends DialogFragment implements TabLayout.OnTabSelectedListener, ViewPager.OnPageChangeListener {

    @BindView(R.id.sliding_tabs) public TabLayout tabs;
    @BindView(R.id.pager_temp_pin) public ViewPager pagerPin;
    private PagerAdapter pinAdapter;

    private DatabaseReference mDatabase;
    private String accessKey;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static CreatePinFragment newInstance(String accessKey) {
        CreatePinFragment f = new CreatePinFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("accessKey", accessKey);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        accessKey = getArguments().getString("accessKey");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_create_pin, container, false);
        ButterKnife.bind(this, v);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        pinAdapter = new PagerAdapter(getChildFragmentManager());
        pagerPin.setAdapter(pinAdapter);
        pagerPin.setOffscreenPageLimit(2);
        pagerPin.addOnPageChangeListener(this);

        int height = Utils.getScreenHeight(getActivity()) / 2;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
        lp.addRule(RelativeLayout.BELOW, R.id.sliding_tabs);
        pagerPin.setLayoutParams(lp);

        tabs.addTab(tabs.newTab().setText("Pin"));
        tabs.addOnTabSelectedListener(this);

        pinAdapter.add(PinFragment.newInstance(accessKey, ""), "Pin");

        return v;
    }

    @OnClick(R.id.btn_create_pin)
    public void createPin() {
        PinFragment pinFragment = ((PinFragment) pinAdapter.getItem(0));
        DatePickerFragment datePickerFragment = null;
        TimePickerFragment timePickerFragment = null;

        if (pinFragment.isTempPin()) {
            datePickerFragment = ((DatePickerFragment) pinAdapter.getItem(1));
            timePickerFragment = ((TimePickerFragment) pinAdapter.getItem(2));
        }

        if (null == pinFragment) {
            //literally can't do anything
            getDialog().dismiss();
        }

        if (pinFragment.getPin().length() == 0) {
            Snackbar.make(getDialog().getWindow().getDecorView(), "Please enter a pin", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (pinFragment.isTempPin()) {
            //have they set a date and time?
            if (null != datePickerFragment.getDate() && null != timePickerFragment.getTime()) {
                createTempPin(pinFragment.getPin(), datePickerFragment.getDate(), timePickerFragment.getTime());
            } else if (null != datePickerFragment && null != timePickerFragment) {
                switch (pagerPin.getCurrentItem()) {
                    case 0:
                        if (pinFragment.getPin().length() == 0) {
                            Snackbar.make(getDialog().getWindow().getDecorView(), "Please enter a pin", Snackbar.LENGTH_SHORT).show();
                            return;
                        }

                        tabs.getTabAt(pagerPin.getCurrentItem()).setText(pinFragment.getPin());
                        break;
                    case 1:
                        if (null == datePickerFragment.getFormattedDate()) {
                            Snackbar.make(getDialog().getWindow().getDecorView(), "Please select a date", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        tabs.getTabAt(pagerPin.getCurrentItem()).setText(datePickerFragment.getFormattedDate());
                        break;
                    case 2:
                        if (null == timePickerFragment.getTime()) {
                            Snackbar.make(getDialog().getWindow().getDecorView(), "Please select a time", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        tabs.getTabAt(pagerPin.getCurrentItem()).setText(timePickerFragment.getFormattedTime());
                        break;
                    default:
                        break;
                }

                pagerPin.setCurrentItem(pagerPin.getCurrentItem() + 1, true);
            } else {
                Toast.makeText(getActivity(), "An error has occured", Toast.LENGTH_SHORT).show();
                getDialog().dismiss();
            }
            return;
        }

        String activeLocation = Utils.getSetting(getString(R.string.preferences_active_location), "", getActivity());
        AccessKey activeAccessKey = pinFragment.getSelectedAccessKey();
        if (activeAccessKey.equals("")) {
            return;
        }

        Pin pin = new Pin(pinFragment.getPin(), activeAccessKey.getKey(), activeLocation, -1);
        mDatabase.child("pins").push().setValue(pin).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity(), "PIN created successfully", Toast.LENGTH_SHORT).show();
                getDialog().dismiss();
            }
        });
    }

    /**
     * @param pin
     * @param date
     * @param time
     */
    private void createTempPin(final String pin, Calendar date, Calendar time) {
        final String activeLocation = Utils.getSetting(getString(R.string.preferences_active_location), "", getActivity());
        if (activeLocation.equals("")) {
            Toast.makeText(getActivity(), "Invalid location", Toast.LENGTH_SHORT).show();
            //finish? dismiss?
            return;
        }

        final Calendar expirationDate = Calendar.getInstance();
        expirationDate.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));

        final String tempAccessKey = mDatabase.child("access-keys").push().getKey();
        AccessKey tempKey = new AccessKey("temp", activeLocation, null, true, true, expirationDate.getTimeInMillis());

        mDatabase.child("access-keys").child(tempAccessKey).setValue(tempKey).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override public void onSuccess(Void aVoid) {
                Pin tempPin = new Pin(pin, tempAccessKey, activeLocation, expirationDate.getTimeInMillis());
                mDatabase.child("pins").push().setValue(tempPin).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Your temporary PIN has been created", Toast.LENGTH_SHORT).show();
                        getDialog().dismiss();
                    }
                });
            }
        });
    }

    public void addDateTime() {
        pinAdapter.add(DatePickerFragment.newInstance("", ""), "Date");
        pinAdapter.add(TimePickerFragment.newInstance("", ""), "Time");
        tabs.addTab(tabs.newTab().setText("Date"));
        tabs.addTab(tabs.newTab().setText("Time"));
    }

    public void removeDateTime() {
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(PinFragment.newInstance("", ""));
        pinAdapter = new PagerAdapter(getChildFragmentManager());
        pinAdapter.setFragments(fragments, new ArrayList<String>(fragments.size()));
        pagerPin.setAdapter(pinAdapter);
        pagerPin.setOffscreenPageLimit(2);
        tabs.removeAllTabs();
        tabs.addTab(tabs.newTab().setText("Pin"));
    }

    @Override
    public void onStart() {
        super.onStart();

        WindowManager.LayoutParams lp = getDialog().getWindow().getAttributes();
        lp.width = Utils.getScreenWidth(getActivity()) - 75;
        getDialog().getWindow().setAttributes(lp);
    }

    @Override public void onTabSelected(TabLayout.Tab tab) {
        pagerPin.setCurrentItem(tab.getPosition(), true);
    }

    @Override public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override public void onPageSelected(int position) {
        tabs.getTabAt(position).select();
    }

    @Override public void onPageScrollStateChanged(int state) {

    }
}
