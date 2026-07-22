package com.limtide.ugclite.data.repository;

import com.limtide.ugclite.data.model.Post;

import java.util.ArrayList;
import java.util.List;

final class FeedPostFilter {
    private FeedPostFilter() {
    }

    static List<Post> visiblePosts(List<Post> posts) {
        List<Post> filtered = new ArrayList<>();
        if (posts == null || posts.isEmpty()) {
            return filtered;
        }

        for (Post post : posts) {
            if (isVisible(post)) {
                filtered.add(post);
            }
        }
        return filtered;
    }

    private static boolean isVisible(Post post) {
        if (post == null || post.clips == null || post.clips.isEmpty()) {
            return false;
        }
        for (Post.Clip clip : post.clips) {
            if (clip != null && (clip.type == 0 || clip.type == 1)) {
                return true;
            }
        }
        return false;
    }
}
