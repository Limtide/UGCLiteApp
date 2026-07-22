package com.limtide.ugclite.utils;

import android.app.Activity;
import android.content.Intent;

import com.limtide.ugclite.ui.activity.LoginActivity;

public final class AuthenticationGate {
    private AuthenticationGate() {
    }

    public static boolean requireAuthenticated(Activity activity) {
        if (AuthenticatedSession.isAuthenticated()) {
            return true;
        }

        AuthenticatedSession.clear();
        PreferenceManager.getInstance(activity).clearLoginState();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
        return false;
    }
}
