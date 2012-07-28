package data.scrap.util;

import data.scrap.ExeResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class GetUtil {
    private final static Log log = LogFactory.getLog(GetUtil.class);
    public static ExeResult doGet(String url){
        DefaultHttpClient httpClient=new DefaultHttpClient();
        HttpGet get=new HttpGet(url);
        ExeResult result= null;
        try {
            result = new ExeResult(httpClient, httpClient.execute(get));
            result.setUrl(url);
        } catch (IOException e) {
            log.error(e);
            return null;
        }

        return result;
    }
}
