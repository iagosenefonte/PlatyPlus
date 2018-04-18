package com.labiapari.platyplus;

import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Created by iago on 06/04/18.
 */

abstract class AbstractSmartAdapter extends RecyclerView.Adapter<SmartViewHolder> {

    protected List mData;

    protected OnLastItemReachedListener mOnLastItemReachedListener;

    protected OnItemClickListener mOnItemClickListener;

    @Override
    public final void onBindViewHolder(SmartViewHolder holder, int position) {
        holder.bind(getItem(position));
        if(mOnLastItemReachedListener!=null)
            if(position == mData.size()-1)
                mOnLastItemReachedListener.onLastReached();
    }

    protected abstract Object getItem(int position);


    public interface OnLastItemReachedListener {
        void onLastReached();
    }

    public interface OnItemClickListener<T>{
        void onClick(T data, IViewInjector injector);
    }

}
