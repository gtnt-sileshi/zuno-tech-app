package com.zunotech.app.interfaces;

import java.util.ArrayList;

import com.zunotech.app.item.ItemData;
import com.zunotech.app.item.ItemLiveTv;

public interface LiveIDListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemLiveTv> arrayListLive, ArrayList<ItemData> arrayListRelated);
}
