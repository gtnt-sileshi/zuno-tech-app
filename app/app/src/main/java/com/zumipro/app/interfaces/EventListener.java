package com.zumipro.app.interfaces;

import java.util.ArrayList;

import com.zumipro.app.item.ItemEvent;

public interface EventListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemEvent> arrayListData);
}