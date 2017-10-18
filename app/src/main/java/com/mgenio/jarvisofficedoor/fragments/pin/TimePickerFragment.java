package com.mgenio.jarvisofficedoor.fragments.pin;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.dialogs.CreatePinFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TimePickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimePickerFragment extends Fragment implements TimePicker.OnTimeChangedListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    @BindView(R.id.timePicker) TimePicker timePicker;
    private Calendar mCalendar;

    public TimePickerFragment() {
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
    public static TimePickerFragment newInstance(String param1, String param2) {
        TimePickerFragment fragment = new TimePickerFragment();
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
        View view = inflater.inflate(R.layout.fragment_time_picker, container, false);
        ButterKnife.bind(this, view);

        timePicker.setOnTimeChangedListener(this);

        return view;
    }

    @Override public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        CreatePinFragment fragment = ((CreatePinFragment) getParentFragment());
        if (null != fragment) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            mCalendar = Calendar.getInstance();
            mCalendar.set(0, 0, 0, hourOfDay, minute);

            fragment.tabs.getTabAt(fragment.pagerPin.getCurrentItem()).setText(getFormattedTime());
            fragment.pagerPin.setCurrentItem(fragment.pagerPin.getCurrentItem() + 1, true);
        }
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        if (null != mCalendar) {
            return sdf.format(mCalendar.getTime());
        } else {
            return null;
        }
    }

    public Calendar getTime() {
        return mCalendar;
    }
}
