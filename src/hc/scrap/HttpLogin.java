package hc.scrap;

import com.xdg.util.XmlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class HttpLogin {
    private static final String LOGIN_PATH = "login";
    private String configPkgName;
    private final static Log log = LogFactory.getLog(HttpLogin.class);

    public HttpLogin(String configPkgName) {
        this.configPkgName = configPkgName;
    }

    public LoginResult login() {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        LoginResult loginResult=new LoginResult();
        HttpEntity entity = null;

        try {
            SAXReader saxReader = new SAXReader();
            Document doc = saxReader.read(HttpLogin.class.getResourceAsStream(configPkgName));

            HttpResponse response = null;
            HttpPost httpPost = new HttpPost(XmlUtil.getElementValue(doc, "//" + LOGIN_PATH + "/url"));


            List<NameValuePair> nvps = new ArrayList<NameValuePair>();

            for (Element element : XmlUtil.selectElements(doc, "//" + LOGIN_PATH + "/fields/field")) {
                String name = XmlUtil.getAttrValue(element, "name");
                String value = XmlUtil.getAttrValue(element, "value");
                nvps.add(new BasicNameValuePair(name, value));
            }

            httpPost.setEntity(new UrlEncodedFormEntity(nvps,
                    StringUtils.isEmpty(XmlUtil.getElementValue(doc, "//" + LOGIN_PATH + "/encoding")) ? Consts.UTF_8 : Charset.forName(XmlUtil.getElementValue(doc, "//" + LOGIN_PATH + "/encoding"))));

            response = httpclient.execute(httpPost);
            entity = response.getEntity();

            loginResult.setHttpClient(httpclient);
            loginResult.setStatusCode(response.getStatusLine().getStatusCode());
            if (loginResult.getStatusCode()== HttpStatus.SC_OK){
                String successfulStr=XmlUtil.getElementValue(doc, "//" + LOGIN_PATH + "/success-value");
                loginResult.setResultContent(EntityUtils.toString(entity));
                loginResult.setSuccessful(loginResult.getResultContent().contains(successfulStr));
            }

            EntityUtils.consume(entity);

        } catch (ClientProtocolException e) {
            log.error(e);
        } catch (DocumentException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        } finally {
           return loginResult;
        }
    }
}
