package com.example.trailx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btnFazerTrilha = findViewById(R.id.btnFazerTrilha);
        btnFazerTrilha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DoActivity.class);
                startActivity(intent);
            }
        });

        Button btnGerenciarTrilha = findViewById(R.id.btnGerenciarTrilha);
        btnGerenciarTrilha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ManageActivity.class);
                startActivity(intent);
            }
        });


    }
}