package com.limtide.ugclite.ui.viewmodel;

import com.limtide.ugclite.data.model.Post;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FeedListMergerTest {
    @Test
    public void emptyRefreshClearsExistingPosts() {
        List<Post> existing = Arrays.asList(new Post(), new Post());
        List<Post> result = FeedListMerger.merge(existing, Collections.emptyList(), true);
        assertTrue(result.isEmpty());
    }

    @Test
    public void nullRefreshIsNormalizedToEmptyList() {
        List<Post> result = FeedListMerger.merge(
                Collections.singletonList(new Post()),
                null,
                true);
        assertTrue(result.isEmpty());
    }

    @Test
    public void loadMoreAppendsWithoutMutatingExistingList() {
        List<Post> existing = Collections.singletonList(new Post());
        List<Post> incoming = Arrays.asList(new Post(), new Post());
        List<Post> result = FeedListMerger.merge(existing, incoming, false);
        assertEquals(3, result.size());
        assertEquals(1, existing.size());
    }
}
