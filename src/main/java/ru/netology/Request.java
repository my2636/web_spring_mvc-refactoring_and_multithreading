package ru.netology;


import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicHeader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Request {
    private String method;
    private String path;
    private String headers;
    private String requestBody;

    public Request(String method, String path, String headers, String requestBody) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.requestBody = requestBody;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeaders() {
        return headers;
    }

    public String getRequestBody() {
        return requestBody;
    }

    @Override
    public String toString() {
        return "Request{" +
                "\n method='" + method + '\'' +
                "\n\n path='" + path + '\'' +
                "\n\n headers='" + headers + '\'' +
                "\n\n requestBody='" + requestBody + '\'' +
                '}';
    }

    public NameValuePair getQueryParam(String name) {

        String namedHeader = this.headers.lines()
                .filter(h -> h.startsWith(name))
                .findFirst()
                .orElse(null);

        assert namedHeader != null;
        return new BasicHeader(name, namedHeader.substring(name.length() + 1).trim());
    }

    public List<NameValuePair> getQueryParams() {
        List<NameValuePair> paramsList = new ArrayList<>();

        String[] headersString = this.headers.split("\r\n");
        System.out.println("\n\n\nArray:::" + Arrays.toString(headersString));

        for (int i = 0; i < headersString.length; i++) {
            paramsList.add(new BasicHeader(headersString[i].substring(0, headersString[i].indexOf(":")),
                    headersString[i].substring(headersString[i].indexOf(" "))));

        }
        return paramsList;
    }
}
