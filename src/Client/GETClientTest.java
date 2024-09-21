package Client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class GETClientTest {

    static final String requestAddress = "src/Client/resources/GETRequest.txt";
    static final String closeAddress = "src/Client/resources/CLOSERequest.txt";
    static final String expectedRequest = "GET /weather/{{URI}} HTTP/1.1" +
            "User-Agent: ATOMClient/1/0" +
            "Content-Length: 0" +
            "Connection: keep-alive";

    static final String expectedFormat = "GET /weather/recent HTTP/1.1" +
            "User-Agent: ATOMClient/1/0" +
            "Content-Length: 0" +
            "Connection: keep-alive";

    static final String payload = "recent";

    static GETClient client;
    static String host = "127.0.0.1";
    static int port = 8080;

    @BeforeEach
    void setUp() {
        try(Socket socket = new Socket(host, port)) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            client = new GETClient(socket, out, in);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }


    @AfterEach
    void tearDown() {
        try {
            client.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }


    @Test
    @DisplayName("File Read Test")
    void readFile() throws IOException {
        String actualRequest = client.readFile(requestAddress);
        assertEquals(expectedRequest, actualRequest);
    }


    @Test
    void sendHTTP_message() throws IOException {
        client.sendHTTP_message(requestAddress, payload);
    }

}