package com.accenture.springai_bootcamp_demo.client;

import com.accenture.springai_bootcamp_demo.entity.ChatMessage;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Connects the existing saved conversations to the local Ollama model. */
@Component
public class OllamaChatClient {

    private final ChatClient chatClient;

    public OllamaChatClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String complete(List<ChatMessage> history) {
        List<Message> messages = history.stream().map(this::toMessage).toList();
        String reply;
        try {
            reply = chatClient.prompt()
                    .system("You are a helpful, concise assistant. Answer in simple English.")
                    .messages(messages)
                    .call()
                    .content();
        } catch (RuntimeException ex) {
            throw new AiUnavailableException(ex);
        }
        if (!StringUtils.hasText(reply)) {
            throw new AiUnavailableException("Ollama returned an empty response. Please try again.");
        }
        return reply.trim();
    }

    private Message toMessage(ChatMessage message) {
        return switch (message.getRole()) {
            case SYSTEM -> new SystemMessage(message.getContent());
            case USER -> new UserMessage(message.getContent());
            case ASSISTANT -> new AssistantMessage(message.getContent());
        };
    }
}
