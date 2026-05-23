package com.workscheduler.report;

import com.workscheduler.model.Task;
import com.workscheduler.model.User;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportGenerator {

    public void generateDailyReport(List<Task> tasks, User user) throws Exception {
        LocalDate today = LocalDate.now();
        String filename = "reports/дневной_отчёт_" + user.getUsername() + "_" + today + ".txt";
        generateTxtReport(tasks, user, filename, "ДНЕВНОЙ ОТЧЁТ - " + today);
    }

    public void generateWeeklyReport(List<Task> tasks, User user) throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        String filename = "reports/недельный_отчёт_" + user.getUsername() + "_" + weekStart + ".txt";
        generateTxtReport(tasks, user, filename, "НЕДЕЛЬНЫЙ ОТЧЁТ - неделя от " + weekStart);
    }

    public void generateMonthlyReport(List<Task> tasks, User user) throws Exception {
        LocalDate today = LocalDate.now();
        String filename = "reports/месячный_отчёт_" + user.getUsername() + "_" + today.getMonthValue() + "_" + today.getYear() + ".txt";
        generateTxtReport(tasks, user, filename, "МЕСЯЧНЫЙ ОТЧЁТ - " + today.getMonth() + " " + today.getYear());
    }

    private void generateTxtReport(List<Task> tasks, User user, String filename, String title) throws Exception {
        File reportsDir = new File("reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }

        List<Task> filteredTasks = filterTasksByPeriod(tasks, title);

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, false))) {
            writer.println("=" .repeat(60));
            writer.println("         " + title);
            writer.println("=" .repeat(60));
            writer.println();

            writer.println("Пользователь: " + user.getFullName() + " (" + user.getUsername() + ")");
            writer.println("Дата генерации: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
            writer.println();

            List<Task> completedTasks = filteredTasks.stream()
                    .filter(t -> "COMPLETED".equals(t.getStatus()))
                    .collect(Collectors.toList());

            int totalTasks = filteredTasks.size();
            int completedCount = completedTasks.size();
            int pendingCount = (int) filteredTasks.stream().filter(t -> "PENDING".equals(t.getStatus())).count();
            int inProgressCount = (int) filteredTasks.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();

            int totalEstimatedMinutes = completedTasks.stream().mapToInt(Task::getEstimatedMinutes).sum();
            int totalActualMinutes = completedTasks.stream().mapToInt(Task::getActualMinutes).sum();
            // ИСПРАВЛЕННАЯ ФОРМУЛА: (План / Факт) * 100%
            double efficiency = totalActualMinutes > 0 ? (double) totalEstimatedMinutes / totalActualMinutes * 100 : 0;

            writer.println("=" .repeat(40));
            writer.println("СТАТИСТИКА");
            writer.println("=" .repeat(40));
            writer.println("Всего задач: " + totalTasks);
            writer.println("Завершено: " + completedCount);
            writer.println("В ожидании: " + pendingCount);
            writer.println("В работе: " + inProgressCount);
            writer.println("Планируемое время: " + formatMinutes(totalEstimatedMinutes));
            writer.println("Фактическое время: " + formatMinutes(totalActualMinutes));
            writer.println("Эффективность: " + String.format("%.1f", efficiency) + "%");

            if (efficiency >= 100) {
                writer.println("  (Отлично! Вы уложились в срок или быстрее)");
            } else {
                writer.println("  (Требуется корректировка планирования)");
            }
            writer.println();

            if (!filteredTasks.isEmpty()) {
                writer.println("=" .repeat(40));
                writer.println("СПИСОК ЗАДАЧ");
                writer.println("=" .repeat(40));
                writer.println();

                writer.printf("%-4s | %-25s | %-12s | %-10s | %-8s | %-8s%n",
                        "ID", "Название", "Категория", "Статус", "План", "Факт");
                writer.println("-".repeat(90));

                for (Task task : filteredTasks) {
                    String statusRu = "";
                    switch(task.getStatus()) {
                        case "PENDING": statusRu = "Ожидает"; break;
                        case "IN_PROGRESS": statusRu = "В работе"; break;
                        case "COMPLETED": statusRu = "Завершено"; break;
                        case "CANCELLED": statusRu = "Отменено"; break;
                        default: statusRu = task.getStatus();
                    }

                    String titleShort = task.getTitle().length() > 23 ? task.getTitle().substring(0, 20) + "..." : task.getTitle();
                    String category = task.getCategory() != null ? task.getCategory() : "Общая";
                    if (category.length() > 10) category = category.substring(0, 8);

                    writer.printf("%-4d | %-25s | %-12s | %-10s | %-8d | %-8d%n",
                            task.getId(),
                            titleShort,
                            category,
                            statusRu,
                            task.getEstimatedMinutes(),
                            task.getActualMinutes()
                    );
                }
                writer.println();
            } else {
                writer.println("Нет задач за выбранный период");
                writer.println();
            }

            generateTextChart(writer, completedTasks);

            writer.println("=" .repeat(60));
            writer.println("Конец отчёта");
            writer.println("=" .repeat(60));
        }

        System.out.println("Отчёт сохранён: " + filename);
    }

    private void generateTextChart(PrintWriter writer, List<Task> tasks) {
        if (tasks.isEmpty()) return;

        Map<String, Integer> categoryTime = tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory() : "Общая",
                        Collectors.summingInt(Task::getActualMinutes)
                ));

        if (categoryTime.isEmpty()) return;

        writer.println("=" .repeat(40));
        writer.println("РАСПРЕДЕЛЕНИЕ ВРЕМЕНИ ПО КАТЕГОРИЯМ");
        writer.println("=" .repeat(40));

        int maxValue = categoryTime.values().stream().max(Integer::compareTo).orElse(1);
        int maxBarLength = 40;

        for (Map.Entry<String, Integer> entry : categoryTime.entrySet()) {
            int barLength = (int)((double)entry.getValue() / maxValue * maxBarLength);
            String bar = "█".repeat(Math.max(1, barLength));
            writer.printf("%-12s | %s %d мин%n", entry.getKey(), bar, entry.getValue());
        }
        writer.println();
    }

    private List<Task> filterTasksByPeriod(List<Task> tasks, String title) {
        LocalDate now = LocalDate.now();

        if (title.contains("ДНЕВНОЙ")) {
            return tasks.stream()
                    .filter(t -> t.getStartTime() != null)
                    .filter(t -> t.getStartTime().toLocalDate().equals(now))
                    .collect(Collectors.toList());
        } else if (title.contains("НЕДЕЛЬНЫЙ")) {
            LocalDate weekAgo = now.minusDays(7);
            return tasks.stream()
                    .filter(t -> t.getStartTime() != null)
                    .filter(t -> !t.getStartTime().toLocalDate().isBefore(weekAgo))
                    .collect(Collectors.toList());
        } else {
            return tasks.stream()
                    .filter(t -> t.getStartTime() != null)
                    .filter(t -> t.getStartTime().getMonth() == now.getMonth())
                    .filter(t -> t.getStartTime().getYear() == now.getYear())
                    .collect(Collectors.toList());
        }
    }

    private String formatMinutes(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours > 0) {
            return hours + "ч " + mins + "мин";
        }
        return mins + " мин";
    }
}