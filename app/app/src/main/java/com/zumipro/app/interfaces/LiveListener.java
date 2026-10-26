package com.zumipro.app.interfaces;

import java.util.ArrayList;

import com.zumipro.app.item.ItemData;

public interface LiveListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemData> arrayListData);
}