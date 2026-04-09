package com.example.pontosale.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pontosale.R;
import com.example.pontosale.session.SessionManager;
import com.example.pontosale.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    EditText email, senha;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);

        email = findViewById(R.id.editMail);
        senha = findViewById(R.id.editSenha);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailText = email.getText().toString();
                String senhaText = senha.getText().toString();

//                if (emailText.equals("admin") && senhaText.equals("123")) {
//                    Toast.makeText(MainActivity.this, "Login OK", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(MainActivity.this, "Login inválido", Toast.LENGTH_SHORT).show();
//                }

                try {
                    JSONObject json = new JSONObject();

                    json.put("email", emailText);
                    json.put("senha", senhaText);

                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = RequestBody.create(
                            json.toString(),
                            MediaType.parse("application/json")
                    );

                    Request request = new Request.Builder()
                            .url(Constants.BASE_URL + "auth/signin")
                            .post(body)
                                .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();

                            Log.d("login", e.getMessage());
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (!response.isSuccessful()) {
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, "Email ou senha incorreto. Por favor tente novamente", Toast.LENGTH_LONG).show();
                                });

                                return;
                            }

                            String responseBody = response.body().string();

                            try {
                                JSONObject json = new JSONObject(responseBody);

                                String token = json.getString("token");

                                SessionManager sessionManager = new SessionManager(MainActivity.this);

                                sessionManager.saveToken(token);

                                Intent home = new Intent(MainActivity.this, MenuPrincipal.class);

                                startActivity(home);
                            } catch (JSONException e) {
                                Log.d("login-error", e.getMessage());
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}