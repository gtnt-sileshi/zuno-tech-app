package com.zumipro.app.interfaces;

import java.util.ArrayList;

import com.zumipro.app.item.ItemPost;

public interface HomeListener {
    void onStart();
    void onEnd(String success, String message, ArrayList<ItemPost> arrayListPost);
}
