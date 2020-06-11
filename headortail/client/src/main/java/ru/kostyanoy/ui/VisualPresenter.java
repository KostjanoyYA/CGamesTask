package ru.kostyanoy.ui;

import javax.swing.*;

public interface VisualPresenter {
    void createMainWindow()  throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException;
    String askServerIP();
    String askNickName();
    void showMessage(String message);
    int askExit(String message);

}
