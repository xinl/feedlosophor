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
import java.util.concurrent.Future;
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
	public String topic;
	public List<String> titleList;
	public List<String> idList;
	public List<String> contentList;
	private static String feeds;
	private static HashMap<String, ArrayList<ArrayList<String>>> feedList;
	
	
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
	 public static HashMap<String, String> getFeedTitles(String token) {
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
		HashMap<String, String> titles = getFeedTitles(token);
		
		JSONWriter jsonWriter;
		feedList = new HashMap<String, ArrayList<ArrayList<String>>>();
		String url = "https://www.google.com/reader/api/0/stream/contents/?access_token=" + token + 
				"&xt=user/-/state/com.google/read";
		if (maxCount > 0) {
			url += "&n=" + maxCount;
		}
		String response = OauthVeriServlet.HttpConnect("GET", url, null); 
		String accountId;
		String streamId;
		String feedTitle;
		String feedId;
		String feedLabel;
		String feedContent;
		JSONArray ja2 = null;
		JSONArray ja = null;
		JSONObject jo = null;
		
		try {
			jsonWriter = new JSONStringer().object().key("entryBank").object();
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}
		try {
			jo = new JSONObject(response);
			accountId = jo.getString("id");
			ArrayList<ArrayList<String>> arrList = new ArrayList<ArrayList<String>>();
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
				if (jo.has("summary")) {
					feedContent = jo.getJSONObject("summary").getString("content");
				}
				else {
					feedContent = jo.getJSONObject("content").getString("content");
				}
				
				PutInfo(accountId, feedId, feedTitle, feedContent);
				PutInfo(streamId, feedId, feedTitle, feedContent);
				
				feedLabel = null;
				for (int j = 0; j < ja2.length(); j++) {
					feedLabel = ja2.getString(i);
					if (!feedLabel.contains("label")) {
						feedLabel = null;
					}
					else {
						PutInfo(feedLabel, feedId, feedTitle, feedContent);
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
		
		ArrayList<FeedReader> requests = new ArrayList<FeedReader>();
		ArrayList<JSONArray> hierachies = new ArrayList<JSONArray>();
		for (Map.Entry<String, ArrayList<ArrayList<String>>> entry : feedList.entrySet()) {
			FeedReader reader = new FeedReader();
			reader.topic = entry.getKey();
			reader.idList = entry.getValue().get(0);
			reader.titleList = entry.getValue().get(1);
			reader.contentList = entry.getValue().get(2);
			requests.add(reader);
		}
		try {
                FeedHierachyFactory fhf = new FeedHierachyFactory();
                ArrayList<Future<JSONArray>> futures = new ArrayList<Future<JSONArray>>();

                for (FeedReader fr : requests) {
                    String[] texts = fr.getContents().toArray(new String[fr.getContents().size()]);
                    String[] titless = fr.getTitles().toArray(new String[fr.getTitles().size()]);
                    String[] ids = fr.getIds().toArray(new String[fr.getIds().size()]);
                    futures.add(fhf.submitHierarchyRequest(texts, titless, ids, "COMPLETE", 1, 6, 6));
                }
                for (Future<JSONArray> ft : futures) {
                    try {
                        hierachies.add(ft.get());
                    } catch (Exception e) {
                        e.printStackTrace();
                        fhf.restart();
                    }
                }
                fhf.shutDown();
                System.out.println(hierachies.get(0).length() + " clusters:");
                  for (int i = 0; i < hierachies.get(0).length(); ++i)
                      System.out.println(hierachies.get(0).get(i));
                  
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		
		url = "https://www.google.com/reader/api/0/unread-count?output=json&access_token=" + token;
		response = OauthVeriServlet.HttpConnect("GET", url, null); 
		try {
			jo = new JSONObject(response);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		try {
			ja = jo.getJSONArray("unreadcounts");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		String id;
		try {
			jsonWriter = jsonWriter.key("feedList").object();
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
				for (int k = 0; k < requests.size(); k++) {
					if (requests.get(k).topic.equals(titles.get(id))) {
						jsonWriter = jsonWriter.key("groups").value(hierachies.get(k).toString());
					}
				}
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
		
//		resp.getWriter().println(access_token);
		return feeds;
	}
	
	public static void PutInfo(String id, String feedId, String feedTitle, String feedContent) {
		ArrayList<ArrayList<String>> allList = null;
		allList = feedList.get(id);
		if (allList == null) {
			allList = new ArrayList<ArrayList<String>>(); 
			feedList.put(id, allList);
		}
		if (allList.size() == 0) {
			ArrayList<String> lids = new ArrayList<String>();
			ArrayList<String> ltitles = new ArrayList<String>();
			ArrayList<String> lcontents = new ArrayList<String>();
			allList.add(lids);
			allList.add(ltitles);
			allList.add(lcontents);
		}
		allList.get(0).add(feedId);
		allList.get(1).add(feedTitle);
		allList.get(2).add(feedContent);
	}
}
