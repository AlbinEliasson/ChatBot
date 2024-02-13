package com.dt199g.project.models.conversation;

import com.dt199g.project.models.DictionaryApiClient;
import com.dt199g.project.support.Constants;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The ConversationHandler used for finding matching chatbot responses of the
 * user messages and updating the conversation contexts.
 * @author Albin Eliasson
 */
public class ConversationHandler {
    private final List<Conversation> conversations;
    private final Random random;
    private final ConversationContext context;
    private final DictionaryApiClient apiClient;
    private final PublishSubject<String> wordDefinitionSubject;
    private final CompositeDisposable disposables;

    /**
     * Constructor utilized for initializing the conversation list, context, API client,
     * word definition subject, random generator and CompositeDisposable components.
     * @param context the conversation context.
     */
    public ConversationHandler(final ConversationContext context) {
        this.conversations = new CopyOnWriteArrayList<>();
        this.context = context;
        this.random = new Random();
        this.apiClient = new DictionaryApiClient();
        this.wordDefinitionSubject = PublishSubject.create();
        this.disposables = new CompositeDisposable();
    }

    /**
     * Main method for calling and collecting chatbot responses from user message.
     * @param message the user message.
     * @return the chatbot response.
     */
    public String processConversation(final String message) {
        return message.lines()
                .flatMap(this::getRespondMessage)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
    }

    /**
     * Method for accessing each pattern and responses from conversation objects in the
     * conversation list and calling the finding of matching responses.
     * @param line line of the user message.
     * @return a stream of chatbot responses.
     */
    private Stream<String> getRespondMessage(final String line) {
        return conversations.stream().map(conversation ->
                        findMatchingResponse(conversation.getPattern(), conversation.getResponses(), line));
    }

    /**
     * Method for finding matching chatbot responses using the pattern, updating context and
     * if the pattern matches, fetches the word definition from the dictionary API client.
     * @param pattern the pattern to be matched with line from user message.
     * @param responses the responses connected to the pattern.
     * @param line line from the user message.
     * @return a response.
     */
    private String findMatchingResponse(final String pattern, final List<String> responses, final String line) {
        return Stream.of(pattern)
                .map(this::compilePattern)
                .flatMap(Optional::stream)
                .map(compiledPattern -> getMatcher(compiledPattern, line))
                .filter(Matcher::find)
                .peek(this::updateContext)
                .peek(this::fetchWordDefinition)
                .map(matcher -> getRandomResponse(responses))
                .collect(Collectors.joining(" "));
    }

    /**
     * Method for compiling a case-insensitive conversation pattern.
     * @param pattern the conversation pattern.
     * @return Optional containing the compiled pattern, empty if error.
     */
    private Optional<Pattern> compilePattern(final String pattern) {
        try {
            return Optional.of(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));

        } catch (PatternSyntaxException exception) {
            System.err.println("Error compiling conversation pattern: " + exception.getMessage());
        } catch (IllegalArgumentException exception) {
            System.err.println("Unexpected error compiling conversation pattern: " + exception.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Method for accessing the matcher from conversation pattern and line from a user message.
     * @param pattern conversation pattern.
     * @param line line from user message.
     * @return the matcher.
     */
    private Matcher getMatcher(final Pattern pattern, final String line) {
        return pattern.matcher(line);
    }

    /**
     * Method for calling the fetching of a word definition if the pattern from
     * matcher matches and has a capture group.
     * @param matcher the matcher.
     */
    private void fetchWordDefinition(final Matcher matcher) {
        Observable.just(matcher)
                .filter(m -> doesContextMatchPattern(m.pattern().pattern(), Constants.DEFINITION_OBJECT_KEY))
                .filter(this::hasCaptureGroup)
                .subscribe(m -> setWordDefinitionSubject(m.group(1)))
                .dispose();
    }

    /**
     * Method for accessing a random response from a response list.
     * @param responses the response list.
     * @return a random response.
     */
    private String getRandomResponse(final List<String> responses) {
        try {
            return responses.get(random.nextInt(responses.size()));

        } catch (IllegalArgumentException | IndexOutOfBoundsException exception) {
            System.err.println("Error getting a response: " + exception.getMessage());
            return "";
        }
    }

    /**
     * Method for updating/adding contexts if the pattern from matcher matches and
     * has a capture group.
     * @param matcher the matcher.
     */
    private void updateContext(final Matcher matcher) {
        try {
            context.getContextKeysStream()
                    .filter(contextKey -> hasCaptureGroup(matcher))
                    .filter(contextKey -> doesContextMatchPattern(matcher.pattern().pattern(), contextKey))
                    .forEach(contextKey -> context.addContext(contextKey, matcher.group(1)));

        } catch (IllegalStateException | IndexOutOfBoundsException exception) {
            System.err.println("Error updating context: " + exception.getMessage());
        }
    }

    /**
     * Method for fetching word definitions from the Dictionary API client and storing
     * the result in the word definition subject.
     * @param word the user word which definitions are to be found.
     */
    private void setWordDefinitionSubject(final String word) {
        Disposable definitionDisposable = getDefinitionObservable(word)
                .doOnDispose(this::disposeDisposables)
                .subscribe(wordDefinitionSubject::onNext);

        addDisposables(definitionDisposable);
    }

    /**
     * Helper method for checking if the matcher has the group one matching group.
     * @param matcher the matcher.
     * @return true if matcher has matching group one.
     */
    private boolean hasCaptureGroup(final Matcher matcher) {
        return matcher.groupCount() != 0 && matcher.group(1) != null;
    }

    /**
     * Helper method to check if the conversation pattern contains a specific context key.
     * @param pattern the conversation pattern.
     * @param contextKey the context key.
     * @return true if pattern contains context key.
     */
    private boolean doesContextMatchPattern(final String pattern, final String contextKey) {
        return pattern.contains(contextKey);
    }

    /**
     * Method for adding the loaded and parsed conversation list.
     * @param conversations the conversation list containing the parsed and loaded conversation data.
     */
    public void addConversations(final List<Conversation> conversations) {
        try {
            this.conversations.addAll(conversations);
        } catch (UnsupportedOperationException | ClassCastException
                 | NullPointerException | IllegalArgumentException exception) {
            System.err.println("Error adding the conversation list: " + exception.getMessage());
        }
    }

    /**
     * Simple getter for the Dictionary API client get word definition observable.
     * @param word the user word which definitions are to be found.
     * @return and observable of word definition responses.
     */
    private Observable<String> getDefinitionObservable(final String word) {
        return apiClient.getWordDefinition(word);
    }

    /**
     * Simple getter for the word definition subject containing word definitions.
     * @return the word definition subject.
     */
    public PublishSubject<String> getWordDefinitionSubject() {
        return wordDefinitionSubject;
    }

    /**
     * Helper method for disposing of the Dictionary API client get word
     * definition observable resources.
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
