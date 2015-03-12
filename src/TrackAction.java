

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.util.ArrayList;
/**
 * Servlet implementation class TrackAction
 */
@WebServlet("/TrackAction")
public class TrackAction extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TrackAction() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ReadingDBInterface db;
		ConfigManager cm = new ConfigManager(this); // this object gets the database connections values

		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		
		// parameters
		String callback = request.getParameter("callback"); 
		
		String usr = request.getParameter("usr");  
		String grp = request.getParameter("grp");  
		String sid = request.getParameter("sid");
		if(usr == null || grp == null || sid == null){
			writeOutput(out,callback,"{res:0,msg:\"usr, grp or sid not specified\"}");
			return;
		}
		
		String readingId = request.getParameter("readingid");
		
		String fileUrl = request.getParameter("fileurl");  
		int page = -1;
		try{
			page = Integer.parseInt(request.getParameter("page").trim());
		}catch(Exception e){
			
		}
		String action = request.getParameter("action");  
		String comment = request.getParameter("comment"); 
		if(comment == null) comment = "";
		
		if(readingId != null){
			db = new ReadingDBInterface(cm.dbstring,cm.dbuser,cm.dbpass);
			db.openConnection();
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
					
				boolean result = db.insertTrackAction(usr, grp, sid, r.getBookId(), readingIds, fileUrl, page, action, comment);
				db.closeConnection();
				if(result){
					writeOutput(out,callback,"{res:1,msg:\"ok\"}");
//					out.write(URLEncoder.encode("http://columbus.exp.sis.pitt.edu/socialreader/readings/le/le001.pdf?offset=3&u=01", "UTF-8"));
				}else{
					writeOutput(out,callback,"{res:0,msg:\"fail to insert action\"}");
				}
			}else{
				writeOutput(out,callback,"{res:0,msg:\"reading does not exist\"}");
			}
		}else{
			writeOutput(out,callback,"{res:0,msg:\"no reading id\"}");
		}
		

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
