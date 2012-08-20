package hu.page;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import exception.ElementNotExistExp;

public class ValueSetter {
    private HtmlPage page;

    public ValueSetter(HtmlPage page) {
        this.page = page;
    }

    public void setTextValueByID(String id, String value) throws ElementNotExistExp {
        HtmlElement element=page.getHtmlElementById(id);
        if (element==null){
            throw new ElementNotExistExp(id);
        }

        if (element instanceof HtmlPasswordInput){
            ((HtmlPasswordInput)element).setValueAttribute(value);
        }else if (element instanceof HtmlTextInput){
            ((HtmlTextInput)element).setValueAttribute(value);
        } else {
            throw new IllegalArgumentException("element [id="+id+"] is not a textual input");
        }
    }

    public static void setTextValueByID(HtmlPage page, String id, String value) throws ElementNotExistExp {
        new ValueSetter(page).setTextValueByID(id,value);
    }

}
