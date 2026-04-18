package com.example.pontosale.utils;

import android.content.Context;
import android.view.View;

public class Loading {

    public static void showLoading(android.app.Activity activity, View loadingOverlay) {
        Activity.runOnUiThread(activity, () -> {
            loadingOverlay.setVisibility(View.VISIBLE);
        });
    }

    public static void hideLoading(android.app.Activity activity, View loadingOverlay) {
        Activity.runOnUiThread(activity, () -> {
            loadingOverlay.setVisibility(View.GONE);
        });
    }


}
