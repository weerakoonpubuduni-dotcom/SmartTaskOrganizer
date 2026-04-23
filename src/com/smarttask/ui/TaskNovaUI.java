package com.smarttask.ui;

import com.smarttask.manager.TaskManager;
import com.smarttask.model.Task;
import com.smarttask.storage.TaskStorage;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TaskNovaUI {

    private TaskManager manager;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private JFrame frame;
    private JLabel totalLabel, dueSoonLabel, overdueLabel;
    private JTextField nameField, deadlineField;
    private int selectedImportance = 3;
    private JButton[] impButtons;
    private JPanel taskListPanel, alertPanel;
    private JLabel nextTaskName, nextTaskMeta;

    // ─── Colors ───────────────────────────────────────────
    private static final Color BG          = new Color(216, 195, 237);
    private static final Color WHITE       = Color.WHITE;
    private static final Color PURPLE_LIGHT= new Color(238, 237, 254);
    private static final Color PURPLE_MID  = new Color(127, 119, 221);
    private static final Color PURPLE_DARK = new Color(60,  52,  137);
    private static final Color BORDER      = new Color(220, 218, 212);
    private static final Color TEXT_PRI    = new Color(9, 9, 8);
    private static final Color TEXT_SEC    = new Color(87, 86, 86);
    private static final Color TEXT_SEC2   = new Color(207, 204, 204);
    private static final Color RED_BG      = new Color(252, 235, 235);
    private static final Color RED_TEXT    = new Color(113, 15, 15);
    private static final Color ORANGE_BG   = new Color(250, 238, 218);
    private static final Color ORANGE_TEXT = new Color(133, 79,  11);
    private static final Color YELLOW_BG   = new Color(250, 238, 218);
    private static final Color YELLOW_TEXT = new Color(186, 117, 23);
    private static final Color GREEN_BG    = new Color(234, 243, 222);
    private static final Color GREEN_TEXT  = new Color(59,  109, 17);
    private static final Color BLUE_TEXT  = new Color(212, 232, 236);

    public TaskNovaUI() {
        this.manager = new TaskManager(3);
    }

    public void start() {
        SwingUtilities.invokeLater(() -> {
            buildFrame();

            if (manager.isFirstRun()) {
                System.out.println("🌱 First launch — seeding sample tasks.");
                loadSampleTasks();
            } else {
                System.out.println("🔄 Existing save found — skipping sample tasks.");
            }

            refreshDashboard();
            frame.setVisible(true);

            // Print save path so you can verify the file exists on disk
            System.out.println("📁 Save file: " + TaskStorage.getStoragePath());
        });
    }

    // ── Frame ─────────────────────────────────────────────

    private void buildFrame() {
        frame = new JFrame("TaskNova Smart Task Priority Organizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);
        frame.setBackground(BG);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(),   BorderLayout.CENTER);
        frame.setContentPane(root);
    }

    // ── Header ────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(14, 20, 14, 20)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setBackground(WHITE);

        JLabel icon = new JLabel("▦");
        icon.setFont(new Font("SansSerif", Font.BOLD, 18));
        icon.setForeground(PURPLE_MID);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 1));
        titleBlock.setBackground(WHITE);

        JLabel title = new JLabel("TaskNova");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(TEXT_PRI);

        JLabel sub = new JLabel("Smart Task Priority Organizer");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(TEXT_SEC);

        titleBlock.add(title);
        titleBlock.add(sub);
        left.add(icon);
        left.add(titleBlock);
        header.add(left, BorderLayout.WEST);
        return header;
    }

    // ── Body ──────────────────────────────────────────────

    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(14, 14, 14, 14));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill   = GridBagConstraints.BOTH;

        g.gridx = 0; g.gridy = 0; g.gridwidth = 3; g.weightx = 1; g.weighty = 0;
        body.add(buildStatsRow(), g);

        g.gridx = 0; g.gridy = 1; g.gridwidth = 1; g.weightx = 0.35; g.weighty = 0.45;
        body.add(buildAddTaskCard(), g);

        g.gridx = 1; g.gridy = 1; g.gridwidth = 2; g.weightx = 0.65; g.weighty = 0.45;
        body.add(buildAlertsCard(), g);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 3; g.weightx = 1; g.weighty = 0;
        body.add(buildNextTaskCard(), g);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 3; g.weightx = 1; g.weighty = 0.55;
        body.add(buildTaskListCard(), g);

        return body;
    }

    // ── Stats ─────────────────────────────────────────────

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 10, 0));
        row.setBackground(BG);
        totalLabel   = new JLabel("0", SwingConstants.CENTER);
        dueSoonLabel = new JLabel("0", SwingConstants.CENTER);
        overdueLabel = new JLabel("0", SwingConstants.CENTER);
        row.add(statCard(totalLabel,   "Total tasks", TEXT_PRI));
        row.add(statCard(dueSoonLabel, "Due soon",    new Color(186, 117, 23)));
        row.add(statCard(overdueLabel, "Overdue",     RED_TEXT));
        return row;
    }

    private JPanel statCard(JLabel valueLabel, String labelText, Color valueColor) {
        JPanel card = roundedCard();
        card.setLayout(new GridLayout(2, 1, 0, 2));
        card.setBorder(new EmptyBorder(10, 12, 10, 12));
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(valueColor);
        JLabel lbl = new JLabel(labelText, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(TEXT_SEC);
        card.add(valueLabel);
        card.add(lbl);
        return card;
    }

    // ── Add Task Card ─────────────────────────────────────

    private JPanel buildAddTaskCard() {
        JPanel card = roundedCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        card.add(sectionLabel("ADD NEW TASK"));
        card.add(Box.createVerticalStrut(10));

        card.add(fieldLabel("Task name"));
        card.add(Box.createVerticalStrut(4));
        nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        nameField.setFont(new Font("SansSerif", Font.BOLD, 13));
        styleTextField(nameField);
        nameField.setForeground(TEXT_PRI);
        nameField.setBackground(BLUE_TEXT);
        card.add(nameField);
        card.add(Box.createVerticalStrut(7));

        card.add(fieldLabel("Deadline (yyyy-MM-dd)"));
        card.add(Box.createVerticalStrut(4));
        deadlineField = new JTextField();
        deadlineField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        deadlineField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        deadlineField.setText(LocalDate.now().plusDays(3).toString());
        styleTextField(deadlineField);
        deadlineField.setForeground(TEXT_PRI);
        deadlineField.setBackground(BLUE_TEXT);
        card.add(deadlineField);
        card.add(Box.createVerticalStrut(7));

        card.add(fieldLabel("Importance (1 = Low  →  5 = High)"));
        card.add(Box.createVerticalStrut(4));
        card.add(buildImportanceButtons());
        card.add(Box.createVerticalStrut(12));

        JButton addBtn = new JButton("Add Task");
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        addBtn.setForeground(TEXT_PRI);
        addBtn.setBackground(TEXT_SEC2);
        addBtn.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(4, 12, 4, 12)));
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> onAddTask());
        card.add(addBtn);
        return card;
    }

    private JPanel buildImportanceButtons() {
        JPanel row = new JPanel(new GridLayout(1, 5, 5, 0));
        row.setBackground(WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        impButtons = new JButton[5];
        for (int i = 1; i <= 5; i++) {
            final int val = i;
            JButton btn = new JButton(String.valueOf(i));
            btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(new LineBorder(BORDER, 0, true));
            btn.addActionListener(e -> { selectedImportance = val; refreshImportanceButtons(); });
            impButtons[i - 1] = btn;
            row.add(btn);
        }
        refreshImportanceButtons();
        return row;
    }

    private void refreshImportanceButtons() {
        for (int i = 0; i < impButtons.length; i++) {
            if (i + 1 == selectedImportance) {
                impButtons[i].setBackground(PURPLE_LIGHT);
                impButtons[i].setForeground(PURPLE_DARK);
                impButtons[i].setBorder(new LineBorder(PURPLE_MID, 1, true));
            } else {
                impButtons[i].setBackground(new Color(154, 136, 172));
                impButtons[i].setForeground(TEXT_PRI);
                impButtons[i].setBorder(new LineBorder(BORDER, 1, true));
            }
        }
    }

    // ── Alerts Card ───────────────────────────────────────

    private JPanel buildAlertsCard() {
        JPanel card = roundedCard();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.add(sectionLabel("DEADLINE ALERTS"), BorderLayout.NORTH);
        alertPanel = new JPanel();
        alertPanel.setLayout(new BoxLayout(alertPanel, BoxLayout.Y_AXIS));
        alertPanel.setBackground(WHITE);
        JScrollPane scroll = new JScrollPane(alertPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(WHITE);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ── Next Task Card ────────────────────────────────────

    private JPanel buildNextTaskCard() {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        card.setBackground(PURPLE_LIGHT);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(135, 127, 225), 1, true),
                new EmptyBorder(2, 4, 2, 4)));

        JLabel iconLbl = new JLabel("★") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PURPLE_DARK);
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        iconLbl.setPreferredSize(new Dimension(36, 36));
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        iconLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        iconLbl.setForeground(WHITE);

        JPanel textBlock = new JPanel(new GridLayout(3, 1, 0, 1));
        textBlock.setBackground(PURPLE_LIGHT);

        JLabel nextLbl = new JLabel("RECOMMENDED NEXT TASK");
        nextLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        nextLbl.setForeground(PURPLE_MID);

        nextTaskName = new JLabel("—");
        nextTaskName.setFont(new Font("SansSerif", Font.BOLD, 14));
        nextTaskName.setForeground(PURPLE_DARK);

        nextTaskMeta = new JLabel("—");
        nextTaskMeta.setFont(new Font("SansSerif", Font.PLAIN, 11));
        nextTaskMeta.setForeground(new Color(83, 74, 183));

        textBlock.add(nextLbl);
        textBlock.add(nextTaskName);
        textBlock.add(nextTaskMeta);
        card.add(iconLbl);
        card.add(textBlock);
        return card;
    }

    // ── Task List Card ────────────────────────────────────

    private JPanel buildTaskListCard() {
        JPanel card = roundedCard();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.add(sectionLabel("TASKS RANKED BY PRIORITY"), BorderLayout.WEST);

        JPanel colHeaders = new JPanel(new BorderLayout());
        colHeaders.setBackground(WHITE);
        colHeaders.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));

        JPanel cols = new JPanel(new GridLayout(1, 4));
        cols.setBackground(WHITE);
        cols.setBorder(new EmptyBorder(4, 0, 6, 0));
        for (String col : new String[]{"Task", "Deadline", "Importance", "Priority"}) {
            JLabel lbl = new JLabel(col);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
            lbl.setForeground(TEXT_SEC);
            cols.add(lbl);
        }
        colHeaders.add(cols, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(WHITE);
        top.add(header,     BorderLayout.NORTH);
        top.add(colHeaders, BorderLayout.SOUTH);
        card.add(top, BorderLayout.NORTH);

        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        taskListPanel.setBackground(WHITE);

        JScrollPane scroll = new JScrollPane(taskListPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(WHITE);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ── Dashboard Refresh ─────────────────────────────────

    private void refreshDashboard() {
        refreshStats();
        refreshAlerts();
        refreshNextTask();
        refreshTaskList();
    }

    private void refreshStats() {
        List<Task> all = manager.getAllTasks();
        int total   = all.size();
        int overdue = (int) all.stream().filter(t -> t.getDaysUntilDeadline() <= 0).count();
        int dueSoon = (int) all.stream()
                .filter(t -> t.getDaysUntilDeadline() > 0 && t.getDaysUntilDeadline() <= 3).count();
        totalLabel.setText(String.valueOf(total));
        dueSoonLabel.setText(String.valueOf(dueSoon));
        overdueLabel.setText(String.valueOf(overdue));
    }

    private void refreshAlerts() {
        alertPanel.removeAll();
        List<Task> alerts = manager.getAlerts();
        if (alerts.isEmpty()) {
            JLabel none = new JLabel("No upcoming deadlines within 3 days.");
            none.setFont(new Font("SansSerif", Font.PLAIN, 12));
            none.setForeground(TEXT_SEC);
            none.setBorder(new EmptyBorder(6, 0, 6, 0));
            alertPanel.add(none);
        } else {
            for (Task t : alerts) alertPanel.add(buildAlertRow(t));
        }
        alertPanel.revalidate();
        alertPanel.repaint();
    }

    private JPanel buildAlertRow(Task t) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        long days = t.getDaysUntilDeadline();
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("SansSerif", Font.PLAIN, 10));
        dot.setForeground(days <= 0 ? RED_TEXT : ORANGE_TEXT);
        dot.setBorder(new EmptyBorder(0, 0, 0, 4));

        JLabel name = new JLabel(t.getName());
        name.setFont(new Font("SansSerif", Font.PLAIN, 13));
        name.setForeground(TEXT_PRI);

        String dayStr = days <= 0 ? "Overdue" : days + " day(s) left";
        JLabel daysLbl = new JLabel(dayStr);
        daysLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        daysLbl.setForeground(days <= 0 ? RED_TEXT : TEXT_SEC);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        left.setBackground(WHITE);
        left.add(dot);
        left.add(name);

        row.add(left,    BorderLayout.WEST);
        row.add(daysLbl, BorderLayout.EAST);
        row.setBorder(new EmptyBorder(6, 0, 6, 0));
        return row;
    }

    private void refreshNextTask() {
        Task next = manager.getNextTask();
        if (next == null) {
            nextTaskName.setText("No tasks available");
            nextTaskMeta.setText("—");
        } else {
            nextTaskName.setText(next.getName());
            nextTaskMeta.setText(String.format(
                    "Priority: %.4f  |  Due: %s  |  Importance: %d/5",
                    next.getPriorityScore(), next.getDeadline(), next.getImportance()));
        }
    }

    private void refreshTaskList() {
        taskListPanel.removeAll();
        List<Task> tasks = manager.getAllTasks();
        if (tasks.isEmpty()) {
            JLabel empty = new JLabel("No tasks yet. Add one above!");
            empty.setFont(new Font("SansSerif", Font.PLAIN, 13));
            empty.setForeground(TEXT_SEC);
            empty.setBorder(new EmptyBorder(10, 0, 0, 0));
            taskListPanel.add(empty);
        } else {
            int rank = 1;
            for (Task t : tasks) taskListPanel.add(buildTaskRow(t, rank++));
        }
        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    private JPanel buildTaskRow(Task t, int rank) {
        JPanel row = new JPanel(new GridLayout(1, 4));
        row.setBackground(rank % 2 == 0 ? new Color(249, 248, 245) : WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        row.setBorder(new EmptyBorder(8, 0, 8, 0));

        JPanel nameCell = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        nameCell.setBackground(row.getBackground());
        JLabel rankLbl = new JLabel("#" + rank);
        rankLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        rankLbl.setForeground(TEXT_SEC);
        JLabel nameLbl = new JLabel(t.getName());
        nameLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        nameLbl.setForeground(TEXT_PRI);
        nameCell.add(rankLbl);
        nameCell.add(nameLbl);

        JLabel deadlineLbl = new JLabel(t.getDeadline().toString());
        deadlineLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        deadlineLbl.setForeground(TEXT_SEC);

        JLabel impLbl = new JLabel(t.getImportance() + "/5");
        impLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        impLbl.setForeground(TEXT_SEC);

        JPanel scoreCell = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        scoreCell.setBackground(row.getBackground());
        JLabel scoreLbl = new JLabel(String.format("%.4f", t.getPriorityScore()));
        scoreLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        scoreLbl.setForeground(TEXT_PRI);
        scoreCell.add(scoreLbl);
        scoreCell.add(buildBadge(t));

        JButton del = new JButton("✕");
        del.setFont(new Font("SansSerif", Font.PLAIN, 10));
        del.setForeground(TEXT_SEC);
        del.setBackground(WHITE);
        del.setBorder(new LineBorder(BORDER, 1, true));
        del.setFocusPainted(false);
        del.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        del.setPreferredSize(new Dimension(24, 24));
        del.addActionListener(e -> { manager.removeTask(t.getName()); refreshDashboard(); });
        scoreCell.add(del);

        row.add(nameCell);
        row.add(deadlineLbl);
        row.add(impLbl);
        row.add(scoreCell);
        return row;
    }

    private JLabel buildBadge(Task t) {
        long days = t.getDaysUntilDeadline();
        String text; Color bg; Color fg;
        if      (days <= 0) { text = "OVERDUE";  bg = RED_BG;    fg = RED_TEXT;    }
        else if (days <= 1) { text = "CRITICAL"; bg = RED_BG;    fg = RED_TEXT;    }
        else if (days <= 3) { text = "HIGH";     bg = ORANGE_BG; fg = ORANGE_TEXT; }
        else if (days <= 7) { text = "MEDIUM";   bg = YELLOW_BG; fg = YELLOW_TEXT; }
        else                { text = "LOW";      bg = GREEN_BG;  fg = GREEN_TEXT;  }
        JLabel badge = new JLabel(text);
        badge.setFont(new Font("SansSerif", Font.BOLD, 10));
        badge.setForeground(fg);
        badge.setBackground(bg);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(2, 7, 2, 7));
        return badge;
    }

    // ── Add Task Action ───────────────────────────────────

    private void onAddTask() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) { showError("Task name cannot be empty."); return; }

        LocalDate deadline;
        try {
            deadline = LocalDate.parse(deadlineField.getText().trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            showError("Invalid date. Use format: yyyy-MM-dd (e.g. 2026-04-25)");
            return;
        }

        manager.addTask(name, deadline, selectedImportance);

        nameField.setText("");
        deadlineField.setText(LocalDate.now().plusDays(3).toString());
        selectedImportance = 3;
        refreshImportanceButtons();
        refreshDashboard();
    }

    // ── Helpers ───────────────────────────────────────────

    private JPanel roundedCard() {
        JPanel card = new JPanel();
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(0, 0, 0, 0)));
        return card;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(TEXT_SEC);
        return lbl;
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(TEXT_SEC);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void styleTextField(JTextField f) {
        f.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(4, 8, 4, 8)));
        f.setBackground(new Color(157, 170, 202));
        f.setForeground(TEXT_PRI);
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── Sample Data (first launch only) ──────────────────

    private void loadSampleTasks() {
        manager.addTask("Submit Assignment",  LocalDate.now().plusDays(2),  5);
        manager.addTask("Read Chapter 4",     LocalDate.now().plusDays(7),  3);
        manager.addTask("Team Meeting Prep",  LocalDate.now().plusDays(1),  4);
        manager.addTask("Fix Project Bug",    LocalDate.now().plusDays(0),  5);
        manager.addTask("Update Resume",      LocalDate.now().plusDays(14), 2);
    }
}