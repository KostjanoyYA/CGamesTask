package ru.kostyanoy.ui;

import ru.kostyanoy.dataexchange.ClientExchanger;
import ru.kostyanoy.mode.GameMode;
import ru.kostyanoy.mode.SinglePlayer;
import ru.kostyanoy.mode.StressTest;

import javax.swing.*;
import java.awt.*;

import static javax.swing.JOptionPane.showMessageDialog;

public class StartGUIFormer implements VisualPresenter {

    private JFrame frame;
    private final String name = "Choose 'Heads or tails' client";
    private static final Font FONT = new Font("Tahoma", Font.PLAIN, 14);
    private GameMode gameMode;

    @Override
    public void createMainWindow(ClientExchanger client) throws ClassNotFoundException,
            UnsupportedLookAndFeelException,
            InstantiationException,
            IllegalAccessException {
        javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        frame = new JFrame(name);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(320, 240));
        frame.setLocationByPlatform(true);

        //Buttons
        JPanel buttonPanel = new JPanel();
        frame.add(buttonPanel, BorderLayout.CENTER);
        buttonPanel.setLayout(new GridLayout(2, 1, 5, 5));

        JButton singlePlayerButton = createButton("Single player");
        singlePlayerButton.addActionListener(e -> chooseGameMode(new SinglePlayer(), client));
        buttonPanel.add(singlePlayerButton);

        JButton stressTestButton = createButton("Stress test");
        stressTestButton.addActionListener(e -> chooseGameMode(new StressTest(), client));
        buttonPanel.add(stressTestButton);

        //Window
        frame.setResizable(false);
        frame.setFont(FONT);
        frame.pack();
        frame.setVisible(true);
    }

    private void chooseGameMode(GameMode mode, ClientExchanger client) {
        gameMode = mode;
        frame.setVisible(false);
        frame.dispose();
        gameMode.playGame(client);
    }

    private JButton createButton(String text) {
        JButton singlePlayerButton = new JButton(text);
        singlePlayerButton.setFocusable(false);
        singlePlayerButton.setFont(FONT);
        return singlePlayerButton;
    }


    //PopUps
    @Override
    public String askServerIP() {
        String userString = JOptionPane.showInputDialog(
                frame,
                "Enter IP Address of the Server:",
                "Welcome to the " + name,
                JOptionPane.QUESTION_MESSAGE);
        if (userString == null) {
            System.exit(0);
        }
        return userString;
    }

    @Override
    public void showMessage(String message) {
        showMessageDialog(frame, message);
    }
}
