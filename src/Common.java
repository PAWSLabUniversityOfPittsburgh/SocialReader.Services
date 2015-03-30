import java.io.PrintWriter;
import java.util.ArrayList;


public class Common {
	public static void writeOutput(PrintWriter out, String callback, String output){
		if(callback != null) out.write(callback+"(");
		out.write(output);
		if(callback != null) out.write(");");
	}
	
	public static String convertStreamToString(java.io.InputStream is) {
		if(is == null) return "";
	    java.util.Scanner s = new java.util.Scanner(is,"UTF-8").useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
	public static ArrayList<String> parseReadingIds(String idsAsStr){
    	ArrayList<String> readingIds = new ArrayList<String>();
    	String[] ids = idsAsStr.split("]");
		for(int i =0; i<ids.length;i++){
			ids[i] = ids[i].replaceAll("\\[", "").replaceAll("\\]", "");
			readingIds.add(ids[i]);
		}
		return readingIds;
	}
	
	
	
	public static boolean contains(int[] array, int value){
		for(int i : array) if(i == value) return true;
		return false;
	}
	
	
}
