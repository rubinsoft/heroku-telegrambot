package io.github.rubinsoft.pengrad.openshift;

import java.io.IOException;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;

public class MyBotUtils {

	public static String html2db(String content){
		return content	.replaceAll("\\u00E0", "&#224;")//"a'")
						.replaceAll("\\u00E8", "&#232;")//"e'")
						.replaceAll("\\u00E9", "&#233;")//"e'")
						.replaceAll("\\u00EC", "&#236;")//"i'")
						.replaceAll("\\u00F2", "&#242;")//"o'")
						.replaceAll("\\u00F9", "&#249;");//"u'");
	}
	
	public static String db2html(String content){
		return content	.replaceAll("&#224;","\\u00E0")//"a'")
						.replaceAll("&#232;", "\\u00E8")//"e'")
						.replaceAll("&#233;", "\\u00E9")//"e'")
						.replaceAll("&#236;", "\\u00EC")//"i'")
						.replaceAll("&#242;", "\\u00F2")//"o'")
						.replaceAll("&#249;", "\\u00F9");//"u'");
	}
	
	public static String extractPostRequestBody(HttpServletRequest request) throws IOException {
	    if ("POST".equalsIgnoreCase(request.getMethod())) {
	        @SuppressWarnings("resource")
			Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
	        return s.hasNext() ? s.next() : "";
	    }
	    return "";
	}
}
