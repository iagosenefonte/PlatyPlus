package com.labiapari.platyplus;

import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by iago on 06/04/18.
 */

public class SmartAdapter extends AbstractSmartAdapter {

    private Map<Type, IViewHolderCreator> creators = new HashMap<>();

    private List<Type> dataTypes = new ArrayList<>();

    private IViewHolderCreator defaultCreator = null;

    private OnSelectionChangeListener mSelectionChangeListener = null;

    private SelectionRecorder mRecorder;

    private List<?> mCurrentData;

    private HashMap<Object, Filter> mFilterMap = new HashMap<>();

    private HashMap<Object, Comparator> mSortingMap = new HashMap<>();

    protected SmartAdapter() {

    }

    public static SmartAdapter create() {
        return new SmartAdapter();
    }

    public static <T extends SmartAdapter> T create(Class<T> c) {
        try {
            return c.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> SmartAdapter register(final int layoutRes, final SmartInjector<T> smartInjector) {
        Type type = getSlimInjectorActualTypeArguments(smartInjector);
        if (type == null) {
            throw new IllegalArgumentException();
        }

        creators.put(type, new IViewHolderCreator<T>() {
            @Override
            public SmartTypeViewHolder<T> create(ViewGroup parent) {
                return new SmartTypeViewHolder<T>(parent, layoutRes) {
                    @Override
                    protected void onBind(T data, IViewInjector injector) {
                        smartInjector.onInject(data, injector);
                        if(mRecorder!=null && mRecorder.has(data) && mSelectionChangeListener!=null)
                            mSelectionChangeListener.onItemSelected(data, injector);
                        else
                            mSelectionChangeListener.onItemUnselected(data, injector);

                    }
                };
            }
        });

        return this;
    }

    public SmartAdapter updateData(List<?> data) {
        mData = data;
        mCurrentData = data;
        return this;
    }

    private <T> Type getSlimInjectorActualTypeArguments(SmartInjector<T> slimInjector) {
        Type[] interfaces = slimInjector.getClass().getGenericInterfaces();
        for (Type type : interfaces) {
            if (type instanceof ParameterizedType) {
                if (((ParameterizedType) type).getRawType().equals(SmartInjector.class)) {
                    Type actualType = ((ParameterizedType) type).getActualTypeArguments()[0];
                    if (actualType instanceof Class) {
                        return actualType;
                    } else {
                        throw new IllegalArgumentException("The generic type argument of SlimInjector is NOT support Generic Parameterized Type now, Please using a WRAPPER class install of it directly.");
                    }
                }
            }
        }
        return null;
    }

    private boolean isTypeMatch(Type type, Type targetType) {
        if (type instanceof Class && targetType instanceof Class) {
            if (((Class) type).isAssignableFrom((Class) targetType)) {
                return true;
            }
        } else if (type instanceof ParameterizedType && targetType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            ParameterizedType parameterizedTargetType = (ParameterizedType) targetType;
            if (isTypeMatch(parameterizedType.getRawType(), ((ParameterizedType) targetType).getRawType())) {
                Type[] types = parameterizedType.getActualTypeArguments();
                Type[] targetTypes = parameterizedTargetType.getActualTypeArguments();
                if (types == null || targetTypes == null || types.length != targetTypes.length) {
                    return false;
                }
                int len = types.length;
                for (int i = 0; i < len; i++) {
                    if (!isTypeMatch(types[i], targetTypes[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public SmartAdapter attachTo(RecyclerView... recyclerViews) {
        for (RecyclerView recyclerView : recyclerViews) {
            recyclerView.setAdapter(this);
        }
        return this;
    }

    public void add(List<?> data) {
        if (data == null)
            return;
        if (mData == null)
            mData = data;
        else
            mData.addAll(data);
        mCurrentData = mData;
    }

    public void remove(List<?> data) {
        if (data == null || mData == null)
            return;
        mData.removeAll(data);
        mCurrentData = mData;
    }

    public void remove(Object data) {
        if (data == null)
            return;
        if (mData == null)
            return;
        mData.remove(data);
        mCurrentData = mData;
    }

    @Override
    public SmartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Type dataType = dataTypes.get(viewType);
        IViewHolderCreator creator = creators.get(dataType);
        if (creator == null) {
            for (Type t : creators.keySet()) {
                if (isTypeMatch(t, dataType)) {
                    creator = creators.get(t);
                    break;
                }
            }
        }
        if (creator == null) {
            if (defaultCreator == null) {
                throw new IllegalArgumentException(String.format("Neither the TYPE: %s not The DEFAULT injector found...", dataType));
            }
            creator = defaultCreator;
        }

        SmartViewHolder holder = creator.create(parent);

        solveInteractions(holder);

        return holder;
    }

    public <T> SmartAdapter filter(boolean clearOthers, Filter<T> filter){
        mCurrentData = clearOthers ? mData : mCurrentData;
        Iterator iterator = mCurrentData.iterator();
        while(iterator.hasNext()){
            Object item = iterator.next();
            if(!filter.filtrate((T) item))
                iterator.remove();
        }
        return this;
    }

    public <T> SmartAdapter filter(boolean clearOthers, Object key){
        mCurrentData = clearOthers ? mData : mCurrentData;
        Iterator iterator = mCurrentData.iterator();
        while(iterator.hasNext()){
            Object item = iterator.next();
            if(mFilterMap.containsKey(key) && !mFilterMap.get(key).filtrate((T) item))
                iterator.remove();
        }
        return this;
    }

    public <T> SmartAdapter addFilter(Object key, Filter<T> filter){
        mFilterMap.put(key, filter);
        return this;
    }

    public <T> SmartAdapter addSorting(Object key, Comparator<T> comparator){
        mSortingMap.put(key, comparator);
        return this;
    }

    public SmartAdapter sort(boolean clearOthers, Object key){
        mCurrentData = clearOthers ? mData : mCurrentData;
        if(mSortingMap.containsKey(key))
            Collections.sort(mCurrentData, mSortingMap.get(key));
        return this;
    }

    public SmartAdapter sort(boolean clearOthers, final Comparator comparator){
        mCurrentData = clearOthers ? mData : mCurrentData;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mCurrentData.sort(comparator);
        }else{
            Collections.sort(mCurrentData, comparator);
        }
        return this;
    }

    private void solveInteractions(final SmartViewHolder holder) {
        if (mRecorder != null) {

            final View view = mRecorder.anchorViewId != 0 ? holder.itemView.findViewById(mRecorder.anchorViewId) : holder.itemView;

            if(!view.equals(holder.itemView)){
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mOnItemClickListener!=null && holder.getAdapterPosition()>=0)
                            mOnItemClickListener.onClick(mCurrentData.get(holder.getAdapterPosition()), holder.injector);
                    }
                });
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRecorder.isSelectionOpen()) {
                        mRecorder.select(mCurrentData.get(holder.getAdapterPosition()), mSelectionChangeListener);
                        notifyDataSetChanged();
                    }else
                    if (mOnItemClickListener != null)
                        mOnItemClickListener.onClick(mCurrentData.get(holder.getAdapterPosition()), holder.injector);
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mRecorder.select(mCurrentData.get(holder.getAdapterPosition()), mSelectionChangeListener);
                    notifyDataSetChanged();
                    return true;
                }
            });
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnItemClickListener!=null)
                        mOnItemClickListener.onClick(mCurrentData.get(holder.getAdapterPosition()), holder.injector);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mCurrentData == null ? 0 : mCurrentData.size();
    }

