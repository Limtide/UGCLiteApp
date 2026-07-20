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

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityRecreationTest {

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
}
