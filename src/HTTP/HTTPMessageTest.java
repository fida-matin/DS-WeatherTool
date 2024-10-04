package HTTP;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Encompasses all testing with HTTP message formatting
class HTTPMessageTest {

    static final String reqfirstLine = "GET /weather/recent HTTP/1.1";
    static final String resfirstLine = "HTTP/1.1 200 OK";
    static HTTP_message message;

    static String[][] headers = {{"User-Agent", "ATOMClient/1/0"}, {"Content-Type", "application/json"}, {"Content-Length", "0"}, {"Connection", "keep-alive"}};

    static String formatted_headers = """
            User-Agent: ATOMClient/1/0
            Connection: keep-alive
            Content-Length: 0
            Content-Type: application/json""";
    static String body = """
            id:IDS60901
            name:Adelaide (West Terrace /  ngayirdapira)
            state: SA
            time_zone:CST
            lat:-34.9
            lon:138.6
            local_date_time:15/04:00pm
            local_date_time_full:20230715160000
            air_temp:13.3
            apparent_t:9.5
            cloud:Partly cloudy
            dewpt:5.7
            press:1023.9
            rel_hum:60
            wind_dir:S
            wind_spd_kmh:15
            wind_spd_kt:8""";

    @BeforeEach
    void setUp() {
        message = new HTTP_message();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("Determining Message Type Test")
    void determine_type() {
        assertEquals(HTTP_request.class, message.determine_type(reqfirstLine).getClass());
        assertEquals(HTTP_response.class, message.determine_type(resfirstLine).getClass());
    }

    @Test
    void setHeader() {
        message.setHeader("User-Agent", "ATOMClient/1/0");
        message.setHeader("Content-Type", "application/json");
        message.setHeader("Content-Length", "0");
        message.setHeader("Connection", "keep-alive");

        assertEquals(headers[0][1], message.getHeader("User-Agent"));
        assertEquals(headers[1][1], message.getHeader("Content-Type"));
        assertEquals(headers[2][1], message.getHeader("Content-Length"));
        assertEquals(headers[3][1], message.getHeader("Connection"));

    }

    @Test
    void setBody() {
        message.setBody("""
                id:IDS60901
                name:Adelaide (West Terrace /  ngayirdapira)
                state: SA
                time_zone:CST
                lat:-34.9
                lon:138.6
                local_date_time:15/04:00pm
                local_date_time_full:20230715160000
                air_temp:13.3
                apparent_t:9.5
                cloud:Partly cloudy
                dewpt:5.7
                press:1023.9
                rel_hum:60
                wind_dir:S
                wind_spd_kmh:15
                wind_spd_kt:8""");
        assertEquals(body, message.getBody());
    }


    @Test
    void headersToString() {
        message.setHeader("User-Agent", "ATOMClient/1/0");
        message.setHeader("Content-Type", "application/json");
        message.setHeader("Content-Length", "0");
        message.setHeader("Connection", "keep-alive");

        assertEquals(formatted_headers, message.headersToString());
    }
}