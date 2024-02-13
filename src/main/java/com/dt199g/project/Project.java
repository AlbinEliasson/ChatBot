package com.dt199g.project;

import com.dt199g.project.controllers.AppController;

/**
 * The main starting point for Project Assignment.
 * Responsible for the creation of the app controller and starting the chatbot.
 * @author Albin Eliasson
 */
public final class Project {
    private Project() { // Utility classes should not have a public or default constructor
        throw new IllegalStateException("Utility class");
    }

    /**
     * Create app controller and start the chatbot.
     * @param args command arguments.
     */
    public static void main(final String... args) {
        new AppController().startChatBot();
    }
}
