package com.limtide.ugclite.ui.activity;

import static org.junit.Assert.assertNotEquals;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.limtide.ugclite.utils.AuthenticatedSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProtectedActivityAuthenticationTest {
    @Before
    public void clearSession() {
        AuthenticatedSession.clear();
    }

    @After
    public void clearSessionAfterTest() {
        AuthenticatedSession.clear();
    }

    @Test
    public void mainActivityCannotRemainResumedWithoutSession() {
        assertRedirected(MainActivity.class);
    }

    @Test
    public void postDetailActivityCannotRemainResumedWithoutSession() {
        assertRedirected(PostDetailActivity.class);
    }

    @Test
    public void hashtagActivityCannotRemainResumedWithoutSession() {
        assertRedirected(HashtagActivity.class);
    }

    private <T extends AppCompatActivity> void assertRedirected(Class<T> activityClass) {
        try (ActivityScenario<T> scenario = ActivityScenario.launch(activityClass)) {
            assertNotEquals(Lifecycle.State.RESUMED, scenario.getState());
        }
    }
}
