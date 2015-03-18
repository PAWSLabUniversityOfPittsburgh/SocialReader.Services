

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class RCGetStructure
 */
@WebServlet("/RCGetStructure")
public class RCGetStructure extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RCGetStructure() {
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
		
		// parameters
		String structureId = request.getParameter("structureid");  
		String callback = request.getParameter("callback"); 
		
		ServletContext context = this.getServletContext();
		
		InputStream input = context.getResourceAsStream("./WEB-INF/structure/"+structureId+".json");
		String output = Common.convertStreamToString(input);
		
		input.close();
		
		if (output == null || output.length() == 0){
			output = "{\"error\":\"fail to retrieve the structure requested\"}";
		}
		Common.writeOutput(out,callback,output);
		//System.out.println(output);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
	

}
