package com.limtide.ugclite.ui.adapter;

import java.util.List;

final class AppendOnlyListUpdate {
    private AppendOnlyListUpdate() {
    }

    static int appendStart(List<?> current, List<?> incoming) {
        if (incoming == null || incoming.size() < current.size()) {
            return -1;
        }
        for (int index = 0; index < current.size(); index++) {
            if (current.get(index) != incoming.get(index)) {
                return -1;
            }
        }
        return current.size();
    }
}
