package feedlosophor.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class OauthVeriServlet extends HttpServlet {
	static String access_token = null;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		String content = "code=" + req.getParameter("code") + "&client_id=" + 
				LoginServlet.clientID + "&client_secret=" + LoginServlet.clientSecret + "&redirect_uri=" + 
				LoginServlet.redirectURL + "&grant_type=authorization_code";
		String response = HttpConnect("POST", "https://accounts.google.com/o/oauth2/token", content);

		JSONObject jo = null;
		try {
			jo = new JSONObject(response.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			access_token = (String) jo.get("access_token");
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}
		
		resp.getWriter().println(FeedReader.getFeeds(access_token, 5));
//		FeedReader reader = FeedReader.getUnreadFeeds(access_token);
//		resp.getWriter().println(access_token);
	}

	public static String HttpConnect(String method, String urlString, String postContent) {
		URL url = null;
		StringBuffer response = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(method);
			if (method.toUpperCase().equals("POST")) {
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
				wr.writeBytes (postContent);
				wr.flush ();
				wr.close ();
			}
			connection.connect();
			
			//Get Response	
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			response = new StringBuffer(); 
			while((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return response.toString();
	}
}
