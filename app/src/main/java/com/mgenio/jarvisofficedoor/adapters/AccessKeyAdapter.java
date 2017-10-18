package com.mgenio.jarvisofficedoor.adapters;

/**
 * Created by anelson on 1/7/16.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.interfaces.OnRecyclerViewItemClickListener;
import com.mgenio.jarvisofficedoor.interfaces.OnRecyclerViewItemLongClickListener;
import com.mgenio.jarvisofficedoor.models.AccessKey;
import com.mgenio.jarvisofficedoor.utils.CircleTransform;
import com.mgenio.jarvisofficedoor.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AccessKeyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private ArrayList<AccessKey> models;
    private OnRecyclerViewItemClickListener<AccessKey> itemClickListener;
    private OnRecyclerViewItemLongClickListener<AccessKey> itemLongClickListener;
    private Activity mActivity;
    private int layout;


    //animation
    private boolean animateItems = false;
    private static final int ANIMATED_ITEMS_COUNT = 7;
    private int lastAnimatedPosition = -1;

    public AccessKeyAdapter(ArrayList<AccessKey> models, Activity activity) {
        this.models = models;
        this.mActivity = activity;
    }

    public AccessKeyAdapter(Activity activity, int layout) {
        this.models = new ArrayList<>();
        this.mActivity = activity;
        this.layout = layout;
    }

    public void setModels(List<AccessKey> models) {
        ArrayList<AccessKey> tracks = new ArrayList<>(models.size());
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
    public int getItemViewType(int position) {
        if (this.layout == R.layout.list_row_item_access_key) {
            return 0;
        } else if (this.layout == R.layout.list_row_item_access_key_user) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(layout, viewGroup, false);
        v.setOnClickListener(this);
        v.setOnLongClickListener(this);

        if (viewType == 0) {
            return new DetailViewHolder(v);
        } else if (viewType == 1) {
            return new UserViewHolder(v);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        runEnterAnimation(viewHolder.itemView, i);
        final AccessKey model = models.get(i);
        viewHolder.itemView.setTag(model);
        if (viewHolder.getItemViewType() == 0) {
            DetailViewHolder detailViewHolder = (DetailViewHolder) viewHolder;
            bindDetailViewHolder(detailViewHolder, model);
        } else if (viewHolder.getItemViewType() == 1) {
            UserViewHolder userViewHolder = (UserViewHolder) viewHolder;
            bindUserViewHolder(userViewHolder, model, i);
        }
    }

    /**
     * @param viewHolder
     * @param model
     */
    private void bindDetailViewHolder(DetailViewHolder viewHolder, final AccessKey model) {
        int width = (int) (Utils.getScreenWidth(mActivity) * .45);
        CardView.LayoutParams lp = new CardView.LayoutParams(width, CardView.LayoutParams.WRAP_CONTENT);
        int px = 12;
        lp.setMargins(px, px, px, px);
        viewHolder.cardAccessKey.setLayoutParams(lp);

        viewHolder.tvAccessKeyType.setText(model.getType());

        if (model.getName() != null) {
            viewHolder.tvAccessKeyUserName.setText(model.getName());
        } else if (!model.isRegistered()) {
            viewHolder.tvAccessKeyUserName.setText("Unprovisioned Access Key");
        } else {
            viewHolder.tvAccessKeyUserName.setText("Unknown");
        }

        if (!model.isEnabled()) {
            viewHolder.cardAccessKey.setCardBackgroundColor(mActivity.getResources().getColor(R.color.background));
        } else {
            viewHolder.cardAccessKey.setCardBackgroundColor(mActivity.getResources().getColor(R.color.white));
        }

        Picasso.with(mActivity).load(model.getImageUrl())
                .placeholder(R.drawable.error_image)
                .transform(new CircleTransform())
                .into(viewHolder.ivAccessKeyImage);
    }

    /**
     * @param viewHolder
     * @param model
     */
    @SuppressLint("NewApi")
    private void bindUserViewHolder(UserViewHolder viewHolder, final AccessKey model, int i) {
        if (model.getName() != null) {
            viewHolder.tvAccessKeyUserName.setText(model.getName());
        } else if (!model.isRegistered()) {
            viewHolder.tvAccessKeyUserName.setText("New User");
        } else {
            viewHolder.tvAccessKeyUserName.setText("Unknown");
        }

        if (model.isSelected()) {
            viewHolder.tvAccessKeyUserName.setTextColor(Color.WHITE);
        } else {
            viewHolder.tvAccessKeyUserName.setTextColor(Color.BLACK);
        }

        Picasso.with(mActivity).load(model.getImageUrl())
                .error(R.drawable.error_image)
                .placeholder(R.drawable.error_image)
                .transform(new CircleTransform())
                .into(viewHolder.ivAccessKeyUserIcon);
    }

    @Override
    public int getItemCount() {
        return models == null ? 0 : models.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_access_key_user_icon) ImageView ivAccessKeyUserIcon;
        @BindView(R.id.tv_access_key_user_name) TextView tvAccessKeyUserName;

        public UserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public static class DetailViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.card_access_key) CardView cardAccessKey;
        @BindView(R.id.layout_access_key) RelativeLayout layoutAccessKey;
        @BindView(R.id.iv_access_key_image) ImageView ivAccessKeyImage;
        @BindView(R.id.tv_access_key_user_name) TextView tvAccessKeyUserName;
        @BindView(R.id.tv_access_key_type) TextView tvAccessKeyType;

        public DetailViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public void add(AccessKey item, int position) {
        models.add(position, item);
        notifyItemInserted(0);
    }

    public void remove(AccessKey item) {
        int position = -1;
        for (int i = 0; i < models.size(); i++) {
            if (item.getKey().equals(models.get(i).getKey())) {
                position = i;
            }
        }

        models.remove(position);
        notifyItemRemoved(position);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener<AccessKey> listener) {
        this.itemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnRecyclerViewItemLongClickListener<AccessKey> listener) {
        this.itemLongClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (itemClickListener != null) {
            AccessKey model = (AccessKey) v.getTag();
            itemClickListener.onItemClick(v, model);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (itemLongClickListener != null) {
            AccessKey model = (AccessKey) v.getTag();
            itemLongClickListener.onItemLongClick(v, model);
            return true;
        } else {
            return false;
        }
    }
}
