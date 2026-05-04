package taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import taskmanager.api.SchedulePlanner;
import taskmanager.api.TaskManager;
import taskmanager.exception.TaskNotFoundException;
import taskmanager.impl.DefaultTaskManager;
import taskmanager.impl.DefaultTaskService;
import taskmanager.model.ScheduleRecommendation;
import taskmanager.model.Task;

/**
 * Main Swing GUI for the Smart Task Manager.
 *
 * <p>Features:
 * <ul>
 *   <li>Add, view, and delete tasks</li>
 *   <li>Update weather status for a selected task</li>
 *   <li>Suggest weather-aware schedule for all tasks</li>
 *   <li>Visual feedback for errors and warnings</li>
 * </ul>
 *
 * <p>All reactive operations run on background threads (Schedulers.boundedElastic()).
 * UI updates always happen on the Event Dispatch Thread via SwingUtilities.invokeLater().
 */
public class SmartTaskManagerFrame extends JFrame {
/** The manager responsible for task logic. */
    private final TaskManager taskManager;

    /** The service used to manage task data operations. */
    private final DefaultTaskService taskService;

    /** The planner responsible for weather-based scheduling. */
    private final SchedulePlanner schedulePlanner;

/** The table component displaying the list of tasks. */
    private final JTable taskTable;

/** The table model containing the underlying task data. */    
private final DefaultTableModel tableModel;

    /** Label used to display system status updates to the user. */
    private final JLabel statusLabel;
    
    /** Field for entering the city name. */
    private final JTextField cityField = new JTextField("Jeddah", 12);

