package page;

import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class Clicker {
    private final static Log log = LogFactory.getLog(Clicker.class);
    private HtmlPage page;

    public Clicker(HtmlPage page) {
        this.page = page;
    }

    public HtmlPage clickByID(String id){
        HtmlInput buttonInput=page.getHtmlElementById(id);
        if (buttonInput==null){
            throw new IllegalArgumentException("element with id '"+id+"' doesn't exist!");
        }

        try {
            if (buttonInput instanceof HtmlButtonInput){
               return  ((HtmlButtonInput)buttonInput).click();
            } else if (buttonInput instanceof HtmlSubmitInput){
                return  ((HtmlSubmitInput)buttonInput).click();
            } else {
                throw new IllegalArgumentException("element with id '"+id+"' is not button");
            }
        } catch (IOException e) {
            log.error(e);
            return null;
        }
    }
}
