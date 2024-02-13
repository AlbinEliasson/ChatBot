package com.dt199g.project.models.conversation;

import java.util.List;

/**
 * The Conversation component represents a conversation pattern and respective responses.
 * @author Albin Eliasson
 */
public class Conversation {
    private final String type;
    private final String pattern;
    private final List<String> responses;

    /**
     * Constructor used to initialize the conversation type, pattern and responses.
     * @param type the type of conversation.
     * @param pattern the conversation regex pattern.
     * @param responses the list of responses connected to the pattern.
     */
    public Conversation(final String type, final String pattern, final List<String> responses) {
        this.type = type;
        this.pattern = pattern;
        this.responses = responses;
    }

    /**
     * Getter for the type of conversation.
     * @return the type of conversation.
     */
    public String getType() {
        return type;
    }

    /**
     * Getter for the conversation pattern.
     * @return the pattern.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Getter for the list of responses.
     * @return the list of responses.
     */
    public List<String> getResponses() {
        return responses;
    }
}
