package com.labiapari.platyplus;

import android.app.Activity;

import java.lang.reflect.Field;

/**
 * Created by iago on 14/04/18.
 */

public class Binder {

    public static void bind(Activity activity){
        Field[] fields = activity.getClass().getDeclaredFields();
        for (Field field:
             fields) {
            if(field.isAnnotationPresent(Bind.class)){
                try {
                    int id = field.getAnnotation(Bind.class).value();
                    if(id==-1){
                        id = activity.getResources().getIdentifier(field.getName(), "id", activity.getPackageName());
                    }
                    field.setAccessible(true);
                    field.set(activity, field.getType().cast(activity.findViewById(id)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
