package com.example.project;

import android.app.AlertDialog;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.util.Log;
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
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class FocusTab extends AppCompatActivity {

    // Variable from layout
    ImageView homeTab, calendarTab;
    RelativeLayout front, behind;
    Button btnStart;
    ImageView btnEnd, btnPause, btnPlay, btnWhiteNoise, matrixTab, habitTab;
    LinearLayout pauseLayout, playLayout;
    TextView btnOption, focusNotes, focusTime, focusTime2, pomoTime;
    ProgressBar progressBar2;
    MediaPlayer mediaPlayer;

    // Self-made variable
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private boolean isPaused = false;
    private String focusNotesText = "";
    private int prefFocusTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.focus);

        front = findViewById(R.id.front);
        behind = findViewById(R.id.behind);

        homeTab = findViewById(R.id.homeTab);
        calendarTab = findViewById(R.id.calendarTab);
        matrixTab = findViewById(R.id.matrixTab);
        habitTab = findViewById(R.id.habitTabFocus);

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

        SharedPreferences sharedPreferences = getSharedPreferences("FocusTabPrefs", MODE_PRIVATE);
        prefFocusTime = sharedPreferences.getInt("focusTime", 5);
        focusTime.setText(String.format("%02d:00", prefFocusTime));

        homeTab.setOnClickListener(view -> {
            startActivity(new Intent(FocusTab.this, MainActivity.class));
        });

        calendarTab.setOnClickListener(view -> {
            startActivity(new Intent(FocusTab.this, CalendarTab.class));
        });

        matrixTab.setOnClickListener(view -> {
            startActivity(new Intent(FocusTab.this, Matrix_Eisenhower.class));
        });

        habitTab.setOnClickListener(view -> {
            startActivity(new Intent(FocusTab.this, HabitActivity.class));
        });

        btnStart.setOnClickListener(view -> {
            front.setVisibility(View.GONE);
            behind.setVisibility(View.VISIBLE);

            if (pauseLayout.getVisibility() == View.GONE) {
                pauseLayout.setVisibility(View.VISIBLE);
                playLayout.setVisibility(View.GONE);
            }

            int totalSecond = Integer.parseInt(focusTime.getText().toString().substring(0, 2)) * 60;
            timeLeftInMillis = totalSecond * 1000L;

            progressBar2.setMax(totalSecond);
            progressBar2.setProgress(0);

            mediaPlayer = MediaPlayer.create(this, R.raw.ting);

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
            PopupWindow popupWindow = new PopupWindow(popupView, 700, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            popupWindow.showAsDropDown(view, -630, -100);

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

            TextView btnCancel = focusTimeView.findViewById(R.id.button2);
            TextView btnSave = focusTimeView.findViewById(R.id.button3);

            AlertDialog dialog = builder.create();

            btnCancel.setOnClickListener(v2 -> {
                dialog.dismiss();
            });

            btnSave.setOnClickListener(v2 -> {
                int newTime = seekBar.getProgress() + 5;
                focusTime.setText(String.format("%02d:00", newTime));
                prefFocusTime = newTime;

                dialog.dismiss();
            });

            dialog.show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if the activity was launched from the widget
        if (getIntent() != null && getIntent().hasExtra("fromWidget")) {
            // Auto-start the focus timer
            front.setVisibility(View.GONE);
            behind.setVisibility(View.VISIBLE);
            pauseLayout.setVisibility(View.VISIBLE);
            playLayout.setVisibility(View.GONE);

            int totalSecond = Integer.parseInt(focusTime.getText().toString().substring(0, 2)) * 60;
            timeLeftInMillis = totalSecond * 1000L; // Use the actual time instead of hardcoded 15 seconds

            progressBar2.setMax(totalSecond);
            progressBar2.setProgress(0);

            mediaPlayer = MediaPlayer.create(this, R.raw.ting);
            startTimer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = getSharedPreferences("FocusTabPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("focusTime", prefFocusTime);
        editor.apply();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Focus Time Ended";
            String description = "Notifications for when focus time ends";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("focus_time_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showEndFocusNotification() {
        createNotificationChannel();

        Intent intent = new Intent(this, FocusTab.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "focus_time_channel")
                .setSmallIcon(R.drawable.ticktick_icon)
                .setContentTitle("Focus Time Ended")
                .setContentText("Your focus session has ended. Take a break!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
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
                mediaPlayer.setLooping(true);
                mediaPlayer.start();

                showEndFocusDialog();
                showEndFocusNotification();
            }
        }.start();
    }

    private void showEndFocusDialog() {
        View endFocusView = getLayoutInflater().inflate(R.layout.notification_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(FocusTab.this);
        builder.setView(endFocusView);

        TextView title = endFocusView.findViewById(R.id.title);
        TextView content = endFocusView.findViewById(R.id.content);
        TextView btnConfirm = endFocusView.findViewById(R.id.btnConfirm);

        title.setText("Hoàn thành tập trung");
        content.setText("Hãy nghỉ ngơi một chút trước khi bắt đầu lại!");
        btnConfirm.setText("Ok");

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> {
            stopMusicAndDismiss(dialog);
        });

        btnConfirm.setOnClickListener(v -> {
            stopMusicAndDismiss(dialog);
        });

        dialog.show();
    }

    private void stopMusicAndDismiss(AlertDialog dialog) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        dialog.dismiss();
        btnEnd.performClick();
    }
}
