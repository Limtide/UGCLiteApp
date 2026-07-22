package com.limtide.ugclite.ui.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.fragment.app.FragmentManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.limtide.ugclite.R;
import com.limtide.ugclite.ui.fragment.HomeFragment;
import com.limtide.ugclite.ui.fragment.ProfileFragment;
import com.limtide.ugclite.utils.AuthenticatedSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityRecreationTest {
    @Before
    public void establishSession() {
        AuthenticatedSession.establish("instrumented-user");
    }

    @After
    public void clearSession() {
        AuthenticatedSession.clear();
    }


    @Test
    public void recreate_reusesFragmentsAndKeepsProfileVisible() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                FragmentManager manager = activity.getSupportFragmentManager();
                manager.executePendingTransactions();
                HomeFragment home = (HomeFragment) manager.findFragmentByTag("home");
                ProfileFragment profile = (ProfileFragment) manager.findFragmentByTag("profile");

                activity.findViewById(R.id.profile_tab).performClick();
                manager.executePendingTransactions();

                assertNotNull(home);
                assertNotNull(profile);
                assertTrue(home.isHidden());
                assertFalse(profile.isHidden());
            });

            scenario.recreate();
            scenario.onActivity(activity -> {
                FragmentManager manager = activity.getSupportFragmentManager();
                manager.executePendingTransactions();
                HomeFragment home = (HomeFragment) manager.findFragmentByTag("home");
                ProfileFragment profile = (ProfileFragment) manager.findFragmentByTag("profile");

                assertEquals(2, manager.getFragments().size());
                assertNotNull(home);
                assertNotNull(profile);
                assertTrue(home.isHidden());
                assertFalse(profile.isHidden());
            });
        }
    }

    @Test
    public void recreateAfterPlaceholderTab_hidesPreviouslyVisibleProfileWhenHomeSelected() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                FragmentManager manager = activity.getSupportFragmentManager();
                manager.executePendingTransactions();

                activity.findViewById(R.id.profile_tab).performClick();
                manager.executePendingTransactions();
                activity.findViewById(R.id.friends_tab).performClick();
                manager.executePendingTransactions();

                ProfileFragment profile = (ProfileFragment) manager.findFragmentByTag("profile");
                assertNotNull(profile);
                assertFalse(profile.isHidden());
            });

            scenario.recreate();
            scenario.onActivity(activity -> {
                FragmentManager manager = activity.getSupportFragmentManager();
                manager.executePendingTransactions();
                activity.findViewById(R.id.home_tab).performClick();
                manager.executePendingTransactions();

                HomeFragment home = (HomeFragment) manager.findFragmentByTag("home");
                ProfileFragment profile = (ProfileFragment) manager.findFragmentByTag("profile");
                assertEquals(2, manager.getFragments().size());
                assertNotNull(home);
                assertNotNull(profile);
                assertFalse(home.isHidden());
                assertTrue(profile.isHidden());
            });
        }
    }
}
