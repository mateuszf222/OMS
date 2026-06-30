package org.example.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Zrzuca OpenAPI wygenerowane przez springdoc (z kontrolerów i anotacji) do pliku,
 * na potrzeby bramki CI "drift kod &lt;-&gt; kontrakt" (06-api-contracts.md, API-SPEC-1).
 *
 * Wynik: target/generated-openapi/order-service.openapi.json — w CI porównywany (oasdiff)
 * z kontraktem źródłowym docs/contracts/openapi/order-service.v1.yaml. Rozjazd = czerwony PR.
 *
 * Wzorowany na OrderIntegrationTest: pełny kontekst na Testcontainers (Postgres + Kafka),
 * bez realnego Keycloaka (issuer-uri rozwiązywany leniwie; dostęp przez jwt() z spring-security-test).
 */
@SpringBootTest(properties = {
        "springdoc.api-docs.version=openapi_3_1",
        "eureka.client.enabled=false"
})
@AutoConfigureMockMvc
@Testcontainers
class OpenApiSpecDumpTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("orderdb_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:4.1.1"));

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Test
    void dumpsSpringdocOpenApiToTarget() throws Exception {
        String spec = mockMvc.perform(get("/v3/api-docs").with(jwt()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(spec).contains("/api/v1/orders");

        Path out = Path.of("target/generated-openapi/order-service.openapi.json");
        Files.createDirectories(out.getParent());
        Files.writeString(out, spec);
    }
}
