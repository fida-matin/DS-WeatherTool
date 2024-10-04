// University of Adelaide - Distributed Systems 2024
// Fida Matin - a1798239
// 4 October 2024


package HTTP;

public class HTTP_response extends HTTP_message {
    private String status_code;

    public HTTP_response() {
        super();
    }

    public HTTP_response( HTTP_message httpMessage, String status_code, String protocolVersion) {
        this.status_code = status_code;
        this.protocolVersion = protocolVersion;
    }

    public String getStatus_code() {
        return status_code.trim();
    }

    public String getReponseLine() {
        return protocolVersion + " " + status_code;
    }

    public void setStatusCode(String status_code) {
        this.status_code = status_code;
    }

    public String convertToString() {
        return protocolVersion + " " + status_code + "\r\n" + headersToString() + "\r\n\r\n" + getBody();
    }


}
