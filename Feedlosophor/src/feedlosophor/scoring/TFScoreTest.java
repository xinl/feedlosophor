package feedlosophor.scoring;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONArray;

import feedlosophor.clusterer.HClusterer;

public class TFScoreTest {

	public static void main(String[] args) {
		Test();
	}

	public static void Test() {


		String[] input = {readFile("test_files/1_romney.txt"),
				readFile("test_files/2_romney.txt"),
				readFile("test_files/3_romney.txt"),
				readFile("test_files/4_romney.txt"),
				readFile("test_files/5_romney.txt"),
				readFile("test_files/6_romney.txt"),
				readFile("test_files/1_iphone5.txt"),
				readFile("test_files/2_iphone5.txt"),
				readFile("test_files/3_iphone5.txt"),
				readFile("test_files/4_iphone5.txt"),
				readFile("test_files/5_iphone5.txt"),
				readFile("test_files/6_iphone5.txt"),
		};
		String[] ids = {"romney_1", 
				"romney_2", 
				"romney_3", 
				"romney_4", 
				"romney_5", 
				"romney_6",
				"iphone_1", 
				"iphone_2", "iphone_3", "iphone_4", "iphone_5", "iphone_6"};		

		TFCalculator tfc = new TFCalculator(input, ids);
		//System.out.println("vector size = " + tfc.tfWords.size());
		for (String s : tfc.tfWords) {
			//System.out.print(s + " ");			
		}
		System.out.println();
		//System.out.println(tfc.getResult());
		try {
		    //[SINGLE|COMPLETE|AVERAGE|MEAN|CENTROID|WARD|ADJCOMLPETE|NEIGHBOR_JOINING]
	            HClusterer hc = new HClusterer("AVERAGE", 1, 6, 5.6);
	            String jsonHierachy = hc.getJsonHierachy(new ByteArrayInputStream(tfc.getResult().getBytes("UTF-8")));
	            System.out.println(jsonHierachy);
	            String result = hc.getClusters(jsonHierachy);
	            System.out.println(result);
	            JSONArray jsonResult = new JSONArray(result);
	            System.out.println(jsonResult.length() + " clusters:");
	            for (int i = 0; i < jsonResult.length(); ++i)
	                System.out.println(jsonResult.get(i));
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}

	public static String readFile(String path) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder(); 
		try {

			String sCurrentLine;
			br = new BufferedReader(new FileReader(path));
			while ((sCurrentLine = br.readLine()) != null) {
				sb.append(sCurrentLine).append("\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return sb.toString();
	}
}
