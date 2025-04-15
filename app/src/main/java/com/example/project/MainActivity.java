package com.example.project;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.*;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;

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
                                String email = signInAccount.getEmail();

                                SQLiteDatabase db = null;
                                Cursor cursor = null;
                                try {
                                    db = DatabaseHelper.getInstance(this).openDatabase();

                                    String query = "SELECT id FROM tbl_user WHERE email = ?";
                                    cursor = db.rawQuery(query, new String[]{email});

                                    if (cursor == null || !cursor.moveToFirst()) {
                                        String insertQuery = "INSERT INTO tbl_user (email, isGoogle, premium) VALUES (?, ?, ?)";
                                        db.execSQL(insertQuery, new Object[]{email, 1, 0});
                                        Log.d("DB", "Inserted new user: " + email);
                                    } else {
                                        Log.d("DB", "User already exists: " + email);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(MainActivity.this, "Lỗi truy vấn người dùng", Toast.LENGTH_SHORT).show();
                                } finally {
                                    if (cursor != null) cursor.close();
                                    if (db != null) db.close();
                                }

                                // Sau khi xử lý DB -> chuyển sang HomeActivity
                                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                finish();
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

        // Nếu đã đăng nhập -> chuyển thẳng đến HomeActivity
        if (auth.getCurrentUser() != null) {
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

        // Đăng nhập Email (nếu có)
        Button btnEmail = findViewById(R.id.btnLoginEmail);
        btnEmail.setOnClickListener(view ->
                Toast.makeText(this, "Email login chưa được xử lý", Toast.LENGTH_SHORT).show()
        );
    }
}
