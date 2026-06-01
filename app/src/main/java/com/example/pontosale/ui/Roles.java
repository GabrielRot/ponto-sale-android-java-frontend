package com.example.pontosale.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pontosale.R;
import com.example.pontosale.session.SessionManager;
import com.example.pontosale.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Roles extends AppCompatActivity {

    private OkHttpClient client = new OkHttpClient();

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_roles);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(Roles.this);

        fetchData();

        MaterialButton btnCadastrar = findViewById(R.id.btnCadastrar);

        btnCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(Roles.this, FormRole.class);

            startActivity(intent);
        });
    }

    private void fetchData() {
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "role")
                .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("roles", e.getMessage());

                Toast.makeText(Roles.this, "Falha ao requisitar roles", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    try {
                        JSONArray jsonArray = new JSONArray(responseData);

                        runOnUiThread(() -> buildUI(jsonArray));
                    } catch (Exception e) {
                        Log.e("roles", e.getMessage());
                    }
                }
            }
        });
    }

    private void buildUI(JSONArray jsonArray) {
        try {
            LinearLayout cardsContainer = findViewById(R.id.rolesContainer);

            cardsContainer.removeAllViews();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject data = jsonArray.getJSONObject(i);

                Long roleId = data.getLong("id");
                String nome = data.getString("nome");
                String descricao = data.getString("descricao");

                MaterialCardView cardView = new MaterialCardView(this);

                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

                cardView.setLayoutParams(cardParams);

                cardView.setRadius(16);
                cardView.setStrokeWidth(2);
                cardView.setStrokeColor(getColor(R.color.gray));
                cardView.setCardElevation(4);
                cardView.setUseCompatPadding(true);

                //layout card
                LinearLayout layoutCard = new LinearLayout(this);

                ViewGroup.LayoutParams layoutCardParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );

                layoutCard.setLayoutParams(layoutCardParams);
                layoutCard.setOrientation(LinearLayout.HORIZONTAL);
                layoutCard.setPadding(8, 8, 8, 8);

                //layout icon left
                LinearLayout layoutIconLeft = new LinearLayout(this);

                ViewGroup.LayoutParams layoutIconLeftParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

                layoutIconLeft.setLayoutParams(layoutIconLeftParams);

                ImageView iconLeft = new ImageView(this);

                iconLeft.setLayoutParams(layoutIconLeftParams);

                iconLeft.setImageResource(R.drawable.ic_baseline_lock_person_24);

                layoutIconLeft.addView(iconLeft);

                layoutCard.addView(layoutIconLeft);

                //layout nome e descricao
                LinearLayout layoutContent = new LinearLayout(this);

                int widthInPx = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        280,
                        getResources().getDisplayMetrics()
                );

                ViewGroup.LayoutParams layoutContentParams = new LinearLayout.LayoutParams(
                        widthInPx,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

                ViewGroup.MarginLayoutParams layoutContentMarginParams = (ViewGroup.MarginLayoutParams) layoutContentParams;

                layoutContentMarginParams.leftMargin = 4;

                layoutContent.setLayoutParams(layoutContentMarginParams);
                layoutContent.setOrientation(LinearLayout.VERTICAL);

                TextView roleName = new TextView(this);

                ViewGroup.LayoutParams textParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

                roleName.setLayoutParams(textParams);
                roleName.setText(nome);
                roleName.setTypeface(roleName.getTypeface(), Typeface.BOLD);
                roleName.setTextSize(16);

                layoutContent.addView(roleName);

                TextView roleDescription = new TextView(this);

                roleDescription.setLayoutParams(textParams);
                roleDescription.setText((!descricao.isEmpty()) ? descricao : "-");
                roleDescription.setTextSize(12);

                layoutContent.addView(roleDescription);

                layoutCard.addView(layoutContent);

                LinearLayout layoutIconRight = new LinearLayout(this);

                layoutIconRight.setLayoutParams(layoutCardParams);
                layoutIconRight.setGravity(Gravity.CENTER);

                MaterialCardView iconEditCard = new MaterialCardView(this);

                int iconsWidthPX = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        25,
                        getResources().getDisplayMetrics()
                );


                ViewGroup.LayoutParams iconEditLayoutParams = new LinearLayout.LayoutParams(
                        iconsWidthPX,
                        iconsWidthPX
                );

                ViewGroup.MarginLayoutParams iconEditParams = (ViewGroup.MarginLayoutParams) iconEditLayoutParams;

                iconEditParams.setMarginEnd(6);

                iconEditCard.setLayoutParams(iconEditParams);
                iconEditCard.setStrokeWidth(0);
                iconEditCard.setFocusable(true);
                iconEditCard.setClickable(true);
                iconEditCard.setPadding(16,16,16,16);

                ImageView iconEdit = new ImageView(this);

                iconEdit.setLayoutParams(layoutIconLeftParams);

                iconEdit.setImageResource(R.drawable.ic_outline_edit_square_24);

                iconEditCard.addView(iconEdit);

                iconEditCard.setOnClickListener(v -> {
                    Intent intent = new Intent(Roles.this, FormRole.class);

                    intent.putExtra("ID_ROLE", roleId);

                    startActivity(intent);
                });

                layoutIconRight.addView(iconEditCard);

                MaterialCardView iconDeleteCard = new MaterialCardView(this);

                iconDeleteCard.setLayoutParams(iconEditParams);
                iconDeleteCard.setStrokeWidth(0);
                iconDeleteCard.setFocusable(true);
                iconDeleteCard.setClickable(true);
                iconDeleteCard.setPadding(16, 16, 16, 16);

                ImageView iconDelete = new ImageView(this);

                iconDelete.setLayoutParams(iconEditParams);
                iconDelete.setImageResource(R.drawable.ic_baseline_delete_outline_24);
                iconDelete.setColorFilter(ContextCompat.getColor(this, R.color.delete));

                iconDeleteCard.addView(iconDelete);

                iconDeleteCard.setOnClickListener(v -> {
                    HttpUrl url = HttpUrl.parse(Constants.BASE_URL + "role")
                            .newBuilder()
                            .addQueryParameter("id", String.valueOf(roleId))
                            .build();

                    Request request = new Request.Builder()
                            .url(url)
                            .delete()
                            .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e("roles", e.getMessage());

                            Toast.makeText(Roles.this, "Falha ao excluir role", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            Toast.makeText(Roles.this, "Role excluida com sucesso", Toast.LENGTH_SHORT).show();

                            cardsContainer.removeView(cardView);
                        }
                    });
                });

                layoutIconRight.addView(iconDeleteCard);

                layoutCard.addView(layoutIconRight);

                cardView.addView(layoutCard);

                cardsContainer.addView(cardView);

            }
        } catch (Exception e) {
            Log.e("roles", e.getMessage());

            Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
        }
    }
}