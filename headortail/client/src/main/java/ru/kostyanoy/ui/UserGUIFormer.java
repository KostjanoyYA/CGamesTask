package ru.kostyanoy.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.dataexchange.ClientExchanger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;

import static javax.swing.JOptionPane.showMessageDialog;

public class UserGUIFormer implements VisualPresenter {

    private JFrame frame;
    private JComboBox comboBox;
    private final String name = "Heads and tails client";
    private final ClientExchanger exchanger;
    private static final int REFRESH_TIMEOUT = 1000;
    private static final Font FONT = new Font("Tahoma", Font.PLAIN, 14);
    private static final Logger log = LoggerFactory.getLogger(UserGUIFormer.class);

    public UserGUIFormer(ClientExchanger exchanger) {
        this.exchanger = exchanger;
    }

    @Override
    public void createMainWindow() throws ClassNotFoundException,
            UnsupportedLookAndFeelException,
            InstantiationException,
            IllegalAccessException {
        javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        frame = new JFrame(name);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setMinimumSize(new Dimension(320, 240));
        frame.setLocationByPlatform(true);
        frame.addWindowListener(new WindowListener() {
            public void windowActivated(WindowEvent event) {}

            public void windowClosed(WindowEvent event) {}

            public void windowDeactivated(WindowEvent event) {}

            public void windowDeiconified(WindowEvent event) {}

            public void windowIconified(WindowEvent event) {}

            public void windowOpened(WindowEvent event) {}

            public void windowClosing(WindowEvent event) {
                Object[] options = {"Exit", "Cancel!"};
                int rc = JOptionPane.showOptionDialog(
                        event.getWindow(), "Exit now?",
                        "Exit confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);
                if (rc == 0) {
                    event.getWindow().setVisible(false);
                    exchanger.stopExchange();
                    System.exit(0);
                }
            }
        });

        //Menu
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> frame.getWindowListeners()[0].windowClosing(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));

        JMenu jMenu = new JMenu("File");
        jMenu.add(exitItem);

        JMenuBar jMenuBar = new JMenuBar();
        jMenuBar.add(jMenu);

        frame.setJMenuBar(jMenuBar);

        //Right panel
        JPanel inputPanel = new JPanel();
        frame.add(inputPanel, BorderLayout.EAST);
        inputPanel.setLayout(new GridLayout(4, 1, 5, 5));

        JTextField betField = new JTextField(String.valueOf(Integer.MAX_VALUE).length());
        betField.setText("1");
        betField.setFont(FONT);
        inputPanel.add(betField);

        String[] items = exchanger.getPossibleOptions();
        comboBox = new JComboBox(items);
        inputPanel.add(comboBox);

        JLabel roundResultValue = new JLabel(exchanger.getPreviousRoundResult());
        roundResultValue.setFont(FONT);
        inputPanel.add(roundResultValue);


        JLabel accountLabel = new JLabel("0");
        accountLabel.setFont(FONT);
        inputPanel.add(accountLabel);

        //Left panel
        JPanel labelPanel = new JPanel();
        frame.add(labelPanel, BorderLayout.WEST);
        labelPanel.setLayout(new GridLayout(4, 1, 5, 5));

        JLabel betLabel = new JLabel("Enter your bet here:");
        betLabel.setFont(FONT);
        labelPanel.add(betLabel);

        JLabel yourChoice = new JLabel("Your choice:");
        yourChoice.setFont(FONT);
        labelPanel.add(yourChoice);

        JLabel roundResult = new JLabel("Previous round result:");
        roundResult.setFont(FONT);
        labelPanel.add(roundResult);

        JLabel yourAccount = new JLabel("Your account:");
        yourAccount.setFont(FONT);
        labelPanel.add(yourAccount);

        //Button panel
        JPanel buttonPanel = new JPanel();
        frame.add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.setLayout(new BorderLayout());

        JButton makeBetButton = new JButton("Make a bet");
        makeBetButton.setMinimumSize(new Dimension(30, 100));
        makeBetButton.setFocusable(false);
        makeBetButton.setFont(FONT);
        makeBetButton.addActionListener(e -> {
            exchanger.sendStake(Math.abs(Long.parseLong(betField.getText())), (String) comboBox.getSelectedItem());
            log.debug("comboBox.getSelectedItem() = {}", comboBox.getSelectedItem());
        });

        buttonPanel.add(makeBetButton, BorderLayout.CENTER);

        //Window
        frame.setResizable(false);
        frame.setFont(FONT);
        frame.pack();
        frame.setVisible(true);

        while (!Thread.interrupted()) {
            accountLabel.setText(String.valueOf(exchanger.getPlayerState().getTokenCount()));
            refreshComboBox();
            roundResultValue.setText(exchanger.getPreviousRoundResult());
            try {
                Thread.sleep(REFRESH_TIMEOUT);
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
                break;
            }
        }
    }

    private void refreshComboBox() {
        //TODO Исправить список в Combobox
        for (int i = comboBox.getItemCount()-1; i >= 0; i--) {
            if (!Arrays.asList(exchanger.getPossibleOptions()).contains(comboBox.getItemAt(i))) {
                comboBox.remove(i);
            }
        }

        for (int i = 0; i < exchanger.getPossibleOptions().length; i++) {
            for (int j = 0; j < comboBox.getItemCount(); j++) {
                if (comboBox.getItemAt(j).equals(exchanger.getPossibleOptions()[i])) {
                    continue;
                }
                comboBox.addItem(exchanger.getPossibleOptions()[i]);
            }
        }
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
            //frame.getWindowListeners()[0].windowClosing(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            System.exit(0);
        }
        return userString;

    }

    @Override
    public String askNickName() {
        String userString = JOptionPane.showInputDialog(
                frame,
                "Enter your Nickname:",
                "Game nickname",
                JOptionPane.QUESTION_MESSAGE);
        return userString;
    }

    @Override
    public int askExit(String message) {
        int userChoise = JOptionPane.showConfirmDialog(
                frame,
                message,
                "Exit", JOptionPane.YES_NO_OPTION);
        log.debug("userChoise = {}", userChoise);
        if (userChoise == JOptionPane.NO_OPTION) {
            exchanger.stopExchange();
            System.exit(0);
        }
        return userChoise;
    }

    @Override
    public void showMessage(String message) {
        showMessageDialog(frame, message);
    }
}
