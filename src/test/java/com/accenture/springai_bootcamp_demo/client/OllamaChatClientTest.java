package com.accenture.springai_bootcamp_demo.client;

import com.accenture.springai_bootcamp_demo.entity.ChatMessage;
import com.accenture.springai_bootcamp_demo.entity.Role;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.ChatOptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OllamaChatClientTest {

    @Mock
    private ChatModel chatModel;

    private OllamaChatClient client;

    @BeforeEach
    void setUp() {
        when(chatModel.getOptions()).thenReturn(ChatOptions.builder().build());
        client = new OllamaChatClient(ChatClient.create(chatModel));
    }

    @Test
    void sendsConversationHistoryInOrderWithCorrectRoles() {
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
                List.of(new Generation(new AssistantMessage("  Mock reply  ")))));
        List<ChatMessage> history = List.of(
                ChatMessage.of(Role.SYSTEM, "Be helpful"),
                ChatMessage.of(Role.USER, "What is Java?"),
                ChatMessage.of(Role.ASSISTANT, "Mock previous reply"),
                ChatMessage.of(Role.USER, "Give an example"));

        assertThat(client.complete(history)).isEqualTo("Mock reply");

        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(captor.capture());
        List<Message> messages = captor.getValue().getInstructions();
        assertThat(messages).extracting(Message::getMessageType).containsExactly(
                MessageType.SYSTEM, MessageType.SYSTEM, MessageType.USER,
                MessageType.ASSISTANT, MessageType.USER);
        assertThat(messages).extracting(Message::getText).containsSubsequence(
                "Be helpful", "What is Java?", "Mock previous reply", "Give an example");
    }

    @Test
    void wrapsModelFailure() {
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> client.complete(List.of(ChatMessage.of(Role.USER, "Hello"))))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("Ollama");
    }

    @Test
    void rejectsEmptyReply() {
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
                List.of(new Generation(new AssistantMessage(" ")))));

        assertThatThrownBy(() -> client.complete(List.of(ChatMessage.of(Role.USER, "Hello"))))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("empty response");
    }
}
