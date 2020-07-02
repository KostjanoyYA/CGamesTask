package ru.kostyanoy.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    GameMode gameMode;
    private static final Logger log = LoggerFactory.getLogger(StartGUIFormer.class);

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
        buttonPanel.setLayout(new BorderLayout());

        JButton singlePlayerButton = new JButton("Single player");
        singlePlayerButton.setFocusable(false);
        singlePlayerButton.setFont(FONT);
        singlePlayerButton.addActionListener(e -> {
            gameMode = new SinglePlayer();
            gameMode.playGame(client);
            frame.dispose();

        });
        buttonPanel.add(singlePlayerButton, BorderLayout.NORTH);

        JButton stressTestButton = new JButton("Stress test");
        stressTestButton.setFocusable(false);
        stressTestButton.setFont(FONT);
        stressTestButton.addActionListener(e -> {
            gameMode = new StressTest();
            gameMode.playGame(client);
            frame.dispose();
        });
        buttonPanel.add(singlePlayerButton, BorderLayout.SOUTH);

        //Window
        frame.setResizable(false);
        frame.setFont(FONT);
        frame.pack();
        frame.setVisible(true);
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
    public int askExit(String message) {
        int userChoise = JOptionPane.showConfirmDialog(
                frame,
                message,
                "Exit", JOptionPane.YES_NO_OPTION);
        return userChoise;
    }

    @Override
    public void showMessage(String message) {
        showMessageDialog(frame, message);
    }
}
