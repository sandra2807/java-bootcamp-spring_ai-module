package com.accenture.springai_bootcamp_demo.service;

import com.accenture.springai_bootcamp_demo.client.AiUnavailableException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
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
class JavaStudyServiceTest {

    @Mock
    private ChatModel chatModel;

    private JavaStudyService service;

    @BeforeEach
    void setUp() {
        // Real ChatClient prompt building, but the model is a mock: no HTTP requests.
        when(chatModel.getOptions()).thenReturn(ChatOptions.builder().build());
        service = new JavaStudyService(ChatClient.create(chatModel));
    }

    @Test
    void explainsWithBeginnerPromptAndReturnsModelText() {
        when(chatModel.call(any(Prompt.class))).thenReturn(reply("  Mock explanation  "));

        assertThat(service.explain("  What is polymorphism in Java?  "))
                .isEqualTo("Mock explanation");

        Prompt prompt = capturedPrompt();
        assertThat(prompt.getSystemMessage().getText())
                .contains("beginner-friendly English", "Java code example");
        assertThat(prompt.getUserMessage().getText())
                .isEqualTo("What is polymorphism in Java?");
    }

    @Test
    void quizRequestsExactlyThreeQuestionsWithAnswers() {
        when(chatModel.call(any(Prompt.class))).thenReturn(reply("Mock quiz"));

        assertThat(service.quiz("Java inheritance")).isEqualTo("Mock quiz");

        Prompt prompt = capturedPrompt();
        assertThat(prompt.getSystemMessage().getText())
                .contains("exactly 3", "answer", "simple English");
        assertThat(prompt.getUserMessage().getText()).isEqualTo("Java inheritance");
    }

    @Test
    void preservesJavaBracesInUserTopic() {
        when(chatModel.call(any(Prompt.class))).thenReturn(reply("Mock explanation"));

        service.explain("Explain class Animal { void speak() {} }");

        assertThat(capturedPrompt().getUserMessage().getText())
                .isEqualTo("Explain class Animal { void speak() {} }");
    }

    @Test
    void wrapsModelFailureWithUsefulMessage() {
        RuntimeException failure = new RuntimeException("Connection refused");
        when(chatModel.call(any(Prompt.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.explain("Java interfaces"))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("Ollama")
                .hasMessageContaining("http://localhost:11434")
                .hasCause(failure);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsEmptyModelContent(String content) {
        when(chatModel.call(any(Prompt.class))).thenReturn(reply(content));

        assertThatThrownBy(() -> service.quiz("Java classes"))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("empty response");
    }

    private Prompt capturedPrompt() {
        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(captor.capture());
        return captor.getValue();
    }

    private ChatResponse reply(String content) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(content))));
    }
}
