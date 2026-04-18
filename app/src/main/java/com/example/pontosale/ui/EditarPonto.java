package com.example.pontosale.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.graphics.shapes.Utils;

import com.example.pontosale.R;
import com.example.pontosale.utils.Text;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditarPonto extends AppCompatActivity {

    private TextInputEditText edtDataInicial, edtHoraInicial, edtDataFinal, edtHoraFinal;
    private Calendar calendar;
    private final Locale localeBR = new Locale("pt", "BR");

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

        edtDataInicial = findViewById(R.id.edtDataInicial);
        edtHoraInicial = findViewById(R.id.edtHoraInicial);

        edtDataFinal = findViewById(R.id.edtDataFinal);
        edtHoraFinal = findViewById(R.id.edtHoraFinal);

        calendar = Calendar.getInstance();

//        preencherCamposDataHora(edtDataInicial, edtHoraInicial, );
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