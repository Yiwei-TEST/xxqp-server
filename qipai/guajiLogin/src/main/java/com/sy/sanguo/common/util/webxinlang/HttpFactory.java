package com.sy.sanguo.common.util.webxinlang;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;



@SuppressWarnings("deprecation")
public class HttpFactory {

	private static final String ENCODING_GZIP = "gzip";
	/** 连接池默认最大数 */
	private static final int maxConnections = 15;
	/** 连接超时，包含readTimeout和socketTimeout */
	private static final int timeout = 30 * 1000;

	/**

	 * 代理对象

	 */
	public static class Proxy {

		String proxy;
		int port;
	}
	
	/**

	 * 创建联网核心对象

	 * 

	 * @param header

	 *            请求头

	 * @param retryCount

	 *            重试次数

	 * @param timeout

	 *            超时时间

	 * @param proxy

	 *            代理对象

	 * @return 配置过后的httpClient 核心对象

	 */
    public static final DefaultHttpClient createHttpClient(
			Map<String, String> header, int retryCount, int timeout, Proxy proxy) {
		HttpParams params = new BasicHttpParams();
		if (proxy != null) {
			HttpHost host = new HttpHost(proxy.proxy, proxy.port);
			params.setParameter(ConnRouteParams.DEFAULT_PROXY, host);
		}

		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		HttpConnectionParams.setStaleCheckingEnabled(params, false);

		// Default connection and socket timeout of 30 seconds. Tweak to taste.


		HttpConnectionParams.setConnectionTimeout(params, timeout);
		HttpConnectionParams.setSoTimeout(params, timeout);
		HttpConnectionParams.setTcpNoDelay(params, true);
		HttpConnectionParams.setSocketBufferSize(params, 8192);

		HttpClientParams.setRedirecting(params, true);

		ConnManagerParams.setTimeout(params, timeout);
		ConnManagerParams.setMaxConnectionsPerRoute(params,
				new ConnPerRouteBean(maxConnections));
		ConnManagerParams.setMaxTotalConnections(params, 20);

		// Sets up the http part of the service.


		final SchemeRegistry supportedSchemes = new SchemeRegistry();

		// Register the "http" protocol scheme, it is required


		// by the default operator to look up socket factories.


		final SocketFactory sf = PlainSocketFactory.getSocketFactory();
		
		SSLSocketFactory  sslSocketFactory = null;
		//新增SSLSocketFactory.getSocketFactory()


		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);  
			sslSocketFactory = new MySSLSocketFactory(trustStore);  
		} catch (Exception e) {
			sslSocketFactory = SSLSocketFactory.getSocketFactory();
		}  
		supportedSchemes.register(new Scheme("http", sf, 80));
		supportedSchemes.register(new Scheme("https", sslSocketFactory, 443));
		final ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager(
				params, supportedSchemes);
		DefaultHttpClient httpClient = new DefaultHttpClient(ccm, params);
		/**

		 * no Cookie 

		 */
		 CookieSpecFactory csf = new CookieSpecFactory() {
		     public CookieSpec newInstance(HttpParams params) {
		         return new BrowserCompatSpec() {   
		             @Override
		             public void validate(Cookie cookie, CookieOrigin origin)
		             throws MalformedCookieException {
		                 // Oh, I am easy


		             }
		         };
		     }
		 };
		httpClient.getCookieSpecs().register("easy", csf);
		  httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, "easy");
		//新增authScope


		

		setAuthScope(httpClient);
		
		return httpClient;
	}
    private static void setAuthScope(DefaultHttpClient httpClient) {
//		BasicScheme basicScheme = new BasicScheme();


//		AuthScope mAuthScope = new AuthScope(SERVER_HOST, AuthScope.ANY_PORT);


		httpClient.setCredentialsProvider(new BasicCredentialsProvider());
//		BasicHttpContext localcontext = new BasicHttpContext();


//		localcontext.setAttribute("preemptive-auth", basicScheme);


	}

	/**

	 * 只包含包含gzip拦截器请求头

	 * 

	 * @param timeout

	 * @param retryCount

	 * @param proxy

	 * @return

	 */
	public static final DefaultHttpClient createHttpClient(int timeout,int retryCount, Proxy proxy) {
		return createHttpClient(null, retryCount, timeout, proxy);
	}

	/**

	 * 支持代理,默认超时时间为timeout

	 * 

	 * @param retryCount

	 * @param proxy

	 * @return

	 */
	public static final DefaultHttpClient createHttpClient(int retryCount,Proxy proxy) {
		return createHttpClient(null, retryCount, timeout, proxy);
	}

	/**

	 * 不需要代理，超时时间为timeout

	 * 

	 * @param retryCount

	 * @return

	 */
	public static final DefaultHttpClient createHttpClient(int retryCount) {
		return createHttpClient(null, retryCount, timeout, null);
	}

	/**

	 * 不需要代理，超时时间为timeout，无特殊head头,重试三次

	 * 

	 * @return

	 */
	public static final DefaultHttpClient createHttpClient() {
		return createHttpClient(3);
	}

	
	
	public static class MySSLSocketFactory extends SSLSocketFactory {  
        SSLContext sslContext = SSLContext.getInstance("TLS");  
  
        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException,  
                KeyManagementException, KeyStoreException, UnrecoverableKeyException {  
            super(truststore);  
  
            TrustManager tm = new X509TrustManager() {  
                public void checkClientTrusted(X509Certificate[] chain, String authType)  
                        throws CertificateException {  
                }  
  
                public void checkServerTrusted(X509Certificate[] chain, String authType)  
                        throws CertificateException {  
                }  
  
                public X509Certificate[] getAcceptedIssuers() {  
                    return null;  
                }  
            };  
  
            sslContext.init(null, new TrustManager[] { tm }, null);  
        }  
  
        @Override  
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)  
                throws IOException, UnknownHostException {  
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);  
        }  
  
        @Override  
        public Socket createSocket() throws IOException {  
            return sslContext.getSocketFactory().createSocket();  
        }  
    }  
	
	/**

	 * HttpRequestInterceptor for DefaultHttpClient

	 */
	public static HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
		@Override
		public void process(final HttpRequest request, final HttpContext context) {
			AuthState authState = (AuthState) context
					.getAttribute(ClientContext.TARGET_AUTH_STATE);
			CredentialsProvider credsProvider = (CredentialsProvider) context
					.getAttribute(ClientContext.CREDS_PROVIDER);
			HttpHost targetHost = (HttpHost) context
					.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

			if (authState.getAuthScheme() == null) {
				AuthScope authScope = new AuthScope(targetHost.getHostName(),
						targetHost.getPort());
				Credentials creds = credsProvider.getCredentials(authScope);
				if (creds != null) {
					authState.setAuthScheme(new BasicScheme());
					authState.setCredentials(creds);
				}
			}
			request.setHeader("Accept-Encoding", ENCODING_GZIP);
		}
	};
	
}