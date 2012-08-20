package hc.scrap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

public class ExeResult {
    private HttpClient httpClient;
    private HttpResponse response;
    private String url;

    public ExeResult(HttpClient httpClient, HttpResponse response) {
        this.httpClient = httpClient;
        this.response = response;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
