package com.dt199g.project.models;

import com.dt199g.project.support.Constants;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * DictionaryApiClient component used to send API request and parse response
 * for a specific word definition.
 * @author Albin Eliasson
 */
public class DictionaryApiClient {

    /**
     * Not utilized DictionaryApiClient constructor.
     */
    public DictionaryApiClient() { }

    /**
     * Main method for fetching word API data from API url + provided word
     * and parsing response for the word definition.
     * @param word the word for which the definition is to be found.
     * @return an Observable containing either the definitions or a
     * no definition found response.
     */
    public Observable<String> getWordDefinition(final String word) {
        return fetchApiData(Constants.BASE_API_URL + word)
                .map(response -> getDefinition(response, word))
                .doOnError(throwable -> logError(throwable, word))
                .onErrorReturnItem(Constants.NO_DEFINITION_RESPONSE + word);
    }

    /**
     * Method for fetching the word API data from the API url using an HTTP client.
     * @param apiUrl url containing the API url and provided word.
     * @return an Observable containing the API response or error.
     */
    private Observable<String> fetchApiData(final String apiUrl) {
        return Observable.<String>create(emitter -> {
            try {
                HttpClient httpClient = HttpClient.newHttpClient();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                emitter.onNext(response.body());
                emitter.onComplete();

            } catch (IOException exception) {
                emitter.onError(new IOException("Error connecting to the API", exception));
            } catch (InterruptedException exception) {
                emitter.onError(new InterruptedException("API request interrupted: " + exception.getMessage()));
            } catch (Exception exception) {
                emitter.onError(new Exception("Unexpected error during API request", exception));
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Method for either returning parsing of the API response if response is a
     * JSON array, or a no definition response.
     * @param response the API response.
     * @param word the word for which the definition is to be found.
     * @return the parsed definition of API response or a no definition response.
     */
    private String getDefinition(final String response, final String word) {
       return isResponseJsonArray(response) ? parseJsonArray(response) : Constants.NO_DEFINITION_RESPONSE + word;
    }

    /**
     * Method for checking if the API response is a JSON array by checking if
     * the first character is a left bracket.
     * @param response the API response.
     * @return true if the first character in response is a left character.
     */
    private boolean isResponseJsonArray(final String response) {
        return response.trim().charAt(0) == '[';
    }

    /**
     * Method for parsing the response by accessing the main entry JSON array,
     * accessing the contained JSON objects and to access the contained meanings JSON array.
     * @param response the API response.
     * @return the parsed word definitions of the API response.
     */
    private String parseJsonArray(final String response) {
        JSONArray entryArray = new JSONArray(response);

        return IntStream.range(0, entryArray.length())
                .mapToObj(entryArray::getJSONObject)
                .map(this::parseEntry)
                .collect(Collectors.joining());
    }

    /**
     * Method for accessing and calling the parsing of the meanings JSON array.
     * @param entryObject the entry JSON object.
     * @return the parsed word definition of the API response.
     */
    private String parseEntry(final JSONObject entryObject) {
        return parseMeaningsArray(getJsonArray(entryObject, Constants.MEANING_ARRAY_KEY));
    }

    /**
     * Method for parsing the meanings JSON array and accessing the contained
     * definitions JSON array.
     * @param meaningsArray the meanings JSON array.
     * @return the parsed word definition of the API response.
     */
    private String parseMeaningsArray(final JSONArray meaningsArray) {
        return IntStream.range(0, meaningsArray.length())
                .mapToObj(meaningsArray::getJSONObject)
                .map(this::parseMeaning)
                .collect(Collectors.joining());
    }

    /**
     * Method for accessing and calling the parsing of the definitions JSON array.
     * @param meaningObject the meanings JSON object.
     * @return the parsed word definition of the API response.
     */
    private String parseMeaning(final JSONObject meaningObject) {
        return parseDefinitionsArray(getJsonArray(meaningObject, Constants.DEFINITION_ARRAY_KEY));
    }

    /**
     * Method for parsing the definitions JSON array and accessing the contained
     * definition JSON object value.
     * @param definitionArray the definitions JSON array.
     * @return the parsed word definition of the API response.
     */
    private String parseDefinitionsArray(final JSONArray definitionArray) {
        return IntStream.range(0, definitionArray.length())
                .mapToObj(definitionArray::getJSONObject)
                .map(this::parseDefinition)
                .collect(Collectors.joining());
    }

    /**
     * Method for accessing the definition JSON object value.
     * @param definitionObject the definition JSON object.
     * @return the definition JSON object value.
     */
    private String parseDefinition(final JSONObject definitionObject) {
        return getJsonObjectValue(definitionObject, Constants.DEFINITION_OBJECT_KEY, Constants.DEFINITION_TITLE);
    }

    /**
     * Helper method for accessing a JSON array from JSON object and key.
     * @param jsonObject the JSON object which the JSON array is to be accessed.
     * @param arrayKey the JSON array key.
     * @return the JSON array from JSON object.
     */
    private JSONArray getJsonArray(final JSONObject jsonObject, final String arrayKey) {
        return jsonObject.getJSONArray(arrayKey);
    }

    /**
     * Helper method for accessing the JSON object value string from JSON object
     * using the object key, if present.
     * @param jsonObject the JSON object.
     * @param objectKey the JSON object key.
     * @param title a custom title to be added with the JSON object string value.
     * @return the JSON object string value if present, otherwise empty string.
     */
    private String getJsonObjectValue(final JSONObject jsonObject, final String objectKey, final String title) {
        return jsonObject.has(objectKey) ? "\n" + title + jsonObject.getString(objectKey) : "";
    }

    /**
     * Helper method for printing error messages in console.
     * @param throwable the throwable error.
     * @param word the word for which the definition is to be found.
     */
    private void logError(final Throwable throwable, final String word) {
        System.err.println("Error fetching definition for word " + word + " : " + throwable.getMessage());
    }
}
