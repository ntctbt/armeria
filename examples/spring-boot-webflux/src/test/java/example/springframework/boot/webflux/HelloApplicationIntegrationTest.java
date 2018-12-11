package example.springframework.boot.webflux;

import javax.annotation.PostConstruct;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.linecorp.armeria.spring.web.reactive.ArmeriaClientHttpConnector;

import example.springframework.boot.webflux.ValidationExceptionHandler.ErrorResponse;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class HelloApplicationIntegrationTest {

    @LocalServerPort
    int port;

    private WebTestClient client;

    @PostConstruct
    public void setup() {
        // Use ArmeriaClientHttpConnector if you want to send an HTTP request to the running
        // Spring Boot application via Armeria HTTP client.
        client = WebTestClient.bindToServer(new ArmeriaClientHttpConnector())
                              .baseUrl("http://127.0.0.1:" + port)
                              .build();
    }

    @Test
    public void fromSpringController() {
        client.get()
              .uri("/hello")
              .exchange()
              .expectStatus().isOk()
              .expectBody(String.class).isEqualTo("Hello, World");
    }

    @Test
    public void fromArmeriaAnnotatedService() {
        client.get()
              .uri("/armeria/hello/Spring")
              .exchange()
              .expectStatus().isOk()
              .expectBody(String.class)
              .isEqualTo("Hello, Spring! This message is from Armeria annotated service!");

        // Validation failure because the 'name' value is too short.
        client.get()
              .uri("/armeria/hello/a")
              .exchange()
              .expectStatus().isBadRequest()
              .expectBody(ErrorResponse.class);
    }
}
