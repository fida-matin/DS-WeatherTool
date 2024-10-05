// University of Adelaide - Distributed Systems 2024
// Fida Matin - a1798239
// 4 October 2024


package Server.Aggregation;

import HTTP.HTTP_message;
import HTTP.HTTP_request;
import util.JSONObject;
import util.LamportClock;
import util.Weather;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static Client.Content.ContentServer.handleCLI_Arguments;

public class AggregationServer implements AutoCloseable, Serializable {

    // Status Code
    public static final String INTERNAL_SERVER_ERROR = "500 Internal_server_error";
    public static final String OK_CODE = "200 OK";
    public static final String CREATED_CODE = "201 HTTP_CREATED";
    public static final String METHOD_NOT_IMPLEMENTED_CODE = "400 Method_not_implemented";

    public static final String PUT_RESPONSE = "Aggregation Server successfully received PUT request at ";
    public static final String POST_RESPONSE = "Aggregation Server does not support POST requests";
    public static final String DELETE_RESPONSE = "Aggregation Server does not support DELETE requests";
    public static final String ERROR_RESPONSE = "Aggregation Server failed to process request at ";

    private final ServerSocket server;
    public LamportClock clock;
    private final ExecutorService threadPool;

    @Serial
    private static final long serialVersionUID = 1L;

    public Map<UUID, LinkedList<Weather>> CS_Updates = new HashMap<>();
    public Map<String, Weather> stationUpdates = new LinkedHashMap<>();

    public static final String BASE_PATH = "src/Server/Aggregation/resources";

    boolean fileCreated = false;


    public AggregationServer(ServerSocket server) {
        this.server = server;
        this.threadPool =  Executors.newFixedThreadPool(10);;
        clock = new LamportClock();
    }

    public void readRequest(Socket clientSocket, BufferedReader in, BufferedWriter out) throws IOException {
        try {
            HTTP_message message = new HTTP_message();

            String val = in.readLine();
            if (val == null) {
                throw new IOException("Closed Connection");
            } else if (val.isEmpty()) {
                message = null;
            }

            try {
                if (message != null) {
                    message = message.determine_type(val);
                }
            } catch (Exception e) {
                System.err.println("Error parsing request: " + e.getMessage());
            }

            if (message != null) {
                message = read_headers(message, in, out);
                message = read_body(message, in, out);
            }

            HTTP_request request = (HTTP_request) message;
            assert request != null;
            System.out.println("Received client request from " + clientSocket.getInetAddress() + ":\n" + request.convertToString());

            // perform an HTTP response
            String response = "";
            switch (request.getRequestMethod() != null ? request.getRequestMethod() : "") {
                case "GET":
                    response = this.doGETRequest(request);
                    break;
                case "PUT":
                    response = this.doPUTRequest(request);
                    break;
                case "POST":
                    response = buildResponse(METHOD_NOT_IMPLEMENTED_CODE, POST_RESPONSE, Collections.emptyMap());
                    break;
                case "DELETE":
                    response = buildResponse(METHOD_NOT_IMPLEMENTED_CODE, DELETE_RESPONSE, Collections.emptyMap());
                    break;
                default:
                    System.err.println("Unsupported request method: " + request.getRequestMethod());
                    break;
            }
            fileCreated = false;
            out.write(response, 0, response.length());
            out.flush();
            System.out.println("Sent response to client " + clientSocket.getInetAddress() + ":\n" + response);
        } catch (IOException e) {
            System.err.println("I/O exception while processing request from: " + clientSocket.getInetAddress());
        } catch (Exception e) {
            System.err.println("Unexpected error while processing request from: " + clientSocket.getInetAddress());
        }

    }

    public HTTP_message read_headers(HTTP_message message, BufferedReader in, BufferedWriter out) throws IOException {
        String val;
        while ((val = in.readLine()) != null && !val.isEmpty()) {
            String[] header = val.split(":", 2);
            if (header.length >= 2) {
                message.setHeader(header[0].trim(), header[1].trim());
            } else {
                throw new IllegalArgumentException("Invalid header: " + val);
            }
        }

        return message;
    }

