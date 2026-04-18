package com.example.pontosale.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pontosale.R;
import com.google.android.material.card.MaterialCardView;

public class MenuPrincipal extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_principal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialCardView registrarPontoCard = findViewById(R.id.registrarPonto);

        registrarPontoCard.setOnClickListener(v -> {
            Intent intent = new Intent(MenuPrincipal.this, RegistrarPonto.class);

            startActivity(intent);
        });

        MaterialCardView cadastrarUsuario = findViewById(R.id.cadastrarUsuario);

        cadastrarUsuario.setOnClickListener(v -> {
            Intent intent = new Intent(MenuPrincipal.this, CadastrarUsuario.class);

            startActivity(intent);
        });

        MaterialCardView pontosRegistrados = findViewById(R.id.pontosRegistrados);

        pontosRegistrados.setOnClickListener(v -> {
            Intent intent = new Intent(MenuPrincipal.this, PontosRegistrados.class);

            startActivity(intent);
        });

        MaterialCardView roles = findViewById(R.id.roles);

        roles.setOnClickListener(v -> {
            Intent intent = new Intent(MenuPrincipal.this, Roles.class);

            startActivity(intent);
        });
    }
}