package com.labiapari.platyplus;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by iago on 13/04/18.
 */

public class Form<T> implements Observer.Observable{

    private T mObject;

    private HashMap<View, Field> mMap;

    private Observer mObserver;

    public Form(T object) {
        mObject = object;
        mMap = new HashMap();
        mObserver = new Observer();
    }

    public void link(EditText editText, final String fieldName) {
        Field field = getField(fieldName);
        setText(editText, field);
        mMap.put(editText, field);
        mObserver.add(mObject, field, this);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                fillField(s.toString(), fieldName);
            }
        });
    }

    private void setText(TextView view, Field field) {
        try {
            view.setText((CharSequence) field.get(mObject));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void fillField(String value, String fieldName) {
        try {
            Field field = getField(fieldName);
            if (field!=null && field.getType() == String.class) {
                field.setAccessible(true);
                field.set(mObject, value);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    private final Field getField(String fieldName) {
        try {
            Field field = mObject.getClass().getDeclaredField(fieldName);
            if (field != null) {
                return field;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void observe() {
        Set<View> keys = mMap.keySet();
        for (View key:
             keys) {
            if(key instanceof TextView)
                setText((TextView) key, mMap.get(key));
        }
    }
}
