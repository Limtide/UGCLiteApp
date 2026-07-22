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
            if (normalizeVisibleClips(post)) {
                filtered.add(post);
            }
        }
        return filtered;
    }

    private static boolean normalizeVisibleClips(Post post) {
        if (post == null || post.clips == null || post.clips.isEmpty()) {
            return false;
        }
        List<Post.Clip> visibleClips = new ArrayList<>();
        for (Post.Clip clip : post.clips) {
            if (clip != null && (clip.type == 0 || clip.type == 1)) {
                visibleClips.add(clip);
            }
        }
        post.clips = visibleClips;
        return !visibleClips.isEmpty();
    }
}
