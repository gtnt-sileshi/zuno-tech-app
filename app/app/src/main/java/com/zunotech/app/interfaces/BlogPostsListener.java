package com.zunotech.app.interfaces;

import com.zunotech.app.item.ItemBlogPost;
import java.util.ArrayList;

public interface BlogPostsListener {
    void onStart();
    void onEnd(String success, String message, ArrayList<ItemBlogPost> posts);
}
