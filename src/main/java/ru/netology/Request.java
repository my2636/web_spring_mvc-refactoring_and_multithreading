package ru.netology;



public class Request {
    private String method;
    private String headers;
    private String requestBody;

    public Request(String method, String headers, String requestBody) {
        this.method = method;
        this.headers = headers;
        this.requestBody = requestBody;
    }

    public String getMethod() {
        return method;
    }

    public String getHeaders() {
        return headers;
    }

    public String getRequestBody() {
        return requestBody;
    }

}
