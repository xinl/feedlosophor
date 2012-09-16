package feedlosophor.server;
import com.google.appengine.api.rdbms.AppEngineDriver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginServlet extends HttpServlet {
	static String baseURL = "https://accounts.google.com/o/oauth2/auth";
	static String clientID = "849962972797.apps.googleusercontent.com";
	static String clientSecret = "Z8a46r3wehfKQIGfYSKxEs5j";
	static String scope = "https://www.google.com/reader/api http://www.google.com/reader/atom";
	static String redirectURL = "http://127.0.0.1:8888/oauthcallback";
			
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String url = baseURL + "?redirect_uri=" + URLEncoder.encode(redirectURL, "UTF-8") + "&scope=" + 
			URLEncoder.encode(scope, "UTF-8") + "&response_type=code&client_id=" + URLEncoder.encode(clientID, "UTF-8");
		resp.sendRedirect(url);
	}

}
