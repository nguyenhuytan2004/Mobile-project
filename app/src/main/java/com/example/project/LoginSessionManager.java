package com.example.project;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class LoginSessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";

    private static LoginSessionManager instance;
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    private LoginSessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized LoginSessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new LoginSessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public void createSession(int userId) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout(Context context) {
        editor.clear();
        editor.commit();

        // Đăng xuất khỏi FirebaseAuth nếu đang đăng nhập
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            auth.signOut();
        }

        // Đăng xuất khỏi Google SignIn nếu cần
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, options);
        googleSignInClient.signOut(); // async nhưng không cần chờ ở đây
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }
}
