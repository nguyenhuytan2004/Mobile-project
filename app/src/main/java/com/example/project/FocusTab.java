package com.example.project;

import android.app.AlertDialog;

import android.content.Intent;

import android.os.Bundle;
import android.os.CountDownTimer;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;

public class FocusTab extends AppCompatActivity {

    // Variable from layout
    ImageView homeTab, calendarTab;
    RelativeLayout front, behind;
    Button btnStart;
    ImageView btnEnd, btnPause, btnPlay, btnWhiteNoise, matrixTab;
    LinearLayout pauseLayout, playLayout;
    TextView btnOption, focusNotes, focusTime, focusTime2, pomoTime;
    ProgressBar progressBar2;

    // Self-made variable
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private boolean isPaused = false;
    private String focusNotesText = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.focus);

        front = findViewById(R.id.front);
        behind = findViewById(R.id.behind);

        homeTab = findViewById(R.id.homeTab);
        calendarTab = findViewById(R.id.calendarTab);
        matrixTab = findViewById(R.id.matrixTab);

        btnStart = findViewById(R.id.btnStart);
        btnEnd = findViewById(R.id.end_button);
        btnPause = findViewById(R.id.pause_button);
        btnPlay = findViewById(R.id.play_button);
        btnWhiteNoise = findViewById(R.id.white_noise_button);
        btnOption = findViewById(R.id.textView53);

        pauseLayout = findViewById(R.id.pauseLayout);
        playLayout = findViewById(R.id.playLayout);

        focusNotes = findViewById(R.id.textView54);
        focusTime = findViewById(R.id.countdown_timer);
        focusTime2 = findViewById(R.id.countdown_timer2);

        progressBar2 = findViewById(R.id.progressBar2);

        homeTab.setOnClickListener(view -> {
            startActivity(new Intent(FocusTab.this, MainActivity.class));
        });

        calendarTab.setOnClickListener(view -> {
            startActivity(new Intent(FocusTab.this, CalendarTab.class));
        });

        matrixTab.setOnClickListener(view -> {
            startActivity(new Intent(FocusTab.this, Matrix_Eisenhower.class));
        });

        btnStart.setOnClickListener(view -> {
            front.setVisibility(View.GONE);
            behind.setVisibility(View.VISIBLE);

            if (pauseLayout.getVisibility() == View.GONE) {
                pauseLayout.setVisibility(View.VISIBLE);
                playLayout.setVisibility(View.GONE);
            }

            int totalSecond = Integer.parseInt(focusTime.getText().toString().substring(0, 2)) * 60;
            timeLeftInMillis = totalSecond * 1000;

            progressBar2.setMax(totalSecond);
            progressBar2.setProgress(0);

            startTimer();
        });

        btnEnd.setOnClickListener(view -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }

            front.setVisibility(View.VISIBLE);
            behind.setVisibility(View.GONE);
        });

        btnPause.setOnClickListener(view -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
                isPaused = true;
            }

            pauseLayout.setVisibility(View.GONE);
            playLayout.setVisibility(View.VISIBLE);
        });

        btnPlay.setOnClickListener(view -> {
            if (isPaused) {
                startTimer();
                isPaused = false;
            }

            pauseLayout.setVisibility(View.VISIBLE);
            playLayout.setVisibility(View.GONE);
        });

        btnWhiteNoise.setOnClickListener(view -> {
            startActivity(new Intent(FocusTab.this, WhiteNoise.class));
        });

        btnOption.setOnClickListener(view -> {
            View popupView = LayoutInflater.from(FocusTab.this).inflate(R.layout.option_in_focus, null);
            PopupWindow popupWindow = new PopupWindow(popupView, 500, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            popupWindow.showAsDropDown(view, -430, -100);

            popupView.findViewById(R.id.focusSetting).setOnClickListener(v -> {
                Toast.makeText(FocusTab.this, "Cài đặt tập trung", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            });

            popupView.findViewById(R.id.addDedicatedRecord).setOnClickListener(v -> {
                Toast.makeText(FocusTab.this, "Thêm bản ghi chuyên tâm", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            });
        });

        focusNotes.setOnClickListener(v -> {
            View focusNotesView = getLayoutInflater().inflate(R.layout.add_focus_notes, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(FocusTab.this);
            builder.setView(focusNotesView);

            EditText editText = focusNotesView.findViewById(R.id.addNoteEditText);
            if (!focusNotesText.isEmpty()) {
                editText.setText(focusNotesText);
            }

            Button button2 = focusNotesView.findViewById(R.id.button2);
            Button button3 = focusNotesView.findViewById(R.id.button3);

            AlertDialog dialog = builder.create();

            button2.setOnClickListener(v2 -> {
                dialog.dismiss();
            });

            button3.setOnClickListener(v2 -> {
                focusNotesText = editText.getText().toString();
                dialog.dismiss();
            });

            dialog.show();
        });

        focusTime.setOnClickListener(v -> {
            View focusTimeView = getLayoutInflater().inflate(R.layout.change_focus_time, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(FocusTab.this);
            builder.setView(focusTimeView);

            SeekBar seekBar = focusTimeView.findViewById(R.id.seekBar);
            pomoTime = focusTimeView.findViewById(R.id.pomoTime);
            seekBar.setProgress(Integer.parseInt(focusTime.getText().toString().substring(0, 2)) - 5);
            pomoTime.setText(String.valueOf(seekBar.getProgress() + 5));

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int value = progress + 5;
                    pomoTime.setText(String.valueOf(value));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            Button btnCancel = focusTimeView.findViewById(R.id.button2);
            Button btnSave = focusTimeView.findViewById(R.id.button3);

            AlertDialog dialog = builder.create();

            btnCancel.setOnClickListener(v2 -> {
                dialog.dismiss();
            });

            btnSave.setOnClickListener(v2 -> {
                int newTime = seekBar.getProgress() + 5;
                focusTime.setText(String.format("%02d:00", newTime));
                dialog.dismiss();
            });

            dialog.show();
        });
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                int secondsLeft = (int) (millisUntilFinished / 1000);
                focusTime2.setText(String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60));
                progressBar2.setProgress(progressBar2.getMax() - secondsLeft);
            }

            public void onFinish() {
                btnEnd.performClick();
            }
        }.start();
    }
}
