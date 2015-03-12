

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class CreateAnnotation
 */
@WebServlet("/CreateAnnotation")
public class CreateAnnotation extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateAnnotation() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ReadingDBInterface db;
		ConfigManager cm = new ConfigManager(this); // this object gets the database connections values

		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		
		
		
		// parameters
		
		String callback = request.getParameter("callback");
//		String annotationType = request.getParameter("annotationtype");
//		if(annotationType == null || annotationType.length()==0){
//			Common.writeOutput(out,callback,"{res:0,msg:\"annotation type not specified or not valid\"}");
//			return;
//		}
//
//		String usr = request.getParameter("usr");  
//		String grp = request.getParameter("grp");  
//		String sid = request.getParameter("sid");
//		if(usr == null || grp == null || sid == null){
//			Common.writeOutput(out,callback,"{res:0,msg:\"usr, grp or sid not specified\"}");
//			return;
//		}
//		usr = usr.trim();
//		grp = grp.trim();
//		sid = sid.trim();
		
//		String annotationJSON = request.getParameter("annotation");  
//		
//		JSONObject json = new JSONObject(annotationJSON);
        
		StringBuilder sb = new StringBuilder();
	    BufferedReader reader = request.getReader();
	    try {
	        String line;
	        while ((line = reader.readLine()) != null) {
	            sb.append(line).append('\n');
	        }
	    } finally {
	        reader.close();
	    }
	    //System.out.println(sb.toString());
	
	    try{
	    	JSONObject json = new JSONObject(sb.toString());
	 
	    	String ranges = (!json.isNull("ranges")? json.getJSONArray("ranges").toString() : "[]");
	    	String permissions = (!json.isNull("permissions") ? json.getJSONObject("permissions").toString() : "{}");
	    	String tags = (!json.isNull("tags") ? json.getJSONArray("tags").toString() : "[]");
	    	String links = (!json.isNull("links") ? json.getJSONArray("links").toString() : "[]");
	    	String usr = (!json.isNull("usr") ? json.getString("usr") : "");
	    	String grp = (!json.isNull("grp") ? json.getString("grp") : "");
	    	String sid = (!json.isNull("sid") ? json.getString("sid") : "");
	    	String quote = (!json.isNull("quote")  ? json.getString("quote") : "");
	    	String consumer = (!json.isNull("consumer")  ? json.getString("consumer") : "");
	    	String fileurl = (!json.isNull("fileurl") ? json.getString("fileurl") : "");
	    	String readingid = (!json.isNull("readingid")  ? json.getString("readingid") : "");
	    	String annotationtype = (!json.isNull("annotationtype") ? json.getString("annotationtype") : "");

	    	String text = (!json.isNull("text") ? json.getString("text") : "");

	    	int page = (!json.isNull("page") ? json.getInt("page") : -1);
	    	int parent = (!json.isNull("parent") ? json.getInt("parent") : -1);
	    	
	    	
	    	db = new ReadingDBInterface(cm.dbstring,cm.dbuser,cm.dbpass);
			db.openConnection();
			
			Reading r = db.getRedingInfo(readingid);
			if(r != null){
				String readingIds = "[" + readingid + "]";
				// A page of a pdf or image base book can have more than one reading id.
				if(r.getFormat().equalsIgnoreCase("pdf") || Reading.isImage(r.getFormat())){
					if(page > 0){						
						ArrayList<String> readings = db.getAllRadingForAPage(r.getBookId(), page, fileurl, r.getFormat());
						for(String rid : readings){
							readingIds += "["+rid+"]";
						}											
					}
				}
				long key = db.insertAnnotation(annotationtype, usr, grp, sid, readingIds, fileurl, page, ranges, text, 
						parent, quote, consumer, permissions, tags, links);
				Common.writeOutput(out,callback,"{\"id\":\""+key+"\"}");
			}else{
				Common.writeOutput(out,callback,"{\"error\":\"no readingid included or not valid reading id\"}");
			}
			
			
			db.closeConnection();
	    	
		    
	    	//System.out.println(ranges.toString());
	    	//Common.writeOutput(out,callback,ranges.toString());
		    
		    
	    }catch(Exception e){
		    e.printStackTrace();
		    Common.writeOutput(out,callback,"{\"error\":\"problem on parsing the json\"}");
	    }
	    
        
	    	

	    //Common.writeOutput(out,callback,sb.toString());
        
        
        
        
//		String readingId = request.getParameter("readingid");  
//		String fileUrl = request.getParameter("fileurl");  
//		int page = -1;
//		try{
//			page = Integer.parseInt(request.getParameter("page").trim());
//		}catch(Exception e){
//			page = -1;
//		}
//		String text = (request.getParameter("text")==null ? "" : request.getParameter("text")); 
//		String position = (request.getParameter("position")==null ? "" : request.getParameter("position")); 
//		int parentAnnotationId = -1;
//		try{
//			parentAnnotationId = Integer.parseInt(request.getParameter("parent").trim());
//		}catch(Exception e){
//			parentAnnotationId = -1;
//		}
		

	}

}
