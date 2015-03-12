import java.io.PrintWriter;


public class Common {
	public static void writeOutput(PrintWriter out, String callback, String output){
		if(callback != null) out.write(callback+"(");
		out.write(output);
		if(callback != null) out.write(");");
	}
}
