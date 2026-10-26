package com.zumipro.app.interfaces;

import com.zumipro.app.item.ItemBlogComment;
import java.util.ArrayList;

public interface BlogCommentsListener {
    void onStart();
    void onEnd(String success, String message, ArrayList<ItemBlogComment> comments);
}
