import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.*;
import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * http请求客户端
 * 支持https，ssl认证（单双向）
 * 使用httpclient
 * 使用连接池
 * HttpClient全局唯一，连接池全局唯一
 */
public class CustomHttpClient {

    public static final String HTTP_GET = "HTTP_GET";
    public static final String HTTP_POST = "HTTP_POST";
    public static final String ENCODING = "UTF-8";
    public static final String REQUEST_ERR = "-1";
    public static final int TIMEOUT = 1000 * 30;

    private static PoolingHttpClientConnectionManager connManager = null;
    private static Map<String, String> defaultHeader = new HashMap<>();
    private static HttpClient client = null;
    private static HttpRequestRetryHandler retry = null;


    /**
     * 静态代码块配置连接池信息
     */
    static {
        initConnectionManager();
        initRetryHandler();
        initClient();
        initDefaultHeader();
    }

    private CustomHttpClient() {
    }

    private static void initDefaultHeader() {
        //初始化默认请求头
        defaultHeader.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        defaultHeader.put("Accept-Encoding", "gzip, deflate, br");
        defaultHeader.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        defaultHeader.put("Connection", "keep-alive");
        defaultHeader.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36");
    }

    private static void initConnectionManager() {
        if (connManager == null) {
            try {
                SSLContext sslContext = getSSLContext(true, null, null);
                // 注册
                Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                        .register("https", new SSLConnectionSocketFactory(sslContext))
                        .build();

                // ssl注册到连接池
                connManager = new PoolingHttpClientConnectionManager(registry);
                connManager.setMaxTotal(1000);  // 连接池最大连接数
                connManager.setDefaultMaxPerRoute(20);  // 每个路由最大连接数
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void initClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                // 获取连接超时时间
                .setConnectionRequestTimeout(TIMEOUT)
                // 请求超时时间
                .setConnectTimeout(TIMEOUT)
                // 响应超时时间
                .setSocketTimeout(TIMEOUT)
                .build();

        // 创建httpClient
        client = HttpClients.custom()
                // 把请求相关的超时信息设置到连接客户端
                .setDefaultRequestConfig(requestConfig)
                // 把请求重试设置到连接客户端
                .setRetryHandler(retry)
                .setConnectionManager(connManager)
                .build();
    }

    /**
     * 获取SSL上下文对象,用来构建SSL Socket连接
     *
     * @param isDeceive 是否绕过SSL，绕过ssl时所有验证都默认通过
     * @param creFile   整数文件,isDeceive为true 可传null
     * @param crePwd    整数密码,isDeceive为true 可传null, 空字符为没有密码
     * @return SSL上下文对象
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws IOException
     * @throws FileNotFoundException
     * @throws CertificateException
     */
    private static SSLContext getSSLContext(boolean isDeceive, File creFile, String crePwd) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, FileNotFoundException, IOException {

        SSLContext sslContext = null;

        if (isDeceive) {
            sslContext = SSLContext.getInstance("SSLv3");
            // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
            X509TrustManager x509TrustManager = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
            };
            sslContext.init(null, new TrustManager[]{x509TrustManager}, null);
        } else {
            if (null != creFile && creFile.length() > 0) {
                if (null != crePwd) {
                    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    keyStore.load(new FileInputStream(creFile), crePwd.toCharArray());
                    sslContext = SSLContexts.custom().loadTrustMaterial(keyStore, new TrustSelfSignedStrategy()).build();
                } else {
                    throw new SSLHandshakeException("整数密码为空");
                }
            }
        }

        return sslContext;

    }

    private static void initRetryHandler() {
        if (retry == null) {
            retry = new HttpRequestRetryHandler() {
                @Override
                public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                    if (executionCount >= 3) {// 如果已经重试了3次，就放弃
                        return false;
                    }
                    if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                        return true;
                    }
                    if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                        return false;
                    }
                    if (exception instanceof InterruptedIOException) {// 超时
                        return true;
                    }
                    if (exception instanceof UnknownHostException) {// 目标服务器不可达
                        return false;
                    }
                    if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                        return false;
                    }
                    if (exception instanceof SSLException) {// ssl握手异常
                        return false;
                    }
                    HttpClientContext clientContext = HttpClientContext.adapt(context);
                    HttpRequest request = clientContext.getRequest();
                    // 如果请求是幂等的，就再次尝试
                    if (!(request instanceof HttpEntityEnclosingRequest)) {
                        return true;
                    }
                    return false;
                }
            };
        }
    }


    /**
     * 发送请求
     *
     * @param url
     * @param params
     * @param headers
     * @param isStream
     * @return
     * @throws URISyntaxException
     */
    public static String httpRequest(String url, String requestMethod, Map<String, Object> params, Map<String, Object> headers, boolean isStream) throws URISyntaxException, UnsupportedEncodingException {
        HttpRequestBase requestBase = null;

        if (requestMethod.equals(HTTP_GET)) {
            URIBuilder uriBuilder = new URIBuilder(url);
            // 添加请求参数信息
            if (null != params) {
                uriBuilder.setParameters(covertParams2NVPS(params));
            }

            // 创建请求
            requestBase = new HttpGet(uriBuilder.build());
        } else if (requestMethod.equals(HTTP_POST)) {
            requestBase = new HttpPost(url);
            ((HttpPost) requestBase).setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                requestBase.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }
        //添加默认请求头
        for (Map.Entry<String, String> entry : defaultHeader.entrySet()) {
            if (null != headers && !headers.containsKey(entry.getKey())) {
                requestBase.addHeader(entry.getKey(), entry.getValue());
            }
        }

        return requestBase == null ? REQUEST_ERR : getResult(requestBase, isStream);

    }

    private static String getResult(HttpRequestBase httpRequest, boolean isStream) {
        System.out.println("["+new Date()+"]: Requesting--"+httpRequest.getURI());

        // 响应结果
        StringBuilder sb = null;
        CloseableHttpResponse response = null;

        try {
            // 获取连接客户端
            // 发起请求
            response = (CloseableHttpResponse) client.execute(httpRequest);
            int respCode = response.getStatusLine().getStatusCode();

            // 如果是重定向
            if (302 == respCode) {
                String locationUrl = response.getLastHeader("Location").getValue();
                return getResult(new HttpPost(locationUrl), isStream);
            }
            // 正确响应
            if (200 == respCode) {
                // 获得响应实体
                HttpEntity entity = response.getEntity();
                sb = new StringBuilder();

                // 如果是以流的形式获取
                if (isStream) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), ENCODING));
                    String len = "";
                    while ((len = br.readLine()) != null) {
                        sb.append(len);
                    }
                } else {
                    sb.append(EntityUtils.toString(entity, ENCODING));
                    if (sb.length() < 1) {
                        sb.append("-1");
                    }
                }

            }
        } catch (ConnectionPoolTimeoutException e) {
            System.err.println("从连接池获取连接超时!!!");
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            System.err.println("响应超时");
            e.printStackTrace();
        } catch (ConnectTimeoutException e) {
            System.err.println("请求超时");
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            System.err.println("http协议错误");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            System.err.println("不支持的字符编码");
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            System.err.println("不支持的请求操作");
            e.printStackTrace();
        } catch (ParseException e) {
            System.err.println("解析错误");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO错误");
            e.printStackTrace();
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    System.err.println("关闭响应连接出错");
                    e.printStackTrace();
                }
            }

        }
        return sb == null ? REQUEST_ERR : ("".equals(sb.toString().trim()) ? "-1" : sb.toString());
    }

    /**
     * Map转换成NameValuePair List集合
     *
     * @param params map
     * @return NameValuePair List集合
     */
    private static List<NameValuePair> covertParams2NVPS(Map<String, Object> params) {

        List<NameValuePair> paramList = new LinkedList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
        }
        return paramList;

    }
}
