package com.zumipro.app.interfaces;

import java.util.ArrayList;

import com.zumipro.app.item.ItemRadio;

public interface RadioListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemRadio> arrayListData);
}
