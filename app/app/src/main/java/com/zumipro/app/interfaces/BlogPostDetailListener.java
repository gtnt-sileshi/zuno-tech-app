package com.zumipro.app.interfaces;

import com.zumipro.app.item.ItemBlogPost;

public interface BlogPostDetailListener {
    void onStart();
    void onEnd(String success, String message, ItemBlogPost post);
}
