package org.acme.kafka;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.SseEventSource;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;

@QuarkusTest
public class RiscosResourceTest {

    @TestHTTPResource("/consumed-riscos")
    URI consumedRiscos;

    @Test
    public void testHelloEndpoint() throws InterruptedException {
        // cria um client para `consumed-riscos` e coleta recursos consumidos da lista
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(consumedRiscos);

        List<String> received = new CopyOnWriteArrayList<>();

        SseEventSource source = SseEventSource.target(target).build();
        source.register(inboundSseEvent -> received.add(inboundSseEvent.readData()));

        // em thread separada, feed the `RiscoResource`
        ExecutorService riscoSender = startSendingRiscos();

        source.open();

        //verifica se depois de 5 segundos, tem 2 itens coletados e se está conforme esperado
        await().atMost(5, SECONDS).until(() -> received.size() >= 2);
        assertThat(received, Matchers.hasItems("Cliente1 teste cartao 10,00",
                "Cliente2 teste veiculos 50000,00"));
        source.close();

        // shutdown the executor that is feeding the `MovieResource`
        riscoSender.shutdownNow();
        riscoSender.awaitTermination(5, SECONDS);
    }

    private ExecutorService startSendingRiscos() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            while (true) {
                given()
                        .contentType(ContentType.JSON)
                        .body("{\"cadastro\":\"Cliente1\",\"documento\":\"teste\",\"produto\":\"cartao\",\"valor\":\"10,00\"}")
                .when()
                        .post("/riscos")
                .then()
                        .statusCode(202);

                given()
                        .contentType(ContentType.JSON)
                        .body("{\"cadastro\":\"Cliente2\",\"documento\":\"teste\",\"produto\":\"veiculos\",\"valor\":\"50000,00\"}")
                .when()
                        .post("/riscos")
                .then()
                        .statusCode(202);

                try {
                    Thread.sleep(200L);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        return executorService;
    }

}
