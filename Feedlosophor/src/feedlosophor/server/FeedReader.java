package feedlosophor.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;


/**
 * Handles GR api calls
 * @author joe
 *
 */
public class FeedReader {
	private String token;
	private List<String> titleList;
	private List<String> idList;
	private List<String> contentList;
	private String jsonUnreadCount;
	private String jsonUnreadLabelCount;
	private String jsonUnreadStreamCount;
	private static String feeds;
	private static HashMap<String, ArrayList<String[]>> feedList;
	
	/*
	public FeedReader(String token) {
		this.token = token;
		this.titles = new ArrayList<String>();
		this.ids = new ArrayList<String>();
		this.contents = new ArrayList<String>();
	}
	*/
	
	
	public List<String> getIds() {
		return idList;
	}
	
	public List<String> getTitles() {
		return titleList;
	}
	
	public List<String> getContents() {
		return contentList;
	}
	
	
	public static FeedReader getUnreadFeedsByLabel(String token, String label) {
		return null;
	}
	
	public static FeedReader getUnreadFeedsByStream(String token, String stream) {
		return null;
	}
	
	
	public static HashMap<String, String> getTitles(String token) {
		String url = "https://www.google.com/reader/api/0/subscription/list?output=json&access_token=" + token;
		String response = OauthVeriServlet.HttpConnect("GET", url, null); 
		if (response == null) {
			return null;
		}
		HashMap<String, String> titles = new HashMap<String, String>();
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
		String title = null;
		String label = null;
		JSONArray labels = null;
		
		try {
			for (int i = 0; i < ja.length(); i++) {
				jo = ja.getJSONObject(i);
				titles.put(jo.getString("id"), jo.getString("title"));
				labels = jo.getJSONArray("categories");
				for (int j = 0; j < labels.length(); j++) {
					jo = labels.getJSONObject(j);
					titles.put(jo.getString("id"), jo.getString("label"));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return titles;
	}
	
	public static String getFeeds(String token, int maxCount) {
		HashMap<String, String> titles = getTitles(token);
		
		String url = "https://www.google.com/reader/api/0/unread-count?output=json&access_token=" + token;
		String response = OauthVeriServlet.HttpConnect("GET", url, null); 
		JSONObject jo;
		try {
			jo = new JSONObject(response);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		JSONArray ja;
		try {
			ja = jo.getJSONArray("unreadcounts");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		String id;
		JSONWriter jsonWriter;
		try {
			jsonWriter = new JSONStringer().object().key("feedList").object();
		} catch (JSONException e2) {
			e2.printStackTrace();
			return null;
		}
		try {
			for (int i = 0; i < ja.length(); i++) {
				jo = ja.getJSONObject(i);
				id = jo.getString("id");
				jsonWriter = jsonWriter.key(id).object();
				jsonWriter = jsonWriter.key("title").value(titles.get(id));
				jsonWriter = jsonWriter.key("unread").value(jo.getString("count"));
//				jsonWriter = jsonWriter.key("groups").value();
				jsonWriter = jsonWriter.endObject();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		try {
			jsonWriter = jsonWriter.endObject();
		} catch (JSONException e2) {
			e2.printStackTrace();
			return null;
		}
		
		feedList = new HashMap<String, ArrayList<String[]>>();
		url = "https://www.google.com/reader/api/0/stream/contents/?access_token=" + token + 
				"&xt=user/-/state/com.google/read";
		if (maxCount > 0) {
			url += "&n=" + maxCount;
		}
		response = OauthVeriServlet.HttpConnect("GET", url, null); 
		String accountId;
		String streamId;
		String feedTitle;
		String feedId;
		JSONArray ja2 = null;
		
		try {
			jsonWriter = jsonWriter.key("entryBank").object();
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}
		try {
			jo = new JSONObject(response);
			accountId = jo.getString("id");
			ArrayList<String[]> arrList = new ArrayList<String[]>();
			feedList.put(accountId, arrList);
			ja = jo.getJSONArray("items");
			for (int i = 0; i < ja.length(); i++) {
				jsonWriter = jsonWriter.key(ja.getJSONObject(i).getString("id"));
				jsonWriter = jsonWriter.value(ja.getString(i));
				
				jo = ja.getJSONObject(i);
				feedId = jo.getString("id");
				streamId = jo.getJSONObject("origin").getString("streamId");
				feedTitle = jo.getJSONObject("origin").getString("title");
				ja2 = jo.getJSONArray("categories");
				for (int j = 0; j < ja2.length(); j++) {
					if (ja2.getString(i).contains("label")) {
					}
				}
			}
			jsonWriter = jsonWriter.endObject();
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		try {
			feeds = jsonWriter.endObject().toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return feeds;
	}
	
}
