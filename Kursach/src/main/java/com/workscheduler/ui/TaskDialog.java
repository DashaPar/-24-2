package com.workscheduler.ui;

import com.workscheduler.database.TaskDAO;
import com.workscheduler.model.User;
import com.workscheduler.model.Task;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class TaskDialog extends JDialog {
    private JTextField titleField;
    private JTextArea descArea;
    private JComboBox<String> categoryCombo;
    private DatePicker startDatePicker;
    private TimePicker startTimePicker;
    private DatePicker endDatePicker;
    private TimePicker endTimePicker;
    private JSpinner estimatedSpinner;
    private JSpinner actualSpinner;
    private JComboBox<String> statusCombo;
    private TaskDAO taskDAO;
    private User user;
    private Task existingTask;
    private boolean success = false;

    public TaskDialog(JFrame parent, User user, Task existingTask) {
        super(parent, existingTask == null ? "Новая задача" : "Редактирование задачи", true);
        this.user = user;
        this.existingTask = existingTask;
        this.taskDAO = new TaskDAO();
        initUI();
        if (existingTask != null) {
            loadTaskData();
        }
        setSize(600, 650);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        JLabel titleLabel = new JLabel(existingTask == null ? "Новая задача" : "Редактирование задачи");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(30, 58, 138));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        row++;

        JSeparator sep = new JSeparator();
        gbc.gridy = row;
        mainPanel.add(sep, gbc);
        row++;

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Название задачи:*"), gbc);
        titleField = new JTextField(20);
        titleField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1;
        mainPanel.add(titleField, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Описание:"), gbc);
        descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane scrollPane = new JScrollPane(descArea);
        gbc.gridx = 1;
        mainPanel.add(scrollPane, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Категория:"), gbc);
        String[] categories = {"Работа", "Личное", "Учёба", "Встреча", "Разработка", "Звонок", "Командировка"};
        categoryCombo = new JComboBox<>(categories);
        categoryCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        gbc.gridx = 1;
        mainPanel.add(categoryCombo, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Дата начала:"), gbc);
        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        startPanel.setBackground(Color.WHITE);
        startDatePicker = new DatePicker();
        startTimePicker = new TimePicker();
        startPanel.add(startDatePicker);
        startPanel.add(startTimePicker);
        gbc.gridx = 1;
        mainPanel.add(startPanel, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Дата окончания:"), gbc);
        JPanel endPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        endPanel.setBackground(Color.WHITE);
        endDatePicker = new DatePicker();
        endTimePicker = new TimePicker();
        endPanel.add(endDatePicker);
        endPanel.add(endTimePicker);
        gbc.gridx = 1;
        mainPanel.add(endPanel, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Планируемое время (мин):"), gbc);
        estimatedSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 15));
        estimatedSpinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        gbc.gridx = 1;
        mainPanel.add(estimatedSpinner, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Фактическое время (мин):"), gbc);
        actualSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 15));
        actualSpinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        gbc.gridx = 1;
        mainPanel.add(actualSpinner, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Статус:"), gbc);
        String[] statuses = {"Ожидает", "В работе", "Завершено", "Отменено"};
        statusCombo = new JComboBox<>(statuses);
        statusCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        gbc.gridx = 1;
        mainPanel.add(statusCombo, gbc);
        row++;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveBtn = new JButton("Сохранить");
        saveBtn.setBackground(new Color(30, 58, 138));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton cancelBtn = new JButton("Отмена");
        cancelBtn.setBackground(new Color(100, 116, 139));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        saveBtn.addActionListener(e -> saveAndClose());
        cancelBtn.addActionListener(e -> dispose());

        add(mainPanel, BorderLayout.CENTER);
    }

    private void loadTaskData() {
        titleField.setText(existingTask.getTitle());
        descArea.setText(existingTask.getDescription());
        if (existingTask.getCategory() != null) {
            categoryCombo.setSelectedItem(existingTask.getCategory());
        }
        if (existingTask.getStartTime() != null) {
            startDatePicker.setDate(existingTask.getStartTime().toLocalDate());
            startTimePicker.setTime(existingTask.getStartTime().toLocalTime());
        }
        if (existingTask.getEndTime() != null) {
            endDatePicker.setDate(existingTask.getEndTime().toLocalDate());
            endTimePicker.setTime(existingTask.getEndTime().toLocalTime());
        }
        estimatedSpinner.setValue(existingTask.getEstimatedMinutes());
        actualSpinner.setValue(existingTask.getActualMinutes());

        String statusRu = "";
        switch(existingTask.getStatus()) {
            case "PENDING": statusRu = "Ожидает"; break;
            case "IN_PROGRESS": statusRu = "В работе"; break;
            case "COMPLETED": statusRu = "Завершено"; break;
            case "CANCELLED": statusRu = "Отменено"; break;
        }
        statusCombo.setSelectedItem(statusRu);
    }

    private LocalDateTime getDateTimeFromPickers(DatePicker datePicker, TimePicker timePicker) {
        LocalDate date = datePicker.getDate();
        LocalTime time = timePicker.getTime();
        if (date != null && time != null) {
            return LocalDateTime.of(date, time);
        } else if (date != null) {
            return date.atStartOfDay();
        }
        return null;
    }

    private String mapStatusToEn(String statusRu) {
        if (statusRu.contains("Ожидает")) return "PENDING";
        if (statusRu.contains("В работе")) return "IN_PROGRESS";
        if (statusRu.contains("Завершено")) return "COMPLETED";
        if (statusRu.contains("Отменено")) return "CANCELLED";
        return "PENDING";
    }

    private void saveAndClose() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, укажите название задачи", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDateTime startTime = getDateTimeFromPickers(startDatePicker, startTimePicker);
        LocalDateTime endTime = getDateTimeFromPickers(endDatePicker, endTimePicker);

        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            JOptionPane.showMessageDialog(this, "Время окончания не может быть раньше времени начала", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String statusEn = mapStatusToEn((String) statusCombo.getSelectedItem());

        boolean operationSuccess = false;

        if (existingTask == null) {
            Task task = new Task(0, user.getId(), title, descArea.getText(),
                    (String) categoryCombo.getSelectedItem(), startTime, endTime,
                    (int) estimatedSpinner.getValue(), (int) actualSpinner.getValue(),
                    statusEn, System.currentTimeMillis());

            if (taskDAO.addTask(task)) {
                JOptionPane.showMessageDialog(this, "Задача успешно добавлена!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                operationSuccess = true;
            } else {
                JOptionPane.showMessageDialog(this, "Не удалось добавить задачу", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            existingTask.setTitle(title);
            existingTask.setDescription(descArea.getText());
            existingTask.setCategory((String) categoryCombo.getSelectedItem());
            existingTask.setStartTime(startTime);
            existingTask.setEndTime(endTime);
            existingTask.setEstimatedMinutes((int) estimatedSpinner.getValue());
            existingTask.setActualMinutes((int) actualSpinner.getValue());
            existingTask.setStatus(statusEn);

            if (taskDAO.updateTask(existingTask)) {
                JOptionPane.showMessageDialog(this, "Задача успешно обновлена!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                operationSuccess = true;
            } else {
                JOptionPane.showMessageDialog(this, "Не удалось обновить задачу", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }

        if (operationSuccess) {
            dispose(); // Закрываем окно только при успехе
        }
    }
}