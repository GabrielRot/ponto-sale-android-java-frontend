package com.example.pontosale.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pontosale.R;
import com.example.pontosale.dto.RoleDTO;
import com.example.pontosale.session.SessionManager;
import com.example.pontosale.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FormRole extends AppCompatActivity {

    private Long roleId;
    private SessionManager sessionManager;

    private OkHttpClient client = new OkHttpClient();

    private MaterialButton btnSalvar;

    private TextInputEditText nome, descricao;

    private MaterialCheckBox criarUsuarios, visualizarUsuarios, atualizarUsuarios, deletarUsuarios;
    private MaterialCheckBox criarRole, visualizarRole, atualizarRole, deletarRole;
    private MaterialCheckBox visualizarTodosApontamentos;

    Map<String, MaterialCheckBox> mapPermissoes = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_role);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnSalvar = findViewById(R.id.btnSalvar);

        nome = findViewById(R.id.editNome);
        descricao = findViewById(R.id.editDescricao);

        criarUsuarios = findViewById(R.id.ckbCriarUsuarios);
        visualizarUsuarios = findViewById(R.id.ckbVisualizarUsuarios);
        atualizarUsuarios = findViewById(R.id.ckbAtualizarUsuarios);
        deletarUsuarios = findViewById(R.id.ckbDeletarUsuarios);

        criarRole = findViewById(R.id.ckbCriarRoles);
        visualizarRole = findViewById(R.id.ckbVisualizarRoles);
        atualizarRole = findViewById(R.id.ckbAtualizarRoles);
        deletarRole = findViewById(R.id.ckbDeletarRoles);

        visualizarTodosApontamentos = findViewById(R.id.ckbVisualizarApontamentos);

        roleId = getIntent().getLongExtra("ID_ROLE", -1);

        sessionManager = new SessionManager(this);

        if (roleId != -1) {
            fetchData(roleId);
        }

        btnSalvar.setOnClickListener(v -> {
            String nomeValue      = nome.getText().toString();
            String descricaoValue = descricao.getText().toString();

            List<String> permissoesRole = new ArrayList<>();

            if (criarUsuarios.isChecked()) permissoesRole.add("CREATE_USER");
            if (visualizarUsuarios.isChecked()) permissoesRole.add("READ_USER");
            if (atualizarUsuarios.isChecked()) permissoesRole.add("UPDATE_USER");
            if (deletarUsuarios.isChecked()) permissoesRole.add("DELETE_USER");

            if (criarRole.isChecked()) permissoesRole.add("CREATE_ROLE");
            if (visualizarRole.isChecked()) permissoesRole.add("READ_ROLE");
            if (atualizarRole.isChecked()) permissoesRole.add("UPDATE_ROLE");
            if (deletarRole.isChecked()) permissoesRole.add("DELETE_ROLE");

            if (visualizarTodosApontamentos.isChecked()) permissoesRole.add("VIEW_ALL_APPOINTMENTS");

            RoleDTO roleDTO = new RoleDTO(roleId, nomeValue, descricaoValue, permissoesRole);

            Gson gson = new Gson();

            String json = gson.toJson(roleDTO);

            RequestBody requestBody = RequestBody.create(
                json,
                    MediaType.parse("application/json")
            );

            Request request;

            if (roleId != -1) {
                request = new Request.Builder()
                        .url(Constants.BASE_URL + "role")
                        .put(requestBody)
                        .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                        .build();
            } else {
                request = new Request.Builder()
                        .url(Constants.BASE_URL + "role")
                        .post(requestBody)
                        .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                        .build();
            }

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("formRole", e.getMessage());

                    runOnUiThread(() -> {
                        Toast.makeText(FormRole.this, "Falha ao salvar dados", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    runOnUiThread(() -> {
                        Toast.makeText(FormRole.this, "Dados salvos com sucesso", Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }

    private void fetchData(Long roleId) {
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "role/" + roleId)
                .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("formRole", e.getMessage());

                Toast.makeText(FormRole.this, "Falha ao requisitar Role", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    runOnUiThread(() -> {
                        try {
                            JSONObject data = new JSONObject(responseData);

                            nome.setText(data.getString("nome"));
                            descricao.setText(data.getString("descricao"));

                            mapPermissoes.put("CREATE_USER", criarUsuarios);
                            mapPermissoes.put("READ_USER", visualizarUsuarios);
                            mapPermissoes.put("UPDATE_USER", atualizarUsuarios);
                            mapPermissoes.put("DELETE_USER", deletarUsuarios);

                            mapPermissoes.put("CREATE_ROLE", criarRole);
                            mapPermissoes.put("READ_ROLE", visualizarRole);
                            mapPermissoes.put("UPDATE_ROLE", atualizarRole);
                            mapPermissoes.put("DELETE_ROLE", deletarRole);

                            mapPermissoes.put("VIEW_ALL_APPOINTMENTS", visualizarTodosApontamentos);

                            JSONArray permissoes = data.getJSONArray("permissoes");

                            for (int i = 0; i < permissoes.length(); i++) {
                                String permissao  = permissoes.getString(i);

                                MaterialCheckBox checkBox = mapPermissoes.get(permissao);

                                if (checkBox != null) {
                                    checkBox.setChecked(true);
                                }
                            }
                        } catch (Exception e) {
                            Log.e("formRole", e.getMessage());

                            Toast.makeText(FormRole.this, "Falha ao iniciar dados", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(FormRole.this, "Falha ao consultar Role", Toast.LENGTH_SHORT);
                }
            }
        });
    }
}