    public HTTP_message read_body(HTTP_message message, BufferedReader in, BufferedWriter out) throws IOException {
        int length;

        try {
            length = Integer.parseInt(message.getHeader("Content-Length"));
        } catch (Exception e) {
            System.out.println("Invalid Content-Length header: " + message.getHeader("Content-Length"));
            throw new ProtocolException("Invalid Content-Length header");
        }

        if (length > 0) {
            char[] bodyChars = new char[length];
            int bytesRead = 0;

            while (bytesRead < length) {
                int result = in.read(bodyChars, bytesRead, length - bytesRead);

                // Handle end of stream
                if (result == -1) {
                    throw new IOException("Connection closed before all data was read");
                }

                // If '\r\n' occurs before expected content length, consider it as a client mistake
                if (bodyChars[bytesRead] == '\r' && bodyChars[bytesRead + 1] == '\n') {
                    throw new IOException("Encountered CRLF before expected content length was read");
                }

                bytesRead += result;
            }

            // Validate bytesRead against contentLength
            if (bytesRead != length) {
                throw new IOException("Incorrect Content-Length header. Expected: " + length + " but got: " + bytesRead);
            }
            message.setBody(new String(bodyChars, 0, bytesRead));

        } else {
            message.setBody("");
        }

        return message;

    }

