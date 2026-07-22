package com.limtide.ugclite.ui.viewmodel;

import com.limtide.ugclite.data.model.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class FeedListMerger {
    private FeedListMerger() {
    }

    static List<Post> merge(List<Post> current, List<Post> incoming, boolean refresh) {
        List<Post> safeIncoming = incoming == null ? Collections.emptyList() : incoming;
        if (refresh) {
            return new ArrayList<>(safeIncoming);
        }

        List<Post> merged = current == null
                ? new ArrayList<>()
                : new ArrayList<>(current);
        merged.addAll(safeIncoming);
        return merged;
    }
}
