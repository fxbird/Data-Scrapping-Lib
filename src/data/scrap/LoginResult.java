package data.scrap;

import org.apache.http.client.HttpClient;

public class LoginResult {
    private HttpClient httpClient;
    private boolean successful;
    private int statusCode;
    private String resultContent;

    public LoginResult(HttpClient httpClient, boolean successful, int statusCode) {
        this.httpClient = httpClient;
        this.successful = successful;
        this.statusCode = statusCode;
    }

    public LoginResult() {
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResultContent() {
        return resultContent;
    }

    public void setResultContent(String resultContent) {
        this.resultContent = resultContent;
    }
}
