package com.example.pontosale.utils;

public class Activity {

    public static void runOnUiThread(android.app.Activity activity, Runnable runnable) {
        if (activity != null) {
            activity.runOnUiThread(runnable);
        }
    }

}
