package com.zunotech.app.interfaces;

import java.util.ArrayList;

import com.zunotech.app.item.ItemData;

public interface LiveListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemData> arrayListData);
}