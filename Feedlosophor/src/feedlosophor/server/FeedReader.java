package feedlosophor.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Handles GR api calls
 * @author joe
 *
 */
public class FeedReader {
	private String token;
	private List<String> titles;
	private List<String> ids;
	private List<String> contents;
	private String jsonUnreadCount;
	private String jsonUnreadLabelCount;
	private String jsonUnreadStreamCount;
	
	public FeedReader(String token) {
		this.token = token;
		this.titles = new ArrayList<String>();
		this.ids = new ArrayList<String>();
		this.contents = new ArrayList<String>();
	}
	
	public List<String> getIds() {
		return ids;
	}
	
	public List<String> getTitles() {
		return titles;
	}
	
	public List<String> getContents() {
		return contents;
	}
	
	public static FeedReader getUnreadFeedsByLabel(String token, String label) {
		return null;
	}
	
	public static FeedReader getUnreadFeedsByStream(String token, String stream) {
		return null;
	}
	
	
	public static HashMap<String, ArrayList<String>> getLabelMapping(String token) {
		String url = "https://www.google.com/reader/api/0/subscription/list?output=json&access_token=" + token;
		String response = OauthVeriServlet.HttpConnect("GET", url, null); 
		if (response == null) {
			return null;
		}
		HashMap<String, ArrayList<String>> labelMapping = new HashMap<String, ArrayList<String>>();
		JSONObject jo = null;
		JSONArray ja = null;
		try {
			jo = new JSONObject(response);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		try {
			//get subscriptions array
			ja = jo.getJSONArray("subscriptions");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		//get label-to-stream mapping
		String streamId = null;
		String label = null;
		JSONArray labels = null;
		
		try {
			for (int i = 0; i < ja.length(); i++) {
				jo = ja.getJSONObject(i);
				streamId = jo.getString("id");
				labels = jo.getJSONArray("categories");
				for (int j = 0; j < labels.length(); j++) {
					label = labels.getJSONObject(j).getString("label");
					
					//Create new entry if label has not shown up before,
					//otherwise just add stream id to existing array list
					if (!labelMapping.containsKey(label)) {
						ArrayList<String> streamIds = new ArrayList<String>();
						streamIds.add(streamId);
						labelMapping.put(label, streamIds);
					}
					else {
						labelMapping.get(label).add(streamId);
					}
				}
				//Mark stream id as label in the hash map
				if (labels.length() == 0) {
					labelMapping.put(streamId, null);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return labelMapping;
	}
	
	public static FeedReader getUnreadFeeds(String token, int maxCount) {
		HashMap<String, ArrayList<String>> labelMapping = getLabelMapping(token);
		
		String url = "https://www.google.com/reader/api/0/stream/contents/?access_token=" + token + 
				"&xt=user/-/state/com.google/read";
		if (maxCount > 0) {
			url += "&n=" + maxCount;
		}
		String response = OauthVeriServlet.HttpConnect("GET", url, null); 
		return null;
	}
}
