package Client.GET;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;


public class GETClient implements AutoCloseable {
    protected Socket socket;
    protected BufferedReader in;
    protected Writer out;

    public GETClient(Socket socket, Writer out, BufferedReader in) {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    public String readFile(String filePath) {
        return getString(filePath);
    }

    @NotNull
    public static String getString(String filePath) {
        StringBuilder fileContent = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                fileContent.append(line);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return fileContent.toString();
    }

    protected String build_request(String requestAddress, String... payload) {
        String request = readFile(requestAddress);

        if (payload[0] != null) {
            String URI = payload[0];
            return request.replace("{{URI}}", URI);
        }

        return request;
    }


    public void sendHTTP_message(String location, String... payload) throws IOException {
        String request = build_request(location, payload);
        out.write(request, 0, request.length());
        out.flush();
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
