package com.mgenio.jarvisofficedoor.interfaces;

/**
 * Created by anelson on 1/7/16.
 */
import android.view.View;

public interface OnRecyclerViewItemLongClickListener<T> {
    public void onItemLongClick(View view, T model);
}
