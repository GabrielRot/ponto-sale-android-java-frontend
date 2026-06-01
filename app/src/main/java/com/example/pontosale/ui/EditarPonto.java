package com.example.pontosale.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.graphics.shapes.Utils;

import com.example.pontosale.R;
import com.example.pontosale.session.SessionManager;
import com.example.pontosale.utils.Constants;
import com.example.pontosale.utils.Text;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditarPonto extends AppCompatActivity {

    private TextInputEditText edtDataInicial, edtHoraInicial, edtDataFinal, edtHoraFinal;
    private TextView txtNomeUsuario;
    private ImageView fotoUsuario;
    private MaterialButton btnSalvarPonto;
    private Calendar calendar;
    private final Locale localeBR = new Locale("pt", "BR");

    private OkHttpClient client = new OkHttpClient();

    private SessionManager sessionManager;

    private long idPonto;

    private void onClickEditData(View v) {
        Calendar calendar = Calendar.getInstance();

        int ano = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        EditarPonto.this,
                        (view, year, month, dayOfMonth) -> {

                            String data =
                                    String.format(
                                            "%02d/%02d/%04d",
                                            dayOfMonth,
                                            month + 1,
                                            year
                                    );

                            ((TextInputEditText) v).setText(data);
                        },
                        ano,
                        mes,
                        dia
                );

        datePickerDialog.show();
    }

    private String convertDate(String date, String hour) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate localDate = LocalDate.parse(date, dateFormatter);

        LocalTime localTime = LocalTime.parse(hour);

        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);

        String dataHora = localDateTime.format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        );

        return dataHora;
    }

    private void onClickSalvarPonto(View v) {
        String dataAbertura, horaAbertura, dataFechamento, horaFechamento;

        dataAbertura = edtDataInicial.getText().toString();

        if (dataAbertura.isEmpty()) {
            edtDataInicial.setError("Data de abertura nula");

            return;
        }

        horaAbertura = edtHoraInicial.getText().toString();

        if (horaAbertura.isEmpty()) {
            edtHoraInicial.setError("Hora de abertura nula");

            return;
        }

        dataFechamento = edtDataFinal.getText().toString();

        if (dataFechamento.isEmpty()) {
            edtDataFinal.setError("Data de fechamento nula");

            return;
        }

        horaFechamento = edtHoraFinal.getText().toString();

        if (horaFechamento.isEmpty()) {
            edtHoraFinal.setError("Hora de fechamento nuula");

            return;
        }

        try {
            LocalTime.parse(horaAbertura);
            LocalTime.parse(horaFechamento);
        } catch (Exception e) {
            Log.e("editarPonto", e.getMessage());

            Toast.makeText(this, "Falhao ao validar horas", Toast.LENGTH_SHORT).show();

            return;
        }

        String dataHoraAbertura   = convertDate(dataAbertura, horaAbertura);
        String dataHoraFechamento = convertDate(dataFechamento, horaFechamento);

        Log.i("dataHora", dataHoraAbertura);
        Log.i("dataHora", dataHoraFechamento);


        JSONObject json = new JSONObject();

        try {
            json.put("id", idPonto);
            json.put("dataHoraAbertura", dataHoraAbertura);
            json.put("dataHoraFechamento", dataHoraFechamento);
        } catch (JSONException jsonException) {
          Log.e("editarPonto", jsonException.getMessage());

            Toast.makeText(this, "Falha ao salvar data", Toast.LENGTH_SHORT).show();
        }

        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        RequestBody requestBody = RequestBody.create(json.toString(), JSON);

        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "ponto")
                .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                .put(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("editarPonto", e.getMessage());

                runOnUiThread(() -> {
                    Toast.makeText(EditarPonto.this, "Falha ao salvar dados", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(EditarPonto.this, "Dados salvos com sucesso", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditarPonto.this, "Falha ao salvar dados", Toast.LENGTH_SHORT).show();
                    }
                });
             }
        });
    }

    private TextWatcher setTextWatcher(TextInputEditText input) {
        TextWatcher textWatcher = new TextWatcher() {

            private boolean isUpdating;

            @Override
            public void afterTextChanged(Editable editable) {

                if (isUpdating) return;

                isUpdating = true;

                String text = editable.toString().replaceAll("[^\\d]", "");

                if (text.length() > 4) {
                    text = text.substring(0, 4);
                }

                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < text.length(); i++) {

                    if (i == 2) {
                        formatted.append(":");
                    }

                    formatted.append(text.charAt(i));

                }

                input.setText(formatted.toString());

                input.setSelection(
                        formatted.length()
                );

//                String strHour   = text.substring(0, 2);
//                String strMinute = text.substring(2, 4);
//
//                int hour   = Integer.parseInt(strHour);
//                int minute = Integer.parseInt(strMinute);
//
//                if (hour > 23) {
//                    input.setError("Hora inválida");
//                }
//
//                if (minute > 59) {
//                    input.setError("Minuto inválido");
//                }

                isUpdating = false;

            }

            @Override
            public void beforeTextChanged(
                    CharSequence charSequence,
                    int i,
                    int i1,
                    int i2
            ) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
        };

        return textWatcher;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_ponto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fotoUsuario = findViewById(R.id.fotoUsuario);

        txtNomeUsuario = findViewById(R.id.nomeUsuario);

        edtDataInicial = findViewById(R.id.edtDataInicial);
        edtHoraInicial = findViewById(R.id.edtHoraInicial);

        edtDataInicial.setOnClickListener(this::onClickEditData);
        edtHoraInicial.addTextChangedListener(setTextWatcher(edtHoraInicial));

        edtDataFinal = findViewById(R.id.edtDataFinal);
        edtHoraFinal = findViewById(R.id.edtHoraFinal);

        edtDataFinal.setOnClickListener(this::onClickEditData);
        edtHoraFinal.addTextChangedListener(setTextWatcher(edtHoraFinal));

        btnSalvarPonto = findViewById(R.id.salvarPonto);

        btnSalvarPonto.setOnClickListener(this::onClickSalvarPonto);

        calendar = Calendar.getInstance();

        sessionManager = new SessionManager(this);

        idPonto = getIntent().getLongExtra("ID_PONTO", -1);

        fetchData();

