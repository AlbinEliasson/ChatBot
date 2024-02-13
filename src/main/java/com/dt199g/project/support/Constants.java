package com.dt199g.project.support;

/**
 * Constants interface used for storing constants used in the application.
 * @author Albin Eliasson
 */
public interface Constants {
    // Main Frame Constants
    int DEFAULT_WINDOW_WIDTH = 900;
    int DEFAULT_WINDOW_HEIGHT = 900;
    String APP_HEADER = "Chat bot";

    // Content Panel Constants
    String SEND_MESSAGE_BUTTON_TEXT = "Send";
    int SEND_MESSAGE_BUTTON_WIDTH = 100;
    int SEND_MESSAGE_BUTTON_HEIGHT = 150;
    int SEND_MESSAGE_BUTTON_FONT_SIZE = 20;
    int INPUT_TEXT_PANEL_HEIGHT = (int) (DEFAULT_WINDOW_HEIGHT * 0.1);
    int CONTENT_AREA_FONT_SIZE = 20;
    int INPUT_AREA_FONT_SIZE = 18;

    // Model constants
    String USER_CHAT_NAME = "You: ";
    String BOT_CHAT_NAME = "Bot: ";
    String NO_RESPONSE = "Sorry i didn't understand.";
    String DEFINITION_RESPONSE = "get definition response";

    // Model Context Constants
    String NAME_CONTEXT = "name";
    String NAME_REPLACE_HOLDER = "[name]";
    String MUSIC_CONTEXT = "music";
    String MUSIC_REPLACE_HOLDER = "[music]";
    String CAKE_CONTEXT = "cake";
    String CAKE_REPLACE_HOLDER = "[cake]";
    String HOBBY_CONTEXT = "hobby";
    String HOBBY_REPLACE_HOLDER = "[hobby]";
    String HOBBY_REASON_CONTEXT = "interested";
    String HOBBY_REASON_REPLACE_HOLDER = "[reason]";

    // Model File Reading Constants
    String CONVERSATION_FILE_PATH = "conversation_data.xml";
    String CONVERSATION_TAG_NAME = "conversation";
    String TYPE_TAG_NAME = "type";
    String PATTERN_TAG_NAME = "pattern";
    String RESPONSE_TAG_NAME = "response";
    String ID_ATTRIBUTE = "id";

    // Model Dictionary Api Client Constants
    String BASE_API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";
    String NO_DEFINITION_RESPONSE = "Sorry, i could not find the definition for: ";
    String DEFINITION_OBJECT_KEY = "definition";
    String DEFINITION_TITLE = "Definition: ";
    String MEANING_ARRAY_KEY = "meanings";
    String DEFINITION_ARRAY_KEY = "definitions";
}
