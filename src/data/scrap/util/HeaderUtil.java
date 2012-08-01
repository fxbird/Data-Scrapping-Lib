package data.scrap.util;

import com.xdg.util.ArrayUtil;
import com.xdg.util.PathUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

public class HeaderUtil {
    public static String getFileName(HttpResponse response, String url) {
        if (response.getHeaders("Content-Disposition").length > 0) {
            return response.getHeaders("Content-Disposition")[0].getElements()[0].getParameterByName("filename").getValue();
        } else {
            return PathUtil.getFileNameFromPath(url);
        }
    }

    public static void printHeaders(HttpResponse response) {
        for (Header header : response.getAllHeaders()) {
            System.out.println(header.getName() + " : " + header.getValue());
        }
    }

    public static boolean isFile(HttpResponse response) {
        printHeaders(response);
        String[] downloadable = {"application/octet-stream", "application/pdf",
                "application/zip", "application/x-gzip", "application/x-zip-compressed",
        "application/x-javascript"};
        String contentType = response.getHeaders("Content-Type")[0].getValue().toLowerCase();
        if (ArrayUtil.contains(contentType, downloadable)) {
            return true;
        } else {
            return false;
        }
    }

    public static long getSize(HttpResponse response) {
        return Long.parseLong(response.getHeaders("Content-Length")[0].getValue());
    }
}
