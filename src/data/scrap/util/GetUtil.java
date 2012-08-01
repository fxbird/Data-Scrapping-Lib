package data.scrap.util;

import data.scrap.ExeResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

import java.io.IOException;

public class GetUtil {
    private final static Log log = LogFactory.getLog(GetUtil.class);

    public static ExeResult doGet(String url) {
        PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
        cm.setMaxTotal(100);
        DefaultHttpClient httpClient = new DefaultHttpClient(cm);
        HttpGet get = new HttpGet(url);
        ExeResult result = null;
        try {
            result = new ExeResult(httpClient, httpClient.execute(get));
            result.setUrl(url);
        } catch (IOException e) {
            log.error(e);
            return null;
        }

        return result;
    }

    public static ExeResult doGet(HttpClient httpClient, String url) {
        PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
        cm.setMaxTotal(100);
        HttpGet get = new HttpGet(url);
        ExeResult result = null;
        try {
            result = new ExeResult(httpClient, httpClient.execute(get));
            result.setUrl(url);
        } catch (IOException e) {
            if (e instanceof NoHttpResponseException){
                log.error("Can't get connection to '"+url+"'");
            }else{
                log.error(e);
            }
            return null;
        }

        return result;
    }

    public static ExeResult doProxyGet(String proxyHost, int proxyPort, String targetUrl) {
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

        return doGet(httpClient,targetUrl);

    }
}
