package ru.kostyanoy.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.connection.Exchanger;
import ru.kostyanoy.dataexchange.ClientExchanger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;

import static javax.swing.JOptionPane.showMessageDialog;

public class GUIFormer implements VisualPresenter {

    private JFrame frame;
    private JTextArea outputTextArea;
    private String name = "OpeChatCo";
    private String fullName = "The Open Chat for Cooperation";
    private Exchanger exchanger;
    private static final int REFRASH_TIMEOUT = 2000;
    private static final Logger log = LoggerFactory.getLogger(GUIFormer.class);

    public GUIFormer(Exchanger exchanger) {
        this.exchanger = exchanger;
    }

    @Override
    public void createMainWindow() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        frame = new JFrame(name + " - " + fullName);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setMinimumSize(new Dimension(640, 480));
        frame.setLocationByPlatform(true);
        frame.addWindowListener(new WindowListener() {
            public void windowActivated(WindowEvent event) {
            }

            public void windowClosed(WindowEvent event) {
            }

            public void windowDeactivated(WindowEvent event) {
            }

            public void windowDeiconified(WindowEvent event) {
            }

            public void windowIconified(WindowEvent event) {
            }

            public void windowOpened(WindowEvent event) {
            }

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

        //Output
        JPanel outputPanel = new JPanel();
        frame.add(outputPanel, BorderLayout.CENTER);
        outputPanel.setLayout(new BorderLayout());
        outputTextArea = new JTextArea(name + " started\n\n");
        outputTextArea.setEditable(false);
        JScrollPane outputTextScrolls = new JScrollPane(outputTextArea);

        outputPanel.add(outputTextScrolls);
        outputTextScrolls.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        outputTextScrolls.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        outputTextScrolls.createHorizontalScrollBar();
        outputTextScrolls.createVerticalScrollBar();

        //Input
        JPanel inputPanel = new JPanel();
        frame.add(inputPanel, BorderLayout.SOUTH);
        inputPanel.setLayout(new BorderLayout());
        JTextArea inputTextArea = new JTextArea("Enter your message here");
        JScrollPane inputTextScrolls = new JScrollPane(inputTextArea);

        inputPanel.add(inputTextScrolls, BorderLayout.CENTER);
        inputTextScrolls.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        inputTextScrolls.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        inputTextScrolls.createHorizontalScrollBar();
        inputTextScrolls.createVerticalScrollBar();
        JButton sendButton = new JButton("Send");
        sendButton.setMinimumSize(new Dimension(30, 60));
        sendButton.setFocusable(false);
        sendButton.addActionListener(e -> {
            if (inputTextArea.getText().isEmpty()) {
                return;
            }
            String messageText = inputTextArea.getText();
            outputTextArea.append("\nYour message:\n" + messageText + "\n");
            inputTextArea.replaceRange("", 0, inputTextArea.getText().length());
            exchanger.sendMessage(messageText);
        });

        inputPanel.add(sendButton, BorderLayout.EAST);

        //List of participants
        JPanel participantsPanel = new JPanel();
        frame.add(participantsPanel, BorderLayout.WEST);
        participantsPanel.setLayout(new BorderLayout());
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> participantList = new JList<>(listModel);
        participantList.setPrototypeCellValue("0123456789101112131415");
        participantList.setLayoutOrientation(JList.VERTICAL);


        JScrollPane participantsTextScrolls = new JScrollPane(participantList);

        participantsPanel.add(participantsTextScrolls);
        participantsTextScrolls.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        participantsTextScrolls.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        participantsTextScrolls.createHorizontalScrollBar();
        participantsTextScrolls.createVerticalScrollBar();

        //Window
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);

        String[] newParticipantList;
        String[] newMessages;
        while (((ClientExchanger) exchanger).getConnection().isConnected()) {
            newParticipantList = exchanger.getParticipants().clone();
            Arrays.sort(newParticipantList);
            listModel.clear();
            for (String participant : newParticipantList) {
                listModel.addElement(participant);
            }

            newMessages = exchanger.getIncomingMessages().clone();
            for (String message : newMessages) {
                outputTextArea.append(message + "\n");
            }
            exchanger.clearUnansweredMessages();

            try {
                Thread.sleep(REFRASH_TIMEOUT);
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
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
                "Chat nickname",
                JOptionPane.QUESTION_MESSAGE);
        if (userString == null) {
            askServerIP();
        }
        return userString;
    }

    @Override
    public int askExit(String message) {
        int userChoise = JOptionPane.showConfirmDialog(
                frame,
                message,
                "Exit", JOptionPane.YES_NO_OPTION);
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
