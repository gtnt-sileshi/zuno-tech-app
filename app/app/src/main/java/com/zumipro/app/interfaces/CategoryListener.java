package com.zumipro.app.interfaces;

import java.util.ArrayList;

import com.zumipro.app.item.ItemCat;

public interface CategoryListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemCat> arrayListCat);
}