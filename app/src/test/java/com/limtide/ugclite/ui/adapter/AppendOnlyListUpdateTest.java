package com.limtide.ugclite.ui.adapter;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AppendOnlyListUpdateTest {

    @Test
    public void sharedIdentityPrefixReturnsAppendStart() {
        Object first = new Object();
        Object second = new Object();
        Object third = new Object();
        List<Object> current = Arrays.asList(first, second);
        List<Object> incoming = Arrays.asList(first, second, third);

        assertEquals(2, AppendOnlyListUpdate.appendStart(current, incoming));
    }

    @Test
    public void unchangedIdentityListProducesNoAppendItems() {
        Object first = new Object();
        List<Object> current = Collections.singletonList(first);

        assertEquals(1, AppendOnlyListUpdate.appendStart(current, current));
    }

    @Test
    public void refreshedObjectsRequireReplacementEvenWhenValuesMatch() {
        List<String> current = Collections.singletonList(new String("post-1"));
        List<String> incoming = Collections.singletonList(new String("post-1"));

        assertEquals(-1, AppendOnlyListUpdate.appendStart(current, incoming));
    }

    @Test
    public void shorterIncomingListRequiresReplacement() {
        List<Object> current = Arrays.asList(new Object(), new Object());

        assertEquals(-1, AppendOnlyListUpdate.appendStart(
                current,
                Collections.singletonList(current.get(0))));
    }
}
