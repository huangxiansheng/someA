package com.hs.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *@描述 
 *@日期 2018年4月23日下午4:01:21
 *@作者 hsq54321@126.com
 */
public class IpUtil {
	
	/**
	 * 获取本地ip
	 * */
	public static String getV4IP(){
		String ip = "";
		String chinaz = "http://ip.chinaz.com/getip.aspx";//http://ip.chinaz.com
		
		StringBuilder inputLine = IpUtil.httpURLString(chinaz);
		
		Pattern p = Pattern.compile("[0-9]{0,3}\\.[0-9]{0,3}\\.[0-9]{0,3}\\.[0-9]{0,3}");
		Matcher m = p.matcher(inputLine.toString());
		if(m.find()){
			String ipstr = m.group(0);
			ip = ipstr;
		}
		return ip;
	}
	
	public static StringBuilder httpURLString(String urlStr) {
		StringBuilder inputLine = new StringBuilder();
		String read = "";
		URL url = null;
		HttpURLConnection urlConnection = null;
		BufferedReader in = null;
		try {
			url = new URL(urlStr);
			urlConnection = (HttpURLConnection) url.openConnection();
		    in = new BufferedReader( new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
			while((read=in.readLine())!=null){
				inputLine.append(read+"\r\n");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return inputLine;
	}
	
}