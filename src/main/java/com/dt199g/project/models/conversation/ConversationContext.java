package com.dt199g.project.models.conversation;

import com.dt199g.project.support.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * The ConversationContext component used for storing and replacing
 * conversation contexts in responses.
 * @author Albin Eliasson
 */
public class ConversationContext {
    private final Map<String, String> context;
    private final Map<String, String> contextKeyAndReplaceHolder;
    private final List<ContextReplacement> contextReplacements;

    /**
     * Constructor used to initialize the context map, replacement list
     * and context/response key replacements map.
     */
    public ConversationContext() {
        this.context = new ConcurrentHashMap<>();
        this.contextReplacements = new ArrayList<>();
        contextKeyAndReplaceHolder = initializeConstants();
        contextReplacements.addAll(getContextReplacementList());
    }

    /**
     * Method used to initialize the context/response key replacements map containing
     * context keys used in the context map and response replacement keys which can be
     * found in the responses where context can be added.
     * @return the context/response key replacements map.
     */
    private Map<String, String> initializeConstants() {
        Map<String, String> constants = new HashMap<>();
        constants.put(Constants.NAME_CONTEXT, Constants.NAME_REPLACE_HOLDER);
        constants.put(Constants.MUSIC_CONTEXT, Constants.MUSIC_REPLACE_HOLDER);
        constants.put(Constants.CAKE_CONTEXT, Constants.CAKE_REPLACE_HOLDER);
        constants.put(Constants.HOBBY_CONTEXT, Constants.HOBBY_REPLACE_HOLDER);
        constants.put(Constants.HOBBY_REASON_CONTEXT, Constants.HOBBY_REASON_REPLACE_HOLDER);
        return constants;
    }

    /**
     * Method for adding user contexts in the context map, e.g., the user's hobby.
     * @param key a context key.
     * @param value a context from the user, e.g., the user's hobby or actual name.
     */
    public void addContext(final String key, final String value) {
        Optional.ofNullable(key)
                .filter(k -> value != null)
                .ifPresentOrElse(validKey -> {
                    try {
                        context.put(validKey, value);
                    } catch (UnsupportedOperationException | ClassCastException | IllegalArgumentException e) {
                        System.err.println("Error adding context: " + e.getMessage());
                    }
                    }, () -> System.err.println(
                                "Invalid key or value for context: key = " + key + ", value = " + value));
    }

    /**
     * Method used to replace response replacement keys in the response with the stored
     * contexts using the context replacement rules.
     * @param response the chatbot response.
     * @return the response with the response keys replaced with either
     * contexts or if no context is stored, the context key removed.
     */
    public String replaceContexts(final String response) {
        return contextReplacements.stream()
                .reduce((f1, f2) -> (s, c) -> f2.apply(f1.apply(s, c), c))
                .map(replacement -> replacement.apply(response, context))
                .orElse(response);
    }

    /**
     * Method used to create a context replacement list from the context/response
     * key replacements map, used for context replacements.
     * @return a context replacement list.
     */
    private List<ContextReplacement> getContextReplacementList() {
        return contextKeyAndReplaceHolder.entrySet().stream()
                .map(entry -> ContextReplacement.create(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * Helper method for accessing a stream of the context keys from the context/response
     * key replacements map.
     * @return a stream of context keys.
     */
    public Stream<String> getContextKeysStream() {
        return contextKeyAndReplaceHolder.keySet().stream();
    }

    /**
     * The ContextReplacement functional interface represents a context replacement rule used
     * for replacing a response replacement key with a stored context if context exists,
     * otherwise replaced with an empty string.
     * @author Albin Eliasson
     */
    @FunctionalInterface
    interface ContextReplacement {
        /**
         * Method used to apply the context replacement rule.
         * @param response the chatbot response.
         * @param context the context map.
         * @return the response with context if context exists, otherwise the response.
         */
        String apply(String response, Map<String, String> context);

        /**
         * Method used for creating a context replacement rule for replacing a response
         * replacement key with a stored context if context exists, otherwise replaced
         * with an empty string.
         * @param contextKey the context key.
         * @param replaceHolder the response replacement key.
         * @return a context replacement rule.
         */
        static ContextReplacement create(String contextKey, String replaceHolder) {
            return (response, context) -> {
                Optional<String> contextValue = Optional.ofNullable(context.get(contextKey));

                return contextValue.map(value -> response.replace(replaceHolder, value))
                        .orElse(response.replace(" " + replaceHolder, ""));
            };
        }
    }
}
