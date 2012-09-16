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

		Pattern p = Pattern.compile("\"access_token\".*?\"(.*?)\",");
		Matcher m = p.matcher(response.toString());
		if (m.find()) {
			access_token = m.group(1);
			resp.getWriter().println(access_token);
			FeedReader reader = new FeedReader(access_token);
			String unreadFeeds = reader.getUnreadFeeds();
		}
	}

	public String HttpConnect(String method, String urlString, String postContent) {
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
			connection.setDoOutput(true);
			connection.setRequestMethod(method);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
			wr.writeBytes (postContent);
			wr.flush ();
			wr.close ();

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
