package com.limtide.ugclite.data.repository;

import com.limtide.ugclite.data.model.Post;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;

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
    @Test
    public void mixedClipListsAreNormalizedBeforePublication() {
        Post.Clip valid = new Post.Clip();
        valid.type = 1;
        Post.Clip unsupported = new Post.Clip();
        unsupported.type = 9;

        Post nullFirst = new Post();
        nullFirst.clips = Arrays.asList(null, unsupported, valid);
        Post nullLast = new Post();
        nullLast.clips = Arrays.asList(valid, unsupported, null);

        assertNormalizedToOnlyValidClip(nullFirst, valid);
        assertNormalizedToOnlyValidClip(nullLast, valid);
    }

    private static void assertNormalizedToOnlyValidClip(Post post, Post.Clip valid) {
        java.util.List<Post> visible = FeedPostFilter.visiblePosts(
                Collections.singletonList(post));

        assertEquals(1, visible.size());
        assertEquals(1, visible.get(0).clips.size());
        assertSame(valid, visible.get(0).clips.get(0));
    }


}
