package feedlosophor.server;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONException;

import feedlosophor.clusterer.HClusterer;
import feedlosophor.scoring.TFScore;;

/**
 * A feed hierarchical clusters factory
 * @author Ruogu Hu
 *
 */
public class FeedHierachyFactory {

    private ExecutorService es = Executors.newCachedThreadPool();

    /**
     * Constructor (similar for HClusterer)
     * [SINGLE|COMPLETE|AVERAGE|MEAN|CENTROID|WARD|ADJCOMLPETE|NEIGHBOR_JOINING]
     * @param linkageMethod: see weka's <code>HierarchicalClusterer.TAGS_LINK_TYPE</code>
     * @param nClusters: number of clusters at the top level of hierarchy
     * @param clusterNumLeavesThreshold: maximum number of leaves per flattened cluster
     * @param clusterDistThreshold: minimum distance between two clusters considered different.
     */
    public FeedHierachyFactory() {
    }

    /**
     * 
     */
    public Future<JSONArray> submitHierarchyRequest(String[] texts, String[] titles, String[] ids,
            String linkageMethod, int nClusters, int clusterNumLeavesThreshold, double clusterDistThreshold) {
        return es.submit(new HierarchyTask(texts, titles, ids, linkageMethod, 
                nClusters, clusterNumLeavesThreshold, clusterDistThreshold));
    }

    public void restart() {
        es.shutdown();
        es = Executors.newCachedThreadPool();
    }

    public static void main(String[] args) {
        FeedHierachyFactory fhf = new FeedHierachyFactory();
        ArrayList<FeedReader> requests = new ArrayList<FeedReader>();
        ArrayList<Future<JSONArray>> futures = new ArrayList<Future<JSONArray>>();
        ArrayList<JSONArray> hierachies = new ArrayList<JSONArray>();

        for (FeedReader fr : requests)
            futures.add(fhf.submitHierarchyRequest(null, null, null, "AVERAGE", 1, 5, 6));
        for (Future<JSONArray> ft : futures) {
            try {
                hierachies.add(ft.get());
            } catch (Exception e) {
                e.printStackTrace();
                fhf.restart();
            }
        }
    }



}

class HierarchyTask implements Callable<JSONArray> {
    private String[] texts;
    private String[] titles;
    private String[] ids;
    private HClusterer hc;
    private TFScore tf;

    HierarchyTask(String[] texts, String[] titles, String[] ids, 
            String linkageMethod, int nClusters, int clusterNumLeavesThreshold, double clusterDistThreshold) {
        this.texts = texts;
        this.titles = titles;
        this.ids = ids;
        this.hc = new HClusterer(linkageMethod, nClusters, clusterNumLeavesThreshold, clusterDistThreshold);

    }

    @Override
    public JSONArray call() throws Exception {
        return getJsonHierachicalClusters(texts, titles, ids);
    }

    /**
     * Given three equal length array of strings corresponding to feed ids, titles, and texts,
     * give back the hierarchy of feeds labeled by ids in JSON format.
     * @param texts
     * @param ids
     * @return hierarchy
     */
    private JSONArray getJsonHierachicalClusters(String[] texts, String[] titles, String[] ids) {      
        tf = new TFScore(texts, titles, ids);
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