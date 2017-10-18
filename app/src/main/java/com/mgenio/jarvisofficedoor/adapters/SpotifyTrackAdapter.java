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
import android.widget.ImageView;
import android.widget.TextView;

import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.interfaces.OnRecyclerViewItemClickListener;
import com.mgenio.jarvisofficedoor.interfaces.OnRecyclerViewItemLongClickListener;
import com.mgenio.jarvisofficedoor.models.Track;
import com.mgenio.jarvisofficedoor.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SpotifyTrackAdapter extends RecyclerView.Adapter<SpotifyTrackAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private ArrayList<Track> models;
    private OnRecyclerViewItemClickListener<Track> itemClickListener;
    private OnRecyclerViewItemLongClickListener<Track> itemLongClickListener;
    private static Activity mActivity;

    //animation
    private boolean animateItems = true;
    private static final int ANIMATED_ITEMS_COUNT = 7;
    private int lastAnimatedPosition = -1;

    public SpotifyTrackAdapter(ArrayList<Track> models, Activity activity) {
        this.models = models;
        this.mActivity = activity;
    }

    public SpotifyTrackAdapter(Activity activity) {
        this.models = new ArrayList<>();
        this.mActivity = activity;
    }

    public void setTracks(List<Track> models) {
        ArrayList<Track> tracks = new ArrayList<>(models.size());
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
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row_item_spotify_track, viewGroup, false);
        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        runEnterAnimation(viewHolder.itemView, i);
        final Track model = models.get(i);
        viewHolder.itemView.setTag(model);

        viewHolder.tvTrackTitle.setText(model.getName());
        viewHolder.tvArtistName.setText(model.getAlbum().getArtist().getName());

        Picasso.with(mActivity).load(model.getAlbum().getImageUrl())
                .into(viewHolder.ivAlbumArt);
    }

    @Override
    public int getItemCount() {
        return models == null ? 0 : models.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_album_art) ImageView ivAlbumArt;
        @BindView(R.id.tv_track_title) TextView tvTrackTitle;
        @BindView(R.id.tv_artist_name) TextView tvArtistName;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public void clear() {
        models.clear();
        notifyDataSetChanged();
    }

    public void add(Track item, int position) {
        models.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(Track item) {
        int position = models.indexOf(item);
        models.remove(position);
        notifyItemRemoved(position);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener<Track> listener) {
        this.itemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnRecyclerViewItemLongClickListener<Track> listener) {
        this.itemLongClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (itemClickListener != null) {
            Track model = (Track) v.getTag();
            itemClickListener.onItemClick(v, model);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (itemLongClickListener != null) {
            Track model = (Track) v.getTag();
            itemLongClickListener.onItemLongClick(v, model);
            return true;
        } else {
            return false;
        }
    }
}
