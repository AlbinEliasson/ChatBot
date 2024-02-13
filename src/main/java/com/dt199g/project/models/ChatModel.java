package com.dt199g.project.models;

import com.dt199g.project.models.conversation.ConversationContext;
import com.dt199g.project.models.conversation.ConversationHandler;
import com.dt199g.project.models.conversation.ConversationLoader;
import com.dt199g.project.support.Constants;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * The ChatModel component responsible for managing the chatbot response messages
 * and storing the conversation data on application startup in the
 * {@link ConversationHandler} component.
 * @author Albin Eliasson
 */
public class ChatModel {
    private final ConversationHandler conversationHandler;
    private final ConversationContext context;
    private final CompositeDisposable disposables;

    /**
     * Constructor utilized for initializing the conversation context, handler,
     * composite disposable, calling the loading/parsing of the conversation
     * data and storing the data in the handler.
     */
    public ChatModel() {
        this.context = new ConversationContext();
        this.conversationHandler = new ConversationHandler(context);
        this.disposables = new CompositeDisposable();
        handleFileConversationList();
    }

    /**
     * Method for getting chatbot response by calling the processing of user messages.
     * @param message the user message.
     * @return an observable of chatbot response messages.
     */
    public Observable<String> getResponse(final String message) {
        return Observable.just(message)
                .map(conversationHandler::processConversation)
                .filter(response -> !response.isEmpty())
                .flatMap(this::processNonEmptyResponse)
                .defaultIfEmpty(Constants.NO_RESPONSE);
    }

    /**
     * Helper method for accessing the definition of user word if definition response,
     * otherwise adding context to the provided response.
     * @param response the chatbot response.
     * @return an observable of chatbot response.
     */
    private Observable<String> processNonEmptyResponse(final String response) {
        return Observable.just(response)
                .filter(r -> r.equals(Constants.DEFINITION_RESPONSE))
                .switchMap(r -> conversationHandler.getWordDefinitionSubject().take(1))
                .switchIfEmpty(Observable.just(addContextToResponse(response)));
    }

    /**
     * Method for calling the replacement of response keywords with stored contexts.
     * @param response the chatbot response.
     * @return the response with context, if keywords and context are available
     * otherwise the initially provided response.
     */
    private String addContextToResponse(final String response) {
        return context.replaceContexts(response);
    }

    /**
     * Method for adding the string username "You:" before each user message.
     * @param message the user message.
     * @return the user message with added username.
     */
    public String addUserChatName(final String message) {
        return Constants.USER_CHAT_NAME + message + "\n";
    }

    /**
     * Method for adding the string chatbot username "Bot:" before each chatbot response.
     * @param message the chatbot response message.
     * @return the chatbot response with added username.
     */
    public String addBotChatName(final String message) {
        return Constants.BOT_CHAT_NAME + message + "\n";
    }

    /**
     * Method for calling the loading/parsing of conversation data and storing the
     * data in the ConversationHandler.
     */
    private void handleFileConversationList() {
        Disposable conversationDisposable = ConversationLoader.getConversationList(
                getClass().getClassLoader().getResourceAsStream(Constants.CONVERSATION_FILE_PATH))
                .doFinally(this::disposeDisposables)
                .subscribe(
                        conversationHandler::addConversations,
                        throwable -> System.err.println("Error loading conversations: " + throwable.getMessage())
                );

        addDisposables(conversationDisposable);
    }

    /**
     * Helper method for disposing of conversation loading/parsing resources.
     */
    private void disposeDisposables() {
        Stream.of(disposables)
                .filter(Objects::nonNull)
                .filter(disposables -> !disposables.isDisposed())
                .forEach(CompositeDisposable::dispose);
    }

    /**
     * Helper method for adding disposables to the CompositeDisposable.
     * @param disposable the disposable to be added.
     */
    private void addDisposables(final Disposable disposable) {
        disposables.add(disposable);
    }
}
