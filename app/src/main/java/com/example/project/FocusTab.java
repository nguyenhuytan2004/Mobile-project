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

import java.util.Map;
import java.util.Objects;

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
    private String prefWhiteNoise;
    SharedPreferences sharedPreferences;
    Map<String, Integer> sounds = Map.of(
            "clock", R.raw.clock_sound,
            "rain", R.raw.rain_sound
    );

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

        sharedPreferences = getSharedPreferences("FocusTabPrefs", MODE_PRIVATE);
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
            isPaused = false;
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

            startTimer();

            sharedPreferences = getSharedPreferences("whiteNoise", MODE_PRIVATE);
            prefWhiteNoise = sharedPreferences.getString("storage_sound", "none");

            if (!DatabaseHelper.isPremiumUser(FocusTab.this)) {
                prefWhiteNoise = "none";
            }

            if (prefWhiteNoise.equals("none")) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            } else {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(this, sounds.get(prefWhiteNoise));
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                }
            }
        });

        btnEnd.setOnClickListener(view -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            front.setVisibility(View.VISIBLE);
            behind.setVisibility(View.GONE);
        });

        btnPause.setOnClickListener(view -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
                isPaused = true;
            }

            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }

            pauseLayout.setVisibility(View.GONE);
            playLayout.setVisibility(View.VISIBLE);
        });

        btnPlay.setOnClickListener(view -> {
            if (isPaused) {
                startTimer();
                isPaused = false;
            }

            if (mediaPlayer != null) {
                mediaPlayer.start();
            }

            pauseLayout.setVisibility(View.VISIBLE);
            playLayout.setVisibility(View.GONE);
        });

        btnWhiteNoise.setOnClickListener(view -> {
            boolean isPremiumUser = DatabaseHelper.isPremiumUser(FocusTab.this);
            if (!isPremiumUser) {
                startActivity(new Intent(FocusTab.this, PremiumRequestActivity.class));
                return;
            }

            startActivity(new Intent(FocusTab.this, WhiteNoise.class));
        });

        btnOption.setOnClickListener(view -> {
            View popupView = LayoutInflater.from(FocusTab.this).inflate(R.layout.option_in_focus, null);
            PopupWindow popupWindow = new PopupWindow(popupView, 700, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            popupWindow.showAsDropDown(view, -630, -100);

            popupView.findViewById(R.id.focusSetting).setOnClickListener(v -> {
                Toast.makeText(FocusTab.this, getResources().getString(R.string.focus_setting), Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            });

            popupView.findViewById(R.id.addDedicatedRecord).setOnClickListener(v -> {
                Toast.makeText(FocusTab.this, getResources().getString(R.string.focus_add_count_focus_time), Toast.LENGTH_SHORT).show();
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
            seekBar.setProgress(Integer.parseInt(focusTime.getText().toString().substring(0, 2)) - 1);
            pomoTime.setText(String.valueOf(seekBar.getProgress() + 1));

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int value = progress + 1;
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
                int newTime = seekBar.getProgress() + 1;
                focusTime.setText(String.format("%02d:00", newTime));
                prefFocusTime = newTime;

                dialog.dismiss();
            });

            dialog.show();
        });

        if (getIntent().getAction() != null && getIntent().getAction().equals("START_FOCUS")) {
            sharedPreferences = getSharedPreferences("whiteNoise", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("sound", sharedPreferences.getString("storage_sound", "none"));
            editor.apply();

            btnStart.performClick();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        sharedPreferences = getSharedPreferences("whiteNoise", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("sound", sharedPreferences.getString("storage_sound", "none"));
        editor.apply();
        if (mediaPlayer == null)
            isPaused = true;

        if (Objects.equals(intent.getAction(), "START_FOCUS") && countDownTimer == null) {
            btnStart.performClick();
        }
    }

    @Override
    protected void onResume() {
        Log.e("FocusTab", "onResume called");
        super.onResume();
        sharedPreferences = getSharedPreferences("whiteNoise", MODE_PRIVATE);
        if (countDownTimer != null) {
            Log.d("FocusTab", "onResume: countDownTimer is not null");
            if (!DatabaseHelper.isPremiumUser(FocusTab.this)) {
                prefWhiteNoise = "none";
            } else {
                prefWhiteNoise = sharedPreferences.getString("storage_sound", "none");
            }
        } else {
            prefWhiteNoise = sharedPreferences.getString("sound", "none");
        }
        if (prefWhiteNoise.equals("none")) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } else {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(this, sounds.get(prefWhiteNoise));
            mediaPlayer.setLooping(true);
            if (!isPaused) {
                mediaPlayer.start();
            }
        }
    }

    @Override
    protected void onStop() {
        Log.e("FocusTab", "onStop called");
        super.onStop();
        sharedPreferences = getSharedPreferences("FocusTabPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("focusTime", prefFocusTime);
        editor.apply();

        sharedPreferences = getSharedPreferences("whiteNoise", MODE_PRIVATE);
        SharedPreferences.Editor whiteNoiseEditor = sharedPreferences.edit();
        whiteNoiseEditor.remove("sound");
        whiteNoiseEditor.apply();
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
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }

                mediaPlayer = MediaPlayer.create(FocusTab.this, R.raw.ting);
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

        title.setText(getResources().getString(R.string.focus_complete));
        content.setText(getResources().getString(R.string.focus_advice));
        btnConfirm.setText(getResources().getString(R.string.focus_got_it));

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> {
            stopNotificationMusic(dialog);
        });

        btnConfirm.setOnClickListener(v -> {
            stopNotificationMusic(dialog);
        });

        dialog.show();
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
                .setContentTitle(getResources().getString(R.string.focus_complete))
                .setContentText(getResources().getString(R.string.focus_advice))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private void stopNotificationMusic(AlertDialog dialog) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        dialog.dismiss();
        btnEnd.performClick();
    }
}
