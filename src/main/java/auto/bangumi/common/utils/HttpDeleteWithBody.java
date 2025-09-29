package auto.bangumi.common.utils;

import org.apache.http.client.methods.HttpPost;

/**
 * HttpDeleteWithBody
 *
 * @author 查查
 * @since 2025/9/23
 */
public class HttpDeleteWithBody extends HttpPost {
    public HttpDeleteWithBody(String url) {
        super(url);
    }

    @Override
    public String getMethod() {
        return "DELETE";
    }
}
