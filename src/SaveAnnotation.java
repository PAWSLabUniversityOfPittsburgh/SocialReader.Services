

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
public class SaveAnnotation extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SaveAnnotation() {
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
		
		String callback = request.getParameter("callback"); 
		// parameters
		String mode = request.getParameter("mode");  // new, modify
		if(mode == null || (!mode.equalsIgnoreCase("new") && !mode.equalsIgnoreCase("modify"))){
			Common.writeOutput(out,callback,"{res:0,msg:\"mode not specified or not valid (use new or modify)\"}");
			return;
		}
		mode = mode.trim();
		String annotationType = request.getParameter("annotationtype");
		if(mode.equalsIgnoreCase("new") && (annotationType == null || annotationType.length()==0)){
			Common.writeOutput(out,callback,"{res:0,msg:\"annotation type not specified or not valid\"}");
			return;
		}
		if(mode.equalsIgnoreCase("new")) annotationType = annotationType.trim();
		else annotationType = "";
		
		String usr = request.getParameter("usr");  
		String grp = request.getParameter("grp");  
		String sid = request.getParameter("sid");
		if(usr == null || grp == null || sid == null){
			Common.writeOutput(out,callback,"{res:0,msg:\"usr, grp or sid not specified\"}");
			return;
		}
		usr = usr.trim();
		grp = grp.trim();
		sid = sid.trim();
		
		String readingId = request.getParameter("readingid");  
		String fileUrl = request.getParameter("fileurl");  
		int page = -1;
		try{
			page = Integer.parseInt(request.getParameter("page").trim());
		}catch(Exception e){
			page = -1;
		}
		String text = (request.getParameter("text")==null ? "" : request.getParameter("text")); 
		String position = (request.getParameter("position")==null ? "" : request.getParameter("position")); 
		int parentAnnotationId = -1;
		try{
			parentAnnotationId = Integer.parseInt(request.getParameter("parent").trim());
		}catch(Exception e){
			parentAnnotationId = -1;
		}
		
		db = new ReadingDBInterface(cm.dbstring,cm.dbuser,cm.dbpass);
		db.openConnection();
		if(mode.equalsIgnoreCase("new")){
			if(annotationType.equalsIgnoreCase("reply")){
				long key = db.insertAnnotation(annotationType, usr, grp, sid, "", "", -1, "", text, 
						parentAnnotationId,"","","","","");
				if(key != -1){
					Common.writeOutput(out,callback,"{res:1,annotationid:"+key+",msg:\"ok\"}");
				}else{
					Common.writeOutput(out,callback,"{res:0,msg:\"error on inserting a reply\"}");
				}
			}
			else if(readingId != null){
				Reading r = db.getRedingInfo(readingId);
				if(r != null){
					String readingIds = "";
					if(page > 0){
						ArrayList<String> readings = db.getAllRadingForAPage(r.getBookId(), page, r.getUrl(), r.getFormat());
						for(String rid : readings){
							readingIds += "["+rid+"]";
						}					
					}else{
						readingIds = "["+readingId+"]";
					}
						
					long key = db.insertAnnotation(annotationType, usr, grp, sid, readingIds, fileUrl, page, position, text, 
							parentAnnotationId,"","","","","");
					
					
					if(key != -1){
						Common.writeOutput(out,callback,"{res:1,annotationid:"+key+",msg:\"ok\"}");
//						out.write("\n");
//						out.write(URLEncoder.encode("http://columbus.exp.sis.pitt.edu/socialreader/readings/le/le001.pdf?offset=3&u=01", "UTF-8"));
					}else{
						Common.writeOutput(out,callback,"{res:0,msg:\"fail to insert annotation\"}");
					}
					
				}else{
					Common.writeOutput(out,callback,"{res:0,msg:\"reading does not exist\"}");
				}
			}else{
				Common.writeOutput(out,callback,"{res:0,msg:\"no reading id\"}");
			}
	
		}else{ // modify
			if(db.updateAnnotation(parentAnnotationId, position, text)){
				Common.writeOutput(out,callback,"{res:1,msg:\"ok\"}");
			}else{
				Common.writeOutput(out,callback,"{res:0,msg:\"fail to modify a comment\"}");
			}
		}
		db.closeConnection();
	}


}
