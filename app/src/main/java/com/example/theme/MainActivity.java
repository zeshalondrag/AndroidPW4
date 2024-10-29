package com.example.theme;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    SharedPreferences themeSettings;
    SharedPreferences.Editor settingsEditor;
    ImageButton imageTheme;

    private boolean playerXTurn = true;
    private boolean gameOver = false;
    private boolean isPlayWithBot = false;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        themeSettings = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        if (!themeSettings.contains("MODE_NIGHT_ON")) {
            settingsEditor = themeSettings.edit();
            settingsEditor.putBoolean("MODE_NIGHT_ON", false);
            settingsEditor.apply();
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            setCurrentTheme();
        }

        setContentView(R.layout.activity_main);

        imageTheme = findViewById(R.id.imgbtn);
        updateImageButton();
        imageTheme.setOnClickListener(v -> toggleTheme());

        GridLayout gridLayout = findViewById(R.id.gameGrid);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            int finalI = i;
            button.setOnClickListener(v -> makeMove(button, finalI));
        }

        Button restartBtn = findViewById(R.id.restartBtn);
        restartBtn.setOnClickListener(v -> resetGame());

        Button playWithBotBtn = findViewById(R.id.playWithBotBtn);
        Button playWithFriendBtn = findViewById(R.id.playWithFriendBtn);

        playWithBotBtn.setOnClickListener(v -> {
            isPlayWithBot = true;
            resetGame();
            Toast.makeText(this, "Режим: Игра с ботом", Toast.LENGTH_SHORT).show();
        });

        playWithFriendBtn.setOnClickListener(v -> {
            isPlayWithBot = false;
            resetGame();
            Toast.makeText(this, "Режим: Игра на двоих", Toast.LENGTH_SHORT).show();
        });

        loadStats();
    }

    private void toggleTheme() {
        if (themeSettings.getBoolean("MODE_NIGHT_ON", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            settingsEditor = themeSettings.edit();
            settingsEditor.putBoolean("MODE_NIGHT_ON", false);
            settingsEditor.apply();
            Toast.makeText(MainActivity.this, "Тёмная тема отключена", Toast.LENGTH_SHORT).show();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            settingsEditor = themeSettings.edit();
            settingsEditor.putBoolean("MODE_NIGHT_ON", true);
            settingsEditor.apply();
            Toast.makeText(MainActivity.this, "Тёмная тема включена", Toast.LENGTH_SHORT).show();
        }
        updateImageButton();
    }

    private void updateImageButton() {
        if (themeSettings.getBoolean("MODE_NIGHT_ON", false)) {
            imageTheme.setImageResource(R.drawable.sun);
        } else {
            imageTheme.setImageResource(R.drawable.moon);
        }
    }

    private void setCurrentTheme() {
        if (themeSettings.getBoolean("MODE_NIGHT_ON", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void makeMove(Button button, int index) {
        if (!button.getText().toString().isEmpty() || gameOver) return;

        button.setText(playerXTurn ? "X" : "O");

        checkGameState();

        playerXTurn = !playerXTurn;

        if (isPlayWithBot && !gameOver && !playerXTurn) {
            botMakeMove();
        }
    }

    private void botMakeMove() {
        GridLayout gridLayout = findViewById(R.id.gameGrid);
        List<Button> availableButtons = new ArrayList<>();

        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            if (button.getText().toString().isEmpty()) {
                availableButtons.add(button);
            }
        }

        if (!availableButtons.isEmpty()) {
            Button botButton = availableButtons.get(random.nextInt(availableButtons.size()));
            botButton.setText("O");

            checkGameState();
            playerXTurn = !playerXTurn;
        }
    }

    private void checkGameState() {
        String winner = checkForWinner();

        if (winner != null) {
            Toast.makeText(this, "Победил " + winner, Toast.LENGTH_SHORT).show();
            saveStats(winner);
            gameOver = true;
        } else if (checkForDraw()) {
            Toast.makeText(this, "Ничья", Toast.LENGTH_SHORT).show();
            saveStats("Ничья");
            gameOver = true;
        }
    }

    private String checkForWinner() {
        GridLayout gridLayout = findViewById(R.id.gameGrid);
        String[][] field = new String[3][3];

        for (int i = 0; i < 9; i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            field[i / 3][i % 3] = button.getText().toString();
        }

        for (int i = 0; i < 3; i++) {
            if (field[i][0].equals(field[i][1]) && field[i][1].equals(field[i][2]) && !field[i][0].isEmpty()) {
                return field[i][0];
            }

            if (field[0][i].equals(field[1][i]) && field[1][i].equals(field[2][i]) && !field[0][i].isEmpty()) {
                return field[0][i];
            }
        }

        if (field[0][0].equals(field[1][1]) && field[1][1].equals(field[2][2]) && !field[0][0].isEmpty()) {
            return field[0][0];
        }

        if (field[0][2].equals(field[1][1]) && field[1][1].equals(field[2][0]) && !field[0][2].isEmpty()) {
            return field[0][2];
        }

        return null;
    }

    private boolean checkForDraw() {
        GridLayout gridLayout = findViewById(R.id.gameGrid);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            if (button.getText().toString().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void resetGame() {
        GridLayout gridLayout = findViewById(R.id.gameGrid);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            button.setText("");
        }
        gameOver = false;
        playerXTurn = true;
    }

    private void saveStats(String result) {
        SharedPreferences preferences = getSharedPreferences("stats", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        int xWins = preferences.getInt("XWins", 0);
        int oWins = preferences.getInt("OWins", 0);
        int draws = preferences.getInt("Draws", 0);

        if (result.equals("X")) {
            editor.putInt("XWins", ++xWins);
        } else if (result.equals("O")) {
            editor.putInt("OWins", ++oWins);
        } else {
            editor.putInt("Draws", ++draws);
        }

        editor.apply();
        updateStats();
    }

    private void loadStats() {
        SharedPreferences preferences = getSharedPreferences("stats", MODE_PRIVATE);
        int xWins = preferences.getInt("XWins", 0);
        int oWins = preferences.getInt("OWins", 0);
        int draws = preferences.getInt("Draws", 0);

        TextView statsView = findViewById(R.id.statsView);
        statsView.setText("Крестики: " + xWins + " | Нолики: " + oWins + " | Ничья: " + draws);
    }

    private void updateStats() {
        SharedPreferences preferences = getSharedPreferences("stats", MODE_PRIVATE);
        int xWins = preferences.getInt("XWins", 0);
        int oWins = preferences.getInt("OWins", 0);
        int draws = preferences.getInt("Draws", 0);

        TextView statsView = findViewById(R.id.statsView);
        statsView.setText("Крестики: " + xWins + " | Нолики: " + oWins + " | Ничья: " + draws);
    }
}
