package com.accenture.springai_bootcamp_demo.controller;

import com.accenture.springai_bootcamp_demo.dto.StudyRequest;
import com.accenture.springai_bootcamp_demo.dto.StudyResponse;
import com.accenture.springai_bootcamp_demo.service.JavaStudyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class JavaStudyController {

    private final JavaStudyService javaStudyService;

    public JavaStudyController(JavaStudyService javaStudyService) {
        this.javaStudyService = javaStudyService;
    }

    @PostMapping("/explain")
    public StudyResponse explain(@Valid @RequestBody StudyRequest request) {
        return new StudyResponse(javaStudyService.explain(request.topic()));
    }

    @PostMapping("/quiz")
    public StudyResponse quiz(@Valid @RequestBody StudyRequest request) {
        return new StudyResponse(javaStudyService.quiz(request.topic()));
    }
}
