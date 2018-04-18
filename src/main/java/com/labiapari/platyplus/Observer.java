package com.labiapari.platyplus;

import android.os.Handler;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by iago on 13/04/18.
 */

public class Observer {

    private Map<Observable, Map<Map, Object>> mMap;

    private Handler mHandler = new Handler();

    public Observer(){

        mMap = new HashMap<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (true) {
                    try {
                        Thread.sleep(10);
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                compare();
                            }
                        });
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        }).start();
    }

    private void compare() {

        Set<Observable> observableKeys = mMap.keySet();
        for (Observable observableKey:
             observableKeys) {
            Set<Map> keys = mMap.get(observableKey).keySet();
            for (Map<Object, Field>key:
                    keys) {
                Set<Object> innerKey = key.keySet();
                for (Object object:
                        innerKey) {
                    try {
                        if(!mMap.get(observableKey).get(key).equals(key.get(object).get(object))){
                            observableKey.observe();
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public void add(Object object, Field field, Observable observable){
        try {
            Map<Object, Field> innerMap = new HashMap<>();
            Map<Map, Object> externalMap = new HashMap<>();;
            innerMap.put(object, field);
            externalMap.put(innerMap, field.get(object));
            mMap.put(observable, externalMap);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public interface Observable{
        void observe();
    }

}
