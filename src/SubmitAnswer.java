

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

/**
 * Servlet implementation class SubmitAnswer
 */
@WebServlet("/SubmitAnswer")
public class SubmitAnswer extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SubmitAnswer() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
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
	
	    try{
	    	JSONObject json = new JSONObject(sb.toString());
	    	String usr = (!json.isNull("usr") ? json.getString("usr") : "");
	    	String questionId = (!json.isNull("questionid") ? json.getString("questionid") : "");
	    	double score = (!json.isNull("score") ? json.getDouble("score") : 0.0);
	    	
	    	String answers = (!json.isNull("answer-choices")? json.getJSONArray("answer-choices").toString() : "");
	    	answers = answers.replaceAll("\\[", "").replaceAll("\\]","").replaceAll(",",";").replaceAll("\"","");

	    	db = new ReadingDBInterface(cm.dbstring,cm.dbuser,cm.dbpass);
			db.openConnection();
			
			//System.out.println(answers);
			long id = db.insertAnswer(questionId, usr, score, answers);
			db.closeConnection();
	    	
		    
	    	//System.out.println(ranges.toString());
	    	Common.writeOutput(out,callback,"{\"res\":1,\"msg\":\"ok\",\"answerid\":\""+id+"\"}");
		    
		    
	    }catch(Exception e){
		    e.printStackTrace();
		    Common.writeOutput(out,callback,"{\"error\":\"problem on parsing the json\"}");
	    }
	    
        
	}

}
