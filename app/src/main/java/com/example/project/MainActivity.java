package com.example.project;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

//import com.example.project.network.SecureHttpClient;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.*;
import com.google.android.gms.tasks.Task;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;

    LoginSessionManager loginSessionManager;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                        AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                        System.out.println("MainActivity ID Token: " + signInAccount.getIdToken());

                        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String idToken = signInAccount.getIdToken();

                                //OkHttpClient client = SecureHttpClient.getSecureClient(MainActivity.this);
                                OkHttpClient client = new OkHttpClient();
                                JSONObject json = new JSONObject();
                                try {
                                    json.put("idToken", idToken);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                RequestBody body = RequestBody.create(
                                        json.toString(),
                                        MediaType.parse("application/json")
                                );

                                Request request = new Request.Builder()
                                        .url("https://ticktickserver.onrender.com/api/auth/google")
                                        .post(body)
                                        .build();

                                client.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Không kết nối được server", Toast.LENGTH_SHORT).show());
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        if (!response.isSuccessful()) {
                                            String errorBody = response.body().string();
                                            Log.e("TOKEN_VERIFY_ERROR", "Response code: " + response.code() + " | Body: " + errorBody);
                                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Xác minh token thất bại: " + response.code(), Toast.LENGTH_SHORT).show());
                                            return;
                                        }

                                        String resBody = response.body().string();
                                        try {
                                            JSONObject resJson = new JSONObject(resBody);
                                            String email = resJson.getString("email");
                                            boolean isGoogle = resJson.getBoolean("isGoogle");
                                            boolean isPremium = resJson.getBoolean("isPremium");

                                            // Cập nhật vào DB local
                                            SQLiteDatabase db = null;
                                            Cursor cursor = null;
                                            try {
                                                db = DatabaseHelper.getInstance(MainActivity.this).openDatabase();

                                                String query = "SELECT id FROM tbl_user WHERE email = ?";
                                                cursor = db.rawQuery(query, new String[]{email});

                                                int userId = -1;

                                                if (cursor == null || !cursor.moveToFirst()) {
                                                    String insertQuery = "INSERT INTO tbl_user (email, isGoogle, is_premium) VALUES (?, ?, ?)";
                                                    SQLiteStatement stmt = db.compileStatement(insertQuery);
                                                    stmt.bindString(1, email);
                                                    stmt.bindLong(2, isGoogle ? 1 : 0);
                                                    stmt.bindLong(3, isPremium ? 1 : 0);
                                                    stmt.executeInsert();

                                                    // Lấy lại userId sau khi insert
                                                    cursor = db.rawQuery(query, new String[]{email});
                                                    if (cursor.moveToFirst()) {
                                                        userId = cursor.getInt(0);
                                                    }
                                                } else {
                                                    userId = cursor.getInt(0);
                                                    String updateQuery = "UPDATE tbl_user SET isGoogle = ?, is_premium = ? WHERE email = ?";
                                                    SQLiteStatement stmt = db.compileStatement(updateQuery);
                                                    stmt.bindLong(1, isGoogle ? 1 : 0);
                                                    stmt.bindLong(2, isPremium ? 1 : 0);
                                                    stmt.bindString(3, email);
                                                    stmt.executeUpdateDelete();
                                                }

                                                if (userId != -1) {
                                                    // Gọi LoginSessionManager để tạo session
                                                    LoginSessionManager.getInstance(MainActivity.this).createSession(userId);
                                                }

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            } finally {
                                                if (cursor != null) cursor.close();
                                                if (db != null) db.close();
                                            }
                                            runOnUiThread(() -> {
                                                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                                finish();
                                            });

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(MainActivity.this, "Đăng nhập thất bại: " + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (ApiException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Google Sign-In lỗi", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // layout chứa nút Google, Email

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();

        loginSessionManager = LoginSessionManager.getInstance(this);

        // Nếu đã đăng nhập -> chuyển thẳng đến HomeActivity
        if (auth.getCurrentUser() != null) {
            Log.d("MainActivity", "User đã đăng nhập: " + auth.getCurrentUser().getEmail());
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }

        // Cấu hình đăng nhập Google
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);

        // Đăng nhập Google
        Button btnGoogle = findViewById(R.id.btnLoginGoogle);
        btnGoogle.setOnClickListener(view -> {
            Intent intent = googleSignInClient.getSignInIntent();
            activityResultLauncher.launch(intent);
        });

        /*
        Button btnEmail = findViewById(R.id.btnLoginEmail);
        btnEmail.setOnClickListener(view ->
                Toast.makeText(this, "Email login chưa được xử lý", Toast.LENGTH_SHORT).show()
        );*/
    }
}
