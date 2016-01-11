package com.clicksend.sdk.http;

import java.util.Map;
import java.util.HashMap;

import org.apache.http.client.HttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;

import com.clicksend.sdk.util.Definitions;

/**
 * HttpClientUtils.java<br><br>
 *
 * Created on 24 August 2014, 00:23
 *
 * @author  Hüseyin ZAHMACIOĞLU {@link http://www.bumin.com.tr}
 * @version 1.0
 * 
 */
public class HttpClientUtils {

    private final static Map<String, HttpClientUtils> instances = new HashMap<String, HttpClientUtils>();

    private final ThreadSafeClientConnManager threadSafeClientConnectionManager;
    
    private final int connectionTimeout;
    private final int socketTimeout;
    
    private HttpClientUtils(int connectionTimeout, int socketTimeout) {        
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
        
        this.threadSafeClientConnectionManager = new ThreadSafeClientConnManager();
        this.threadSafeClientConnectionManager.setDefaultMaxPerRoute(200);
        this.threadSafeClientConnectionManager.setMaxTotal(200);
    }

    /**
     * Return an existing or instantiate a new HttpClient factory instance with explicitly specified connection and read timeout values
     *
     * @param connectionTimeout the timeout value in milliseconds to use when establishing a new http socket
     * @param socketTimeout the timeout value in milliseconds to wait for a http response before closing the socket
     *
     * @return HttpClientUtils an instance of the HttpClient factory primed with the requested timeout values
     */
    public static HttpClientUtils getInstance(int connectionTimeout, int socketTimeout) {
        String key = "ct-" + connectionTimeout + "-st-" + socketTimeout;
        HttpClientUtils instance = instances.get(key);
        if (instance == null) {
            instance = new HttpClientUtils(connectionTimeout, socketTimeout);
            instances.put(key, instance);
        }
        return instance;
    }

    /**
     * Instantiate a new HttpClient instance that uses the timeout values associated with this factory instance
     *
     * @return HttpClient a new HttpClient instance
     */
    public HttpClient getNewHttpClient() {
        HttpParams httpClientParams = new BasicHttpParams();
        HttpProtocolParams.setUserAgent(httpClientParams, Definitions.SDK_USER_AGENT_STRING + " " + Definitions.SDK_USER_AGENT_VERSION);
        HttpConnectionParams.setConnectionTimeout(httpClientParams, this.connectionTimeout);
        HttpConnectionParams.setSoTimeout(httpClientParams, this.socketTimeout);
        HttpConnectionParams.setStaleCheckingEnabled(httpClientParams, true);
        HttpConnectionParams.setTcpNoDelay(httpClientParams, true);
        return new DefaultHttpClient(this.threadSafeClientConnectionManager, httpClientParams);
    }

	@Override
	public String toString() {
		return "HttpClientUtils [threadSafeClientConnectionManager="
				+ threadSafeClientConnectionManager + ", connectionTimeout="
				+ connectionTimeout + ", socketTimeout=" + socketTimeout + "]";
	}
}
