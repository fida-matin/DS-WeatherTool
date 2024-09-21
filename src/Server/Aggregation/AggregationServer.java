package Server.Aggregation;

import HTTP.HTTP_message;
import HTTP.HTTP_request;

import java.net.*;
import java.io.*;

public class AggregationServer implements AutoCloseable {
    private final ServerSocket server;

    public AggregationServer(ServerSocket server) {
        this.server = server;
    }

    public void readRequest(Socket clientSocket, BufferedReader in, BufferedWriter out) throws IOException {
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

        // perform a HTTP response


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


    @Override
    public void close() throws Exception {
        server.close();
    }
}
