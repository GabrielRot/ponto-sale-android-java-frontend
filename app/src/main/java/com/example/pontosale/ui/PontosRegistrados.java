package com.example.pontosale.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pontosale.R;
import com.example.pontosale.session.SessionManager;
import com.example.pontosale.utils.Constants;
import com.example.pontosale.utils.Text;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.InputStream;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PontosRegistrados extends AppCompatActivity {

    private ScrollView scrollView;

    private LinearLayout apontamentosContainer;

    private SessionManager sessionManager;

    OkHttpClient client = new OkHttpClient();

    private MaterialButton btnGerarRelatorio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pontos_registrados);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AndroidThreeTen.init(this);

        scrollView = findViewById(R.id.main);

        apontamentosContainer = findViewById(R.id.apontamentosContainer);

        sessionManager = new SessionManager(this);

        btnGerarRelatorio = findViewById(R.id.btnGerarRelatorio);

        btnGerarRelatorio.setOnClickListener(v -> {
            Request request = new Request.Builder()
                    .url(Constants.BASE_URL + "ponto/relatorio-pdf")
                    .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("pontoUser", e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Log.d("PDF", "Content-Type: " + response.header("Content-Type"));
                    Log.d("PDF", "Code: " + response.code());

                    if (response.isSuccessful()) {
                        File pdfFile = new File(
                                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                                "relatorio-ponto.pdf"
                        );

                        InputStream inputStream = response.body().byteStream();

                        FileOutputStream outputStream = new FileOutputStream(pdfFile);

                        byte[] buffer = new byte[4096];

                        int bytesRead;

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        outputStream.flush();

                        outputStream.close();

                        inputStream.close();

                        Log.d("PDF", "Arquivo salvo: " + pdfFile.exists());
                        Log.d("PDF", "Tamanho: " + pdfFile.length());

                        runOnUiThread(() -> abrirPdf(pdfFile));
                    }
                }
            });
        });

        fetchData();
    }

    private void abrirPdf(File pdfFile) {

        Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                pdfFile
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setDataAndType(uri, "application/pdf");

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        Intent chooser = Intent.createChooser(
                intent,
                "Abrir PDF com"
        );

        try {
            startActivity(chooser);
        } catch (Exception e) {
            Log.e("pontoUser", e.getMessage());

            Toast.makeText(
                    this,
                    "Nenhum leitor PDF encontrado",
                    Toast.LENGTH_LONG
            ).show();
        }

    }

    private void fetchData() {
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "ponto/pontos-user")
                .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("pontoUser", e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    try {
                        JSONArray jsonArray = new JSONArray(responseData);

                        runOnUiThread(() -> buildUI(jsonArray));
                    } catch (Exception e) {
                        Log.e("pontoUser", e.getMessage());
                    }
                }
            }
        });
    }

    private void buildUI(JSONArray jsonArray) {
        try {
            apontamentosContainer.removeAllViews();

            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(
                    "EEEE dd/MM/yyyy HH:mm",
                    new Locale("pt", "BR")
            );



            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject data = jsonArray.getJSONObject(i);

                Long id = data.getLong("id");
                String dataHoraEntradaStr = data.getString("dataHoraAbertura");
                String dataHoraFechamentoStr = data.getString("dataHoraFechamento");
                String tipoInsercaoPonto = data.getString("tipoInsercaoPonto");

                LocalDateTime entrada = LocalDateTime.parse(dataHoraEntradaStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                LocalDateTime saida = LocalDateTime.parse(dataHoraFechamentoStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                String entradaFormatada = Text.capitalize(entrada.format(outputFormatter));
                String saidaFormatada = Text.capitalize(saida.format(outputFormatter));

                // Cálculo da duração
                Duration duration = Duration.between(entrada, saida);

                Log.d("duration", String.valueOf(duration.toMinutes()));

                long horas = duration.toHours();
                long minutos = duration.toMinutes() % 60;

                String tempoTrabalhado = horas + "h";

                tempoTrabalhado += (minutos > 0) ? " " + String.format("%02d", minutos) + "m" : "";

                // Criar o card
                MaterialCardView card = new MaterialCardView(this);

                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

                final long idPonto = id;

                card.setOnClickListener(v -> {
                    Intent intent = new Intent(PontosRegistrados.this, EditarPonto.class);

                    intent.putExtra("ID_PONTO", idPonto);

                    startActivity(intent);
                });

                cardParams.setMargins(0, 0, 0, 24);

                card.setLayoutParams(cardParams);

                card.setRadius(16);
                card.setStrokeWidth(2);
                card.setStrokeColor(getColor(R.color.gray));
                card.setCardElevation(4);
                card.setUseCompatPadding(true);
                card.setClickable(true);
                card.setFocusable(true);

                // Layout Principal
                LinearLayout layoutPrincipal = new LinearLayout(this);
                layoutPrincipal.setOrientation(LinearLayout.HORIZONTAL);
                layoutPrincipal.setPadding(24, 24, 24, 24);
                layoutPrincipal.setGravity(Gravity.CENTER_VERTICAL);

                // Ícone
                ImageView icon = new ImageView(this);
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(60, 60);
                iconParams.setMargins(0, 0, 16, 0);
                icon.setLayoutParams(iconParams);
                icon.setImageResource(R.drawable.ic_baseline_access_time_24);

                // Layout das datas
                LinearLayout layoutDatas = new LinearLayout(this);
                layoutDatas.setOrientation(LinearLayout.VERTICAL);
                layoutDatas.setLayoutParams(
                        new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1)
                );

                TextView txtEntrada = new TextView(this);
                txtEntrada.setText(entradaFormatada);
                txtEntrada.setTextColor(getColor(R.color.gray_dark));

                TextView txtSaida = new TextView(this);
                txtSaida.setText(saidaFormatada);
                txtSaida.setTextColor(getColor(R.color.gray_dark));

                layoutDatas.addView(txtEntrada);
                layoutDatas.addView(txtSaida);

                //Tempo total
                TextView txtTempo = new TextView(this);
                txtTempo.setText(tempoTrabalhado);
                txtTempo.setTextSize(16);
                txtTempo.setTypeface(null, Typeface.BOLD);
                txtTempo.setGravity(Gravity.END);
                txtTempo.setTextColor(getColor(R.color.gray_dark));

                //Montagem do layout
                layoutPrincipal.addView(icon);
                layoutPrincipal.addView(layoutDatas);
                layoutPrincipal.addView(txtTempo);

                card.addView(layoutPrincipal);
                apontamentosContainer.addView(card);

            }
        } catch (Exception e) {
            Log.e("pontoUser", e.getMessage());

            Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
        }
    }
}