// University of Adelaide - Distributed Systems 2024
// Fida Matin - a1798239
// 4 October 2024

package HTTP;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HTTP_message {
    private final Map<String, String> headers;
    private String body;
    protected String protocolVersion = "HTTP/1.1";

    public HTTP_message() {
        this.headers = new HashMap<>();
    }

    public HTTP_message determine_type(String firstLine) {
        String[] first_line = firstLine.split(" ");

        // Validate the basic structure of the first line
        if (first_line.length != 3) {
            throw new IllegalArgumentException("Invalid first line");
        }

        boolean isRequest = isRequest(first_line);
        boolean isResponse = isResponse(first_line);

        if (isRequest) {
            return new HTTP_request(this, first_line[0], first_line[1], first_line[2]);
        } else if (isResponse) {
            return new HTTP_response(this, first_line[1] + " " + first_line[2], first_line[0]);
        } else {
            System.out.println("Invalid first line: " + firstLine);
            throw new IllegalArgumentException("Invalid first line");
        }
    }

    private boolean isRequest(String[] parts) {
        boolean isValidMethod = parts[0].matches("GET|PUT|POST|DELETE");
        boolean isValidUri = parts[1].startsWith("/");
        boolean isValidProtocol = parts[2].matches("HTTP/1.[01]");

        return isValidMethod && isValidUri && isValidProtocol;
    }

    private boolean isResponse(String[] parts) {
        boolean isValidProtocol = parts[0].matches("HTTP/1.[01]");
        boolean isValidStatusCode = isValidStatusCode(parts[1]);

        return isValidProtocol && isValidStatusCode;
    }

    private boolean isValidStatusCode(String statusCode) {
        // Check if status code is a valid integer
        int statusCodeInt;
        try {
            statusCodeInt = Integer.parseInt(statusCode);
        } catch (NumberFormatException e) {
            return false;
        }
        return statusCodeInt >= 100 && statusCodeInt <= 599;
    }

    public void setHeader(String headerName, String headerValue) {
        switch ((headerName != null) ? headerName : "") {
            case "Connection":
                headers.put("Connection", headerValue);
                break;
            case "Content-Type":
                headers.put("Content-Type", headerValue);
                break;
            case "Content-Length":
                headers.put("Content-Length", headerValue);
                break;
            case "Server":
                headers.put("Server", headerValue);
                break;
            case "User-Agent":
                headers.put("User-Agent", headerValue);
                break;
            case "Timestamp":
                headers.put("Timestamp", headerValue);
                break;
            default:
                System.out.println("Header name unsupported: {0}");
                break;
        }
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHeader(String headerName) {
        return headers.get(headerName);
    }
    public String getBody() {
        return this.body;
    }

    public String headersToString() {
        if (headers.isEmpty()) {
            return "";
        } else {
            return headers.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + ": " + entry.getValue())
                    .collect(Collectors.joining("\n"));
        }
    }



}
