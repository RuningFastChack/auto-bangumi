package auto.bangumi.common.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HttpClientUtil
 *
 * @author sakura
 */
@Slf4j
public abstract class HttpClientUtil {

    private static final int TIMEOUT = 5000;
    private static final String GET = "GET";
    private static final String QUESTION_SIGN = "?";
    private static final String EQUAL_SIGN = "=";
    private static final String RATHER_SIGN = "&";
    private static final String UTF_8 = "UTF-8";

    //region GET
    public static String sendGet(String url) {
        return sendGet(url, null);
    }

    public static String sendGet(String url, Map<String, Object> params) {
        try {
            HttpURLConnection conn = beforeGetRequest(url, params, new HashMap<>());
            String result = afterGetRequest(conn);
            if (result != null)
                return result;
        } catch (Exception e) {
            log.error("sendGet请求失败:{}", e.getMessage());
        }
        return "";
    }

    public static String sendGet(String url, Map<String, Object> params, Map<String, Object> headers) {
        try {
            HttpURLConnection conn = beforeGetRequest(url, params, headers);
            String result = afterGetRequest(conn);
            if (result != null)
                return result;
        } catch (Exception e) {
            log.error("sendGet请求失败:{}", e.getMessage());
        }
        return "";
    }

    public static String sendGetXml(String url, Map<String, Object> params) {
        try {
            HttpURLConnection conn = beforeGetRequest(url, params, new HashMap<>());
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestMethod(GET);
            conn.setRequestProperty("Accept", "application/xml"); // 指定返回 XML
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");  // 防止被屏蔽

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n"); // 保留换行，方便解析 XML
                }
                reader.close();
                return result.toString();
            }
        } catch (Exception e) {
            log.error("sendGetXml请求失败:{}", e.getMessage());
        }
        return "";
    }

    private static String afterGetRequest(HttpURLConnection conn) throws IOException {
        conn.setRequestMethod(GET);
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            return result.toString();
        }
        return null;
    }

    private static HttpURLConnection beforeGetRequest(String url, Map<String, Object> params, Map<String, Object> headers) throws IOException {
        StringBuilder sb = new StringBuilder(url);
        if (params != null && !params.isEmpty()) {
            sb.append(QUESTION_SIGN);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue() != null ? URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8) : "";
                sb.append(key).append(EQUAL_SIGN).append(value).append(RATHER_SIGN);
            }
            sb.deleteCharAt(sb.length() - 1); // 删除最后的 &
        }

        URL realUrl = new URL(sb.toString());
        HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
        conn.setConnectTimeout(TIMEOUT);

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return conn;
    }
    //endregion

    //region POST
    /**
     * POST请求 - form提交
     */
    public static HttpResponse sendFormPost(String host, String path, Map<String, Object> headers,
                                            Map<String, Object> query, Map<String, Object> body) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            return sendFormPost(host, path, headers, query, body, httpClient);
        } catch (Exception e) {
            log.error("sendFormPost请求失败：{}", e.getMessage());
        }
        return null;
    }

    private static HttpResponse sendFormPost(String host, String path, Map<String, Object> headers,
                                             Map<String, Object> query, Map<String, Object> body,
                                             CloseableHttpClient httpClient) throws IOException {
        HttpPost request = new HttpPost(buildUrl(host, path, query));

        if (headers != null) {
            for (Map.Entry<String, Object> e : headers.entrySet()) {
                request.addHeader(e.getKey(), String.valueOf(e.getValue()));
            }
        }

        if (body != null) {
            List<NameValuePair> nameValuePairList = new ArrayList<>();
            for (Map.Entry<String, Object> entry : body.entrySet()) {
                nameValuePairList.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairList, UTF_8);
            request.setEntity(formEntity);
        }

        return httpClient.execute(request);
    }

    /**
     * POST请求 - JSON提交
     */
    public static HttpResponse sendJsonPost(String host, String path, Map<String, Object> headers,
                                            Map<String, Object> query, Map<String, Object> body) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            return sendJsonPost(host, path, headers, query, body, httpClient);
        } catch (Exception e) {
            log.error("sendJsonPost请求失败：{}", e.getMessage());
        }
        return null;
    }

    private static HttpResponse sendJsonPost(String host, String path, Map<String, Object> headers,
                                             Map<String, Object> query, Map<String, Object> body,
                                             CloseableHttpClient httpClient) throws IOException {
        HttpPost request = new HttpPost(buildUrl(host, path, query));

        // 设置默认 Content-Type 为 JSON
        request.setHeader("Content-Type", "application/json");

        if (headers != null) {
            for (Map.Entry<String, Object> e : headers.entrySet()) {
                request.addHeader(e.getKey(), String.valueOf(e.getValue()));
            }
        }

        if (body != null) {
            StringEntity entity = new StringEntity(JSON.toJSONString(body), StandardCharsets.UTF_8);
            request.setEntity(entity);
        }

        return httpClient.execute(request);
    }
    //endregion

    //region PUT

    /**
     * PUT请求 - form提交
     */
    public static HttpResponse sendFormPut(String host, String path, Map<String, Object> headers,
                                           Map<String, Object> query, Map<String, Object> body) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            return sendFormPut(host, path, headers, query, body, httpClient);
        } catch (Exception e) {
            log.error("sendFormPut请求失败：{}", e.getMessage());
        }
        return null;
    }

    private static HttpResponse sendFormPut(String host, String path, Map<String, Object> headers,
                                            Map<String, Object> query, Map<String, Object> body, CloseableHttpClient httpClient) throws IOException {
        HttpPut request = new HttpPut(buildUrl(host, path, query));
        if (headers != null) {
            for (Map.Entry<String, Object> e : headers.entrySet()) {
                request.addHeader(e.getKey(), String.valueOf(e.getValue()));
            }
        }
        if (body != null) {
            List<NameValuePair> nameValuePairList = new ArrayList<>();
            for (Map.Entry<String, Object> entry : body.entrySet()) {
                nameValuePairList.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairList, UTF_8);
            request.setEntity(formEntity);
        }
        return httpClient.execute(request);
    }

    /**
     * PUT请求 - JSON提交
     */
    public static HttpResponse sendJsonPut(String host, String path, Map<String, Object> headers,
                                           Map<String, Object> query, Map<String, Object> body) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            return sendJsonPut(host, path, headers, query, body, httpClient);
        } catch (Exception e) {
            log.error("sendJsonPut请求失败：{}", e.getMessage());
        }
        return null;
    }

    private static HttpResponse sendJsonPut(String host, String path, Map<String, Object> headers,
                                            Map<String, Object> query, Map<String, Object> body,
                                            CloseableHttpClient httpClient) throws IOException {
        HttpPut request = new HttpPut(buildUrl(host, path, query));

        // 设置默认 Content-Type 为 JSON
        request.setHeader("Content-Type", "application/json");

        if (headers != null) {
            for (Map.Entry<String, Object> e : headers.entrySet()) {
                request.addHeader(e.getKey(), String.valueOf(e.getValue()));
            }
        }

        if (body != null) {
            // 使用 Jackson 或其他 JSON 库将 Map 转换为 JSON 字符串
            StringEntity entity = new StringEntity(JSON.toJSONString(body), StandardCharsets.UTF_8);
            request.setEntity(entity);
        }

        return httpClient.execute(request);
    }
    //endregion

    //region DELETE

    /**
     * DELETE请求 - 普通请求（不带请求体）
     */
    public static HttpResponse sendDelete(String host, String path, Map<String, Object> headers,
                                          Map<String, Object> query) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            return sendDelete(host, path, headers, query, httpClient);
        } catch (Exception e) {
            log.error("sendDelete请求失败：{}", e.getMessage());
        }
        return null;
    }

    private static HttpResponse sendDelete(String host, String path, Map<String, Object> headers,
                                           Map<String, Object> query, CloseableHttpClient httpClient) throws IOException {
        HttpDelete request = new HttpDelete(buildUrl(host, path, query));

        if (headers != null) {
            for (Map.Entry<String, Object> e : headers.entrySet()) {
                request.addHeader(e.getKey(), String.valueOf(e.getValue()));
            }
        }

        return httpClient.execute(request);
    }

    /**
     * DELETE请求 - 带JSON请求体（使用自定义 HttpDeleteWithBody）
     */
    public static HttpResponse sendJsonDelete(String host, String path, Map<String, Object> headers,
                                              Map<String, Object> query, Map<String, Object> body) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            return sendJsonDelete(host, path, headers, query, body, httpClient);
        } catch (Exception e) {
            log.error("sendJsonDelete请求失败：{}", e.getMessage());
        }
        return null;
    }

    private static HttpResponse sendJsonDelete(String host, String path, Map<String, Object> headers,
                                               Map<String, Object> query, Map<String, Object> body,
                                               CloseableHttpClient httpClient) throws IOException {
        // 使用自定义的 HttpDeleteWithBody 类
        HttpDeleteWithBody request = new HttpDeleteWithBody(buildUrl(host, path, query));

        // 设置默认 Content-Type 为 JSON
        request.setHeader("Content-Type", "application/json");

        if (headers != null) {
            for (Map.Entry<String, Object> e : headers.entrySet()) {
                request.addHeader(e.getKey(), String.valueOf(e.getValue()));
            }
        }

        if (body != null) {
            StringEntity entity = new StringEntity(JSON.toJSONString(body), StandardCharsets.UTF_8);
            request.setEntity(entity);
        }

        return httpClient.execute(request);
    }

    /**
     * DELETE请求 - 带Form表单请求体
     */
    public static HttpResponse sendFormDelete(String host, String path, Map<String, Object> headers,
                                              Map<String, Object> query, Map<String, Object> body) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            return sendFormDelete(host, path, headers, query, body, httpClient);
        } catch (Exception e) {
            log.error("sendFormDelete请求失败：{}", e.getMessage());
        }
        return null;
    }

    private static HttpResponse sendFormDelete(String host, String path, Map<String, Object> headers,
                                               Map<String, Object> query, Map<String, Object> body,
                                               CloseableHttpClient httpClient) throws IOException {
        // 使用自定义的 HttpDeleteWithBody 类
        HttpDeleteWithBody request = new HttpDeleteWithBody(buildUrl(host, path, query));

        if (headers != null) {
            for (Map.Entry<String, Object> e : headers.entrySet()) {
                request.addHeader(e.getKey(), String.valueOf(e.getValue()));
            }
        }

        if (body != null) {
            List<NameValuePair> nameValuePairList = new ArrayList<>();
            for (Map.Entry<String, Object> entry : body.entrySet()) {
                nameValuePairList.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairList, UTF_8);
            request.setEntity(formEntity);
        }

        return httpClient.execute(request);
    }
    //endregion

    /**
     * 组装URL
     */
    private static String buildUrl(String host, String path, Map<String, Object> query) {
        StringBuilder sbUrl = new StringBuilder();
        try {
            sbUrl.append(host);
            if (path != null && !path.isEmpty()) {
                sbUrl.append(path);
            }
            if (query != null && !query.isEmpty()) {
                StringBuilder sbQuery = new StringBuilder();
                for (Map.Entry<String, Object> entry : query.entrySet()) {
                    if (!sbQuery.isEmpty())
                        sbQuery.append(RATHER_SIGN);
                    sbQuery.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                    sbQuery.append(EQUAL_SIGN);
                    sbQuery.append(URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8));
                }
                sbUrl.append(QUESTION_SIGN).append(sbQuery);
            }
        } catch (Exception e) {
            log.error("组装URL异常:{}", e.getMessage());
        }
        return sbUrl.toString();

    }

    /**
     * 跳过SSL证书检查
     */
    private static class TrustAnyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
