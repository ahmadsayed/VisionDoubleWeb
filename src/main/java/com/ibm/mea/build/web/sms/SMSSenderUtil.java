package com.ibm.mea.build.web.sms;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class SMSSenderUtil {


	  public static void sendSMS(String number, String text) throws MalformedURLException, IOException {
		  	HttpURLConnection url = (HttpURLConnection) new URL("https://iot-sms-wrapper.mybluemix.net/send_sms?number="+number+"&text="+ URLEncoder.encode(text)).openConnection();
		  	url.getResponseCode();	
	  }
	  
	  public static void main(String []args) throws MalformedURLException, IOException {
		  SMSSenderUtil.sendSMS("201118000973","Hello World");
	  }
}
