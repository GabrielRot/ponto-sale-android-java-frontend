package com.example.pontosale.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "app_prefs";
    private static final String KEY_TOKEN = "auth_token";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context contenxt) {
        prefs = contenxt.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Salvar token
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    // Pegar token
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    // Verificar se está logado
    public boolean isLoggedIn() {
        return getToken() != null;
    }

    // Remover token
    public void removeToken() {
        editor.remove(KEY_TOKEN);
        editor.apply();
    }

}
