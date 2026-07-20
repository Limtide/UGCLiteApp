package com.limtide.ugclite;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.limtide.ugclite.utils.PreferenceManager;
import com.limtide.ugclite.utils.AppStartupHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.limtide.ugclite", appContext.getPackageName());
    }

    @Test
    public void loginStatePersistsRequiredIdentityAndClearsOnLogout() {
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        PreferenceManager preferences = PreferenceManager.getInstance(testContext);
        preferences.clearAllPreferences();

        try {
            preferences.saveLoginState(
                    "demo",
                    "user_demo",
                    "session_demo",
                    "normal",
                    true,
                    true
            );

            assertEquals("demo", preferences.getCurrentUsername());
            assertEquals("user_demo", preferences.getCurrentUserId());
            assertTrue(preferences.isLoggedIn());
            assertTrue(preferences.isAutoLoginEnabled());

            preferences.clearLoginState();

            assertFalse(preferences.isLoggedIn());
            assertNull(preferences.getCurrentUsername());
            assertNull(preferences.getCurrentUserId());
            assertNull(preferences.getSessionToken());
        } finally {
            preferences.clearAllPreferences();
        }
    }

    @Test
    public void completingFirstLaunchDoesNotGrantLogin() {
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        PreferenceManager preferences = PreferenceManager.getInstance(testContext);
        preferences.clearAllPreferences();

        try {
            AppStartupHelper.StartupResult firstLaunch =
                    AppStartupHelper.checkStartupFlow(testContext);
            assertTrue(firstLaunch.needLogin);
            assertFalse(firstLaunch.canAutoLogin);
            assertTrue(preferences.isFirstLaunch());

            preferences.setFirstLaunchComplete();

            AppStartupHelper.StartupResult laterLaunch =
                    AppStartupHelper.checkStartupFlow(testContext);
            assertFalse(preferences.isFirstLaunch());
            assertTrue(laterLaunch.needLogin);
            assertFalse(laterLaunch.canAutoLogin);
        } finally {
            preferences.clearAllPreferences();
        }
    }


    @Test
    public void forgedLocalSessionCannotAutoLogin() {
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        PreferenceManager preferences = PreferenceManager.getInstance(testContext);
        preferences.clearAllPreferences();

        try {
            preferences.setFirstLaunchComplete();
            preferences.saveLoginState(
                    "demo",
                    "user_demo",
                    "token_predictable",
                    "normal",
                    true,
                    true);

            AppStartupHelper.StartupResult result = AppStartupHelper.checkStartupFlow(testContext);

            assertTrue(result.needLogin);
            assertFalse(result.canAutoLogin);
        } finally {
            preferences.clearAllPreferences();
        }
    }
}