    public static String buildResponse(String statusCode, String response, Map<String, String> headers) {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("HTTP/1.1 ").append(statusCode).append("\r\n");
        responseBuilder.append("Server: Aggregation Server/1.0 (Unix)\r\n");
        responseBuilder.append("Content-Type: application/json\r\n");

        if (!headers.containsKey("Connection")) {
            responseBuilder.append("Connection: keep-alive\r\n");
        }

        for (Map.Entry<String, String> header : headers.entrySet()) {
            responseBuilder.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        responseBuilder.append("\r\n");
        responseBuilder.append(response);
        return responseBuilder.toString();
    }

    public String getRecentWeatherUpdate(String stationID) {
        if ("recent".equals(stationID)) {
            if (!stationUpdates.isEmpty()) {
                return stationUpdates.entrySet().iterator().next().getValue().weatherData.JSONtoString();
            } else throw new RuntimeException("No weather updates exist.");

        } else {
            return stationUpdates.get(stationID).weatherData.JSONtoString();
        }
    }

    public String handleWeatherRequest(HTTP_request request, String stationID) {
        try {
            String responseBody = this.getRecentWeatherUpdate(stationID);
            return buildResponse(OK_CODE, responseBody, Collections.emptyMap());
        } catch (Exception e) {
            System.err.println("Unexpected error while processing request: " + e.getMessage());
            return buildResponse(INTERNAL_SERVER_ERROR, ERROR_RESPONSE, Collections.emptyMap());
        }
    }

    public String doPUTRequest(HTTP_request request) {
        try {
            int timestamp = Integer.parseInt(request.getHeader("Timestamp"));
            LamportClock.Event event = clock.processTimestamp(timestamp);
            UUID CS_UUID;
            JSONObject weatherData = null;

            // get Content Server ID
            try {
                CS_UUID = UUID.fromString(request.getURI().replace("/data/",""));
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid UUID: " + request.getURI());
                throw new ProtocolException("Invalid UUID: " + request.getURI());
            }

            // get weather data
            try {
                weatherData = new JSONObject(request.getBody());
            } catch (Exception e) {
                System.err.println("Unexpected error while processing request: " + e.getMessage());
            }

            // weather update
            assert weatherData != null;
            Weather weather = new Weather(CS_UUID, weatherData);

            // process request based on the event
            switch (event) {
                case BEFORE:
                    break;
                case CONCURRENT:
                    System.out.println("Received Concurrent event.");
                    Weather recentUpdates = stationUpdates.get(weather.stationID);
                    if (recentUpdates == null || recentUpdates.timestamp.isAfter(recentUpdates.timestamp)) {
                        addWeatherUpdate(weather, CS_UUID);
                    }
                    break;
                case AFTER:
                    System.out.println("Received most recent event.");
                    addWeatherUpdate(weather, CS_UUID);
                    break;
            }

            Map<String, String> headers = Map.of("Connection", "close", "Timestamp", String.valueOf(clock.getTimestamp()));

            return buildResponse( fileCreated ? CREATED_CODE : OK_CODE, PUT_RESPONSE + ZonedDateTime.now(), headers);

        } catch (Exception e) {
            System.err.println("Unexpected error while processing request: " + e.getMessage());
            return buildResponse(INTERNAL_SERVER_ERROR, ERROR_RESPONSE, Collections.emptyMap());
        }
    }

    private void addWeatherUpdate(Weather weather, UUID CS_UUID) throws IOException {
        LinkedList<Weather> updates = CS_Updates.getOrDefault(weather.stationID, new LinkedList<>());
        updates.add(weather);
        if (updates.size() > 20) {
            updates.removeLast();
        }
        CS_Updates.put(weather.CS_UUID, updates);
        stationUpdates.put(weather.stationID, weather);

        String serverFile = BASE_PATH + "/data/" + CS_UUID + ".txt";
        String stationFile = BASE_PATH + "/data/" + "weatherUpdatesByStation" + ".txt";

        fileCreated = this.createFile_IfNoFile(serverFile);

        this.saveToFile(CS_Updates.get(CS_UUID), serverFile);
        this.saveToFile(stationUpdates, stationFile);
    }

    private boolean createFile_IfNoFile(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
                System.out.println("File created: " + fileName);
                return true;
            } catch (IOException e) {
                System.err.println("Could not create file: " + fileName);
                throw e;
            }
        } else {
            System.out.println("File already exists: " + fileName);
            return false;
        }
    }

    private void saveToFile(Object obj, String fileName) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(fileName);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(obj);
        } catch (IOException e) {
            System.err.println("Could not write to file: " + fileName);
            throw e;
        }
    }

    public String doGETRequest(HTTP_request request) {
        try {
            String[] URI_Components = request.getURI().split("/");

            if (URI_Components.length > 1) {
                switch (URI_Components[1]) {
                    case "heartbeat":
                        UUID CS_UUID = UUID.fromString(URI_Components[2]);
                    case "weather":
                        String stationID = URI_Components[2];
                        return handleWeatherRequest(request, stationID);
                    case "shutdown":
                        UUID CS_UUID2 = UUID.fromString(URI_Components[2]);
                    default:
                        throw new IllegalArgumentException("Invalid URI: " + request.getURI());
                }
            } else if (URI_Components[0].equals("/")) {
                return handleWeatherRequest(request, "recent");
            } else {
                throw new IllegalArgumentException("Invalid URI: " + request.getURI());
            }

        } catch (Exception e) {
            System.err.println("Unexpected error while processing request: " + e.getMessage());
            return buildResponse(INTERNAL_SERVER_ERROR, ERROR_RESPONSE, Collections.emptyMap());
        }
    }

    private void handleClientSocket(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.readRequest(clientSocket, in, out);
        } catch (IOException e) {
            System.err.println("Unexpected error while processing request: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Unexpected error while processing request: " + e.getMessage());
            }
        }
    }

    public void run() {
        while (true) {
            try {
                final Socket clientSocket = server.accept();
                threadPool.submit(() -> handleClientSocket(clientSocket));
            } catch (IOException e) {
                System.err.println("Unexpected error while processing request: " + e.getMessage());
                break;
            }
        }
    }

    public static void main(String[] args) {
        Map<String, String> mapped_args = handleCLI_Arguments(args);
        if (mapped_args != null) {
            int port = Integer.parseInt(mapped_args.getOrDefault("port", "8081"));

            try (ServerSocket serverSocket = new ServerSocket(port);

                 AggregationServer server = new AggregationServer(serverSocket)) {

                server.run();
            } catch (NumberFormatException nfe) {
                System.err.println("Invalid port number: " + port + ": " + nfe.getMessage());
            } catch (IOException ioe) {
                System.err.println("Failed to initialize server socket: " + ioe.getMessage());
            } catch (Exception e) {
                System.err.println("Unexpected error while processing request: " + e.getMessage());
            }
        }
    }


    @Override
    public void close() throws Exception {
        server.close();
    }
}
