package com.mgenio.jarvisofficedoor.fragments.pin;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.DatePicker;

import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.dialogs.CreatePinFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.mgenio.jarvisofficedoor.R.id.calendar;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link DatePickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DatePickerFragment extends Fragment implements DatePicker.OnDateChangedListener, CalendarView.OnDateChangeListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    @BindView(R.id.datePicker) DatePicker datePicker;
    @BindView(calendar) CalendarView calendarView;
    private Calendar mCalendar;

    public DatePickerFragment() {
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
    public static DatePickerFragment newInstance(String param1, String param2) {
        DatePickerFragment fragment = new DatePickerFragment();
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
        View view = inflater.inflate(R.layout.fragment_date_picker, container, false);
        ButterKnife.bind(this, view);

        Calendar calendar = Calendar.getInstance();
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), this);
        calendarView.setOnDateChangeListener(this);

        return view;
    }

    @Override public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        CreatePinFragment fragment = ((CreatePinFragment) getParentFragment());
        if (null != fragment) {
            fragment.pagerPin.setCurrentItem(fragment.pagerPin.getCurrentItem() + 1, true);
        }
    }

    @Override public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
        CreatePinFragment fragment = ((CreatePinFragment) getParentFragment());
        if (null != fragment) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            mCalendar = Calendar.getInstance();
            mCalendar.set(year, month, dayOfMonth);

            fragment.tabs.getTabAt(fragment.pagerPin.getCurrentItem()).setText(getFormattedDate());
            fragment.pagerPin.setCurrentItem(fragment.pagerPin.getCurrentItem() + 1, true);
        }
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        if (null != mCalendar) {
            return sdf.format(mCalendar.getTime());
        } else {
            return null;
        }
    }

    public Calendar getDate() {
        return mCalendar;
    }
}
