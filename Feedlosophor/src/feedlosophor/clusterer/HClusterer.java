package feedlosophor.clusterer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.HierarchicalClusterer;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.Tag;
import org.json.*;

public class HClusterer {

    public static void main(String[] args) {

        HierarchicalClusterer hc = new HierarchicalClusterer();
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader("data.arff"));
            Instances data = new Instances(reader);
            reader.close();
            DistanceFunction df = new EuclideanDistance();
            hc.setDistanceFunction(df);
            hc.setNumClusters(1);
            System.out.println(hc.getNumClusters());
            hc.setLinkType(new SelectedTag("WARD", hc.TAGS_LINK_TYPE));
            hc.setPrintNewick(true);

            hc.buildClusterer(data);
            String result = hc.jsonGraph();

            //System.out.println(hc.graph());

            JSONArray tree = new JSONArray(result);

            System.out.println(tree);
            collapse(tree, tree.getDouble(1));
            System.out.println(tree);



            //            ClusterEvaluation eval = new ClusterEvaluation();
            //            eval.setClusterer(hc);
            //            System.out.println(eval.getClusterAssignments());


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void collapse(JSONArray tree, double dist) throws JSONException {
        if (dist >= 8 || dist == -1) {
            if (tree.get(2) instanceof JSONArray) {
                double newDist = ((JSONArray) tree).getDouble(1);
                collapse((JSONArray) ((JSONArray) tree).get(2), newDist);
            }
            if (tree.get(3) instanceof JSONArray) {
                double newDist = ((JSONArray) tree).getDouble(1);
                collapse((JSONArray) ((JSONArray) tree).get(3), newDist);
            }
        } else {
            System.out.println("before: " + tree);
            List<JSONObject> leaves = new ArrayList<JSONObject>();
            if (tree.get(2) instanceof JSONArray) {
                getAllLeaves((JSONArray)((JSONArray) tree).get(2), leaves);
            } else leaves.add((JSONObject)tree.get(2));
            if (tree.get(3) instanceof JSONArray) {
                getAllLeaves((JSONArray)((JSONArray) tree).get(3), leaves);
            } else leaves.add((JSONObject)tree.get(3));
            JSONObject dupes = new JSONObject();
            dupes.put("dupes", leaves);
            tree.put(3, dupes);
            JSONObject best = leaves.get(0);
            tree.put(2, best);
            System.out.println("after: " + tree);

        }
    }

    private static Collection<JSONObject> getAllLeaves(JSONArray tree, Collection<JSONObject> leaves) throws JSONException {
        if (tree.get(2) instanceof JSONObject) 
            leaves.add((JSONObject) tree.get(2));
        else getAllLeaves(tree.getJSONArray(2), leaves);
        if (tree.get(3) instanceof JSONObject) 
            leaves.add((JSONObject) tree.get(3));
        else getAllLeaves(tree.getJSONArray(3), leaves);
        return leaves;
    }

}

