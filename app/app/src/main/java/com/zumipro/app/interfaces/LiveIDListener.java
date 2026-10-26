package com.zumipro.app.interfaces;

import java.util.ArrayList;

import com.zumipro.app.item.ItemData;
import com.zumipro.app.item.ItemLiveTv;

public interface LiveIDListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemLiveTv> arrayListLive, ArrayList<ItemData> arrayListRelated);
}
