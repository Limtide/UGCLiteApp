package com.limtide.ugclite.data.repository;

import com.limtide.ugclite.data.model.Post;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FeedPostFilterTest {
    @Test
    public void nullListBecomesEmptySuccessfulPage() {
        assertTrue(FeedPostFilter.visiblePosts(null).isEmpty());
    }

    @Test
    public void nullPostsAndClipsAreSkipped() {
        Post noClips = new Post();
        noClips.clips = null;
        Post nullClip = new Post();
        nullClip.clips = Collections.singletonList(null);

        assertTrue(FeedPostFilter.visiblePosts(
                Arrays.asList(null, noClips, nullClip)).isEmpty());
    }

    @Test
    public void supportedClipKeepsPost() {
        Post post = new Post();
        Post.Clip clip = new Post.Clip();
        clip.type = 1;
        post.clips = Collections.singletonList(clip);

        assertEquals(1, FeedPostFilter.visiblePosts(
                Collections.singletonList(post)).size());
    }

    @Test
    public void unsupportedClipRemovesPost() {
        Post post = new Post();
        Post.Clip clip = new Post.Clip();
        clip.type = 9;
        post.clips = Collections.singletonList(clip);

        assertTrue(FeedPostFilter.visiblePosts(
                Collections.singletonList(post)).isEmpty());
    }
}
