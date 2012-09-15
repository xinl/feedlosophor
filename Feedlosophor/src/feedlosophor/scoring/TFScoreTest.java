package feedlosophor.scoring;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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
		System.out.println(tfc.getResult());

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
