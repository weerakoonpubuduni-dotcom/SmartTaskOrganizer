package com.smarttask.storage;

import com.smarttask.model.Task;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class TaskStorage {

    private static final Path STORAGE_DIR  =
            Paths.get(System.getProperty("user.home"), ".tasknova");
    private static final Path STORAGE_FILE =
            STORAGE_DIR.resolve("tasks.json");

    // ── Public helper ─────────────────────────────────────


    public static String getStoragePath() {
        return STORAGE_FILE.toAbsolutePath().toString();
    }

    public static boolean isFirstRun() {
        return !Files.exists(STORAGE_FILE);
    }

    // ── Save ─────────────────────────────────────────────

    public static void save(List<Task> tasks) {
        try {
            Files.createDirectories(STORAGE_DIR);   // create ~/.tasknova/ if needed

            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < tasks.size(); i++) {
                Task t = tasks.get(i);
                sb.append("  {")
                        .append("\"name\":").append(jsonString(t.getName())).append(",")
                        .append("\"deadline\":\"").append(t.getDeadline()).append("\",")
                        .append("\"importance\":").append(t.getImportance())
                        .append("}");
                if (i < tasks.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("]");

            // Write atomically: write to a temp file first, then rename
            Path tmp = STORAGE_DIR.resolve("tasks.tmp");
            Files.writeString(tmp, sb.toString());
            Files.move(tmp, STORAGE_FILE,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

        } catch (AtomicMoveNotSupportedException e) {
            // Fallback for systems that don't support atomic move
            try {
                Files.writeString(STORAGE_FILE, buildJson(tasks));
            } catch (IOException ex) {
                System.err.println("⚠️  Could not save tasks: " + ex.getMessage());
            }
        } catch (IOException e) {
            System.err.println("⚠️  Could not save tasks: " + e.getMessage());
        }
    }

    private static String buildJson(List<Task> tasks) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            sb.append("  {")
                    .append("\"name\":").append(jsonString(t.getName())).append(",")
                    .append("\"deadline\":\"").append(t.getDeadline()).append("\",")
                    .append("\"importance\":").append(t.getImportance())
                    .append("}");
            if (i < tasks.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    // ── Load ─────────────────────────────────────────────

    public static List<Task> load() {
        List<Task> tasks = new ArrayList<>();

        if (!Files.exists(STORAGE_FILE)) {
            System.out.println("ℹ️  No save file found — first launch.");
            return tasks;
        }

        System.out.println("📖 Reading from: " + STORAGE_FILE.toAbsolutePath());

        try {
            String content = Files.readString(STORAGE_FILE).trim();

            // Strip outer [ ]
            if (content.startsWith("[")) content = content.substring(1);
            if (content.endsWith("]"))   content = content.substring(0, content.length() - 1);

            content = content.trim();
            if (content.isEmpty()) return tasks;

            List<String> blocks = splitObjects(content);
            for (String block : blocks) {
                Task t = parseTask(block.trim());
                if (t != null) tasks.add(t);
            }

        } catch (IOException e) {
            System.err.println("⚠️  Could not load tasks: " + e.getMessage());
        }

        return tasks;
    }

    // ── Helpers ──────────────────────────────────────────

    private static List<String> splitObjects(String content) {
        List<String> blocks = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    blocks.add(content.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return blocks;
    }


    private static Task parseTask(String obj) {
        try {
            String name        = extractString(obj, "name");
            String deadlineStr = extractString(obj, "deadline");
            int    importance  = extractInt(obj, "importance");

            if (name == null || deadlineStr == null) {
                System.err.println("⚠️  Skipping entry with missing fields: " + obj);
                return null;
            }

            LocalDate deadline = LocalDate.parse(deadlineStr);
            return new Task(name, deadline, importance);

        } catch (Exception e) {
            System.err.println("⚠️  Skipping malformed entry: " + obj + " — " + e.getMessage());
            return null;
        }
    }

    private static String extractString(String obj, String key) {
        String search = "\"" + key + "\":\"";
        int start = obj.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = obj.indexOf("\"", start);
        if (end == -1) return null;
        return obj.substring(start, end);
    }

    private static int extractInt(String obj, String key) {
        String search = "\"" + key + "\":";
        int start = obj.indexOf(search);
        if (start == -1) return 3;
        start += search.length();
        int end = start;
        while (end < obj.length() && Character.isDigit(obj.charAt(end))) end++;
        return Integer.parseInt(obj.substring(start, end));
    }

    private static String jsonString(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}