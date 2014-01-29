package com.jacktech.gymik;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


class MySSLSocketFactory extends SSLSocketFactory {
    SSLContext sslContext = SSLContext.getInstance("TLS");

    public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(truststore);

        X509TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sslContext.init(null, new TrustManager[] { tm }, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }
}

public class PageLoader{
	
	
	private String sessionId = "";
    private CookieStore cookieStore = new BasicCookieStore();
    private HttpContext localContext = new BasicHttpContext();

    public PageLoader(){
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }
	
	public HttpClient getNewHttpClient() {
	    try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	        return new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	        return new DefaultHttpClient();
	    }
	}
	
	/**
	   * Checks if the device has Internet connection.
	   * 
	   * @return <code>true</code> if the phone is connected to the Internet.
	   */
	  public static boolean isOnline(Context c) {
	    ConnectivityManager cm = (ConnectivityManager) c.getSystemService(
	        Context.CONNECTIVITY_SERVICE);

	    NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    if (wifiNetwork != null && wifiNetwork.isConnected()) {
	      return true;
	    }

	    NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    if (mobileNetwork != null && mobileNetwork.isConnected()) {
	      return true;
	    }

	    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
	    if (activeNetwork != null && activeNetwork.isConnected()) {
	      return true;
	    }

	    return false;
	  }
	
	
	private String getSessionId(HttpResponse response){
		Header[] headers = response.getAllHeaders();
        for (int i=0; i < headers.length; i++) {
        	Header h = headers[i];
	        Pattern p = Pattern.compile(".*ASP.NET_SessionId=([^;]+);.*", Pattern.DOTALL);
	        Matcher matcher = p.matcher(h.getValue());
	        if(matcher.matches())
	        	return matcher.group(1);
	    }
        return sessionId;
        
	}
	
	public static String getViewState(String xml){
		String state = "";
		Pattern p = Pattern.compile(".*<input type=\"hidden\" name=\"__VIEWSTATE\" id=\"__VIEWSTATE\" value=\"([^\"]*)\" />.*", Pattern.DOTALL);
        Matcher matcher = p.matcher(xml);
        if(matcher.matches())
      	  state = matcher.group(1);
        
        Log.i("PageLoader.getViewState", "Viewstate: "+state);
        return state;
		
	}
	
	public static String getEventValidation(String xml){
		String state = "";
		Pattern p = Pattern.compile(".*<input type=\"hidden\" name=\"__EVENTVALIDATION\" id=\"__EVENTVALIDATION\" value=\"([^\"]*)\" />.*", Pattern.DOTALL);
        Matcher matcher = p.matcher(xml);
        if(matcher.matches())
      	  state = matcher.group(1);
        
        Log.i("PageLoader.getEventValidation", "EventValidation: "+state);
        return state;
		
	}
	
	public static String getPristiTyden(String xml){
		String state = "";
		Pattern p = Pattern.compile(".*<option value=\"([^\"]*)\">p[^<]*den</option>.*", Pattern.DOTALL);
        Matcher matcher = p.matcher(xml);
        if(matcher.matches())
      	  state = matcher.group(1);
        
        Log.i("PageLoader.getPristiTyden", "PristiTyden: "+state);
        return state;
		
	}
	
	public Document getXml(String url){
		return getXml(url, new ArrayList<NameValuePair>());
	}
	
	public Document getXml(String url, ArrayList<NameValuePair> vars){
		
		 Document ret = null;
		 long dTime = System.currentTimeMillis();   
		 String data = getPage(url, vars);
		 if(data.equals("")) return null;
		 
		 long tTime = System.currentTimeMillis();
		 try{
			 ret = Jsoup.parse(data);
		 } catch( Exception e){
			 return null;
		 }
		 
		 long aTime = System.currentTimeMillis();
		 Log.i("PageLoader.time", "Download ("+url+"): "+(tTime-dTime));
		 Log.i("PageLoader.time", "Parse ("+url+"): "+(aTime-tTime));
		 Log.i("PageLoader.time", "Total ("+url+"): "+(aTime-dTime));
		 
		 return ret;
	}
	
	public String getPage(String url){
		return getPage(url, new ArrayList<NameValuePair>());
	}
	
	public String getPage(String url, ArrayList<NameValuePair> vars) {
        String xml = "";
        
        Log.i("PageLoader.getPage", "Url: "+url);
        long dTime = System.currentTimeMillis();
        
        try {
        	
        	HttpResponse httpResponse;
        	HttpClient httpClient = getNewHttpClient();
        	
        	if(vars.size() > 0){
	            HttpPost httpPost = new HttpPost(url);
	            httpPost.setEntity(new UrlEncodedFormEntity(vars, "UTF-8"));
	            Log.i("PageLoader.getPage", "SessionID: "+sessionId);
	            httpPost.addHeader("Cookie","ASP.NET_SessionId="+sessionId);
	            httpResponse = httpClient.execute(httpPost); 
	            
        	} else{
        		HttpGet httpPost = new HttpGet(url);
	            Log.i("PageLoader.getPage", "SessionID: "+sessionId);
	            httpPost.addHeader("Cookie","ASP.NET_SessionId="+sessionId);
        		httpResponse = httpClient.execute(httpPost); 
        	}
            
            HttpEntity httpEntity = httpResponse.getEntity();
     
            xml = EntityUtils.toString(httpEntity);     
            sessionId = getSessionId(httpResponse);            
        } catch (UnsupportedEncodingException e) {
			return "";
	    } catch (ClientProtocolException e) {
	    	return "";
	    } catch (IOException e) {
			return "";
		}
        
        long aTime = System.currentTimeMillis();
        Log.i("PageLoader.time", "getPage ("+url+"): "+(aTime-dTime));
        return xml;
    }

    public String getPageNormal(String url, ArrayList<NameValuePair> vars) {
        String xml = "";

        Log.i("PageLoader.getPage", "Url: "+url);
        long dTime = System.currentTimeMillis();

        try {

            HttpResponse httpResponse;
            HttpClient httpClient = getNewHttpClient();

            if(vars != null && vars.size() > 0){
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(vars, "UTF-8"));
                //Log.i("PageLoader.getPage", "SessionID: "+sessionId);
                //httpPost.addHeader("Cookie","ASP.NET_SessionId="+sessionId);
                httpResponse = httpClient.execute(httpPost, localContext);

            } else{
                HttpGet httpPost = new HttpGet(url);
                //Log.i("PageLoader.getPage", "SessionID: "+sessionId);
                //httpPost.addHeader("Cookie","ASP.NET_SessionId="+sessionId);
                httpResponse = httpClient.execute(httpPost,localContext);
            }

            HttpEntity httpEntity = httpResponse.getEntity();
            if(httpEntity != null){
                xml = EntityUtils.toString(httpEntity);
                httpEntity.consumeContent();
            }
            //sessionId = getSessionId(httpResponse);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        long aTime = System.currentTimeMillis();
        Log.i("PageLoader.time", "getPage ("+url+"): "+(aTime-dTime));
        return xml;
    }

}
