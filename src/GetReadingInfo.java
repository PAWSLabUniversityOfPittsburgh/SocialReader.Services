// http://localhost:8080/socialreader_services/GetReadingInfo?readingid=lamming-0012

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class getReadingInfo
 */
@WebServlet("/getReadingInfo")
public class GetReadingInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public GetReadingInfo() {
        
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
		String readingId = request.getParameter("readingid");  
		String usr = request.getParameter("usr");  
		String callback = request.getParameter("callback"); 
		String fileURL = request.getParameter("fileurl"); 
		
		db = new ReadingDBInterface(cm.dbstring,cm.dbuser,cm.dbpass);
		db.openConnection();
		
		if(readingId != null && readingId.length()>0){
			
			
			Reading r = db.getRedingInfo(readingId);
			db.getReadingQuestions(r,usr);
			
			
			if(r != null){
				//System.out.println(r.getTitle());
				if(callback != null && callback.length() > 0){
					out.write(callback+"(");
					out.write(r.jsonFormat());
					out.write(")");
				}else{
					out.write(r.jsonFormat());
				}
				
			}else{
				System.out.println("{error:\"no reading found\"}");
			}
		}else if(fileURL != null && fileURL.length()>0){
			ArrayList<String> readingIds = db.getAllRadingForAPage("", 0, fileURL, "html"); // TODO manage file formats!!!
			if(readingIds.size()>0){
				Reading r = db.getRedingInfo(readingIds.get(0));
				db.getReadingQuestions(r,usr);
				
				
				if(r != null){
					//System.out.println(r.getTitle());
					if(callback != null && callback.length() > 0){
						out.write(callback+"(");
						out.write(r.jsonFormat());
						out.write(")");
					}else{
						out.write(r.jsonFormat());
					}
					
				}else{
					System.out.println("{error:\"no reading found\"}");
				}
			}
				
		}
		
		db.closeConnection();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
