
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class GetAnnotations
 */
@WebServlet("/GetAnnotations")
public class GetAnnotations extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetAnnotations() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ReadingDBInterface db;
		ConfigManager cm = new ConfigManager(this); // this object gets the database connections values

		response.setContentType("application/json");
		response.setHeader("Access-Control-Allow-Origin", "*");
        //response.setHeader("Access-Control-Allow-Origin", "http://localhost:8000");
		
		PrintWriter out = response.getWriter();
		
		String callback = request.getParameter("callback"); 
		// parameters
//		String mode = request.getParameter("mode");  // new, modify
//		if(mode == null || (!mode.equalsIgnoreCase("user") && !mode.equalsIgnoreCase("group"))){
//			mode = "all";
//		}
//		mode = mode.trim();
		
		// provide only usr and will retrieve only usr comments
		// provide only grp and will retrieve only grp comments
		// provide both and will retrieve usr and grp comments
		// if none provided, will return all comments
		String usr = request.getParameter("usr");  
		String grp = request.getParameter("grp");  
		
//		String sid = request.getParameter("sid");
//		if(usr == null || grp == null || sid == null){
//			writeOutput(out,callback,"{res:0,msg:\"usr, grp or sid not specified\"}");
//			return;
//		}
//		usr = usr.trim();
//		grp = grp.trim();
//		sid = sid.trim();
		
		String readingId = request.getParameter("readingid");
		
		// String fileUrl = request.getParameter("fileurl");  

		// @@@@ String contentType = request.getParameter("contenttype");
		// provide page to get only the comments within a specific page
		int page = -1;
		try{
			page = Integer.parseInt(request.getParameter("page").trim());
		}catch(Exception e){
			page = -1;
		}
		
		String output = "";
		
		if(readingId != null){
			db = new ReadingDBInterface(cm.dbstring,cm.dbuser,cm.dbpass);
			db.openConnection();
			
			Reading r = db.getRedingInfo(readingId);
			
			if(r != null){
				if(usr != null) usr = usr.trim(); 
				if(grp != null) grp = grp.trim(); 
				ArrayList<String> urls = r.GetFileUrls();
				ArrayList<Annotation> annotations = new ArrayList<Annotation>();
				if(urls.size()>0){
					for(String url : urls) annotations.addAll(db.getAnnotations(url, page, usr, grp));
				}
				

				//db.closeConnection();

				output = "{\n"
						+ "  \"total\":" + annotations.size() + ",\n"
						+ "  \"rows\": [ \n";
				for(Annotation a : annotations){
					output += a.jsonAnnotatorJS("    ")+",\n";
				}
				output = output.substring(0,output.length()-2);
				output += "\n  ]\n}";
			}else{
				output = "{\"error\":\"reading "+readingId+" does not exist\"}";
			}
			db.closeConnection();
		}else{
			output = "{\"error\":\"no reading id provided\"}";
		}
		
		writeOutput(out,callback,output);


	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
	
	public static void writeOutput(PrintWriter out, String callback, String output){
		if(callback != null) out.write(callback+"(");
		out.write(output);
		if(callback != null) out.write(");");
	}

}
