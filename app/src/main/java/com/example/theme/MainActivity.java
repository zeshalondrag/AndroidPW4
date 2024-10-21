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

    private boolean playerXTurn = true; // Человек всегда играет за X
    private boolean gameOver = false;   // Флаг окончания игры
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Настройки темы
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

        // Инициализация игрового поля
        GridLayout gridLayout = findViewById(R.id.gameGrid);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            int finalI = i;
            button.setOnClickListener(v -> makeMove(button, finalI));
        }

        Button restartBtn = findViewById(R.id.restartBtn);
        restartBtn.setOnClickListener(v -> resetGame());

        // Загрузка статистики при старте
        loadStats();
    }

    // Логика смены темы
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

    // Обновление иконки на кнопке смены темы
    private void updateImageButton() {
        if (themeSettings.getBoolean("MODE_NIGHT_ON", false)) {
            imageTheme.setImageResource(R.drawable.sun);
        } else {
            imageTheme.setImageResource(R.drawable.moon);
        }
    }

    // Устанавливаем текущую тему при запуске приложения
    private void setCurrentTheme() {
        if (themeSettings.getBoolean("MODE_NIGHT_ON", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // Ход человека
    private void makeMove(Button button, int index) {
        if (!button.getText().toString().isEmpty() || gameOver) return;

        button.setText(playerXTurn ? "X" : "O");

        checkGameState(); // Проверяем состояние игры

        playerXTurn = !playerXTurn; // Меняем ход

        // После хода игрока даём возможность боту сделать свой ход
        if (!gameOver && !playerXTurn) {
            botMakeMove(); // Ход бота за O
        }
    }

    // Ход бота
    private void botMakeMove() {
        GridLayout gridLayout = findViewById(R.id.gameGrid);
        List<Button> availableButtons = new ArrayList<>();

        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            if (button.getText().toString().isEmpty()) {
                availableButtons.add(button); // Сохраняем пустые клетки
            }
        }

        if (!availableButtons.isEmpty()) {
            Button botButton = availableButtons.get(random.nextInt(availableButtons.size()));
            botButton.setText("O");

            checkGameState(); // Проверяем после хода бота
            playerXTurn = !playerXTurn; // Меняем ход на игрока X
        }
    }

    // Проверка состояния игры
    private void checkGameState() {
        String winner = checkForWinner();

        if (winner != null) {
            Toast.makeText(this, "Победил " + winner, Toast.LENGTH_SHORT).show();
            saveStats(winner); // Обновляем статистику для победителя
            gameOver = true;   // Игра завершена
        } else if (checkForDraw()) {
            Toast.makeText(this, "Ничья", Toast.LENGTH_SHORT).show();
            saveStats("draw"); // Обновляем статистику ничьей
            gameOver = true;   // Игра завершена
        }
    }

    // Проверка победителя
    private String checkForWinner() {
        GridLayout gridLayout = findViewById(R.id.gameGrid);
        String[][] board = new String[3][3];

        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            board[i / 3][i % 3] = button.getText().toString();
        }

        // Проверка строк, столбцов и диагоналей на совпадение
        for (int i = 0; i < 3; i++) {
            if (board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2]) && !board[i][0].isEmpty()) {
                return board[i][0]; // Победа в строке
            }
            if (board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i]) && !board[0][i].isEmpty()) {
                return board[0][i]; // Победа в столбце
            }
        }

        if (board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2]) && !board[0][0].isEmpty()) {
            return board[0][0]; // Победа по диагонали
        }

        if (board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0]) && !board[0][2].isEmpty()) {
            return board[0][2]; // Победа по диагонали
        }

        return null; // Победителя нет
    }

    // Проверка ничьей
    private boolean checkForDraw() {
        GridLayout gridLayout = findViewById(R.id.gameGrid);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            if (button.getText().toString().isEmpty()) {
                return false; // Если есть пустые клетки, то ничьей нет
            }
        }
        return true; // Все клетки заполнены — ничья
    }

    // Сброс игры
    private void resetGame() {
        GridLayout gridLayout = findViewById(R.id.gameGrid);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            button.setText("");  // Очищаем клетки
            button.setEnabled(true); // Делаем их снова активными
        }
        gameOver = false; // Игра начинается заново
        playerXTurn = true; // Ходит X
    }

    // Загрузка статистики из SharedPreferences
    private void loadStats() {
        SharedPreferences prefs = getSharedPreferences("TicTacToeStats", MODE_PRIVATE);
        int xWins = prefs.getInt("xWins", 0);
        int oWins = prefs.getInt("oWins", 0);
        int draws = prefs.getInt("draws", 0);

        TextView statsTextView = findViewById(R.id.statsView);
        statsTextView.setText("Крестики: " + xWins + " | Нолики: " + oWins + " | Ничья: " + draws);
    }

    // Сохранение статистики
    private void saveStats(String winner) {
        SharedPreferences prefs = getSharedPreferences("TicTacToeStats", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int xWins = prefs.getInt("xWins", 0);
        int oWins = prefs.getInt("oWins", 0);
        int draws = prefs.getInt("draws", 0);

        if (winner.equals("X")) {
            xWins++;
            editor.putInt("xWins", xWins);
        } else if (winner.equals("O")) {
            oWins++;
            editor.putInt("oWins", oWins);
        } else if (winner.equals("draw")) {
            draws++;
            editor.putInt("draws", draws);
        }

        editor.apply(); // Сохраняем изменения

        loadStats(); // Обновляем статистику на экране
    }
}