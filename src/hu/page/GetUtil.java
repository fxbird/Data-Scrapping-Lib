package hu.page;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

public class GetUtil {
    public static HtmlPage getPage(String url){
        WebClient webClient=new WebClient();
        try {
          return   webClient.getPage(url);
        } catch (IOException e) {
            return null;
        } finally {
            webClient.closeAllWindows();
        }
    }
}
