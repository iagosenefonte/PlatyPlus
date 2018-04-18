package com.labiapari.platyplus;

/**
 * Created by iago on 06/04/18.
 */
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class SmartViewHolder<D> extends RecyclerView.ViewHolder {

    protected SparseArray<View> mViewMap;

    public IViewInjector injector;

    private AbstractSmartAdapter.OnItemClickListener mOnItemClickListener;

    public SmartViewHolder(ViewGroup parent, int resource) {
        this(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
    }

    public SmartViewHolder(View itemView) {
        super(itemView);
        mViewMap = new SparseArray<>();
    }

    final void bind(D data) {
        if (injector == null) {
            injector = new ViewInjector(this);
        }
        onBind(data, injector);
    }

    protected abstract void onBind(D data, IViewInjector injector);


    public final <T extends View> T findViewById(int id) {
        View view = mViewMap.get(id);
        if (view == null) {
            view = itemView.findViewById(id);
            mViewMap.put(id, view);
        }
        return (T) view;
    }
}
