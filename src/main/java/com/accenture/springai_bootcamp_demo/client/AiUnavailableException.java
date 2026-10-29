package com.accenture.springai_bootcamp_demo.client;

public class AiUnavailableException extends RuntimeException {

    public AiUnavailableException(String message) {
        super(message);
    }

    public AiUnavailableException(Throwable cause) {
        super("Ollama is unavailable or could not generate a response. "
                + "Check that Ollama is running at the configured base URL "
                + "(default http://localhost:11434) and that the configured model "
                + "(default gemma3:1b) has been downloaded.", cause);
    }
}
