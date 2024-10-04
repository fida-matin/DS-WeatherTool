package Client.GET;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

import static Client.Content.ContentServer.handleCLI_Arguments;


public class GETClient implements AutoCloseable {
    protected Socket socket;
    protected BufferedReader in;
    protected Writer out;

    static final String requestAddress = "src/Client/resources/GETRequest.txt";

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

    public static void main(String[] args) {
        Map<String, String> mapped_args = handleCLI_Arguments(args);
        if (mapped_args == null) return;

        String host = mapped_args.getOrDefault("host", "localhost");
        int port = Integer.parseInt(mapped_args.getOrDefault("port", "4567"));
        String request = mapped_args.getOrDefault("request", requestAddress);
        String URI = mapped_args.getOrDefault("URI", "recent");

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             GETClient client = new GETClient(socket, out, in)) {

            client.sendHTTP_message(request, URI);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

        @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
