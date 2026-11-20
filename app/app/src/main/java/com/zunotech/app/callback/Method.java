package com.zunotech.app.callback;

public class Method {

    private Method() {
        throw new IllegalStateException("Utility class");
    }

    // ── App details ────────────────────────────────────────────────
    public static final String METHOD_APP_DETAILS = "app_details";

    // ── Blog — core feature ────────────────────────────────────────
    public static final String METHOD_BLOG_POSTS        = "get_blog_posts";
    public static final String METHOD_BLOG_POST         = "get_blog_post";
    public static final String METHOD_BLOG_BY_CAT       = "get_blog_by_cat";
    public static final String METHOD_BLOG_FEATURED     = "get_blog_featured";
    public static final String METHOD_BLOG_TRENDING     = "get_blog_trending";
    public static final String METHOD_BLOG_SEARCH       = "get_blog_search";
    public static final String METHOD_BLOG_TAGS         = "get_blog_tags";
    public static final String METHOD_BLOG_BY_TAG       = "get_blog_by_tag";
    public static final String METHOD_BLOG_RECENT       = "get_recent_posts";
    public static final String METHOD_BLOG_COMMENTS     = "get_blog_comments";
    public static final String METHOD_BLOG_COMMENT_POST = "post_blog_comment";
    public static final String METHOD_SAVE_POST         = "save_post";
    public static final String METHOD_SAVED_POSTS       = "get_saved_posts";

    // ── Home & navigation ─────────────────────────────────────────
    public static final String METHOD_HOME         = "get_home";
    public static final String METHOD_HOME_DETAILS = "home_collections";
    public static final String METHOD_CAT          = "cat_list";

    // category_type filter values passed as search_type in METHOD_CAT
    public static final String CAT_TYPE_ALL   = "all";
    public static final String CAT_TYPE_BLOG  = "blog";

    // ── Reports, suggestions (device-based) ───────────────────────
    public static final String METHOD_REPORT      = "post_report";
    public static final String METHOD_SUGGESTION  = "post_suggest";

}
