package com.accenture.springai_bootcamp_demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.core.env.Environment;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:sqlite::memory:",
        "spring.datasource.hikari.maximum-pool-size=1"
})
class SpringaiBootcampDemoApplicationTests {

	@Autowired
	private ChatClient chatClient;

	@Autowired
	private OllamaChatModel chatModel;

	@Autowired
	private Environment environment;

	@Test
	void contextLoads() {
		// Real auto-configuration is created without contacting Ollama.
		assertThat(chatClient).isNotNull();
		assertThat(chatModel).isNotNull();
		assertThat(environment.getProperty("spring.ai.ollama.init.pull-model-strategy"))
				.isEqualTo("never");
	}

}
