package hp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class GetUtil {
    private final static Log log = LogFactory.getLog(GetUtil.class);

    public static String getHtml(String url, NodeFilter filter) {
        Parser parser = new Parser();
        try {
            parser.setURL(url);
            NodeList nodeList = parser.extractAllNodesThatMatch(filter);
            if (nodeList.size() == 0) {
                return "";
            } else {
                return nodeList.elementAt(0).toHtml();
            }
        } catch (ParserException e) {
            log.error(e);
            return null;
        }
    }

    public static NodeList getNodeList(String url, NodeFilter filter) {
        Parser parser = new Parser();
        try {
            parser.setURL(url);
            NodeList nodeList = parser.extractAllNodesThatMatch(filter);
            return nodeList;
        } catch (ParserException e) {
            log.error(e);
            return new NodeList();
        }
    }


}
