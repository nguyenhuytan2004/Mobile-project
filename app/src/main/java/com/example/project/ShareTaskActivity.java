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
        // Show different icon based on type
        String icon = t.isNote() ? "📝 " : "✓ ";
        titleView.setText(icon + t.getTitle());
        titleView.setTextSize(16);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);

        // If task has priority, show it
        if (!t.isNote() && t.getPriority() > 0) {
            String priorityText;
            switch (t.getPriority()) {
                case 1: priorityText = "Ưu tiên: Cao (Quan trọng & Khẩn cấp)"; break;
                case 2: priorityText = "Ưu tiên: Trung bình (Quan trọng, Không khẩn cấp)"; break;
                case 3: priorityText = "Ưu tiên: Thấp (Không quan trọng, Khẩn cấp)"; break;
                default: priorityText = "Ưu tiên: Rất thấp (Không quan trọng & Không khẩn cấp)"; break;
            }
            
            TextView priorityView = new TextView(this);
            priorityView.setText(priorityText);
            layout.addView(priorityView);
        }

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
        
        // Show completion status for tasks
        if (!t.isNote()) {
            TextView statusView = new TextView(this);
            statusView.setText("Trạng thái: " + (t.isCompleted() ? "Đã hoàn thành" : "Chưa hoàn thành"));
            layout.addView(statusView);
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
        int noteIndex = 1;
        int taskIndex = 1;

        // First add notes
        sb.append("===== GHI CHÚ =====\n\n");
        boolean hasNotes = false;
        for (Task t : taskList) {
            if (t.isNote()) {
                hasNotes = true;
                sb.append("📝 Ghi chú ").append(noteIndex++).append(": ").append(t.getTitle()).append("\n");

                if (t.getDescription() != null && !t.getDescription().isEmpty()) {
                    sb.append("Nội dung: ").append(t.getDescription()).append("\n");
                }

                if (t.hasReminder()) {
                    sb.append("Hạn: ").append(t.getReminderDate()).append("\n");
                }

                sb.append("\n");
            }
        }
        
        if (!hasNotes) {
            sb.append("Không có ghi chú\n\n");
        }

        // Then add tasks
        sb.append("===== CÔNG VIỆC =====\n\n");
        boolean hasTasks = false;
        for (Task t : taskList) {
            if (!t.isNote()) {
                hasTasks = true;
                String status = t.isCompleted() ? "✓ " : "□ ";
                sb.append(status).append("Công việc ").append(taskIndex++).append(": ").append(t.getTitle()).append("\n");

                String priorityText;
                switch (t.getPriority()) {
                    case 1: priorityText = "Cao"; break;
                    case 2: priorityText = "Trung bình"; break;
                    case 3: priorityText = "Thấp"; break;
                    default: priorityText = "Rất thấp"; break;
                }
                sb.append("Ưu tiên: ").append(priorityText).append("\n");

                if (t.getDescription() != null && !t.getDescription().isEmpty()) {
                    sb.append("Mô tả: ").append(t.getDescription()).append("\n");
                }

                if (t.hasReminder()) {
                    sb.append("Hạn: ").append(t.getReminderDate()).append("\n");
                }
                
                sb.append("Trạng thái: ").append(t.isCompleted() ? "Đã hoàn thành" : "Chưa hoàn thành").append("\n");

                sb.append("\n");
            }
        }
        
        if (!hasTasks) {
            sb.append("Không có công việc\n\n");
        }

        sb.append("🔗 Shared from TickTick");
        return sb.toString();
    }

}