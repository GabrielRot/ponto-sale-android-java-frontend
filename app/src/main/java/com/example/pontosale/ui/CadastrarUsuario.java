package com.example.pontosale.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.internal.utils.ImageUtil;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pontosale.R;
import com.example.pontosale.session.SessionManager;
import com.example.pontosale.utils.Constants;
import com.example.pontosale.utils.ImageUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CadastrarUsuario extends AppCompatActivity {

    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastrar_usuario);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SessionManager sessionManager = new SessionManager(CadastrarUsuario.this);

        TextInputLayout layoutNome = findViewById(R.id.layoutNome);
        TextInputLayout layoutEmail = findViewById(R.id.layoutEmail);
        TextInputLayout layoutSenha = findViewById(R.id.layoutSenha);
        TextInputLayout layoutConfirmarSenha = findViewById(R.id.layoutConfirmarSenha);

        TextInputEditText editNome = findViewById(R.id.editNome);
        TextInputEditText editEmail = findViewById(R.id.editEmail);
        TextInputEditText editSenha = findViewById(R.id.editSenha);
        TextInputEditText editConfirmarSenha = findViewById(R.id.editConfirmarSenha);

        ImageView imgUsuario = findViewById(R.id.imgUsuario);

        MaterialButton btnSelecionarImagem = findViewById(R.id.btnSelecionarImagem);

        ActivityResultLauncher<PickVisualMediaRequest> picker =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                        uri -> {
                            if (uri != null) {
                                selectedImageUri = uri;
                                imgUsuario.setImageURI(uri);
                            }
                        });

        MaterialButton btnCadastrar = findViewById(R.id.btnCadastrar);

        AtomicBoolean erroForm = new AtomicBoolean(false);

        imgUsuario.setOnClickListener(v -> {
            picker.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        btnSelecionarImagem.setOnClickListener(v -> {
            picker.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        btnCadastrar.setOnClickListener(v -> {
            String nome= editNome.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String senha = editSenha.getText().toString().trim();
            String confirmarSenha = editConfirmarSenha.getText().toString().trim();

            if (nome.isEmpty()) {
                layoutNome.setError("Informe o nome");

                erroForm.set(true);

                return;
            } else {
                layoutNome.setError(null);
            }

            if (email.isEmpty()) {
                layoutEmail.setError("Informe o e-mail");

                erroForm.set(true);

                return;
            } else {
                layoutEmail.setError(null);
            }

            String senhaMessage = "Senha e confirmação deverão ser iguais";

            if (senha.isEmpty()) {
                layoutSenha.setError("Informe a senha");

                erroForm.set(true);

                return;
            }

            if (confirmarSenha.isEmpty()) {
                layoutConfirmarSenha.setError("Informe a confirmação de senha");

                erroForm.set(true);

                return;
            }

            if (!senha.isEmpty() && !confirmarSenha.isEmpty()) {
                if (!senha.equals(confirmarSenha)) {
                    layoutSenha.setError(senhaMessage);
                    layoutConfirmarSenha.setError(senhaMessage);

                    erroForm.set(true);

                    return;
                } else {
                    layoutSenha.setError(null);
                    layoutConfirmarSenha.setError(null);

                    erroForm.set(false);

                }
            }

            if (selectedImageUri == null) {
                Toast.makeText(CadastrarUsuario.this, "Selecione uma imagem", Toast.LENGTH_SHORT).show();

                erroForm.set(true);

                return;
            } else {
                erroForm.set(false);
            }

            if (!erroForm.get()) {
                OkHttpClient client = new OkHttpClient();

                byte[] imageBytes = ImageUtils.resizeUriImage(CadastrarUsuario.this, selectedImageUri, 800, 800);

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("nome", nome)
                        .addFormDataPart("email", email)
                        .addFormDataPart("senha", senha)
                        .addFormDataPart(
                                "image",
                                "foto.jpg",
                                RequestBody.create(imageBytes).create(imageBytes, MediaType.parse("image/jpeg"))
                        )
                        .build();

                Request request = new Request.Builder()
                        .url(Constants.BASE_URL + "users/usuario")
                        .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        runOnUiThread(() -> {
                            if (response.isSuccessful()) {
                                Toast.makeText(CadastrarUsuario.this, "Usuário cadastrado com sucesso", Toast.LENGTH_LONG).show();

                                editNome.setText(null);
                                editEmail.setText(null);
                                editSenha.setText(null);
                                editConfirmarSenha.setText(null);
                                imgUsuario.setImageURI(null);
                            } else {
                                Log.e("cadastroUsuario", response.message());
                                Toast.makeText(CadastrarUsuario.this, "Falha ao cadastrar usuário", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }
}