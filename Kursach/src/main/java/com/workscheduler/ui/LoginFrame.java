package com.workscheduler.ui;

import com.workscheduler.database.UserDAO;
import com.workscheduler.database.ActionLogDAO;
import com.workscheduler.model.User;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private UserDAO userDAO;
    private ActionLogDAO logDAO;

    public LoginFrame() {
        userDAO = new UserDAO();
        logDAO = new ActionLogDAO();
        initUI();
    }

    private void initUI() {
        setTitle("Учёт рабочего времени - Вход в систему");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(30, 58, 138), 0, getHeight(), new Color(59, 130, 246));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 12, 12, 12);

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.fill = GridBagConstraints.HORIZONTAL;
        cardGbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Учёт рабочего времени");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 58, 138));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cardGbc.gridx = 0;
        cardGbc.gridy = 0;
        cardGbc.gridwidth = 2;
        cardPanel.add(titleLabel, cardGbc);

        JLabel subtitleLabel = new JLabel("Планировщик задач");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cardGbc.gridy = 1;
        cardPanel.add(subtitleLabel, cardGbc);

        JSeparator separator = new JSeparator();
        cardGbc.gridy = 2;
        cardPanel.add(separator, cardGbc);

        cardGbc.gridy = 3;
        cardGbc.gridwidth = 1;
        JLabel userLabel = new JLabel("Логин:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cardPanel.add(userLabel, cardGbc);

        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        cardGbc.gridx = 1;
        cardPanel.add(usernameField, cardGbc);

        cardGbc.gridx = 0;
        cardGbc.gridy = 4;
        JLabel passLabel = new JLabel("Пароль:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cardPanel.add(passLabel, cardGbc);

        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        cardGbc.gridx = 1;
        cardPanel.add(passwordField, cardGbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton loginBtn = new JButton("Войти");
        loginBtn.setBackground(new Color(30, 58, 138));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton registerBtn = new JButton("Регистрация");
        registerBtn.setBackground(new Color(100, 116, 139));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerBtn.setFocusPainted(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);

        cardGbc.gridx = 0;
        cardGbc.gridy = 5;
        cardGbc.gridwidth = 2;
        cardPanel.add(buttonPanel, cardGbc);

        JLabel errorLabel = new JLabel();
        errorLabel.setForeground(new Color(220, 38, 38));
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cardGbc.gridy = 6;
        cardPanel.add(errorLabel, cardGbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(cardPanel, gbc);

        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Пожалуйста, заполните все поля");
                return;
            }

            User user = userDAO.authenticate(username, password);
            if (user != null) {
                logDAO.logAction(user.getId(), user.getUsername(), "ВХОД", "Пользователь вошёл в систему");
                new MainFrame(user);
                dispose();
            } else {
                errorLabel.setText("Неверный логин или пароль");
            }
        });

        registerBtn.addActionListener(e -> new RegisterDialog(this));

        add(mainPanel);
    }
}