    @Override
    protected Object getItem(int position) {
        return mCurrentData == null || mCurrentData.size() <= position ? null : mCurrentData.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        Object item = mCurrentData.get(position);
        int index = dataTypes.indexOf(item.getClass());
        if (index == -1) {
            dataTypes.add(item.getClass());
        }
        index = dataTypes.indexOf(item.getClass());
        return index;
    }

    public SmartAdapter addOnLastItemReachedListener(OnLastItemReachedListener listener) {
        mOnLastItemReachedListener = listener;
        return this;
    }

    public SmartAdapter setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
        return this;
    }

    public SmartAdapter addOnItemSelectedListener(@NonNull OnSelectionChangeListener listener, @Nullable SelectionRecorder recorder, @IdRes Integer id) {

        if (recorder == null)
            recorder = SelectionRecorder.getInstance();

        mRecorder = recorder;

        if (id != null)
            recorder.anchorViewId = id;

        notifyDataSetChanged();

        mSelectionChangeListener = listener;
        return this;
    }

    public interface OnSelectionChangeListener<T> {

        void onCancelSelection();

        void onStartSelection();

        void onItemSelected(T data, IViewInjector injector);

        void onItemUnselected(T data, IViewInjector injector);
    }

    private interface IViewHolderCreator<T> {
        SmartAdapter.SmartTypeViewHolder<T> create(ViewGroup parent);
    }

    private static abstract class SmartTypeViewHolder<T> extends SmartViewHolder<T> {

        SmartTypeViewHolder(ViewGroup parent, int itemLayoutRes) {
            super(parent, itemLayoutRes);
        }

    }

}