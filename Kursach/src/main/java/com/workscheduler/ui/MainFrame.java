package com.workscheduler.ui;

import com.workscheduler.database.TaskDAO;
import com.workscheduler.database.ActionLogDAO;
import com.workscheduler.model.User;
import com.workscheduler.model.Task;
import com.workscheduler.report.ReportGenerator;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {
    private User currentUser;
    private TaskDAO taskDAO;
    private ActionLogDAO logDAO;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JPanel ganttPanel;
    private JComboBox<String> viewCombo;
    private JSplitPane splitPane;
    private JScrollPane tableScrollPane;

    private final Map<String, Color> categoryColors = new HashMap<>();

    public MainFrame(User user) {
        this.currentUser = user;
        this.taskDAO = new TaskDAO();
        this.logDAO = new ActionLogDAO();
        initColors();
        initUI();
        loadTasks();
        setVisible(true);
    }

    private void initColors() {
        categoryColors.put("Работа", new Color(59, 130, 246));
        categoryColors.put("Личное", new Color(34, 197, 94));
        categoryColors.put("Учёба", new Color(168, 85, 247));
        categoryColors.put("Встреча", new Color(249, 115, 22));
        categoryColors.put("Разработка", new Color(6, 182, 212));
        categoryColors.put("Звонок", new Color(236, 72, 153));
        categoryColors.put("Командировка", new Color(139, 69, 19));
        categoryColors.put("General", new Color(100, 116, 139));
    }

    private void initUI() {
        setTitle("Учёт рабочего времени - " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");
        JMenuItem logoutItem = new JMenuItem("Выход");
        JMenuItem exitItem = new JMenuItem("Закрыть");
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu reportsMenu = new JMenu("Отчёты");
        JMenuItem dailyReportItem = new JMenuItem("Дневной отчёт");
        JMenuItem weeklyReportItem = new JMenuItem("Недельный отчёт");
        JMenuItem monthlyReportItem = new JMenuItem("Месячный отчёт");
        JMenuItem activityLogItem = new JMenuItem("Журнал действий");
        reportsMenu.add(dailyReportItem);
        reportsMenu.add(weeklyReportItem);
        reportsMenu.add(monthlyReportItem);
        reportsMenu.addSeparator();
        reportsMenu.add(activityLogItem);
        menuBar.add(reportsMenu);

        JMenu helpMenu = new JMenu("Помощь");
        JMenuItem aboutItem = new JMenuItem("О программе");
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        if ("ADMIN".equals(currentUser.getRole())) {
            JMenu adminMenu = new JMenu("Админ");
            JMenuItem viewAllLogsItem = new JMenuItem("Все логи");
            adminMenu.add(viewAllLogsItem);
            menuBar.add(adminMenu);
            viewAllLogsItem.addActionListener(e -> viewAllLogs());
        }

        setJMenuBar(menuBar);
        setLayout(new BorderLayout(10, 10));

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(new Color(248, 250, 252));

        JButton addBtn = createStyledButton("Добавить", new Color(30, 58, 138));
        JButton editBtn = createStyledButton("Редактировать", new Color(100, 116, 139));
        JButton deleteBtn = createStyledButton("Удалить", new Color(220, 38, 38));
        JButton viewBtn = createStyledButton("Просмотр", new Color(34, 197, 94));
        JButton refreshBtn = createStyledButton("Обновить", new Color(100, 116, 139));

        toolBar.add(addBtn);
        toolBar.add(editBtn);
        toolBar.add(deleteBtn);
        toolBar.add(viewBtn);
        toolBar.addSeparator();
        toolBar.add(refreshBtn);

        toolBar.addSeparator();
        toolBar.add(new JLabel("   Вид: "));
        viewCombo = new JComboBox<>(new String[]{"Список задач", "Диаграмма Ганта"});
        viewCombo.addActionListener(e -> toggleView());
        toolBar.add(viewCombo);

        add(toolBar, BorderLayout.NORTH);

        String[] columns = {"Задача", "Категория", "Начало", "Конец (Дедлайн)", "План", "Факт", "Статус"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4 || columnIndex == 5) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        // Кастомный рендерер для подсветки дедлайнов
        taskTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                if (!isRowSelected(row)) {
                    String category = (String) getValueAt(row, 1);
                    Color catColor = categoryColors.getOrDefault(category, categoryColors.get("General"));
                    Color bgColor = new Color(
                            Math.min(255, catColor.getRed() + 200),
                            Math.min(255, catColor.getGreen() + 200),
                            Math.min(255, catColor.getBlue() + 200)
                    );

                    // Получаем время окончания (дедлайн) из колонки 3
                    String endTimeStr = (String) getValueAt(row, 3);
                    if (endTimeStr != null && !"Не указано".equals(endTimeStr)) {
                        try {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);
                            LocalDateTime now = LocalDateTime.now();

                            String status = (String) getValueAt(row, 6);
                            boolean isCompleted = "Завершено".equals(status);

                            if (!isCompleted && endTime.isBefore(now)) {
                                // Просрочено - красный
                                bgColor = new Color(255, 200, 200);
                            } else if (!isCompleted) {
                                long daysBetween = ChronoUnit.DAYS.between(now.toLocalDate(), endTime.toLocalDate());

                                if (daysBetween == 0 && endTime.isAfter(now)) {
                                    // Сегодня - оранжевый
                                    bgColor = new Color(255, 220, 180);
                                } else if (daysBetween == 1) {
                                    // Завтра - жёлтый
                                    bgColor = new Color(255, 255, 180);
                                } else if (daysBetween == 2) {
                                    // Через 2 дня - светло-жёлтый
                                    bgColor = new Color(255, 255, 200);
                                }
                            } else if (isCompleted) {
                                // Завершено - зелёный
                                bgColor = new Color(200, 255, 200);
                            }
                        } catch (Exception e) {
                            // Игнорируем ошибки парсинга
                        }
                    }
                    c.setBackground(bgColor);
                }
                return c;
            }
        };

        taskTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        taskTable.setRowHeight(30);
        taskTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Включаем сортировку с правильным сравнением дат
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        taskTable.setRowSorter(sorter);

        // Компаратор для колонки с датой (колонки 2 и 3)
        Comparator<String> dateComparator = (date1, date2) -> {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                if ("Не указано".equals(date1) && "Не указано".equals(date2)) return 0;
                if ("Не указано".equals(date1)) return 1;
                if ("Не указано".equals(date2)) return -1;
                LocalDateTime dt1 = LocalDateTime.parse(date1, formatter);
                LocalDateTime dt2 = LocalDateTime.parse(date2, formatter);
                return dt1.compareTo(dt2);
            } catch (Exception e) {
                return date1.compareTo(date2);
            }
        };

        sorter.setComparator(2, dateComparator);
        sorter.setComparator(3, dateComparator);
        sorter.setComparator(4, Comparator.comparingInt(o -> Integer.parseInt(o.toString())));
        sorter.setComparator(5, Comparator.comparingInt(o -> Integer.parseInt(o.toString())));

        // Сортировка по умолчанию по дате начала (колонка 2)
        sorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(2, SortOrder.ASCENDING)));

        taskTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        taskTable.getColumnModel().getColumn(4).setMaxWidth(70);
        taskTable.getColumnModel().getColumn(5).setMaxWidth(70);
        taskTable.getColumnModel().getColumn(6).setPreferredWidth(100);

        // Двойной клик для просмотра
        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = taskTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        viewTaskDetails();
                    }
                }
            }
        });

        tableScrollPane = new JScrollPane(taskTable);

        ganttPanel = new JPanel(new BorderLayout());
        ganttPanel.setBackground(Color.WHITE);
        ganttPanel.setVisible(false);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, ganttPanel);
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerLocation(400);

        add(splitPane, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(248, 250, 252));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        statusLabel = new JLabel("Готов к работе | Цветовая индикация: Красный - просрочено, Оранжевый - сегодня, Жёлтый - завтра, Светло-жёлтый - через 2 дня, Зелёный - завершено");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addEditTask(null));
        editBtn.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow >= 0) {
                int modelRow = taskTable.convertRowIndexToModel(selectedRow);
                List<Task> tasks = taskDAO.getTasksByUser(currentUser.getId());
                if (modelRow < tasks.size()) {
                    Task task = tasks.get(modelRow);
                    addEditTask(task);
                }
            } else {
                showError("Пожалуйста, выберите задачу для редактирования");
            }
        });
        deleteBtn.addActionListener(e -> deleteTask());
        viewBtn.addActionListener(e -> viewTaskDetails());
        refreshBtn.addActionListener(e -> loadTasks());

        logoutItem.addActionListener(e -> logout());
        exitItem.addActionListener(e -> System.exit(0));
        aboutItem.addActionListener(e -> showAbout());

        dailyReportItem.addActionListener(e -> generateReport("daily"));
        weeklyReportItem.addActionListener(e -> generateReport("weekly"));
        monthlyReportItem.addActionListener(e -> generateReport("monthly"));
        activityLogItem.addActionListener(e -> viewActivityLog());

        logDAO.logAction(currentUser.getId(), currentUser.getUsername(), "ОТКРЫТИЕ", "Открыто главное окно программы");
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 15, 8, 15));
        return btn;
    }

    private void viewTaskDetails() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Пожалуйста, выберите задачу для просмотра");
            return;
        }

        int modelRow = taskTable.convertRowIndexToModel(selectedRow);
        List<Task> tasks = taskDAO.getTasksByUser(currentUser.getId());
        if (modelRow >= tasks.size()) {
            showError("Ошибка: задача не найдена");
            return;
        }

        Task task = tasks.get(modelRow);

        String statusRu = "";
        switch(task.getStatus()) {
            case "PENDING": statusRu = "Ожидает"; break;
            case "IN_PROGRESS": statusRu = "В работе"; break;
            case "COMPLETED": statusRu = "Завершено"; break;
            case "CANCELLED": statusRu = "Отменено"; break;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String startTime = task.getStartTime() != null ? task.getStartTime().format(formatter) : "Не указано";
        String endTime = task.getEndTime() != null ? task.getEndTime().format(formatter) : "Не указано";

        // Информация о дедлайне
        String deadlineInfo = "";
        if (task.getEndTime() != null && !"COMPLETED".equals(task.getStatus())) {
            LocalDateTime now = LocalDateTime.now();
            if (task.getEndTime().isBefore(now)) {
                deadlineInfo = "\n[ВНИМАНИЕ] ДЕДЛАЙН ПРОСРОЧЕН!";
            } else {
                long daysUntil = ChronoUnit.DAYS.between(now.toLocalDate(), task.getEndTime().toLocalDate());
                if (daysUntil == 0) {
                    deadlineInfo = "\n[ВНИМАНИЕ] ДЕДЛАЙН СЕГОДНЯ!";
                } else if (daysUntil == 1) {
                    deadlineInfo = "\n[ВНИМАНИЕ] ДЕДЛАЙН ЗАВТРА!";
                } else if (daysUntil == 2) {
                    deadlineInfo = "\n[ВНИМАНИЕ] ДО ДЕДЛАЙНА 2 ДНЯ";
                } else if (daysUntil <= 5) {
                    deadlineInfo = "\n[ИНФОРМАЦИЯ] До дедлайна " + daysUntil + " дней";
                }
            }
        }

        String details =
                "====================================\n" +
                        "         ПОДРОБНОСТИ ЗАДАЧИ\n" +
                        "====================================\n\n" +
                        "Название: " + task.getTitle() + "\n\n" +
                        "Описание:\n" + (task.getDescription() != null && !task.getDescription().isEmpty() ?
                        task.getDescription() : "Описание отсутствует") + "\n\n" +
                        "Категория: " + (task.getCategory() != null ? task.getCategory() : "Не указана") + "\n" +
                        "Статус: " + statusRu + "\n" +
                        "Дата начала: " + startTime + "\n" +
                        "Дата окончания (Дедлайн): " + endTime + deadlineInfo + "\n" +
                        "Планируемое время: " + task.getEstimatedMinutes() + " мин (" + formatMinutes(task.getEstimatedMinutes()) + ")\n" +
                        "Фактическое время: " + task.getActualMinutes() + " мин (" + formatMinutes(task.getActualMinutes()) + ")\n" +
                        "Дата создания: " + java.time.Instant.ofEpochMilli(task.getCreatedAt())
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().format(formatter) + "\n";

        JTextArea textArea = new JTextArea(details);
        textArea.setEditable(false);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textArea.setMargin(new Insets(15, 15, 15, 15));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 500));

        JOptionPane.showMessageDialog(this, scrollPane, "Просмотр задачи: " + task.getTitle(), JOptionPane.INFORMATION_MESSAGE);

        logDAO.logAction(currentUser.getId(), currentUser.getUsername(), "ПРОСМОТР", "Просмотр описания задачи: " + task.getTitle());
    }

    private void toggleView() {
        String selected = (String) viewCombo.getSelectedItem();
        if ("Диаграмма Ганта".equals(selected)) {
            ganttPanel.removeAll();
            drawGanttChart();
            ganttPanel.setVisible(true);
            splitPane.setBottomComponent(ganttPanel);
            splitPane.setDividerLocation(400);
        } else {
            ganttPanel.setVisible(false);
            splitPane.setBottomComponent(null);
        }
        splitPane.revalidate();
        splitPane.repaint();
    }

    private void drawGanttChart() {
        ganttPanel.removeAll();

        List<Task> tasks = taskDAO.getTasksByUser(currentUser.getId());
        if (tasks.isEmpty()) {
            JLabel emptyLabel = new JLabel("Нет задач для отображения", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            emptyLabel.setForeground(Color.GRAY);
            ganttPanel.add(emptyLabel);
            ganttPanel.revalidate();
            ganttPanel.repaint();
            return;
        }

        List<Task> tasksWithDates = tasks.stream()
                .filter(t -> t.getStartTime() != null && t.getEndTime() != null)
                .collect(Collectors.toList());

        if (tasksWithDates.isEmpty()) {
            JLabel emptyLabel = new JLabel("Нет задач с указанными датами начала и окончания", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(Color.GRAY);
            ganttPanel.add(emptyLabel);
            ganttPanel.revalidate();
            ganttPanel.repaint();
            return;
        }

        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth() - 220;
                int startX = 180;
                int startY = 80;
                int barHeight = 35;
                int rowHeight = 45;

                g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2d.setColor(new Color(30, 58, 138));
                g2d.drawString("Диаграмма Ганта - Распределение задач по времени", 50, 30);

                LocalDateTime minTime = tasksWithDates.stream()
                        .map(Task::getStartTime)
                        .min(LocalDateTime::compareTo)
                        .orElse(LocalDateTime.now());

                LocalDateTime maxTime = tasksWithDates.stream()
                        .map(Task::getEndTime)
                        .max(LocalDateTime::compareTo)
                        .orElse(LocalDateTime.now().plusDays(7));

                long totalMinutes = ChronoUnit.MINUTES.between(minTime, maxTime);
                if (totalMinutes == 0) totalMinutes = 1;

                int y = startY;

                for (Task task : tasksWithDates) {
                    String rawCategory = task.getCategory() != null ? task.getCategory() : "Работа";
                    String cleanCategory = rawCategory.replaceAll("[💼🏠📚🤝💻📞✈️]", "").trim();
                    if (cleanCategory.isEmpty()) cleanCategory = "Работа";

                    Color categoryColor = categoryColors.getOrDefault(cleanCategory, categoryColors.get("General"));

                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    g2d.setColor(Color.BLACK);
                    String title = task.getTitle().length() > 25 ? task.getTitle().substring(0, 22) + "..." : task.getTitle();
                    g2d.drawString(title, 20, y + barHeight - 10);

                    g2d.setFont(new Font("Segoe UI", Font.ITALIC, 10));
                    g2d.setColor(Color.GRAY);
                    g2d.drawString(cleanCategory, 20, y + barHeight + 5);

                    long startOffset = ChronoUnit.MINUTES.between(minTime, task.getStartTime());
                    long duration = ChronoUnit.MINUTES.between(task.getStartTime(), task.getEndTime());

                    int barX = startX + (int)((double)startOffset / totalMinutes * width);
                    int barWidth = Math.max(5, (int)((double)duration / totalMinutes * width));

                    g2d.setColor(categoryColor);
                    g2d.fillRoundRect(barX, y, barWidth, barHeight, 8, 8);
                    g2d.setColor(categoryColor.darker());
                    g2d.drawRoundRect(barX, y, barWidth, barHeight, 8, 8);

                    if (barWidth > 50) {
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        String durationText = formatMinutes((int)duration);
                        g2d.drawString(durationText, barX + 5, y + barHeight - 8);
                    }

                    String statusText = "";
                    switch(task.getStatus()) {
                        case "COMPLETED": statusText = "Завершено"; break;
                        case "IN_PROGRESS": statusText = "В работе"; break;
                        case "CANCELLED": statusText = "Отменено"; break;
                        default: statusText = "Ожидает";
                    }


                    g2d.setColor(Color.BLACK);


                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    g2d.drawString(statusText, barX + barWidth + 5, y + barHeight - 8);

                    y += rowHeight;
                    if (y > getHeight() - 50) break;
                }

                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawLine(startX, startY - 15, startX + width, startY - 15);

                int numMarks = Math.min(8, Math.max(4, width / 80));

                for (int i = 0; i <= numMarks; i++) {
                    int x = startX + (i * width / numMarks);
                    LocalDateTime timePoint = minTime.plusMinutes(totalMinutes * i / numMarks);
                    g2d.drawLine(x, startY - 20, x, startY - 10);
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));

                    String timeStr;
                    if (timePoint.getYear() != minTime.getYear()) {
                        timeStr = timePoint.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    } else {
                        timeStr = timePoint.format(DateTimeFormatter.ofPattern("dd.MM"));
                    }

                    int textWidth = g2d.getFontMetrics().stringWidth(timeStr);
                    g2d.drawString(timeStr, x - textWidth / 2, startY - 22);
                }

                g2d.setFont(new Font("Segoe UI", Font.ITALIC, 10));
                g2d.setColor(new Color(100, 116, 139));
                String dateRange = minTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " - " +
                        maxTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                g2d.drawString("Временная шкала: " + dateRange, startX + 10, startY - 38);
            }
        };

        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(800, tasksWithDates.size() * 45 + 130));

        JScrollPane scrollPane = new JScrollPane(chartPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Цветные столбики задач (по категориям)"
        ));

        ganttPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        legendPanel.setBackground(Color.WHITE);

        String[] legendCategories = {"Работа", "Личное", "Учёба", "Встреча", "Разработка", "Звонок", "Командировка"};
        Color[] legendColors = {
                new Color(59, 130, 246), new Color(34, 197, 94), new Color(168, 85, 247),
                new Color(249, 115, 22), new Color(6, 182, 212), new Color(236, 72, 153), new Color(139, 69, 19)
        };

        for (int i = 0; i < legendCategories.length; i++) {
            JPanel colorBox = new JPanel();
            colorBox.setBackground(legendColors[i]);
            colorBox.setPreferredSize(new Dimension(15, 15));
            JLabel label = new JLabel(legendCategories[i]);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            item.setBackground(Color.WHITE);
            item.add(colorBox);
            item.add(label);
            legendPanel.add(item);
        }

        legendPanel.add(Box.createHorizontalStrut(20));

        JPanel deadlineLegend = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        deadlineLegend.setBackground(Color.WHITE);

        JPanel redBox = new JPanel();
        redBox.setBackground(new Color(255, 200, 200));
        redBox.setPreferredSize(new Dimension(15, 15));
        deadlineLegend.add(redBox);
        deadlineLegend.add(new JLabel("Просрочено"));

        JPanel orangeBox = new JPanel();
        orangeBox.setBackground(new Color(255, 220, 180));
        orangeBox.setPreferredSize(new Dimension(15, 15));
        deadlineLegend.add(orangeBox);
        deadlineLegend.add(new JLabel("Сегодня"));

        JPanel yellowBox = new JPanel();
        yellowBox.setBackground(new Color(255, 255, 180));
        yellowBox.setPreferredSize(new Dimension(15, 15));
        deadlineLegend.add(yellowBox);
        deadlineLegend.add(new JLabel("Завтра"));

        JPanel lightYellowBox = new JPanel();
        lightYellowBox.setBackground(new Color(255, 255, 200));
        lightYellowBox.setPreferredSize(new Dimension(15, 15));
        deadlineLegend.add(lightYellowBox);
        deadlineLegend.add(new JLabel("Через 2 дня"));

        JPanel greenBox = new JPanel();
        greenBox.setBackground(new Color(200, 255, 200));
        greenBox.setPreferredSize(new Dimension(15, 15));
        deadlineLegend.add(greenBox);
        deadlineLegend.add(new JLabel("Завершено"));

        legendPanel.add(deadlineLegend);

        ganttPanel.add(legendPanel, BorderLayout.SOUTH);

        ganttPanel.revalidate();
        ganttPanel.repaint();
    }

    private String formatMinutes(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours > 0) {
            return hours + "ч " + mins + "мин";
        }
        return mins + "мин";
    }

    private void loadTasks() {
        tableModel.setRowCount(0);
        List<Task> tasks = taskDAO.getTasksByUser(currentUser.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        for (Task task : tasks) {
            String statusRu = "";
            switch(task.getStatus()) {
                case "PENDING": statusRu = "Ожидает"; break;
                case "IN_PROGRESS": statusRu = "В работе"; break;
                case "COMPLETED": statusRu = "Завершено"; break;
                case "CANCELLED": statusRu = "Отменено"; break;
                default: statusRu = task.getStatus();
            }

            String cleanCategory = task.getCategory();
            if (cleanCategory != null) {
                cleanCategory = cleanCategory.replaceAll("[💼🏠📚🤝💻📞✈️]", "").trim();
                if (cleanCategory.isEmpty()) cleanCategory = "Работа";
            } else {
                cleanCategory = "Работа";
            }

            tableModel.addRow(new Object[]{
                    task.getTitle(),
                    cleanCategory,
                    task.getStartTime() != null ? task.getStartTime().format(formatter) : "Не указано",
                    task.getEndTime() != null ? task.getEndTime().format(formatter) : "Не указано",
                    task.getEstimatedMinutes(),
                    task.getActualMinutes(),
                    statusRu
            });
        }

        long overdueCount = tasks.stream()
                .filter(t -> !"COMPLETED".equals(t.getStatus()) && t.getEndTime() != null && t.getEndTime().isBefore(LocalDateTime.now()))
                .count();

        statusLabel.setText("Загружено задач: " + tasks.size() + " | Просрочено: " + overdueCount +
                " | Красный - просрочено, Оранжевый - сегодня, Жёлтый - завтра, Светло-жёлтый - через 2 дня, Зелёный - завершено");

        if ("Диаграмма Ганта".equals(viewCombo.getSelectedItem())) {
            ganttPanel.removeAll();
            drawGanttChart();
            ganttPanel.revalidate();
            ganttPanel.repaint();
        }
    }

    private void addEditTask(Task existingTask) {
        TaskDialog dialog = new TaskDialog(this, currentUser, existingTask);
        dialog.setVisible(true);
        loadTasks();
        logDAO.logAction(currentUser.getId(), currentUser.getUsername(),
                existingTask == null ? "ДОБАВЛЕНИЕ" : "РЕДАКТИРОВАНИЕ",
                existingTask == null ? "Добавлена новая задача" : "Отредактирована задача: " + existingTask.getTitle());
    }

    private void deleteTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Пожалуйста, выберите задачу для удаления");
            return;
        }

        int modelRow = taskTable.convertRowIndexToModel(selectedRow);
        List<Task> tasks = taskDAO.getTasksByUser(currentUser.getId());
        if (modelRow >= tasks.size()) {
            showError("Ошибка: задача не найдена");
            return;
        }

        Task task = tasks.get(modelRow);
        String title = task.getTitle();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Удалить задачу \"" + title + "\"?",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (taskDAO.deleteTask(task.getId(), currentUser.getId())) {
                logDAO.logAction(currentUser.getId(), currentUser.getUsername(), "УДАЛЕНИЕ", "Удалена задача: " + title);
                loadTasks();
                showInfo("Задача успешно удалена");
            } else {
                showError("Не удалось удалить задачу");
            }
        }
    }

    private void generateReport(String period) {
        List<Task> tasks = taskDAO.getTasksByUser(currentUser.getId());
        ReportGenerator generator = new ReportGenerator();

        try {
            if ("daily".equals(period)) {
                generator.generateDailyReport(tasks, currentUser);
                logDAO.logAction(currentUser.getId(), currentUser.getUsername(), "ОТЧЁТ", "Сгенерирован дневной отчёт");
                showInfo("Дневной отчёт успешно создан!");
            } else if ("weekly".equals(period)) {
                generator.generateWeeklyReport(tasks, currentUser);
                logDAO.logAction(currentUser.getId(), currentUser.getUsername(), "ОТЧЁТ", "Сгенерирован недельный отчёт");
                showInfo("Недельный отчёт успешно создан!");
            } else if ("monthly".equals(period)) {
                generator.generateMonthlyReport(tasks, currentUser);
                logDAO.logAction(currentUser.getId(), currentUser.getUsername(), "ОТЧЁТ", "Сгенерирован месячный отчёт");
                showInfo("Месячный отчёт успешно создан!");
            }
        } catch (Exception e) {
            showError("Ошибка при генерации отчёта: " + e.getMessage());
        }
    }

    private void viewActivityLog() {
        List<com.workscheduler.model.ActionLog> logs = logDAO.getLogsByUser(currentUser.getId(), 100);

        if (logs.isEmpty()) {
            showInfo("Журнал действий пуст");
            return;
        }

        JDialog logDialog = new JDialog(this, "Журнал действий", true);
        logDialog.setSize(850, 500);
        logDialog.setLocationRelativeTo(this);

        String[] columns = {"Время", "Действие", "Подробности"};
        DefaultTableModel logTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        for (com.workscheduler.model.ActionLog log : logs) {
            java.time.LocalDateTime dateTime = java.time.Instant.ofEpochMilli(log.getTimestamp())
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
            logTableModel.addRow(new Object[]{
                    dateTime.format(formatter),
                    log.getAction(),
                    log.getDetails()
            });
        }

        JTable logTable = new JTable(logTableModel);
        logTable.setRowHeight(28);
        logTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logTable);

        JButton closeBtn = new JButton("Закрыть");
        closeBtn.setBackground(new Color(100, 116, 139));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> logDialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeBtn);

        logDialog.setLayout(new BorderLayout());
        logDialog.add(scrollPane, BorderLayout.CENTER);
        logDialog.add(buttonPanel, BorderLayout.SOUTH);
        logDialog.setVisible(true);
    }

    private void viewAllLogs() {
        if (!"ADMIN".equals(currentUser.getRole())) {
            showError("Доступ запрещён. Только для администратора.");
            return;
        }

        List<com.workscheduler.model.ActionLog> logs = logDAO.getAllLogs(500);

        JDialog logDialog = new JDialog(this, "Все логи пользователей", true);
        logDialog.setSize(1050, 600);
        logDialog.setLocationRelativeTo(this);

        String[] columns = {"Пользователь", "Действие", "Подробности", "Время"};
        DefaultTableModel logTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        for (com.workscheduler.model.ActionLog log : logs) {
            java.time.LocalDateTime dateTime = java.time.Instant.ofEpochMilli(log.getTimestamp())
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
            logTableModel.addRow(new Object[]{
                    log.getUsername(),
                    log.getAction(),
                    log.getDetails(),
                    dateTime.format(formatter)
            });
        }

        JTable logTable = new JTable(logTableModel);
        logTable.setRowHeight(28);
        logTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logTable);

        JButton closeBtn = new JButton("Закрыть");
        closeBtn.setBackground(new Color(100, 116, 139));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.addActionListener(e -> logDialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeBtn);

        logDialog.setLayout(new BorderLayout());
        logDialog.add(scrollPane, BorderLayout.CENTER);
        logDialog.add(buttonPanel, BorderLayout.SOUTH);
        logDialog.setVisible(true);
    }

    private void showAbout() {
        String helpText = "КРАТКОЕ РУКОВОДСТВО ПОЛЬЗОВАТЕЛЯ\n\n" +
                "1. ДОБАВЛЕНИЕ ЗАДАЧИ\n" +
                "   - Нажмите кнопку Добавить\n" +
                "   - Заполните название, категорию, даты\n" +
                "   - Нажмите Сохранить\n\n" +
                "2. ДЕДЛАЙНЫ (цветовая индикация)\n" +
                "   - Дедлайн = дата и время окончания задачи\n" +
                "   - КРАСНЫЙ: дедлайн просрочен\n" +
                "   - ОРАНЖЕВЫЙ: дедлайн сегодня (время ещё не наступило)\n" +
                "   - ЖЁЛТЫЙ: до дедлайна 1 день\n" +
                "   - СВЕТЛО-ЖЁЛТЫЙ: до дедлайна 2 дня\n" +
                "   - ЗЕЛЁНЫЙ: задача завершена\n\n" +
                "3. СОРТИРОВКА\n" +
                "   - Нажмите на заголовок любой колонки\n" +
                "   - Повторный клик меняет порядок\n" +
                "   - Сортировка по дате учитывает год\n\n" +
                "4. ПРОСМОТР ОПИСАНИЯ\n" +
                "   - Выберите задачу и нажмите Просмотр\n" +
                "   - Или сделайте двойной клик по задаче\n\n" +
                "5. РЕДАКТИРОВАНИЕ ЗАДАЧИ\n" +
                "   - Выберите задачу и нажмите Редактировать\n\n" +
                "6. УДАЛЕНИЕ ЗАДАЧИ\n" +
                "   - Выберите задачу и нажмите Удалить\n\n" +
                "7. ОТЧЁТЫ\n" +
                "   - Меню Отчёты -> выберите период\n\n" +
                "8. ДИАГРАММА ГАНТА\n" +
                "   - Выберите вид Диаграмма Ганта\n\n";

        JOptionPane.showMessageDialog(this, helpText, "Помощь - Краткое руководство", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        logDAO.logAction(currentUser.getId(), currentUser.getUsername(), "ВЫХОД", "Пользователь вышел из системы");
        int confirm = JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите выйти?", "Выход", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Информация", JOptionPane.INFORMATION_MESSAGE);
    }
}