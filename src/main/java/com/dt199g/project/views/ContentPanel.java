package com.dt199g.project.views;

import com.dt199g.project.support.Constants;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

/**
 * ContentPanel component which contains the chatbot swing components.
 * @author Albin Eliasson
 */
public class ContentPanel extends JPanel {
    private JTextArea contentArea;
    private JTextArea inputField;
    private JButton sendMessageButton;

    /**
     * Not utilized ContentPanel constructor.
     */
    public ContentPanel() { }

    /**
     * Main method for initializing the content area, input field and
     * send message button swing components.
     */
    public void initializeContentPanel() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEtchedBorder());
        contentArea = initializeContentArea(this);
        JPanel inputPanel = initializeInputPanel(this);
        inputField = initializeInputField(inputPanel);
        sendMessageButton = initializeSendMessageButton(inputPanel);
    }

    /**
     * Method for initializing the JTextArea content area representing
     * the application chat.
     * @param jPanel the ContentPanel component.
     * @return the content area.
     */
    private JTextArea initializeContentArea(final JPanel jPanel) {
        JTextArea contentArea = new JTextArea();
        customizeContentArea(contentArea);

        JScrollPane scrollPane = new JScrollPane(contentArea);
        jPanel.add(scrollPane, BorderLayout.CENTER);

        return contentArea;
    }

    /**
     * Method for customizing the JTextArea content area component.
     * @param textArea the content area.
     */
    private void customizeContentArea(final JTextArea textArea) {
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monaco", Font.PLAIN, Constants.CONTENT_AREA_FONT_SIZE));
    }

    /**
     * Method for initializing the JPanel input panel containing the user
     * text message input field, and JButton send message button.
     * @param jPanel the ContentPanel component.
     * @return the input panel.
     */
    private JPanel initializeInputPanel(final JPanel jPanel) {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.setPreferredSize(new Dimension(Constants.DEFAULT_WINDOW_WIDTH, (Constants.INPUT_TEXT_PANEL_HEIGHT)));

        jPanel.add(inputPanel, BorderLayout.SOUTH);

        return inputPanel;
    }

    /**
     * Method for initializing the JTextArea input field representing the
     * user text message input field.
     * @param jPanel the input panel.
     * @return the input field.
     */
    private JTextArea initializeInputField(final JPanel jPanel) {
        JTextArea inputField = new JTextArea();
        customizeInputField(inputField);

        JScrollPane scrollPane = new JScrollPane(inputField);
        jPanel.add(scrollPane);

        return inputField;
    }

    /**
     * Method for customizing the JTextArea input field component.
     * @param textArea the input field.
     */
    private void customizeInputField(final JTextArea textArea) {
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Monaco", Font.PLAIN, Constants.INPUT_AREA_FONT_SIZE));
        textArea.setBorder(BorderFactory.createBevelBorder(0));
    }

    /**
     * Method for initializing the JButton send message button used to send the user
     * message to chatbot.
     * @param jPanel the input panel.
     * @return the send message button.
     */
    private JButton initializeSendMessageButton(final JPanel jPanel) {
        JButton sendMessageButton = new JButton(Constants.SEND_MESSAGE_BUTTON_TEXT);
        customizeSendMessageButton(sendMessageButton);

        jPanel.add(sendMessageButton);

        return sendMessageButton;
    }

    /**
     * Method for customizing the JButton send message button component.
     * @param button the send message button.
     */
    private void customizeSendMessageButton(final JButton button) {
        button.setFont(new Font("Monaco", Font.BOLD, Constants.SEND_MESSAGE_BUTTON_FONT_SIZE));
        button.setPreferredSize(new Dimension(
                Constants.SEND_MESSAGE_BUTTON_WIDTH, Constants.SEND_MESSAGE_BUTTON_HEIGHT));
        button.setBackground(Color.LIGHT_GRAY);
        button.setBorder(BorderFactory.createBevelBorder(0));
    }

    /**
     * Method for appending user and bot messages to show the conversation in
     * the content area.
     * @param message user or bot message.
     */
    public void setContentArea(final String message) {
        contentArea.append(message);
    }

    /**
     * Method for creating a Flowable to push send message button event clicks.
     * @return a Flowable of send message button action events.
     */
    public Flowable<ActionEvent> sendMessageButtonEvents() {
        return Flowable.create(emitter ->
                sendMessageButton.addActionListener(emitter::onNext), BackpressureStrategy.LATEST);
    }

    /**
     * Method for accessing user messages from the JTextArea input field component.
     * @return the message.
     */
    public String getUserMessageInput() {
        return inputField.getText();
    }

    /**
     * Method for resetting the JTextArea input field.
     */
    public void resetUserMessageInput() {
        inputField.selectAll();
        inputField.replaceSelection("");
    }
}
