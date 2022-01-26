package cn.frame.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * @Title: HttpUtil.java
 * @Package com.game.util
 * @Description: TODO(用一句话描述该文件做什么)
 * @author Charles
 * @date 2016年3月5日 下午5:57:20
 * @version V1.0
 */
public class HttpUtil {
	/**
	 *web请求
	 */
	public static String PostHttpWebURL_JSON(String URL,String jsonParams){
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(URL);
		RequestConfig requestConfig = RequestConfig.custom().
				setConnectTimeout(180 * 1000).setConnectionRequestTimeout(180 * 1000)
				.setSocketTimeout(180 * 1000).setRedirectsEnabled(true).build();

		httpPost.setConfig(requestConfig);
		httpPost.setHeader("Content-Type","application/json");  //
		try {
			httpPost.setEntity(new StringEntity(jsonParams,ContentType.create("application/json", "utf-8")));
			System.out.println("request parameters" + EntityUtils.toString(httpPost.getEntity()));
			HttpResponse response = httpClient.execute(httpPost);
			System.out.println("doPostForInfobipUnsub response"+response.getStatusLine().toString());
			return String.valueOf(response.getStatusLine().getStatusCode());
		} catch (Exception e) {
			e.printStackTrace();
			return "post failure :caused by-->" + e.getMessage().toString();
		}finally {
			if(null != httpClient){
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 *web请求
	 */
	public static String PostHttpWebURL(String URL, Map<String, Object> paramsMap){
		String code = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(URL);
		RequestConfig requestConfig = RequestConfig.custom().
				setConnectTimeout(180 * 1000).setConnectionRequestTimeout(180 * 1000)
				.setSocketTimeout(180 * 1000).setRedirectsEnabled(true).build();
		httpPost.setConfig(requestConfig);

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if(paramsMap != null){
			for (String key : paramsMap.keySet()) {
				nvps.add(new BasicNameValuePair(key, String.valueOf(paramsMap.get(key))));
			}
		}
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			HttpResponse response = httpClient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				code = EntityUtils.toString(response.getEntity());
				return code;
			} else {
				return "Error Response: " + response.getStatusLine().toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "post failure :caused by-->" + e.getMessage().toString();
		}finally {
			if(null != httpClient){
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 *web Get请求
	 */
	public static String GetHttpWebURL(String URL){
		String code = null;
		CloseableHttpClient httpCilent = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.DEFAULT;
		HttpGet httpGet = new HttpGet(URL);
		httpGet.setConfig(requestConfig);
		try {
			HttpResponse httpResponse = httpCilent.execute(httpGet);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				code = EntityUtils.toString(httpResponse.getEntity());//获得返回的结果
			}else if(httpResponse.getStatusLine().getStatusCode() == 400){
				//..........
			}else if(httpResponse.getStatusLine().getStatusCode() == 500){
				//.............
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				httpCilent.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return code;
	}
	/**
	 *web Get请求
	 */
	public static byte[] GetHttpWebURL_Byte(String URL){
		byte[] code = null;
		CloseableHttpClient httpCilent = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.DEFAULT;
		HttpGet httpGet = new HttpGet(URL);
		httpGet.setConfig(requestConfig);
		try {
			HttpResponse httpResponse = httpCilent.execute(httpGet);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				code = EntityUtils.toByteArray(httpResponse.getEntity());
			}else if(httpResponse.getStatusLine().getStatusCode() == 400){
				//..........
			}else if(httpResponse.getStatusLine().getStatusCode() == 500){
				//.............
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				httpCilent.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return code;
	}

	/**
	 * url进行转码
	 * @param text
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String urlEncoderText(String text) throws UnsupportedEncodingException {
		return java.net.URLEncoder.encode(text, "utf-8");
	}
	
	/**
	 * url进行解码
	 * @param text
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String urlDecoderText(String text) throws UnsupportedEncodingException {
		return java.net.URLDecoder.decode(text, "utf-8");
	}

}
