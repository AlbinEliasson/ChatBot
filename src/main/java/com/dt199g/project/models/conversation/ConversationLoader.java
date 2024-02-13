package com.dt199g.project.models.conversation;

import com.dt199g.project.support.Constants;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The ConversationLoader component used to load and parse the conversation
 * data stored in the conversation_data.xml file.
 * @author Albin Eliasson
 */
public final class ConversationLoader {

    /**
     * Empty ConversationLoader constructor.
     */
    private ConversationLoader() { }

    /**
     * Main method for calling the loading, parsing and returning a Single containing a
     * list of {@link Conversation} components from conversation data.
     * @param inputStream the input stream of the conversation_data.xml file.
     * @return a Single containing a list of conversations or empty list if error.
     */
    public static Single<List<Conversation>> getConversationList(final InputStream inputStream) {
        return loadFileConversations(inputStream)
                .toList()
                .observeOn(Schedulers.io())
                .onErrorResumeNext(throwable -> {
                    System.err.println("Error fetching conversation list: " + throwable.getMessage());
                    return Single.just(new ArrayList<>());
                });
    }

    /**
     * Method for calling the loading of conversation data and parsing of node elements.
     * @param inputStream the input stream of the conversation_data.xml file.
     * @return an observable containing conversations.
     */
    public static Observable<Conversation> loadFileConversations(final InputStream inputStream) {
        return getConversationNodes(inputStream, DocumentBuilderFactory.newInstance())
                .flatMap(nodeList -> Observable.fromStream(getNodeStream(nodeList)))
                .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
                .map(node -> (Element) node)
                .flatMap(element -> getConversation(extractTypeFromElement(element), element));
    }

    /**
     * Method for loading the XML conversation file and returning the conversation node lists
     * using document builder.
     * @param inputStream the input stream of the conversation_data.xml file.
     * @param dbf a new instance of the document builder factory.
     * @return an observable of conversation node lists.
     */
    private static Observable<NodeList> getConversationNodes(
            final InputStream inputStream, final DocumentBuilderFactory dbf) {

        return Observable.fromCallable(() -> {
                    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
                    Document document = documentBuilder.parse(inputStream);
                    document.getDocumentElement().normalize();
                    return document.getElementsByTagName(Constants.CONVERSATION_TAG_NAME);
                })
                .subscribeOn(Schedulers.io())
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof SAXException || throwable instanceof IOException
                            || throwable instanceof IllegalArgumentException) {
                        System.err.println("Specific error occurred reading file: " + throwable.getMessage());
                    } else {
                        System.err.println("Unexpected error occurred reading file: " + throwable.getMessage());
                    }
                    return Observable.error(throwable);
                });
    }

    /**
     * Method for calling the creation of conversation objects by accessing the
     * pattern, and id connected responses using the element from each node in the
     * pattern and response node lists.
     * @param type the type of conversation, e.g., Questions or greetings.
     * @param conversationElement the conversation element.
     * @return an observable containing conversation objects.
     */
    private static Observable<Conversation> getConversation(final String type, final Element conversationElement) {
        return Observable.fromStream(getNodeStream(getNodeList(conversationElement, Constants.PATTERN_TAG_NAME)))
                .map(patternNode -> (Element) patternNode)
                .flatMap(patternElement -> getResponses(getElementIdAttribute(patternElement),
                                getNodeList(conversationElement, Constants.RESPONSE_TAG_NAME))
                        .toList()
                        .map(responseList ->
                                createConversation(type, extractPatternFromElement(patternElement), responseList))
                        .toObservable())
                .doOnError(throwable -> System.err.println("Error processing conversation: " + throwable.getMessage()));
    }

    /**
     * Method for accessing the responses from the response node list connected with the
     * provided pattern id.
     * @param patternId the pattern id.
     * @param responseNodes the response node list.
     * @return an observable of responses.
     */
    private static Observable<String> getResponses(final String patternId, final NodeList responseNodes) {
        return Observable.fromStream(getNodeStream(responseNodes))
                .map(node -> (Element) node)
                .filter(element -> patternId.equals(element.getAttribute(Constants.ID_ATTRIBUTE)))
                .map(ConversationLoader::extractResponseFromElement)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Method for creating conversation objects and setting the conversation type,
     * pattern and responses.
     * @param type the type of conversation, e.g., Questions or greetings.
     * @param pattern the conversation pattern connected to the responses.
     * @param responses the responses.
     * @return an observable of conversation objects.
     */
    private static Conversation createConversation(
            final String type, final String pattern, final List<String> responses) {

        return new Conversation(type, pattern, responses);
    }

    /**
     * Helper method for accessing a stream of nodes from a node list.
     * @param nodeList the node list to be streamed.
     * @return a stream of nodes.
     */
    private static Stream<Node> getNodeStream(final NodeList nodeList) {
        return IntStream.range(0, nodeList.getLength())
                .mapToObj(nodeList::item);
    }

    /**
     * Helper method for accessing a node list from an element and tag name.
     * @param element the element which the node list is to be accessed from.
     * @param tagName the name of the tag to match on.
     * @return a node list.
     */
    private static NodeList getNodeList(final Element element, final String tagName) {
        return element.getElementsByTagName(tagName);
    }

    /**
     * Helper method for accessing and parsing a pattern from a pattern element.
     * @param element the pattern element.
     * @return the pattern.
     */
    private static String extractPatternFromElement(final Element element) {
        return element.getTextContent().replaceAll("[\n ]", "").trim();
    }

    /**
     * Helper method for accessing and parsing a response from a response element.
     * @param element the response element.
     * @return the response.
     */
    private static String extractResponseFromElement(final Element element) {
        return element.getTextContent().replaceAll("(  +)", "").trim();
    }

    /**
     * Helper method for accessing and parsing a type from a type element.
     * @param element the type element.
     * @return the type.
     */
    private static String extractTypeFromElement(final Element element) {
        return getNodeList(element, Constants.TYPE_TAG_NAME).item(0).getTextContent();
    }

    /**
     * Method for accessing the id connecting the responses and patterns.
     * @param element the pattern element.
     * @return the id.
     */
    private static String getElementIdAttribute(final Element element) {
        return element.getAttribute(Constants.ID_ATTRIBUTE);
    }
}
