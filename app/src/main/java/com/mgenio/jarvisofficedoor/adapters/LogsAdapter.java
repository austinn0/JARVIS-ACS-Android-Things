package com.mgenio.jarvisofficedoor.adapters;

/**
 * Created by anelson on 1/7/16.
 */

import android.app.Activity;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.interfaces.OnRecyclerViewItemClickListener;
import com.mgenio.jarvisofficedoor.interfaces.OnRecyclerViewItemLongClickListener;
import com.mgenio.jarvisofficedoor.models.AccessKey;
import com.mgenio.jarvisofficedoor.models.Logs;
import com.mgenio.jarvisofficedoor.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private ArrayList<Logs> models;
    private OnRecyclerViewItemClickListener<Logs> itemClickListener;
    private OnRecyclerViewItemLongClickListener<Logs> itemLongClickListener;
    private static Activity mActivity;

    //animation
    private boolean animateItems = false;
    private static final int ANIMATED_ITEMS_COUNT = 7;
    private int lastAnimatedPosition = -1;

    public LogsAdapter(ArrayList<Logs> models, Activity activity) {
        this.models = models;
        this.mActivity = activity;
    }

    public LogsAdapter(Activity activity) {
        this.models = new ArrayList<>();
        this.mActivity = activity;
    }

    public void setLogs(List<Logs> models) {
        ArrayList<Logs> tracks = new ArrayList<>(models.size());
        tracks.addAll(models);

        this.models = tracks;
        notifyDataSetChanged();
    }

    private void runEnterAnimation(View view, int position) {

        if (!animateItems || position >= ANIMATED_ITEMS_COUNT - 1) {
            return;
        }

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(Utils.getScreenHeight(mActivity));
            view.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator(3.f))
                    .setDuration((position * 100) + 1000)
                    .start();
        } else {
            animateItems = false;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row_item_logs, viewGroup, false);
        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        runEnterAnimation(viewHolder.itemView, i);
        final Logs model = models.get(i);
        viewHolder.itemView.setTag(model);

        SimpleDateFormat h = new SimpleDateFormat("h");
        SimpleDateFormat hsm = new SimpleDateFormat("h:mm");
        SimpleDateFormat a = new SimpleDateFormat("a");

        String hour = h.format(new Date(model.getTimestamp()));

        if (hour.length() == 1) {
            viewHolder.hms.setText("\t" + hsm.format(new Date(model.getTimestamp())));
        } else {
            viewHolder.hms.setText(hsm.format(new Date(model.getTimestamp())));
        }

        viewHolder.a.setText(a.format(new Date(model.getTimestamp())));

        if (null != model.getCard()) {
            viewHolder.tvTimeline.setBackgroundColor(mActivity.getResources().getColor(R.color.red));
            viewHolder.fabLogIndicator.setBackgroundTintList(ColorStateList.valueOf(mActivity.getResources().getColor(R.color.red)));
            viewHolder.fabLogIndicator.setImageResource(R.mipmap.ic_payment_white_24dp);
        } else if (null != model.getPin()) {
            viewHolder.tvTimeline.setBackgroundColor(mActivity.getResources().getColor(R.color.purple));
            viewHolder.fabLogIndicator.setBackgroundTintList(ColorStateList.valueOf(mActivity.getResources().getColor(R.color.purple)));
            viewHolder.fabLogIndicator.setImageResource(R.mipmap.ic_fiber_pin_white_24dp);
        } else {
            viewHolder.tvTimeline.setBackgroundColor(mActivity.getResources().getColor(R.color.green));
            viewHolder.fabLogIndicator.setBackgroundTintList(ColorStateList.valueOf(mActivity.getResources().getColor(R.color.green)));
            viewHolder.fabLogIndicator.setImageResource(R.mipmap.ic_home_white_24dp);
        }

        if (null != model.getAccessKey()) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("access-keys").child(model.getAccessKey()).addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(DataSnapshot dataSnapshot) {
                    AccessKey accessKey = dataSnapshot.getValue(AccessKey.class);
                    if (model.isAuthorized()) {
                        viewHolder.tvLogMessage.setText("Door unlocked by " + ((null == accessKey.getName()) ? dataSnapshot.getKey() : accessKey.getName()));
                    } else {
                        viewHolder.tvLogMessage.setText("Unauthorized access by " + ((null == accessKey.getName()) ? dataSnapshot.getKey() : accessKey.getName()));
                    }
                }

                @Override public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else if (null != model.getCard()) {
            viewHolder.tvLogMessage.setText("Invalid card!");
        } else if (null != model.getPin()) {
            viewHolder.tvLogMessage.setText("Invalid pin!");
        }
    }

    @Override
    public int getItemCount() {
        return models == null ? 0 : models.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_hms) TextView hms;
        @BindView(R.id.tv_a) TextView a;
        @BindView(R.id.fab_log_indicator) FloatingActionButton fabLogIndicator;
        @BindView(R.id.tv_timeline) TextView tvTimeline;
        @BindView(R.id.tv_log_message) TextView tvLogMessage;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public void add(Logs item) {
        boolean duplicate = false;
        for (Logs log : models) {
            if (log.getKey().equals(item.getKey())) {
                duplicate = true;
            }
        }

        if (!duplicate) {
            models.add(0, item);
            notifyItemInserted(0);
        }
    }

    public void remove(Logs item) {
        models.remove(item);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener<Logs> listener) {
        this.itemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnRecyclerViewItemLongClickListener<Logs> listener) {
        this.itemLongClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (itemClickListener != null) {
            Logs model = (Logs) v.getTag();
            itemClickListener.onItemClick(v, model);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (itemLongClickListener != null) {
            Logs model = (Logs) v.getTag();
            itemLongClickListener.onItemLongClick(v, model);
            return true;
        } else {
            return false;
        }
    }

    class ValueComparator implements Comparator<Logs> {
        Map<String, Logs> base;

        public ValueComparator(Map<String, Logs> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with
        // equals.
        public int compare(Logs a, Logs b) {
            if (base.get(a).getTimestamp() >= base.get(b).getTimestamp()) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }
}
