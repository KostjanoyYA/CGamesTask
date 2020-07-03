package ru.kostyanoy.ui;

import ru.kostyanoy.dataexchange.ClientExchanger;

import javax.swing.*;

public interface VisualPresenter {
    void createMainWindow(ClientExchanger client) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException;
    String askServerIP();
    void showMessage(String message);
}
