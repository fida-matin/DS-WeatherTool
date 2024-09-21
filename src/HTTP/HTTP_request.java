package HTTP;

import java.util.HashMap;
import java.util.Map;

public class HTTP_request extends HTTP_message {
    private final Map<String, String> req_line = new HashMap<>();

    public HTTP_request() {
        super();
    }

    public HTTP_request(HTTP_message message, String method, String URI, String protocolVersion) {
        this.req_line.put("method", method);
        this.req_line.put("URI", URI);
        this.protocolVersion = protocolVersion;
    }

    public String getRequestMethod() {
        return req_line.get("method");
    }

    public String getURI() {
        return req_line.get("URI");
    }

    public String getRequestLine() {
        return req_line.get("method") + " " + req_line.get("URI") + " " + protocolVersion;
    }

    public void setMethod(String method) {
        req_line.put("method", method);
    }

    public void setURI(String URI) {
        req_line.put("URI", URI);
    }

    public String convertToString() {
        return getRequestLine() + "\r\n" + headersToString() + "\r\n\r\n" + getBody();
    }


}
