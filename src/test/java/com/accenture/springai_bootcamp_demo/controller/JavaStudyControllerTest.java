package com.accenture.springai_bootcamp_demo.controller;

import com.accenture.springai_bootcamp_demo.client.AiUnavailableException;
import com.accenture.springai_bootcamp_demo.config.GlobalExceptionHandler;
import com.accenture.springai_bootcamp_demo.service.JavaStudyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JavaStudyController.class)
@Import(GlobalExceptionHandler.class)
class JavaStudyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JavaStudyService javaStudyService;

    @Test
    void explainReturnsServiceResponse() throws Exception {
        when(javaStudyService.explain("What is polymorphism in Java?"))
                .thenReturn("Mock explanation");

        mockMvc.perform(post("/api/ai/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"topic\":\"What is polymorphism in Java?\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").value("Mock explanation"));

        verify(javaStudyService).explain("What is polymorphism in Java?");
    }

    @Test
    void quizReturnsServiceResponse() throws Exception {
        when(javaStudyService.quiz("Java inheritance")).thenReturn("Mock quiz");

        mockMvc.perform(post("/api/ai/quiz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"topic\":\"Java inheritance\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Mock quiz"));

        verify(javaStudyService).quiz("Java inheritance");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/ai/explain", "/api/ai/quiz"})
    void rejectsBlankTopic(String endpoint) throws Exception {
        assertBadRequest(endpoint, "{\"topic\":\"   \"}");
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"topic\":null}", "{\"topic\":\"\"}", "{broken", ""})
    void rejectsMissingOrMalformedInput(String body) throws Exception {
        assertBadRequest("/api/ai/explain", body);
    }

    @Test
    void rejectsOverlongTopic() throws Exception {
        mockMvc.perform(post("/api/ai/quiz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"topic\":\"" + "a".repeat(1001) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("1000 characters")));

        verifyNoInteractions(javaStudyService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/ai/explain", "/api/ai/quiz"})
    void unavailableAiReturns503ProblemDetail(String endpoint) throws Exception {
        AiUnavailableException failure = new AiUnavailableException(new RuntimeException("Connection refused"));
        if (endpoint.endsWith("explain")) {
            when(javaStudyService.explain(anyString())).thenThrow(failure);
        } else {
            when(javaStudyService.quiz(anyString())).thenThrow(failure);
        }

        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"topic\":\"Java interfaces\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.detail").value(containsString("Ollama")));
    }

    private void assertBadRequest(String endpoint, String body) throws Exception {
        mockMvc.perform(post(endpoint).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(javaStudyService);
    }
}
