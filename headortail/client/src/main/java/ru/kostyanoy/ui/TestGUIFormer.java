package ru.kostyanoy.ui;

import ru.kostyanoy.mode.StressTest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class TestGUIFormer {

    private JFrame frame;
    private JButton startTestButton;
    private final StressTest stressTest;
    private static final Font FONT = new Font("Tahoma", Font.PLAIN, 14);

    public TestGUIFormer(StressTest stressTest) {
        this.stressTest = stressTest;
    }

    public void createMainWindow() throws ClassNotFoundException,
            UnsupportedLookAndFeelException,
            InstantiationException,
            IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        frame = new JFrame("Heads and Tails Test Client");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setMinimumSize(new Dimension(320, 240));
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
                    stressTest.stopTest();
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
        inputPanel.setLayout(new GridLayout(3, 1, 5, 5));

        JTextField clientCountField = createFieldOnPanel("1", inputPanel);
        JTextField requestIntervalField = createFieldOnPanel("1000", inputPanel);
        JTextField requestCountField = createFieldOnPanel("5", inputPanel);

        //Left panel
        JPanel labelPanel = new JPanel();
        frame.add(labelPanel, BorderLayout.WEST);
        labelPanel.setLayout(new GridLayout(3, 1, 5, 5));

        createLabelOnPanel("Client count:", labelPanel);
        createLabelOnPanel("Request interval (ms):", labelPanel);
        createLabelOnPanel("Request count:", labelPanel);

        //Button panel
        JPanel buttonPanel = new JPanel();
        frame.add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.setLayout(new BorderLayout());

        startTestButton = new JButton("Start test");
        startTestButton.setMinimumSize(new Dimension(30, 100));
        startTestButton.setFocusable(false);
        startTestButton.setFont(FONT);

        startTestButton.addActionListener(e -> {
            if (startTestButton.isEnabled()) {
                startTestButton.setEnabled(false);
                startTestButton.setText("Test is in the progress...");
            }
            stressTest.startTest(
                    Integer.parseInt(clientCountField.getText()),
                    Integer.parseInt(requestIntervalField.getText()),
                    Integer.parseInt(requestCountField.getText()));
        });
        buttonPanel.add(startTestButton, BorderLayout.CENTER);

        //Window
        frame.setResizable(false);
        frame.setFont(FONT);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createLabelOnPanel(String text, JPanel panel) {
        JLabel label = new JLabel(text);
        label.setFont(FONT);
        panel.add(label);
    }

    private JTextField createFieldOnPanel(String text, JPanel panel) {
        JTextField field = new JTextField(String.valueOf(Integer.MAX_VALUE).length());
        field.setText(text);
        field.setFont(FONT);
        panel.add(field);
        return field;
    }
}
