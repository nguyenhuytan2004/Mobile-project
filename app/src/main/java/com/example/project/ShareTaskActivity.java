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
                case 1: priorityText = getString(R.string.priority_high); break;
                case 2: priorityText = getString(R.string.priority_medium); break;
                case 3: priorityText = getString(R.string.priority_low); break;
                default: priorityText = getString(R.string.priority_lowest); break;
            }
            
            TextView priorityView = new TextView(this);
            priorityView.setText(priorityText);
            layout.addView(priorityView);
        }

        TextView descView = new TextView(this);
        if (t.getDescription() != null && !t.getDescription().isEmpty()) {
            descView.setText(getString(R.string.description_prefix) + t.getDescription());
        } else {
            descView.setText(getString(R.string.no_description));
        }

        TextView dateView = new TextView(this);
        if (t.hasReminder()) {
            dateView.setText(getString(R.string.due_date_prefix) + t.getReminderDate());
        } else {
            dateView.setText(getString(R.string.no_due_date));
        }
        
        // Show completion status for tasks
        if (!t.isNote()) {
            TextView statusView = new TextView(this);
            statusView.setText(getString(R.string.status_prefix) + 
                (t.isCompleted() ? getString(R.string.status_completed) : getString(R.string.status_incomplete)));
            layout.addView(statusView);
        }

        TextView appView = new TextView(this);
        appView.setText(getString(R.string.share_from));

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
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_task_list_subject));
        intent.putExtra(Intent.EXTRA_TEXT, prepareShareText(taskList));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, getString(R.string.share_via_email)));
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
            startActivity(Intent.createChooser(intent, getString(R.string.share_via_messenger)));
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
            startActivity(Intent.createChooser(intent, getString(R.string.share_via_sms)));
        }
    }

    private void shareViaOtherApps(ArrayList<Task> taskList) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_task_list_subject));
        intent.putExtra(Intent.EXTRA_TEXT, prepareShareText(taskList));
        startActivity(Intent.createChooser(intent, getString(R.string.share_tasks)));
    }
    
    private String prepareShareText(ArrayList<Task> taskList) {
        StringBuilder sb = new StringBuilder();
        int noteIndex = 1;
        int taskIndex = 1;

        // First add notes
        sb.append(getString(R.string.notes_header)).append("\n\n");
        boolean hasNotes = false;
        for (Task t : taskList) {
            if (t.isNote()) {
                hasNotes = true;
                sb.append("📝 ").append(getString(R.string.note_prefix)).append(noteIndex++).append(": ").append(t.getTitle()).append("\n");

                if (t.getDescription() != null && !t.getDescription().isEmpty()) {
                    sb.append(getString(R.string.content_prefix)).append(t.getDescription()).append("\n");
                }

                if (t.hasReminder()) {
                    sb.append(getString(R.string.due_prefix)).append(t.getReminderDate()).append("\n");
                }

                sb.append("\n");
            }
        }
        
        if (!hasNotes) {
            sb.append(getString(R.string.no_notes)).append("\n\n");
        }

        // Then add tasks
        sb.append(getString(R.string.tasks_header)).append("\n\n");
        boolean hasTasks = false;
        for (Task t : taskList) {
            if (!t.isNote()) {
                hasTasks = true;
                String status = t.isCompleted() ? "✓ " : "□ ";
                sb.append(status).append(getString(R.string.task_prefix)).append(taskIndex++).append(": ").append(t.getTitle()).append("\n");

                String priorityText;
                switch (t.getPriority()) {
                    case 1: priorityText = getString(R.string.priority_text_high); break;
                    case 2: priorityText = getString(R.string.priority_text_medium); break;
                    case 3: priorityText = getString(R.string.priority_text_low); break;
                    default: priorityText = getString(R.string.priority_text_lowest); break;
                }
                sb.append(getString(R.string.priority_prefix)).append(priorityText).append("\n");

                if (t.getDescription() != null && !t.getDescription().isEmpty()) {
                    sb.append(getString(R.string.description_prefix)).append(t.getDescription()).append("\n");
                }

                if (t.hasReminder()) {
                    sb.append(getString(R.string.due_prefix)).append(t.getReminderDate()).append("\n");
                }
                
                sb.append(getString(R.string.status_prefix))
                  .append(t.isCompleted() ? getString(R.string.status_completed) : getString(R.string.status_incomplete))
                  .append("\n");

                sb.append("\n");
            }
        }
        
        if (!hasTasks) {
            sb.append(getString(R.string.no_tasks)).append("\n\n");
        }

        sb.append("🔗 ").append(getString(R.string.share_from));
        return sb.toString();
    }
}