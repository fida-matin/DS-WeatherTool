package Server.Aggregation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

class AggregationServerTest {

    AggregationServer AG_server;

    @BeforeEach
    void setUp() throws IOException {

        try (ServerSocket ss = new ServerSocket(8081)) {
            AG_server = new AggregationServer(ss);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void readRequest() {
    }

    @Test
    void read_headers() {
    }

    @Test
    void read_body() {
    }
}