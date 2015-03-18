import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet implementation class RGetData
 */
@WebServlet("/RCGetData")
public class RCGetData extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RCGetData() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ReadingDBInterface db;
		ConfigManager cm = new ConfigManager(this); // this object gets the database connections values
		response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8888");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		
		String usr = request.getParameter("usr");  
		String grp = request.getParameter("grp");  
		
		String sid = request.getParameter("sid");
		
		String structureId = request.getParameter("structureid");
		
		String readingId = request.getParameter("readingid"); // optional. If given just return data for this reading
		boolean getFromCache = (request.getParameter("cache") != null);
		
		String callback = request.getParameter("callback"); 
		
		db = new ReadingDBInterface(cm.dbstring,cm.dbuser,cm.dbpass);
		db.openConnection();
		
		String output = ""; 
		if(getFromCache){
			// GET THE CACHED UM
		}else{
			// GET THE UPDATED UM
			
			// 1. READ THE STRUCTURE OF READINGS
			ServletContext context = this.getServletContext();
			InputStream input = context.getResourceAsStream("./WEB-INF/structure/"+structureId+".json");
			String json = Common.convertStreamToString(input);
			try{
				JSONObject jsonObject = new JSONObject(json);
				JSONArray sections = jsonObject.getJSONArray("children");
				for (int i = 0; i < sections.length(); ++i) {
				    JSONObject section = sections.getJSONObject(i);
				    String name = section.getString("name");
				    //System.out.println(name);
				    
				}
				
			}catch(Exception e){
				output = "{\"error\":\"not able to read structure\"}";
			}
			
			
			
			
		}
		

		db.closeConnection();
		
		
		Common.writeOutput(out,callback,output);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