    private static final String[] COLUMNS = {"ID", "Title", "Due Date/Time", "Weather Sensitive", "Status"};
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Constructs the main application frame.
     *
     * @param taskManager the configured TaskManager instance
     */
    public SmartTaskManagerFrame(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.taskService = ((DefaultTaskManager) taskManager).getTaskService();
        this.schedulePlanner = taskManager.getPlanner();

        setTitle("Smart Task Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 480);
        setLocationRelativeTo(null);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        taskTable = new JTable(tableModel);
        taskTable.setRowHeight(24);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Buttons
        JButton addBtn      = new JButton("Add Task");
        JButton deleteBtn   = new JButton("Delete Task");
        JButton weatherBtn  = new JButton("Update Weather");
        JButton scheduleBtn = new JButton(" Suggest Schedule");
        JButton refreshBtn  = new JButton(" Refresh");

        addBtn.addActionListener(e -> showAddDialog());
        deleteBtn.addActionListener(e -> deleteSelected());
        weatherBtn.addActionListener(e -> updateWeatherForSelected());
        scheduleBtn.addActionListener(e -> showSchedule());
        refreshBtn.addActionListener(e -> loadTasks());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnPanel.add(addBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(weatherBtn);
        btnPanel.add(scheduleBtn);
        btnPanel.add(refreshBtn);
        btnPanel.add(new JLabel("City:"));
        btnPanel.add(cityField);

        // Status bar
        statusLabel = new JLabel("  Ready");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());

        setLayout(new BorderLayout());
        add(btnPanel, BorderLayout.NORTH);
        add(new JScrollPane(taskTable), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        loadTasks();
    }


    /**
     * Loads all tasks from the manager and refreshes the table.
     * Runs on a background thread; table update happens on EDT.
     */
    private void loadTasks() {
        setStatus("Loading tasks...");
        Mono.just(taskManager.getTasks())
            .subscribeOn(Schedulers.boundedElastic())
            .doOnNext(tasks -> SwingUtilities.invokeLater(() -> {
                populateTable(tasks);
                setStatus("Loaded " + tasks.size() + " task(s).");
            }))
            .doOnError(e -> SwingUtilities.invokeLater(() -> showError("Load failed: " + e.getMessage())))
            .subscribe();
    }

    /**
     * Fills the table with task data.
     *
     * @param tasks list of tasks to display
     */
    private void populateTable(List<Task> tasks) {
        tableModel.setRowCount(0);
        for (Task t : tasks) {
            tableModel.addRow(new Object[]{
                t.getId(),
                t.getTitle(),
                t.getDueDateTime() != null ? t.getDueDateTime().format(FMT) : "N/A",
                t.isWeatherSensitive() ? "Yes" : "No",
                "—"
            });
        }
    }


    /**
     * Shows a dialog to create a new task.
     * Validates input before adding via the reactive TaskService.
     */
    private void showAddDialog() {
        JTextField titleField = new JTextField(20);
        JTextField descField  = new JTextField(20);
        JTextField dueField   = new JTextField(LocalDateTime.now().plusHours(2).format(FMT), 20);
        JCheckBox  wxCheck    = new JCheckBox("Weather-sensitive task");

        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        p.add(new JLabel("Title:"));                    p.add(titleField);
        p.add(new JLabel("Description:"));              p.add(descField);
        p.add(new JLabel("Due (yyyy-MM-dd HH:mm):"));  p.add(dueField);
        p.add(new JLabel(""));                          p.add(wxCheck);

        if (JOptionPane.showConfirmDialog(this, p, "Add Task",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        String title = titleField.getText().trim();
        if (title.isEmpty()) { showError("Title cannot be empty."); return; }

        LocalDateTime due;
        try { due = LocalDateTime.parse(dueField.getText().trim(), FMT); }
        catch (Exception ex) { showError("Invalid date. Use: yyyy-MM-dd HH:mm"); return; }

        Task newTask = new Task("task-" + UUID.randomUUID().toString().substring(0, 8),
                                title, due, wxCheck.isSelected());
        newTask.setDescription(descField.getText().trim());

        taskService.addTask(newTask)
                   .subscribeOn(Schedulers.boundedElastic())
                   .doOnSuccess(v -> SwingUtilities.invokeLater(() -> { loadTasks(); setStatus("Added: " + title); }))
                   .doOnError(e -> SwingUtilities.invokeLater(() -> showError("Error: " + e.getMessage())))
                   .subscribe();
    }


    /**
     * Deletes the currently selected task after confirmation.
     *
     * @throws TaskNotFoundException shown as error dialog if task not found
     */
    private void deleteSelected() {
        int row = taskTable.getSelectedRow();
        if (row < 0) { showError("Please select a task to delete."); return; }

        String id    = (String) tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 1);

        if (JOptionPane.showConfirmDialog(this, "Delete \"" + title + "\"?",
                "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        taskService.removeTask(id)
                   .subscribeOn(Schedulers.boundedElastic())
                   .doOnSuccess(v -> SwingUtilities.invokeLater(() -> { loadTasks(); setStatus("Deleted: " + title); }))
                   .doOnError(e -> SwingUtilities.invokeLater(() -> showError("Error: " + e.getMessage())))
                   .subscribe();
    }


    /**
     * Fetches weather for the selected task's location and updates its Status column.
     * Weather is fetched on a background thread; UI updated on EDT.
     */
    private void updateWeatherForSelected() {
        int row = taskTable.getSelectedRow();
        if (row < 0) { showError("Please select a task first."); return; }

        String taskId = (String) tableModel.getValueAt(row, 0);
        setStatus("Fetching weather...");

        String city = cityField.getText().trim();
        if (city.isEmpty()) { showError("Please enter a city name."); return; }

        taskManager.fetchWeather(city)
                   .subscribeOn(Schedulers.boundedElastic())
                   .doOnNext(forecast -> SwingUtilities.invokeLater(() -> {
                       String status = forecast.getPrecipitationProbability() > 0.6 ? "RISKY" : " SAFE";
                       updateStatusInTable(taskId, status);
                       setStatus("Weather: " + forecast.getCondition()
                               + " | " + forecast.getTemperatureCelsius() + "°C"
                               + " | Rain: " + (int)(forecast.getPrecipitationProbability() * 100) + "%");
                   }))
                   .doOnError(e -> SwingUtilities.invokeLater(() -> showError("Weather error: " + e.getMessage())))
                   .subscribe();
    }


    /**
     * Fetches weather and shows schedule recommendations for all tasks in a dialog.
     */
    private void showSchedule() {
        List<Task> tasks = taskManager.getTasks();
        if (tasks.isEmpty()) { showError("No tasks to schedule."); return; }

        setStatus("Generating schedule...");

        String city = cityField.getText().trim();
        if (city.isEmpty()) { showError("Please enter a city name."); return; }

        schedulePlanner.suggestScheduleForLocation(tasks, city)
                       .subscribeOn(Schedulers.boundedElastic())
                       .doOnNext(recs -> SwingUtilities.invokeLater(() -> {
                           StringBuilder sb = new StringBuilder();
                           for (ScheduleRecommendation r : recs) {
                               sb.append(" ").append(r.task().getTitle()).append("\n");
                               sb.append("   ").append(r.recommendation()).append("\n\n");
                           }
                           JTextArea area = new JTextArea(sb.toString(), 15, 45);
                           area.setEditable(false);
                           area.setFont(new Font("Monospaced", Font.PLAIN, 13));
                           JOptionPane.showMessageDialog(this, new JScrollPane(area),
                                   "📅 Schedule Recommendations", JOptionPane.INFORMATION_MESSAGE);
                           setStatus("Schedule ready.");
                       }))
                       .doOnError(e -> SwingUtilities.invokeLater(() -> showError("Error: " + e.getMessage())))
                       .subscribe();
    }


    /**
     * Updates the Status column for a specific task row.
     *
     * @param taskId the task ID to find
     * @param status the new status text
     */
    private void updateStatusInTable(String taskId, String status) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (taskId.equals(tableModel.getValueAt(i, 0))) {
                tableModel.setValueAt(status, i, 4);
                break;
            }
        }
    }

    /**
     * Sets the status bar message.
     *
     * @param msg message to display
     */
    private void setStatus(String msg) {
        statusLabel.setText("  " + msg);
    }

    /**
     * Shows an error dialog.
     *
     * @param msg error message to display
     */
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}