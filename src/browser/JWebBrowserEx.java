package browser;

import chrriis.dj.nativeswing.NSOption;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JWebBrowserEx extends JWebBrowser {
    private List<String> navHist = new ArrayList<>();
    private List<String> fetchHist = new ArrayList<>();
    private final static Log log = LogFactory.getLog(JWebBrowserEx.class);
    private Map<String,ContentHandler> contentHandlers=new HashMap<>();

    public JWebBrowserEx(NSOption... options) {
        super(options);
    }

    public JWebBrowserEx() {
        super();
        addWebBrowserListener(new WebBrowserAdapter() {
            @Override
            public void loadingProgressChanged(WebBrowserEvent e) {
                if (e.getWebBrowser().getLoadingProgress()==100){
                    triggerContentHandle();
                }
            }
        });
    }

    /**
     * navigate to new page if current page is specific page
     * @param newUrl
     * @param oldurl
     */
    public void go(String newUrl, String oldurl) {
        if (navHist.contains(newUrl)) return;

        if (StringUtils.isNotEmpty(getResourceLocation()) && getResourceLocation().contains(oldurl)) {
            log.debug("go to page '"+newUrl+"' from '"+oldurl+"'");
            navigate(newUrl);
            navHist.add(newUrl);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void addContentHandler(String url, ContentHandler handler) {
        contentHandlers.put(url, handler);
    }

    private void triggerContentHandle(){
        String url=getResourceLocation();
        log.debug("current url :'"+url+"'");
        if (fetchHist.contains(url)) return;
        if (StringUtils.isEmpty(url)){
            return;
        }

        for (String urlKey : contentHandlers.keySet()) {
            if (url.contains(urlKey)){
                log.debug("handling content of '" + url + "'");
                contentHandlers.get(urlKey).handle(url,getHTMLContent());
                fetchHist.add(url);
                return;
            }
        }
    }

    /**
     * assign the full navigation path , so that browser can change url on its own
     * @param urls
     */
    public void goByStep(String... urls) {
        for (int i = 1; i < urls.length; i++) {
            go(urls[i], urls[i - 1]);
        }
    }

    public void setFieldValue(String fieldHolderJS,String value){
        executeJavascript(fieldHolderJS+".value='"+value+"'");
    }

    public Object getFieldValue(String fieldHolderJS){
        return executeJavascriptWithResult("return "+fieldHolderJS+".value");
    }

}
