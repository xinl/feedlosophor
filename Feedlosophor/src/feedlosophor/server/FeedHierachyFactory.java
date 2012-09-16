package feedlosophor.server;

import java.io.ByteArrayInputStream;

import org.json.JSONArray;
import org.json.JSONException;

import feedlosophor.clusterer.HClusterer;
import feedlosophor.scoring.TFScore;;

public class FeedHierachyFactory {

    private HClusterer hc;
    private TFScore tf;

    public FeedHierachyFactory(String linkageMethod, int nClusters, int clusterNumLeavesThreshold, double clusterDistThreshold) {
        hc = new HClusterer(linkageMethod, nClusters, clusterNumLeavesThreshold, clusterDistThreshold);
    }

    public JSONArray getJsonHierachicalClusters(String[] texts, String[] ids) {      
        tf = new TFScore(texts, ids);
        //System.out.println("vector size = " + tfc.tfWords.size());
        try {
            String jsonHierachy = hc.getJsonHierachy(new ByteArrayInputStream(tf.getResult().getBytes("UTF-8")));
            //System.out.println(jsonHierachy);
            String result = hc.getClusters(jsonHierachy);
            //System.out.println(result);
            JSONArray jsonResult = new JSONArray(result);
            //System.out.println(jsonResult.length() + " clusters:");
            //            for (int i = 0; i < jsonResult.length(); ++i)
            //                System.out.println(jsonResult.get(i));
            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                return new JSONArray("{\"exception\":\"" + e + "\"}");
            } catch (JSONException e1) {
                e1.printStackTrace();
                return null;
            }
        }
    }

}
