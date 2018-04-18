package com.labiapari.platyplus;

/**
 * Created by iago on 06/04/18.
 */

public interface SmartInjector<T> {
    void onInject(T data, IViewInjector injector);
}
