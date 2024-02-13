package com.dt199g.project.views;

import com.dt199g.project.support.Constants;

import javax.swing.JFrame;
import java.awt.BorderLayout;

/**
 * The main JFrame which is the window frame of the chatbot.
 * @author Albin Eliasson
 */
public class MainFrame extends JFrame {
    private final ContentPanel contentPanel;

    /**
     * Constructor to initialize the content panel.
     * @param contentPanel the content panel.
     */
    public MainFrame(final ContentPanel contentPanel) {
        this.contentPanel = contentPanel;
    }

    /**
     * Method to initialize and show the GUI swing components.
     */
    public void createAndShowGUI() {
        this.setSize(Constants.DEFAULT_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setTitle(Constants.APP_HEADER);
        this.setLayout(new BorderLayout());
        contentPanel.initializeContentPanel();
        contentPanel.setBounds(0, 0, getWidth(), getHeight());
        this.getContentPane().add(contentPanel, BorderLayout.CENTER);
        this.setVisible(true);
    }
}
