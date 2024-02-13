package com.dt199g.project.controllers;

import com.dt199g.project.models.ChatModel;
import com.dt199g.project.views.ContentPanel;
import com.dt199g.project.views.MainFrame;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import javax.swing.SwingUtilities;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * The AppController responsible for initializing and handling the user and chatbot messages
 * and acts as a bridge between the model and view components.
 * @author Albin Eliasson
 */
public class AppController {
    private final MainFrame mainFrame;
    private final ContentPanel contentPanel;
    private final ChatModel chatModel;
    private Flowable<String> sendMessageEventObservable;
    private final CompositeDisposable disposables;

    /**
     * Constructor used to initialize the chat model, content panel, main frame
     * and CompositeDisposable.
     */
    public AppController() {
        contentPanel = new ContentPanel();
        mainFrame = new MainFrame(contentPanel);
        chatModel = new ChatModel();
        disposables = new CompositeDisposable();
    }

    /**
     * Method for calling the initialization of the GUI components and the send message
     * event observable on the EDT.
     */
    public void startChatBot() {
        SwingUtilities.invokeLater(() -> {
            mainFrame.createAndShowGUI();
            sendMessageEventObservable = handleSendMessageButtonEvents();
            handleUserMessage(sendMessageEventObservable);
            handleRespondMessage(sendMessageEventObservable);
        });
    }

    /**
     * Method for calling the appending of user and bot messages to show the conversation in
     * the content area.
     * @param message user or chatbot message.
     */
    private void sendMessageToUser(final String message) {
        contentPanel.setContentArea(message);
    }

    /**
     * Method for handling user messages from the message flowable by appending the user
     * chat name and sending the message to the user.
     * @param messageFlowable the message flowable containing user messages.
     */
    private void handleUserMessage(final Flowable<String> messageFlowable) {
        Disposable messageDisposable = messageFlowable.map(this::addUserChatName)
                .observeOn(Schedulers.from(SwingUtilities::invokeLater))
                .subscribe(this::sendMessageToUser);

        addDisposables(messageDisposable);
    }

    /**
     * Method for handling chatbot respond messages from the message flowable by getting
     * the response messages from the chat model,
     * appending the chatbot username and sending the message to the user.
     * @param messageFlowable the message flowable containing user messages.
     */
    private void handleRespondMessage(final Flowable<String> messageFlowable) {
        Disposable messageDisposable = messageFlowable
                .observeOn(Schedulers.computation())
                .debounce(500, TimeUnit.MILLISECONDS) // Add a 500ms delay for response processing
                .flatMap(this::getRespondMessage)
                .map(this::addBotChatName)
                .observeOn(Schedulers.from(SwingUtilities::invokeLater))
                .subscribe(this::sendMessageToUser);

        addDisposables(messageDisposable);
    }

    /**
     * Method for handling the send message button event observable by accessing the typed
     * user message and resetting the input field.
     * @return a flowable containing user messages.
     */
    private Flowable<String> handleSendMessageButtonEvents() {
        return contentPanel.sendMessageButtonEvents()
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .map(event -> getUserMessages())
                .filter(userMessage -> !userMessage.isEmpty())
                .doOnNext(userMessage -> resetMessageInput())
                .publish()
                .autoConnect();
    }

    /**
     * Method for calling the resetting of the input field.
     */
    private void resetMessageInput() {
        contentPanel.resetUserMessageInput();
    }

    /**
     * Method for accessing the typed user message from the input field.
     * @return the user message.
     */
    private String getUserMessages() {
        return contentPanel.getUserMessageInput();
    }

    /**
     * Method for calling the get chatbot response message.
     * @param message the user message.
     * @return a flowable containing chatbot responses.
     */
    private Flowable<String> getRespondMessage(final String message) {
        return chatModel.getResponse(message).toFlowable(BackpressureStrategy.BUFFER);
    }

    /**
     * Method for calling the appending of the user chat name to user message.
     * @param message the user message.
     * @return the user message with added username.
     */
    private String addUserChatName(final String message) {
        return chatModel.addUserChatName(message);
    }

    /**
     * Method for calling the appending of the chatbot chat name to chatbot response message.
     * @param message the chatbot response.
     * @return the chatbot response with added username.
     */
    private String addBotChatName(final String message) {
        return chatModel.addBotChatName(message);
    }

    /**
     * Helper method for adding disposables to the CompositeDisposable.
     * @param disposable the disposable to be added.
     */
    private void addDisposables(final Disposable disposable) {
        disposables.add(disposable);
    }

    /**
     * Helper method for disposing of user and chatbot message resources.
     */
    private void disposeMessageFlowable() {
        Stream.of(disposables)
                .filter(Objects::nonNull)
                .filter(disposables -> !disposables.isDisposed())
                .forEach(CompositeDisposable::dispose);
    }
}
