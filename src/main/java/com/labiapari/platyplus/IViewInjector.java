package com.labiapari.platyplus;

import android.view.View;

/**
 * Created by iago on 06/04/18.
 */

interface IViewInjector {

    <T extends View> T findViewById(int id);
    <T extends View> T getRootView();
}
