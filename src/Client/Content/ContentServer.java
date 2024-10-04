// University of Adelaide - Distributed Systems 2024
// Fida Matin - a1798239
// 4 October 2024


package Client.Content;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static Client.GET.GETClient.getString;

public class ContentServer {

    public static final String RESOURCE_PATH = "src/Client/Content/resources/";

    static final int HEARTBEAT_INTERVAL = 10;
    public static final String HEARTBEAT_REQUEST = RESOURCE_PATH + "requests/HeartbeatRequest.txt";
    public static final String SHUTDOWN_REQUEST = RESOURCE_PATH + "requests/ShutdownRequest.txt";
    public static final String PUT_REQUEST = RESOURCE_PATH + "requests/PUTRequest.txt";

    public static final String CS_HELP = RESOURCE_PATH + "help/ContentServer_Help.txt";
    public static final String DEFAULT_WEATHER = RESOURCE_PATH + "data/WeatherData.txt";

    protected Socket socket;
    protected BufferedReader in;
    protected Writer out;
    private final UUID uuid;
    private ScheduledExecutorService heartbeat_executor;

    public ContentServer(Socket socket, Writer out, BufferedReader in) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.uuid = UUID.randomUUID();
    }

    public String readFile(String filePath) {
        return getString(filePath);
    }

    protected String build_request(String request_file, String... payload_file) {
        String request = readFile(request_file);
        request = request.replace("{{UUID}}", uuid.toString());

        if (payload_file.length > 0 && payload_file[0] != null) {
            String payload = readFile(payload_file[0]);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String currTime = LocalDateTime.now().format(formatter);
            payload = payload.replace("{{date}}", currTime);

            request = request.replace("{{payload_length}}", String.valueOf(payload.length())).replace("{{payload}}", payload);
        }
        return request;
    }

    public void sendHTTP_message(String location, String... payload) throws IOException {
        String file = (payload != null && payload.length > 0) ? payload[0] : null;
        String request = build_request(location, file);
        out.write(request, 0, request.length());
        out.flush();
    }

    private void startHeartbeat() {
        heartbeat_executor = Executors.newScheduledThreadPool(1);
        heartbeat_executor.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Sending heartbeat\n");
                sendHTTP_message(HEARTBEAT_REQUEST);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }, 10, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    public void shutdown() {
        try {
            sendHTTP_message(SHUTDOWN_REQUEST);

            if (heartbeat_executor != null) {
                stopHeartbeat();
            }
        } catch (IOException e) {
            System.out.println("Error sending shutdown request");
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.err.println("Error sending shutdown request");
        }
    }

    private void stopHeartbeat() {
        heartbeat_executor.shutdown();
        try {
            if (!heartbeat_executor.awaitTermination(10, TimeUnit.SECONDS)) {
                heartbeat_executor.shutdownNow();
                if (!heartbeat_executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("Timed out waiting for executor to shut down");
                }
            }
        } catch (InterruptedException e) {
            heartbeat_executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static Map<String, String> handleCLI_Arguments(String[] args) {
        Map<String, String> mapped_args = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            // if --help or --default, ignore all other arguments
            if (args[i].startsWith("--")) {
                mapped_args.put(args[i].substring(2), "");
                break;
            }

            String key = args[i].substring(2);
            String value = args[i + 1];
            mapped_args.put(key, value);
        }

        if (mapped_args.containsKey("help") || mapped_args.containsKey("h")) {
            System.out.println(CS_HELP);
            return null;
        }

        if (mapped_args.containsKey("default") || mapped_args.containsKey("d")) {
            mapped_args.put("port", "4567");
        }

        if (mapped_args.isEmpty()) {
            System.out.println("No arguments provided. Use --help to see available options");
            return null;
        }

        return mapped_args;
    }

    public static void main(String[] args) throws IOException {
        Map<String, String> mapped_args = handleCLI_Arguments(args);
        if (mapped_args == null) return;

        String host = mapped_args.getOrDefault("host", "localhost");
        int port = Integer.parseInt(mapped_args.getOrDefault("port", "4567"));

        String weatherData = mapped_args.getOrDefault("weather", DEFAULT_WEATHER);

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            ContentServer server = new ContentServer(socket, out, in);
            server.sendHTTP_message(PUT_REQUEST, weatherData);
            Thread.sleep(1000);

            System.out.println("Content server shutting down");
            server.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
