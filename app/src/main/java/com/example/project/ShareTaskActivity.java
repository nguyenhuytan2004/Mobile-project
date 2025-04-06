package com.example.project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ShareTaskActivity extends AppCompatActivity {

    private ImageView backBtn;
    private ImageButton ivMailShare;
    private ImageButton ivMessShare;
    private ImageButton ivSMSShare;
    private ImageButton ivMoreShare;

    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_tasks);

        // Initialize views
        ivMailShare = findViewById(R.id.ivMailShare);
        ivMessShare = findViewById(R.id.ivMessShare);
        ivSMSShare = findViewById(R.id.ivSMSShare);
        ivMoreShare = findViewById(R.id.imageButton8);
        backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> finish());

        // Get task list from intent
        Intent intent = getIntent();
        LinearLayout taskContainer = findViewById(R.id.taskContainer);

        ArrayList<Task> taskList = (ArrayList<Task>) intent.getSerializableExtra("task_list");

        if (taskList != null && !taskList.isEmpty()) {
            for (Task t : taskList) {
                View taskView = createTaskView(t);
                taskContainer.addView(taskView);
            }

            // Optional: set task = first task for sharing logic
            task = taskList.get(0);
        }

        // Set up share buttons
        setupShareButtons(taskList);
    }

    private View createTaskView(Task t) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        TextView titleView = new TextView(this);
        titleView.setText("📝 " + t.getTitle());
        titleView.setTextSize(16);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView descView = new TextView(this);
        if (t.getDescription() != null && !t.getDescription().isEmpty()) {
            descView.setText("Mô tả: " + t.getDescription());
        } else {
            descView.setText("Không có mô tả");
        }

        TextView dateView = new TextView(this);
        if (t.hasReminder()) {
            dateView.setText("Ngày đến hạn: " + t.getReminderDate());
        } else {
            dateView.setText("Không có ngày đến hạn");
        }

        TextView appView = new TextView(this);
        appView.setText("Shared from TickTick");

        View separator = new View(this);
        separator.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2));
        separator.setBackgroundColor(Color.LTGRAY);
        separator.setPadding(0, 10, 0, 10);

        layout.addView(titleView);
        layout.addView(descView);
        layout.addView(dateView);
        layout.addView(appView);
        layout.addView(separator);

        return layout;
    }


    private void setupShareButtons(ArrayList<Task> taskList) {
        ivMailShare.setOnClickListener(v -> shareViaEmail(taskList));
        ivMessShare.setOnClickListener(v -> shareViaMessenger(taskList));
        ivSMSShare.setOnClickListener(v -> shareViaSMS(taskList));
        ivMoreShare.setOnClickListener(v -> shareViaOtherApps(taskList));
    }
    private void shareViaEmail(ArrayList<Task> taskList) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Danh sách công việc");
        intent.putExtra(Intent.EXTRA_TEXT, prepareShareText(taskList));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Chia sẻ qua Email"));
        }
    }

    private void shareViaMessenger(ArrayList<Task> taskList) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, prepareShareText(taskList));
        intent.setPackage("com.facebook.orca");

        try {
            startActivity(intent);
        } catch (Exception e) {
            intent.setPackage(null);
            startActivity(Intent.createChooser(intent, "Chia sẻ qua Messenger"));
        }
    }

    private void shareViaSMS(ArrayList<Task> taskList) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("vnd.android-dir/mms-sms");
        intent.putExtra(Intent.EXTRA_TEXT, prepareShareText(taskList));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, "Chia sẻ qua SMS"));
        }
    }

    private void shareViaOtherApps(ArrayList<Task> taskList) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Danh sách công việc");
        intent.putExtra(Intent.EXTRA_TEXT, prepareShareText(taskList));
        startActivity(Intent.createChooser(intent, "Chia sẻ công việc"));
    }
    private String prepareShareText(ArrayList<Task> taskList) {
        StringBuilder sb = new StringBuilder();
        int index = 1;

        for (Task t : taskList) {
            sb.append("📝 Task ").append(index++).append(": ").append(t.getTitle()).append("\n");

            if (t.getDescription() != null && !t.getDescription().isEmpty()) {
                sb.append("Mô tả: ").append(t.getDescription()).append("\n");
            }

            if (t.hasReminder()) {
                sb.append("Hạn: ").append(t.getReminderDate()).append("\n");
            }

            sb.append("\n");
        }

        sb.append("🔗 Shared from TickTick");
        return sb.toString();
    }

}