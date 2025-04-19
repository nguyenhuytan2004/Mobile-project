package com.example.project;

import com.example.project.Task;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

public class HomeActivity extends AppCompatActivity implements SideBarHelper.SideBarCallback, SideBarHelper.TaskProvider {

    public interface TaskCompletionListener {
        void onTaskCompletionChanged(Task task, boolean isCompleted);
    }

    private FloatingActionButton fabAdd;
    ImageView focusTab, calendarTab, sideBarView, matrixView, habitTab, ivMoreHome;
    private SQLiteDatabase db;
    private LoginSessionManager loginSessionManager;

    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    TextView tvWelcome;
    private LinearLayout categoryContainer;
    private int currentListId = 3; // Default to Welcome list

    private EditText searchBar;
    private String searchKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home); // Layout chính sau đăng nhập

        loginSessionManager = LoginSessionManager.getInstance(this);
        /*if (!loginSessionManager.isLoggedIn()) {
            loginSessionManager.createSession(1); // tạo giả user
        }*/
        Log.d("HomeActivity", "User ID: " + loginSessionManager.getUserId());

        ReminderService.scheduleReminders(this);

        // Ánh xạ UI
        fabAdd = findViewById(R.id.fab_add);
        focusTab = findViewById(R.id.focusTab);
        calendarTab = findViewById(R.id.calendarTab);
        sideBarView = findViewById(R.id.sidebarView);
        matrixView = findViewById(R.id.matrixView);
        habitTab = findViewById(R.id.habitTab);
        ivMoreHome = findViewById(R.id.ivMoreHome);

        tvWelcome = findViewById(R.id.tv_welcome);
        categoryContainer = findViewById(R.id.category_container);

        searchBar = findViewById(R.id.search_bar);


        // Check for list ID from intent
        if (getIntent().hasExtra("listId")) {
            currentListId = getIntent().getIntExtra("listId", 3);
            String listName = getIntent().getStringExtra("listName");
            if (listName != null) {
                tvWelcome.setText("📋 " + listName);
            }
        } else {
            tvWelcome.setText("📋 Welcome");
        }

        loadCategoriesAndTasks(currentListId);

        // FAB
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, NoteActivity.class);
            intent.putExtra("noteId", "-1");
            intent.putExtra("listId", String.valueOf(currentListId));
            startActivity(intent);
        });

        // Sidebar
        sideBarView.setOnClickListener(v -> {
            // Show sidebar with lists from database
            SideBarHelper.showSideBar(this, this, this);
        });

        // Set up the more options menu
        ivMoreHome.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            final String removeString = getResources().getString(R.string.note_delete);
            final String shareString = getResources().getString(R.string.home_share);
            popupMenu.getMenu().add(removeString);
            popupMenu.getMenu().add(shareString);

            popupMenu.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();

                if (title.equals(removeString)) {
                    // Show confirmation dialog
                    showDeleteConfirmationDialog();
                    return true;
                } else if (title.equals(shareString)) {
                    // Check premium status before sharing
                    checkPremiumAndShare();
                    return true;
                } else {
                    return false;
                }
            });

            popupMenu.show();
        });

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                    searchKeyword = searchBar.getText().toString().trim();

                    Log.d("SearchInput", "Search keyword: " + searchKeyword);

                    loadCategoriesAndTasks(currentListId); // Gọi lại hàm load dữ liệu, với filter mới

                    // Ẩn bàn phím
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }

                    return true;
                }
                return false;
            }
        });

        // Tab
        focusTab.setOnClickListener(v -> startActivity(new Intent(this, FocusTab.class)));
        calendarTab.setOnClickListener(v -> startActivity(new Intent(this, CalendarTab.class)));
        matrixView.setOnClickListener(v -> startActivity(new Intent(this, Matrix_Eisenhower.class)));
        habitTab.setOnClickListener(v -> startActivity(new Intent(this, HabitActivity.class)));
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.home_delete_title));
        builder.setMessage(getResources().getString(R.string.home_delete_message));
        
        builder.setPositiveButton(getResources().getString(R.string.note_delete), (dialog, which) -> {
            // Only delete if not the default Welcome list (id 3)
            if (currentListId != 2 && currentListId != 1) {
                deleteCurrentList();
            } else {
                Toast.makeText(this, getResources().getString(R.string.home_cant_delete), Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        
        builder.show();
    }

    private void deleteCurrentList() {
        try {
            db = DatabaseHelper.getInstance(this).openDatabase();
            
            // First get the list name for notification
            String listName = "";
            Cursor cursor = db.rawQuery(
                    "SELECT name FROM tbl_list WHERE id = ?",
                    new String[]{String.valueOf(currentListId)}
            );
            
            if (cursor.moveToFirst()) {
                listName = cursor.getString(0);
            }
            cursor.close();
            
            // Delete the list - this should cascade and delete related categories and tasks
            db.execSQL("DELETE FROM tbl_list WHERE id = ?", new String[]{String.valueOf(currentListId)});
            
            Toast.makeText(this, getResources().getString(R.string.home_delete_success) + listName, Toast.LENGTH_SHORT).show();
            
            // Navigate back to the Welcome list
            currentListId = 3;
            loadCategoriesAndTasks(currentListId);
            tvWelcome.setText("👋 Welcome");
            
        } catch (Exception e) {
            Log.e("HomeActivity", "Error deleting list", e);
        } finally {
            if (db != null) {
                DatabaseHelper.getInstance(this).closeDatabase();
            }
        }
    }

    private void shareCurrentList() {
        // Get list name
        String listName = tvWelcome.getText().toString().replace("📋 ", "");
        
        try {
            db = DatabaseHelper.getInstance(this).openDatabase();
            
            ArrayList<Task> tasksToShare = new ArrayList<>();
            
            Cursor categoryCursor = db.rawQuery(
                    "SELECT c.id, c.name FROM tbl_category c WHERE c.list_id = ?",
                    new String[]{String.valueOf(currentListId)}
            );
            
            int userId = loginSessionManager.getUserId();
            
            while (categoryCursor.moveToNext()) {
                int categoryId = categoryCursor.getInt(0);
                String categoryName = categoryCursor.getString(1);
                
                // 1. Get notes from this category and convert to Task objects
                Cursor noteCursor = db.rawQuery(
                        "SELECT n.id, n.title, n.content, r.date " +
                        "FROM tbl_note n " +
                        "LEFT JOIN tbl_note_reminder r ON n.id = r.note_id " +
                        "WHERE n.user_id = ? AND n.category_id = ?",
                        new String[]{String.valueOf(userId), String.valueOf(categoryId)}
                );
                
                while (noteCursor.moveToNext()) {
                    int noteId = noteCursor.getInt(0);
                    String title = noteCursor.getString(1);
                    String content = noteCursor.isNull(2) ? "" : noteCursor.getString(2);
                    String date = noteCursor.isNull(3) ? null : noteCursor.getString(3);
                    
                    // Create a Task object and set type to "note"
                    Task task = new Task(noteId, title, categoryId);
                    task.setDescription(content);
                    if (date != null) {
                        task.setReminderDate(date);
                    }
                    task.setType("note"); // Add a type marker
                    
                    tasksToShare.add(task);
                }
                noteCursor.close();
                
                // 2. Get actual tasks from this category - use proper join with task_reminder
                Cursor taskCursor = db.rawQuery(
                        "SELECT t.id, t.title, t.content, t.priority, r.date, " +
                        "t.is_completed " +
                        "FROM tbl_task t " +
                        "LEFT JOIN tbl_task_reminder r ON t.id = r.task_id " +
                        "WHERE t.category_id = ?",
                        new String[]{String.valueOf(categoryId)}
                );
                
                while (taskCursor.moveToNext()) {
                    int taskId = taskCursor.getInt(0);
                    String title = taskCursor.getString(1);
                    String content = taskCursor.isNull(2) ? "" : taskCursor.getString(2);
                    int priority = taskCursor.getInt(3);
                    String reminderDate = taskCursor.isNull(4) ? null : taskCursor.getString(4);
                    boolean isCompleted = taskCursor.getInt(5) > 0;
                    
                    // Create a Task object
                    Task task = new Task(taskId, title, categoryId);
                    task.setDescription(content);
                    task.setPriority(priority);
                    task.setCompleted(isCompleted);
                    if (reminderDate != null) {
                        task.setReminderDate(reminderDate);
                    }
                    task.setType("task"); // Add a type marker
                    
                    tasksToShare.add(task);
                }
                taskCursor.close();
            }
            
            categoryCursor.close();
            
            // Launch ShareTaskActivity with the tasks
            if (!tasksToShare.isEmpty()) {
                Intent shareIntent = new Intent(this, ShareTaskActivity.class);
                shareIntent.putExtra("task_list", tasksToShare);
                shareIntent.putExtra("list_name", listName);
                startActivity(shareIntent);
            } else {
                Toast.makeText(this, getResources().getString(R.string.home_share_none), Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Log.e("HomeActivity", "Error preparing tasks for sharing", e);
        } finally {
            if (db != null) {
                DatabaseHelper.getInstance(this).closeDatabase();
            }
        }
    }

    // Add this method after shareCurrentList()
    private void checkPremiumAndShare() {
        // Check if user is premium
        if (DatabaseHelper.isPremiumUser(this)) {
            // User is premium, proceed with sharing
            Log.d("HomeActivity", "User is premium, proceeding with sharing");
            shareCurrentList();
        } else {
            // User is not premium, show upgrade screen
            Intent premiumIntent = new Intent(this, PremiumRequestActivity.class);
            startActivity(premiumIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategoriesAndTasks(currentListId);
        SideBarHelper.markProfileForRefresh();
    }

    // SideBarCallback implementation
    @Override
    public void onTaskCategorySelected(String category) {
        // This will be handled in SideBarHelper
    }

    // TaskProvider implementation
    @Override
    public List<Task> getAllTasks() {
        // Implement if needed to provide tasks to the sidebar
        return new ArrayList<>();

    }

    private void loadCategoriesAndTasks(int listId) {
        int userId = loginSessionManager.getUserId();
        categoryContainer.removeAllViews(); // clear UI trước

        try {
            db = DatabaseHelper.getInstance(this).openDatabase();


            // Special handling for "Hôm nay" list (id = 2)
            if (listId == 2) {
                // Get today's date in different formats
                LocalDate today = LocalDate.now();
                String todayFormatted = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                String todayPattern = "Ngày " + today.getDayOfMonth() + ", tháng " + today.getMonthValue();

                Log.d("HomeActivity", "Today's date formats: " + todayFormatted + " and " + todayPattern);

                // Create a special category view for today's items
                View categoryView = LayoutInflater.from(this).inflate(R.layout.item_category_block, categoryContainer, false);
                TextView categoryTitle = categoryView.findViewById(R.id.category_title);
                LinearLayout taskListLayout = categoryView.findViewById(R.id.task_list_layout);

                categoryTitle.setText("Hôm nay: " + todayFormatted);

                // Count total tasks and notes in database for debugging
                Cursor countCursor = db.rawQuery("SELECT COUNT(*) FROM tbl_note", null);
                if (countCursor.moveToFirst()) {
                    Log.d("HomeActivity", "Total notes in database: " + countCursor.getInt(0));
                }
                countCursor.close();

                countCursor = db.rawQuery("SELECT COUNT(*) FROM tbl_task", null);
                if (countCursor.moveToFirst()) {
                    Log.d("HomeActivity", "Total tasks in database: " + countCursor.getInt(0));
                }
                countCursor.close();

                String noteQuery = "SELECT n.id, n.title, n.content, r.date, c.name " +
                        "FROM tbl_note n " +
                        "JOIN tbl_category c ON n.category_id = c.id " +
                        "LEFT JOIN tbl_note_reminder r ON n.id = r.note_id " +
                        "WHERE n.user_id = ? AND (r.date LIKE ? OR r.date LIKE ? OR r.date LIKE ?)";

                if (!searchKeyword.isEmpty()) {
                    noteQuery += " AND (n.title LIKE ? OR n.content LIKE ?)";
                }

                List<String> noteArgs = new ArrayList<>();
                noteArgs.add(String.valueOf(userId));
                noteArgs.add(todayFormatted + "%");
                noteArgs.add("%" + todayFormatted + "%");
                noteArgs.add("%" + todayPattern + "%");

                if (!searchKeyword.isEmpty()) {
                    noteArgs.add("%" + searchKeyword + "%");
                    noteArgs.add("%" + searchKeyword + "%");
                }

                Cursor noteCursor = db.rawQuery(noteQuery, noteArgs.toArray(new String[0]));

                Log.d("HomeActivity", "Found " + noteCursor.getCount() + " notes for today");

                while (noteCursor.moveToNext()) {
                    int noteId = noteCursor.getInt(0);
                    String title = noteCursor.getString(1);
                    String content = noteCursor.isNull(2) ? "" : noteCursor.getString(2);
                    String date = noteCursor.isNull(3) ? "" : noteCursor.getString(3);
                    String categoryName = noteCursor.getString(4);

                    // Add category name to the title for clarity
                    title = "📝 [" + categoryName + "] " + title;

                    Log.d("HomeActivity", "Adding note: " + title + " with date: " + date);

                    // Use existing addNoteView function
                    addNoteView(taskListLayout, 0, noteId, title, content, date);
                }
                noteCursor.close();

                String taskQuery = "SELECT t.id, t.title, t.content, t.priority, r.date, " +
                        "t.is_completed, c.name " +
                        "FROM tbl_task t " +
                        "JOIN tbl_category c ON t.category_id = c.id " +
                        "LEFT JOIN tbl_task_reminder r ON t.id = r.task_id " +
                        "WHERE (r.date LIKE ? OR r.date LIKE ? OR r.date LIKE ?)";

                if (!searchKeyword.isEmpty()) {
                    taskQuery += " AND (t.title LIKE ? OR t.content LIKE ?)";
                }

                List<String> argsList = new ArrayList<>();
                argsList.add(todayFormatted + "%");
                argsList.add("%" + todayFormatted + "%");
                argsList.add("%" + todayPattern + "%");

                if (!searchKeyword.isEmpty()) {
                    argsList.add("%" + searchKeyword + "%");
                    argsList.add("%" + searchKeyword + "%");
                }
                Cursor taskCursor = db.rawQuery(taskQuery, argsList.toArray(new String[0]));

                Log.d("HomeActivity", "Found " + taskCursor.getCount() + " tasks for today");

                while (taskCursor.moveToNext()) {
                    int taskId = taskCursor.getInt(0);
                    String title = taskCursor.getString(1);
                    String description = taskCursor.isNull(2) ? "" : taskCursor.getString(2);
                    int priority = taskCursor.getInt(3);
                    String reminderDate = taskCursor.isNull(4) ? "" : taskCursor.getString(4);
                    boolean isCompleted = taskCursor.getInt(5) > 0;
                    String categoryName = taskCursor.getString(6);

                    // Add category name to the title for clarity
                    title = "✓ [" + categoryName + "] " + title;

                    Log.d("HomeActivity", "Adding task: " + title + " with date: " + reminderDate);

                    addTaskView(taskListLayout, listId,-1, taskId, title, description, priority, reminderDate, isCompleted);
                }
                taskCursor.close();

                // Add "No tasks for today" message if both cursors were empty
                if (taskListLayout.getChildCount() == 0) {
                    TextView emptyView = new TextView(this);
                    emptyView.setText("Không có công việc hoặc ghi chú nào cho hôm nay");
                    emptyView.setTextColor(Color.WHITE);
                    emptyView.setPadding(16, 16, 16, 16);
                    taskListLayout.addView(emptyView);
                }

                // Add the category view to the container
                categoryContainer.addView(categoryView);
            }
            // Normal list handling 
            else {
                Cursor categoryCursor = db.rawQuery(
                        "SELECT c.id, c.name FROM tbl_category c WHERE c.list_id = ?",
                        new String[]{String.valueOf(listId)}
                );

                while (categoryCursor.moveToNext()) {
                    int categoryId = categoryCursor.getInt(0);
                    String categoryName = categoryCursor.getString(1);

                    View categoryView = LayoutInflater.from(this).inflate(R.layout.item_category_block, categoryContainer, false);
                    TextView categoryTitle = categoryView.findViewById(R.id.category_title);
                    LinearLayout taskListLayout = categoryView.findViewById(R.id.task_list_layout);

                    categoryTitle.setText(categoryName);

                    // --- Query Note ---
                    String noteQuery = "SELECT n.id, n.title, n.content, r.date " +
                            "FROM tbl_note n " +
                            "LEFT JOIN tbl_note_reminder r ON n.id = r.note_id " +
                            "WHERE n.user_id = ? AND n.category_id = ?";

                    List<String> noteArgs = new ArrayList<>();
                    noteArgs.add(String.valueOf(userId));
                    noteArgs.add(String.valueOf(categoryId));

                    if (!searchKeyword.isEmpty()) {
                        noteQuery += " AND (n.title LIKE ? OR n.content LIKE ?)";
                        noteArgs.add("%" + searchKeyword + "%");
                        noteArgs.add("%" + searchKeyword + "%");
                    }

                    Cursor noteCursor = db.rawQuery(noteQuery, noteArgs.toArray(new String[0]));

                    while (noteCursor.moveToNext()) {
                        int noteId = noteCursor.getInt(0);
                        String title = noteCursor.getString(1);
                        String content = noteCursor.isNull(2) ? "" : noteCursor.getString(2);
                        String date = noteCursor.isNull(3) ? "" : noteCursor.getString(3);

                        addNoteView(taskListLayout, categoryId, noteId, title, content, date);
                    }
                    noteCursor.close();

                    // --- Query Task chưa hoàn thành ---
                    String taskQuery = "SELECT t.id, t.title, t.content, t.priority, " +
                            "r.date, t.is_completed " +
                            "FROM tbl_task t " +
                            "LEFT JOIN tbl_task_reminder r ON t.id = r.task_id " +
                            "WHERE t.category_id = ? AND t.is_completed = 0";

                    List<String> taskArgs = new ArrayList<>();
                    taskArgs.add(String.valueOf(categoryId));

                    if (!searchKeyword.isEmpty()) {
                        taskQuery += " AND (t.title LIKE ? OR t.content LIKE ?)";
                        taskArgs.add("%" + searchKeyword + "%");
                        taskArgs.add("%" + searchKeyword + "%");
                    }

                    Cursor taskCursor = db.rawQuery(taskQuery, taskArgs.toArray(new String[0]));

                    while (taskCursor.moveToNext()) {
                        int taskId = taskCursor.getInt(0);
                        String title = taskCursor.getString(1);
                        String content = taskCursor.isNull(2) ? "" : taskCursor.getString(2);
                        int priority = taskCursor.getInt(3);
                        String reminderDate = taskCursor.isNull(4) ? "" : taskCursor.getString(4);
                        boolean isCompleted = taskCursor.getInt(5) > 0;

                        addTaskView(taskListLayout,listId,categoryId, taskId, title, content, priority, reminderDate, isCompleted);
                    }
                    taskCursor.close();

                    // Nếu không có note/task thì thêm dòng "Không có nội dung"
                    if (taskListLayout.getChildCount() == 0) {
                        TextView emptyView = new TextView(this);
                        emptyView.setText(getResources().getString(R.string.home_none));
                        emptyView.setTextColor(Color.WHITE);
                        emptyView.setPadding(16, 16, 16, 16);
                        taskListLayout.addView(emptyView);
                    }

                    categoryContainer.addView(categoryView);
                }

                // ✅ Thêm category "Đã hoàn thành"
                View completedView = LayoutInflater.from(this).inflate(R.layout.item_category_block, categoryContainer, false);
                TextView completedTitle = completedView.findViewById(R.id.category_title);
                LinearLayout completedTaskLayout = completedView.findViewById(R.id.task_list_layout);

                completedTitle.setText(getResources().getString(R.string.home_complete));

                // --- Query các task đã hoàn thành ---
                String completedQuery = "SELECT t.id, t.title, t.content, t.priority, " +
                        "r.date, t.is_completed " +
                        "FROM tbl_task t " +
                        "JOIN tbl_category c ON t.category_id = c.id " +
                        "LEFT JOIN tbl_task_reminder r ON t.id = r.task_id " +
                        "WHERE c.list_id = ? AND t.user_id = ? AND t.is_completed = 1";

                List<String> completedArgs = new ArrayList<>();
                completedArgs.add(String.valueOf(listId));
                completedArgs.add(String.valueOf(userId));

                if (!searchKeyword.isEmpty()) {
                    completedQuery += " AND (t.title LIKE ? OR t.content LIKE ?)";
                    completedArgs.add("%" + searchKeyword + "%");
                    completedArgs.add("%" + searchKeyword + "%");
                }

                Cursor completedCursor = db.rawQuery(completedQuery, completedArgs.toArray(new String[0]));

                while (completedCursor.moveToNext()) {
                    int taskId = completedCursor.getInt(0);
                    String title = completedCursor.getString(1);
                    String content = completedCursor.isNull(2) ? "" : completedCursor.getString(2);
                    int priority = completedCursor.getInt(3);
                    String reminderDate = completedCursor.isNull(4) ? "" : completedCursor.getString(4);
                    boolean isCompleted = completedCursor.getInt(5) > 0;

                    addTaskView(completedTaskLayout,listId,-1, taskId, title, content, priority, reminderDate, isCompleted);
                }

                completedCursor.close();

                if (completedTaskLayout.getChildCount() == 0) {
                    TextView emptyView = new TextView(this);
                    emptyView.setText(getResources().getString(R.string.home_complete_none));
                    emptyView.setTextColor(Color.WHITE);
                    emptyView.setPadding(16, 16, 16, 16);
                    completedTaskLayout.addView(emptyView);
                }

                categoryContainer.addView(completedView);

                categoryCursor.close();
        }

        } catch (Exception e) {
            Log.e("HomeActivity", "Lỗi load category/note: " + e.getMessage(), e);
        } finally {
            if (db != null) DatabaseHelper.getInstance(this).closeDatabase();
        }
    }


    private void addNoteView(LinearLayout targetLayout, int categoryId, int noteId, String title, String content, String date) {
        View noteView = LayoutInflater.from(this).inflate(R.layout.note_item_in_main, targetLayout, false);
        TextView titleTextView = noteView.findViewById(R.id.note_title);
        TextView contentTextView = noteView.findViewById(R.id.note_content);
        TextView dateTextView = noteView.findViewById(R.id.note_date);

        titleTextView.setText(title);

        if (content.isEmpty()) {
            contentTextView.setVisibility(View.GONE);
        } else {
            int numLines = content.split("\n").length;
            if (content.length() > 33) {
                content = content.substring(0, 33) + "...";
            } else if (numLines > 2) {
                content = content.split("\n")[0] + "\n" + content.split("\n")[1] + "...";
            }
            contentTextView.setText(content);
        }

        if (date.isEmpty()) {
            dateTextView.setVisibility(View.GONE);
        } else {
            dateTextView.setText(date);
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(date);
            int day = matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
            int month = matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;

            LocalDate noteDate = LocalDate.of(LocalDate.now().getYear(), month, day);
            dateTextView.setTextColor(getResources().getColor(noteDate.isBefore(LocalDate.now()) ?
                    R.color.red : R.color.statistics_blue));
        }

        noteView.setOnClickListener(v -> {
            Intent intent = new Intent(this, NoteActivity.class);
            intent.putExtra("noteId", String.valueOf(noteId));
            intent.putExtra("categoryId", String.valueOf(categoryId));
            intent.putExtra("listId", String.valueOf(currentListId));
            startActivity(intent);
        });

        targetLayout.addView(noteView);
    }

    private void addTaskView(LinearLayout targetLayout,int listId,int categoryId, int taskId, String title, String content,
                             int priority, String reminderDate, boolean isCompleted) {

        View taskView = LayoutInflater.from(this).inflate(R.layout.task_item_in_main, targetLayout, false);

        TextView titleTextView = taskView.findViewById(R.id.task_title);
        TextView contentTextView = taskView.findViewById(R.id.task_content);
        TextView dateTextView = taskView.findViewById(R.id.task_date);
        CheckBox taskCheckBox = taskView.findViewById(R.id.taskCheckBox);

        titleTextView.setText(title);
        taskCheckBox.setChecked(isCompleted);

        taskCheckBox.setButtonDrawable(isCompleted
                ? R.drawable.ic_checkbox_complete
                : R.drawable.ic_checkbox_notcompleted);


        if (isCompleted) {
            titleTextView.setPaintFlags(titleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            titleTextView.setTextColor(getResources().getColor(R.color.gray));         // màu xám
            contentTextView.setTextColor(getResources().getColor(R.color.gray));       // màu xám
            dateTextView.setTextColor(getResources().getColor(R.color.gray));          // màu xám
            taskCheckBox.setAlpha(0.6f);                                               // làm mờ
            titleTextView.setAlpha(0.6f);
            contentTextView.setAlpha(0.6f);
            dateTextView.setAlpha(0.6f);
        }

        // Set nội dung
        if (content.isEmpty()) {
            contentTextView.setVisibility(View.GONE);
        } else {
            int numLines = content.split("\n").length;
            if (content.length() > 33) {
                content = content.substring(0, 33) + "...";
            } else if (numLines > 2) {
                content = content.split("\n")[0] + "\n" + content.split("\n")[1] + "...";
            }
            contentTextView.setText(content);
        }

        // Ngày nhắc
        if (reminderDate.isEmpty()) {
            dateTextView.setVisibility(View.GONE);
        } else {
            dateTextView.setText(reminderDate);

            try {
                LocalDate taskDate = LocalDate.parse(reminderDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                if (!isCompleted) {
                    dateTextView.setTextColor(getResources().getColor(
                            taskDate.isBefore(LocalDate.now()) ? R.color.red : R.color.statistics_blue));
                }
            } catch (Exception e) {
                if (!isCompleted) {
                    dateTextView.setTextColor(getResources().getColor(R.color.statistics_blue));
                }
            }
        }

        // Mở TaskActivity khi bấm vào task
        taskView.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskActivity.class);
            intent.putExtra("taskId", String.valueOf(taskId));
            intent.putExtra("listId", String.valueOf(listId));
            Log.d("TaskViewClick", "Clicked taskId: " + taskId + ", listId: " + listId);

            if(categoryId!=-1){
                intent.putExtra("categoryId", categoryId);
            }
            startActivity(intent);
        });

        // Khi người dùng check/uncheck
        taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            db = DatabaseHelper.getInstance(this).openDatabase();

            String updateQuery = "UPDATE tbl_task SET is_completed = ? WHERE id = ?";
            Object[] args = new Object[]{ isChecked ? 1 : 0, taskId };
            db.execSQL(updateQuery, args);

            // Load lại để cập nhật giao diện toàn diện
            loadCategoriesAndTasks(currentListId);
        });

        // Add vào layout chính
        targetLayout.addView(taskView);
    }

}
