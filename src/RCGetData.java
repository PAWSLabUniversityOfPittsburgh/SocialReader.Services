import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

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
			Node root = new Node();
			//root.reading =
			
			
			// 1. READ THE STRUCTURE OF READINGS
			ServletContext context = this.getServletContext();
			InputStream input = context.getResourceAsStream("./WEB-INF/structure/"+structureId+".json");
			String json = Common.convertStreamToString(input);
			try{
				JSONObject jsonObject = new JSONObject(json);
				addChildren(root,jsonObject);
				
				//printTree(root," "); // @@@@
				
			}catch(Exception e){
				e.printStackTrace();
			
				output = "{\"error\":\"not able to read structure\"}";
			}
			
			// 2. GET ACTIVITY FROM DB
			ArrayList<PageActivity> activity = db.getActivityByFile(usr);
//			for(PageActivity p : activity){
//				System.out.println(p.readingIds + " " + p.getPageLoads());
//			}
			
			// 3. MAKE A PLAIN LIST OF READINGS AND FILL ACTIVITY OF EACH OF THEM
			HashMap<String,Reading> readingMap = new HashMap<String,Reading>();
			HashMap<String,Integer> annotationCounts = db.getAnnotationCount();
			fillRadingHashMap(root, readingMap,annotationCounts);
			for(PageActivity p : activity){
				for(String rId : p.readingIds){
					Reading r = readingMap.get(rId);
					if(r != null) r.activity.add(p);
				}
			}
			
			computeEachReadingProgress(readingMap,"simple"); // compute rogress of each reading among its own pages and activity tracked
			
			
			// 4. PROPAGATE PROGRESS THROUGH THE TREE
			propagateLevels(root);
			printTree(root,"  ");
			
			// 5. GENERATE JSON
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			
			output = generateJSONFromTree(root);
			output = output.substring(0, output.length()-1);
			
			output = "{\n  \"userid\":\""+usr+"\",\n  \"retrievedon\":\""+dateFormat.format(date)+"\",\n  \"updatedon\":\""+dateFormat.format(date)+"\",\n" 
					+"  \"structureid\":\""+structureId+"\",\n\"data\":["+output+"]\n}";
		}
		

		db.closeConnection();
		
		
		Common.writeOutput(out,callback,output);
	}

	public String generateJSONFromTree(Node r){
		String res = "";
		if(r.reading != null) res += generateJSONFromNode(r);
		if(r.children != null && r.children.size() > 0){
			for(Node c : r.children){
				res += generateJSONFromNode(c)+",";
			}
		}
		
		return res;
	}
	
	public String generateJSONFromNode(Node n){
		if(n.reading == null) return "";
		return "{ \"readingid\":\""+n.reading.getReadingId()+"\", \"k\":"+n.getAggKnowledge()+", \"a\":"+n.reading.getAnnotationCount()+", \"p\":"+n.getAggProgress()+", \"c\":"+n.getAggPConfidence()+"}";
	}
	
	public void addChildren(Node current, JSONObject o){
		JSONArray children = o.getJSONArray("children");
		if(children == null){
			return;
		}else{
			for (int i = 0; i < children.length(); ++i) {
			    JSONObject child = children.getJSONObject(i);
			    Reading r = null;
			    //if(child.has("format")){
				    String bookId = (child.has("bookid") ? child.getString("bookid") : "");
				    int spage = (child.has("spage") ? child.getInt("spage") : 0);
				    int epage = (child.has("epage") ? child.getInt("epage") : 0);
				    String format = (child.has("format") ? child.getString("format") : "");
				    r = new Reading(child.getString("readingid"), child.getString("title"), bookId,
							"", spage, epage, "","", format, "", "");			    	
			    //}
			    Node n = new Node();
			    n.reading = r;
			    n.parent = current;
			    current.children.add(n);
			    
			    addChildren(n,child);
			    
			    //System.out.println(name);
			    
			}
			
		}
	}
	
	public void printTree(Node r, String offSet){
		for(Node n : r.children){
			System.out.println(offSet+n.reading.getReadingId()+" "+n.getAggProgress()+"   "+(n.reading != null ? "R:"+n.reading.getProgress() : "0.0"));
			printTree(n,offSet+"    ");
		}
	}
	
	public void fillRadingHashMap(Node r, HashMap<String,Reading> map, HashMap<String,Integer> annotationCounts){
		for(Node n : r.children){
			//if(n.reading != null) 
			Integer ac = annotationCounts.get(n.reading.getReadingId());
			if(ac == null) ac = 0;
			n.reading.setAnnotationCount(ac);
			map.put(n.reading.getReadingId(),n.reading);
			fillRadingHashMap(n, map, annotationCounts);
		}
	}
	
	public void computeEachReadingProgress(HashMap<String,Reading> readingMap, String method){
		for (Entry<String, Reading> reading : readingMap.entrySet()) {
			Reading r = reading.getValue();
			r.computeSimpleProgress();
			System.out.println(r.getReadingId() + " " + r.getProgress() + " / " + r.getPConfidence());
		}
		
	}
	
	public void propagateLevels(Node n){
		double[] levels = new double[4];
		int nChildren = 0;
		
		if(n.reading != null && n.reading.getFormat().length()>0){
			levels[0] = n.reading.getKnowledge();
			levels[1] = n.reading.getProgress();
			levels[2] = n.reading.getKConfidence();
			levels[3] = n.reading.getPConfidence();
			nChildren = 1;
		}
		
		if(n.children == null || n.children.size() == 0){
			n.setAggKnowledge(levels[0]);
			n.setAggProgress(levels[1]);
			n.setAggKConfidence(levels[2]);
			n.setAggPConfidence(levels[3]);
			return;
		}else{
			for(Node c: n.children){
				propagateLevels(c);
				
				levels[0] += c.getAggKnowledge();
				levels[1] += c.getAggProgress();
				levels[2] += c.getAggKConfidence();
				levels[3] += c.getAggPConfidence();
				
				nChildren++;
			}
			
			
			if(nChildren == 0) nChildren = 1;
			n.setAggKnowledge(levels[0]/nChildren);
			n.setAggProgress(levels[1]/nChildren);
			n.setAggKConfidence(levels[2]/nChildren);
			n.setAggPConfidence(levels[3]/nChildren);
			
		}
		
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
