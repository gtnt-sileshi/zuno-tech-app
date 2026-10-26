package com.zunotech.app.interfaces;

import java.util.ArrayList;

import com.zunotech.app.item.ItemCat;

public interface CategoryListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemCat> arrayListCat);
}