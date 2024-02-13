# ChatBot
A simple rule-based chatbot system using regular expressions, functional and reactive programming principles and 
concepts using Java's Streams API and RxJava 3.

## Environment & Tools
The environments and tools utilized for this project were Java version: 17.0.3, RxJava 3 version: 3.1.6 together with 
Maven version: 3.8.5 and git version: 2.33.1.windows.1 using the Intellij IDE version: 2023.3.2 on a Windows 11 desktop.

## Purpose
The purpose of this project was to design and implement a simple rule-based chatbot system that uses regular 
expressions, functional programming and reactive programming concepts with an emphasis on the usage of 
streamed pipelines; Java's Streams API for functional programming and RxJava 3 for reactive programming. 

The chatbot needs to be designed to interact with a user in a conversational manner, where the user can type in 
text inputs to initiate a conversation with the chatbot. The chatbot can handle it as an incoming event and use 
reactive programming concepts to process and respond to the event in real-time. The chatbot can then use regular 
expressions to parse and understand the user's intent and provide appropriate responses.

### Concrete Goals
* The chatbot uses regular expressions with character classes, alterations, and quantifiers to detect user intents and 
respond appropriately.
* The chatbot demonstrates basic functional programming concepts, such as immutability, recursion, filter operations, 
composition, lambda expressions, and closures.
* The chatbot uses basic Java Streams API operators, such as filter, map, sorted, forEach, count, reduce and shows 
evidence of consideration for maintainability.
* The chatbot uses basic RxJava operators, such as debounce, interval, throttle, buffer and take, and shows evidence
of consideration for maintainability.
* The chatbot has a functional and easy-to-use interface.
* The chatbot handles errors and unknown requests gracefully.
* The code is organized and demonstrates some good practices for functional programming, such as avoiding
mutable state and side effects.

## Execution Of Application
To run the chatbot application, it can be downloaded and run directly from an IDE or the use of the
Maven Command $ mvn clean verify from the terminal which will create a runnable JAR. The runnable JAR can either be
run from the terminal with the java -jar Project-1.0-SNAPSHOT.jar command or directly from an IDE.

To use the chatbot, messages can be typed in the input field at the bottom of the application window. To send the typed
messages to the bot, the "Send" button can be used, located to the right of the input field. The conversation, user and
bot messages can be viewed in the center text area.

Except for standard conversational messages like, "Hello! What is your name?" the chatbot can give word definitions with 
the questions "What's the definition of (a word)" or "Do you know the definition of the word (a word)". The chatbot can 
also draw something in the chat with the questions "Can you draw something?" or "Can you paint me something?"
