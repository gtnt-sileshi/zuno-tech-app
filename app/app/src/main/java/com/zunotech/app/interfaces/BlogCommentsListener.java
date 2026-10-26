package com.zunotech.app.interfaces;

import com.zunotech.app.item.ItemBlogComment;
import java.util.ArrayList;

public interface BlogCommentsListener {
    void onStart();
    void onEnd(String success, String message, ArrayList<ItemBlogComment> comments);
}
