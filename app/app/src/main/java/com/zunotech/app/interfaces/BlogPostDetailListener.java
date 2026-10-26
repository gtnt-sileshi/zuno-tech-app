package com.zunotech.app.interfaces;

import com.zunotech.app.item.ItemBlogPost;

public interface BlogPostDetailListener {
    void onStart();
    void onEnd(String success, String message, ItemBlogPost post);
}