//        preencherCamposDataHora(edtDataInicial, edtHoraInicial, );
    }

    private void fetchData() {
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "ponto/" + idPonto)
                .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("editarPonto", e.getMessage());

                runOnUiThread(() -> {
                    Toast.makeText(EditarPonto.this, "Falha ao consultar dados", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                   try {
                       JSONObject jsonObject = new JSONObject(responseData);

                       String strFotoUsuario = jsonObject.getString("fotoUsuario");

                       byte[] decodedBytes = Base64.decode(strFotoUsuario, Base64.DEFAULT);

                       Bitmap bitmap = BitmapFactory.decodeByteArray(
                               decodedBytes,
                               0,
                               decodedBytes.length
                       );

                       String strNomeUsuario        = jsonObject.getString("nomeUsuario");
                       String dataHoraAberturaStr   = jsonObject.getString("dataHoraAbertura");
                       String dataHoraFechamentoStr = jsonObject.getString("dataHoraFechamento");

                       SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

                       Date dataHoraAbertura   = dateFormat.parse(dataHoraAberturaStr);
                       Date dataHoraFechamento = dateFormat.parse(dataHoraFechamentoStr);

                       SimpleDateFormat dataFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                       SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                       String strDataAbertura = dataFormat.format(dataHoraAbertura);
                       String strHoraAbertura = hourFormat.format(dataHoraAbertura);

                       String strDataFechamento = dataFormat.format(dataHoraFechamento);
                       String strHoraFechamento = hourFormat.format(dataHoraFechamento);

                       runOnUiThread(() -> {

                           fotoUsuario.setImageBitmap(bitmap);

                           txtNomeUsuario.setText(strNomeUsuario);

                           edtDataInicial.setText(strDataAbertura);
                           edtHoraInicial.setText(strHoraAbertura);

                           edtDataFinal.setText(strDataFechamento);
                           edtHoraFinal.setText(strHoraFechamento);
                       });
                   } catch (Exception e) {
                       Log.e("editarPontoErro", e.getMessage());
                   }
                } else {
                    Log.e("editarPonto", "Falha na resposta: " + response.code());
                }
            }
        });
    }

    private void preencherCamposDataHora(TextInputEditText data, TextInputEditText hora, String dataISO) throws ParseException {
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US);

        Date dataParsed = parser.parse(dataISO);

        calendar.setTime(dataParsed);

        SimpleDateFormat sdfData = new SimpleDateFormat("EEEE dd/MM/yyyy", localeBR);
        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", localeBR);

        data.setText(Text.capitalize(sdfData.format(dataParsed)));
        hora.setText(sdfHora.format(dataParsed));
    }

}