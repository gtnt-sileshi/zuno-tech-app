package com.zunotech.app.interfaces;

import java.util.ArrayList;

import com.zunotech.app.item.ItemRadio;

public interface RadioListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemRadio> arrayListData);
}
