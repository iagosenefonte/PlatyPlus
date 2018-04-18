package com.labiapari.platyplus;

/**
 * Created by iago on 18/04/18.
 */

public interface Filter<T> {
    boolean filtrate(T item);
}
