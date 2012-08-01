package page;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

public class ValueSetter {
    private HtmlPage page;

    public ValueSetter(HtmlPage page) {
        this.page = page;
    }

    public void setTextValueByID(String id, String value) {
        HtmlTextInput textInput = (HtmlTextInput) page.getHtmlElementById(id);
        textInput.setValueAttribute(value);
    }

    public static void setTextValueByID(HtmlPage page, String id, String value) {
        HtmlTextInput textInput = (HtmlTextInput) page.getHtmlElementById(id);
        textInput.setValueAttribute(value);
    }

}
