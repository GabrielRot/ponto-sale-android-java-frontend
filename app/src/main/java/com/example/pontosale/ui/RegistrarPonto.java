package com.example.pontosale.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pontosale.R;
import com.example.pontosale.session.SessionManager;
import com.example.pontosale.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegistrarPonto extends AppCompatActivity {

    private int cameraFacing = CameraSelector.LENS_FACING_FRONT;

    private ImageCapture imageCapture;

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();

                imageCapture = new ImageCapture.Builder().build();

                PreviewView previewView = findViewById(R.id.previewView);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this,cameraSelector, preview, imageCapture);
            } catch (Exception e) {
                Log.e("startCamera", e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void enviarFoto(File file) {
        OkHttpClient client = new OkHttpClient();

        RequestBody fileBody = RequestBody.create(
                file,
                MediaType.parse("image/jpeg")
        );

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        String token = new SessionManager(RegistrarPonto.this).getToken();

        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "users/ponto")
                .addHeader("Authorization", "Bearer " + token)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Log.e("registrarPonto", "Erro: " + e.getMessage());

                    Toast.makeText(RegistrarPonto.this, "Falha ao registrar ponto", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(RegistrarPonto.this, "Registro de ponto registrado com sucesso", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegistrarPonto.this, "Validação da imagem de rosto não coincide com imagem da conta", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(getExternalFilesDir(null), "foto_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(RegistrarPonto.this),
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        runOnUiThread(() -> {
                            Log.d("registrarPonto", "Foto salva em: " + photoFile.getAbsolutePath());

                            enviarFoto(photoFile);
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.d("registrarPonto", "Erro: " + exception.getMessage());
                    }
                }
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrar_ponto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            startCamera();
        }

        MaterialButton btnSwithcCamera = findViewById(R.id.btnSwitchCamera);

        btnSwithcCamera.setOnClickListener((v) -> {
            cameraFacing = (cameraFacing == CameraSelector.LENS_FACING_BACK) ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;

            startCamera();
        });

        MaterialButton btnCapture = findViewById(R.id.btnCapture);

        btnCapture.setOnClickListener(v -> {
            capturePhoto();
        });
    }
}