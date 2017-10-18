package com.mgenio.jarvisofficedoor.adapters;

/**
 * Created by anelson on 1/7/16.
 */

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.interfaces.OnRecyclerViewItemClickListener;
import com.mgenio.jarvisofficedoor.interfaces.OnRecyclerViewItemLongClickListener;
import com.mgenio.jarvisofficedoor.models.Card;
import com.mgenio.jarvisofficedoor.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private ArrayList<Card> models;
    private OnRecyclerViewItemClickListener<Card> itemClickListener;
    private OnRecyclerViewItemLongClickListener<Card> itemLongClickListener;
    private static Activity mActivity;

    //animation
    private boolean animateItems = false;
    private static final int ANIMATED_ITEMS_COUNT = 7;
    private int lastAnimatedPosition = -1;

    public CardAdapter(ArrayList<Card> models, Activity activity) {
        this.models = models;
        this.mActivity = activity;
    }

    public CardAdapter(Activity activity) {
        this.models = new ArrayList<>();
        this.mActivity = activity;
    }

    public void setLogs(List<Card> models) {
        ArrayList<Card> tracks = new ArrayList<>(models.size());
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
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row_item_card, viewGroup, false);
        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        runEnterAnimation(viewHolder.itemView, i);
        final Card model = models.get(i);
        viewHolder.itemView.setTag(model);

        viewHolder.tvCardNumber.setText(model.getFacility() + " - " + model.getCard());
    }

    @Override
    public int getItemCount() {
        return models == null ? 0 : models.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_card_number) TextView tvCardNumber;
        @BindView(R.id.tv_card_expiration_date) TextView tvCardExpirationDate;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public void add(Card item) {
        boolean duplicate = false;
        for (Card log : models) {
            if (log.getKey().equals(item.getKey())) {
                duplicate = true;
            }
        }

        if (!duplicate) {
            models.add(0, item);
            notifyItemInserted(0);
        }
    }

    public void remove(Card item) {
        models.remove(item);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener<Card> listener) {
        this.itemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnRecyclerViewItemLongClickListener<Card> listener) {
        this.itemLongClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (itemClickListener != null) {
            Card model = (Card) v.getTag();
            itemClickListener.onItemClick(v, model);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (itemLongClickListener != null) {
            Card model = (Card) v.getTag();
            itemLongClickListener.onItemLongClick(v, model);
            return true;
        } else {
            return false;
        }
    }
}
