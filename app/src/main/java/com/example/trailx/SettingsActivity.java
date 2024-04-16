package com.example.trailx;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private RadioGroup radioGroupVelocidade, radioGroupCoordenadas, radioGroupOrientacaoMapa, radioGroupTipoMapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = getSharedPreferences("configuracoes", MODE_PRIVATE);

        radioGroupVelocidade = findViewById(R.id.radioGroupVelocidade);
        radioGroupCoordenadas = findViewById(R.id.radioGroupCoordenadas);
        radioGroupOrientacaoMapa = findViewById(R.id.radioGroupOrientacaoMapa);
        radioGroupTipoMapa = findViewById(R.id.radioGroupTipoMapa);

        recuperarConfiguracoes();

        radioGroupVelocidade.setOnCheckedChangeListener((group, checkedId) -> salvarConfiguracoes());
        radioGroupCoordenadas.setOnCheckedChangeListener((group, checkedId) -> salvarConfiguracoes());
        radioGroupOrientacaoMapa.setOnCheckedChangeListener((group, checkedId) -> salvarConfiguracoes());
        radioGroupTipoMapa.setOnCheckedChangeListener((group, checkedId) -> salvarConfiguracoes());
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void salvarConfiguracoes() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int selectedVelocidadeId = radioGroupVelocidade.getCheckedRadioButtonId();
        RadioButton radioButtonVelocidade = findViewById(selectedVelocidadeId);
        editor.putString("unidade_velocidade", radioButtonVelocidade.getText().toString());

        int selectedCoordenadasId = radioGroupCoordenadas.getCheckedRadioButtonId();
        RadioButton radioButtonCoordenadas = findViewById(selectedCoordenadasId);
        editor.putString("formato_coordenadas", radioButtonCoordenadas.getText().toString());

        int selectedOrientacaoId = radioGroupOrientacaoMapa.getCheckedRadioButtonId();
        RadioButton radioButtonOrientacao = findViewById(selectedOrientacaoId);
        editor.putString("orientacao_mapa", radioButtonOrientacao.getText().toString());

        int selectedTipoMapaId = radioGroupTipoMapa.getCheckedRadioButtonId();
        RadioButton radioButtonTipoMapa = findViewById(selectedTipoMapaId);
        editor.putString("tipo_mapa", radioButtonTipoMapa.getText().toString());

        editor.apply();

        Toast.makeText(this, "Configurações salvas", Toast.LENGTH_SHORT).show();
    }

    private void recuperarConfiguracoes() {
        String unidadeVelocidade = sharedPreferences.getString("unidade_velocidade", "km/h");
        RadioButton radioButtonVelocidade;
        if (unidadeVelocidade.equals("m/s")) {
            radioButtonVelocidade = findViewById(R.id.radioButtonMs);
        } else {
            radioButtonVelocidade = findViewById(R.id.radioButtonKmh);
        }
        radioButtonVelocidade.setChecked(true);

        String formatoCoordenadas = sharedPreferences.getString("formato_coordenadas", "Graus [+/-DDD.DDDDD]");
        RadioButton radioButtonCoordenadas;
        switch (formatoCoordenadas) {
            case "Graus-Minutos [+/-DDD:MM.MMMMM]":
                radioButtonCoordenadas = findViewById(R.id.radioButtonGrausMinutos);
                break;
            case "Graus-Minutos-Segundos [+/-DDD:MM:SS.SSSSS]":
                radioButtonCoordenadas = findViewById(R.id.radioButtonGrausMinutosSegundos);
                break;
            default:
                radioButtonCoordenadas = findViewById(R.id.radioButtonGraus);
        }
        radioButtonCoordenadas.setChecked(true);

        String orientacaoMapa = sharedPreferences.getString("orientacao_mapa", "Nenhuma");
        RadioButton radioButtonOrientacaoMapa;
        switch (orientacaoMapa) {
            case "North Up":
                radioButtonOrientacaoMapa = findViewById(R.id.radioButtonNorthUp);
                break;
            case "Course Up":
                radioButtonOrientacaoMapa = findViewById(R.id.radioButtonCourseUp);
                break;
            default:
                radioButtonOrientacaoMapa = findViewById(R.id.radioButtonNenhuma);
        }
        radioButtonOrientacaoMapa.setChecked(true);

        String tipoMapa = sharedPreferences.getString("tipo_mapa", "Vetorial");
        RadioButton radioButtonTipoMapa;
        if (tipoMapa.equals("Satélite")) {
            radioButtonTipoMapa = findViewById(R.id.radioButtonSatelite);
        } else {
            radioButtonTipoMapa = findViewById(R.id.radioButtonVetorial);
        }
        radioButtonTipoMapa.setChecked(true);
    }
}
