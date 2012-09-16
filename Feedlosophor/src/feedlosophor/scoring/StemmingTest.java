package feedlosophor.scoring;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;


public class StemmingTest {

	public static void main(String[] args) {
		Test();

	}
	
	public static void Test() {
		String s1 = readFile("1_romney.txt");
		String s2 = readFile("1_iphone5.txt");
		System.out.println(s1);
		System.out.println("---------------------------------------------------------------------------");
		System.out.println(Stemming(s1));
		System.out.println("***************************************************************************");
		System.out.println(s2);
		System.out.println("---------------------------------------------------------------------------");
		System.out.println(Stemming(s2));
		System.out.println("***************************************************************************");

	}

	public static String Stemming(String text) {
		StringBuffer result = new StringBuffer();
        if (text!=null && text.trim().length()>0){
            StringReader tReader = new StringReader(text);
            Analyzer analyzer = new MyAnalyzer();
            TokenStream tStream = analyzer.tokenStream("contents", tReader);
            TermAttribute term = tStream.addAttribute(TermAttribute.class);

            try {
                while (tStream.incrementToken()){
                    result.append(term.term());
                    result.append(" ");
                }
            } catch (IOException ioe){
                //System.out.println("Error: "+ioe.getMessage());
            }
        }

        // If, for some reason, the stemming did not happen, return the original text
        if (result.length()==0)
            result.append(text);
        return result.toString().trim();
		
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

class MyAnalyzer extends Analyzer {

	@Override
	public TokenStream tokenStream(String filedName, Reader reader) {
		return new PorterStemFilter(new LowerCaseTokenizer(reader));
	}
	
	
}

class PositionalPorterStopAnalyzer extends Analyzer {
	private Set stopWords;
	public PositionalPorterStopAnalyzer() {
		this(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
	}
	public PositionalPorterStopAnalyzer(Set stopWords) {
		this.stopWords = stopWords;
	}
	@Override
	public TokenStream tokenStream(String fieldName, java.io.Reader reader) {
		StopFilter stopFilter = new StopFilter(true,
				new LowerCaseTokenizer(reader),
				stopWords);
		stopFilter.setEnablePositionIncrements(true);
		return new PorterStemFilter(stopFilter);
	}


}