package com.workscheduler;

import com.formdev.flatlaf.FlatLightLaf;
import com.workscheduler.ui.LoginFrame;
import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            throwable.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "ОШИБКА ВЫПОЛНЕНИЯ ПРОГРАММЫ\n\n" +
                            "Сообщение: " + throwable.getMessage() +
                            "\n\nРЕКОМЕНДАЦИИ:\n" +
                            "1. Перезапустите программу\n" +
                            "2. Убедитесь, что есть права на запись в папке\n" +
                            "3. При ошибке БД - удалите work_scheduler.db\n" +
                            "4. Сообщите разработчику: " + throwable.getClass().getSimpleName(),
                    "Критическая ошибка",
                    JOptionPane.ERROR_MESSAGE);
        });
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}