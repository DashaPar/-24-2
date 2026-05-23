package com.workscheduler.ui;

import com.workscheduler.database.UserDAO;
import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JTextField fullNameField;
    private UserDAO userDAO;

    public RegisterDialog(JFrame parent) {
        super(parent, "📝 Регистрация нового пользователя", true);
        userDAO = new UserDAO();
        initUI();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setSize(450, 480);
        setLayout(new BorderLayout());

        // Основная панель
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Заголовок
        JLabel titleLabel = new JLabel("Создание аккаунта");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(30, 58, 138));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Поле логина
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("👤 Логин:*"), gbc);
        usernameField = new JTextField(15);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1;
        mainPanel.add(usernameField, gbc);

        // Поле ФИО
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("📛 ФИО:*"), gbc);
        fullNameField = new JTextField(15);
        fullNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1;
        mainPanel.add(fullNameField, gbc);

        // Поле пароля
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("🔒 Пароль:*"), gbc);
        passwordField = new JPasswordField(15);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1;
        mainPanel.add(passwordField, gbc);

        // Подтверждение пароля
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("✅ Подтверждение:*"), gbc);
        confirmField = new JPasswordField(15);
        confirmField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1;
        mainPanel.add(confirmField, gbc);

        // Сообщение об ошибке
        JLabel errorLabel = new JLabel();
        errorLabel.setForeground(new Color(220, 38, 38));
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        mainPanel.add(errorLabel, gbc);

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton registerBtn = new JButton("📝 Зарегистрироваться");
        registerBtn.setBackground(new Color(30, 58, 138));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerBtn.setFocusPainted(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton cancelBtn = new JButton("Отмена");
        cancelBtn.setBackground(new Color(100, 116, 139));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridy = 6;
        mainPanel.add(buttonPanel, gbc);

        // Обработчики
        registerBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());

            if (username.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
                errorLabel.setText("❌ Все поля со звёздочкой (*) обязательны");
                return;
            }

            if (!password.equals(confirm)) {
                errorLabel.setText("❌ Пароли не совпадают");
                return;
            }

            if (password.length() < 4) {
                errorLabel.setText("❌ Пароль должен содержать минимум 4 символа");
                return;
            }

            if (userDAO.registerUser(username, password, fullName)) {
                JOptionPane.showMessageDialog(this,
                        "✅ Регистрация успешна! Теперь вы можете войти.",
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                errorLabel.setText("❌ Пользователь с таким логином уже существует");
            }
        });

        cancelBtn.addActionListener(e -> dispose());

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }
}