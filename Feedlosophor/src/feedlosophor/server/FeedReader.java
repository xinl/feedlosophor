package feedlosophor.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


/**
 * Handles GR api calls
 * @author joe
 *
 */
public class FeedReader {
	private String token;
	
	public FeedReader(String token) {
		this.token = token;
	}
	
	public String getUnreadFeeds() {
		return null;
		
	}
}
