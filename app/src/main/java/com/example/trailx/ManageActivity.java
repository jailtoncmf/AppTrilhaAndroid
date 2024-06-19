package com.example.trailx;


import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.MapFragment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ManageActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> trailSummaries;
    private ArrayList<Integer> trailIds;
    private SQLiteHelper dbHelper;
    private TextView summaryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        listView = findViewById(R.id.listView);
        summaryTextView = findViewById(R.id.summaryTextView);
        trailSummaries = new ArrayList<>();
        trailIds = new ArrayList<>();
        dbHelper = new SQLiteHelper(this);

        loadTrails();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, trailSummaries);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            int trailId = trailIds.get(position);
            showTrailOptionsDialog(trailId);
        });
    }

    private void loadTrails() {
        Cursor cursor = dbHelper.getAllTrails();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));
                float distance = cursor.getFloat(cursor.getColumnIndexOrThrow("distance"));
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow("duration"));
                float avgSpeed = cursor.getFloat(cursor.getColumnIndexOrThrow("avg_speed"));
                String origin = cursor.getString(cursor.getColumnIndexOrThrow("origin"));
                String destination = cursor.getString(cursor.getColumnIndexOrThrow("destination"));

                String summary = "ID: " + id +
                        "\nData: " + formatDate(timestamp) +
                        "\nVelocidade Média: " + String.format(Locale.US, "%.3f m/s", avgSpeed) +
                        "\nDistância: " + String.format(Locale.US, "%.3f metros", distance) +
                        "\nDuração: " + formatDuration(duration);

                trailSummaries.add(summary);
                trailIds.add(id);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }

    private String formatDuration(long millis) {
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void showTrailOptionsDialog(int trailId) {
        new AlertDialog.Builder(this)
                .setTitle("Opções da Trilha")
                .setItems(new CharSequence[]{"Deletar Trilha"}, (dialog, which) -> {
                    if (which == 0) {
                        confirmDeleteTrail(trailId);
                    }
                })
                .show();
    }
    private void confirmDeleteTrail(final int trailId) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir essa trilha?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    dbHelper.deleteTrail(trailId);
                    recreate();
                })
                .setNegativeButton("Não", null)
                .show();
    }
}
