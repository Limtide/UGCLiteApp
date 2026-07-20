package com.limtide.ugclite.data.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.junit.Test;

public class FeedResponseTest {

    private final Gson gson = new Gson();

    @Test
    public void readmeResponse_isRecognizedAsSuccess() {
        FeedResponse response = gson.fromJson(
                "{\"code\":200,\"message\":\"success\",\"data\":[]}",
                FeedResponse.class);

        assertTrue(response.hasRecognizedStatus());
        assertTrue(response.isSuccess());
        assertNotNull(response.getPosts());
    }

    @Test
    public void legacyResponse_isStillSupported() {
        FeedResponse response = gson.fromJson(
                "{\"status_code\":0,\"has_more\":1,\"post_list\":[]}",
                FeedResponse.class);

        assertTrue(response.hasRecognizedStatus());
        assertTrue(response.isSuccess());
        assertTrue(response.hasMoreData());
    }

    @Test
    public void unknownObject_isNotAccidentallySuccessful() {
        FeedResponse response = gson.fromJson("{\"unexpected\":true}", FeedResponse.class);

        assertFalse(response.hasRecognizedStatus());
        assertFalse(response.isSuccess());
    }
}
