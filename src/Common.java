import java.io.PrintWriter;


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
}
