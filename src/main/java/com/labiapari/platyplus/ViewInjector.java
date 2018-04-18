package com.labiapari.platyplus;

import android.view.View;

/**
 * Created by iago on 06/04/18.
 */

public class ViewInjector implements IViewInjector {

    protected SmartViewHolder mViewHolder;

    public ViewInjector(SmartViewHolder viewHolder){
        mViewHolder = viewHolder;
    }

    public <T extends View> T findViewById(int id){
        return (T) mViewHolder.findViewById(id);
    }

    public <T extends View> T getRootView(){
        return (T) mViewHolder.itemView;
    }
}
