import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
	private static boolean verbose = false;
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
		
		String readingIds = request.getParameter("readingids"); // optional. If given just return data for this reading
		boolean flatStructure = (request.getParameter("flat") != null);
		boolean getFromCache = (request.getParameter("cache") != null);
		
		String callback = request.getParameter("callback"); 
		
		db = new ReadingDBInterface(cm.dbstring,cm.dbuser,cm.dbpass);
		db.openConnection();
		
		String output = ""; 
		if(getFromCache){
			// GET THE CACHED UM
			////////////////////////////////////////////////////////
			// TODO  ///////////////////////////////////////////////
			////////////////////////////////////////////////////////
			
		}else{
			// COMPUTE THE UPDATED UM
			boolean error = false;
			boolean store = true;
			Node root = new Node();
			//root.reading =
			
			// 1. GET A STRUCTURE OF READINGS
			
			if(flatStructure){
				// A. Creates a simple structure with just one level behind the root containing the nodes 
				// with readings in the comma-separated list of readings readingIds.
				if(readingIds == null || readingIds.length()==0){
					error = true;
					output = "{res: 0, msg:\"flat parameter specified, but no reading ids included\"}";
				}else{
					ArrayList<Reading> rs = db.getRedingsInfo(readingIds); // this method is the key!!!
					for(Reading r : rs){
						Node n = new Node(r,root);
						root.children.add(n);
					}
				}
				store = false;
			}else{
				// B. READ THE STRUCTURE OF READINGS FROM A FILE
				ServletContext context = this.getServletContext();
				String json = "";
				try{
					InputStream input = context.getResourceAsStream("./WEB-INF/structure/"+structureId+".json");
					json = Common.convertStreamToString(input);
					JSONObject jsonObject = new JSONObject(json);
					addChildren(root,jsonObject);
					
					//printTree(root," "); // @@@@
					
				}catch(Exception e){
					e.printStackTrace();
					error = true;
					output = "{res: 0, msg:\"not able to read structure\"}";
				}
				
			}
			
			
			if(!error){
				// 2. GET ACTIVITY FROM DB
				ArrayList<PageActivity> activity = db.getActivityByFile(usr);
//				for(PageActivity p : activity){
//					System.out.println(p.readingIds + " " + p.getPageLoads());
//				}
				
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
				
				computeEachReadingProgress(readingMap,"simple"); // compute progress of each reading among its own pages and activity tracked
				
				
				// 4. PROPAGATE PROGRESS THROUGH THE TREE
				propagateLevels(root);
				//printTree(root,"  ");
				
				// 5. GENERATE JSON
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = new Date();
				
				output = generateJSONFromTree(root,"RC");
				output = output.substring(0, output.length()-1);
				
				double avgProgress = 0.0;
				if(root.children.size()>0){
					for(Node n: root.children) avgProgress += n.reading.getProgress();
					avgProgress = avgProgress / root.children.size();					
				}
				
				if(store) db.storeUM(usr, sid, structureId, avgProgress, "\"data\":["+output+"]\n}");
				
				
				
				output = "{\n  \"userid\":\""+usr+"\",\n  \"retrievedon\":\""+dateFormat.format(date)+"\",\n  \"updatedon\":\""+dateFormat.format(date)+"\",\n" 
						+"  \"structureid\":\""+structureId+"\",\n\"data\":["+output+"]\n}";	
				// TO DO: Store the computed in rc_cahced_user_models table
				
			}
				

		}
		

		db.closeConnection();
		
		
		Common.writeOutput(out,callback,output);
	}

	
	/**
	 * Post response is different. It assumes a JSON has been pushed in the request with athe content items (reading-ids) to
	 * retrieve in the format specified for Aggregate-UM interaction.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ReadingDBInterface db;
		ConfigManager cm = new ConfigManager(this); // this object gets the database connections values
		response.setHeader("Access-Control-Allow-Origin", "*");
        //response.setHeader("Access-Control-Allow-Origin", "http://localhost:8888");
		response.setContentType("application/json");
		
		String callback = request.getParameter("callback"); 
		
		PrintWriter out = response.getWriter();
		boolean error =false;
		String output = "";
		String json = "";
		
		String usr = null;
		String grp = null;
		String domain = null;
		//each provider has a content list
		//ArrayList<String> contentList = new ArrayList<String>();
		//the key of map is provider name, the ArrayList stores content list of this provider
		Map<String, ArrayList<String>> provider_contentListMap = new HashMap<String, ArrayList<String>>();
		//Map<String, Double[]> 
		try {
			/**
			 * {
					"user­-id" : "dguerra", 
					"group­-id" : "test", 
					"domain" : "java", 
					"content­-list-­by­provider" : 
					[
						{ "provider-­id" : "WE", "content-­list" : [ "ex1","ex2" ] }, 
						{ "provider­-id" : "AE", "content­-list" : [ "ae1" ] }
					] 
				}
			 */
			
			json = Common.convertStreamToString(request.getInputStream());
			JSONObject jsonObject = new JSONObject(json);
			
			//System.out.println(jsonObject.toString());
			
			usr = jsonObject.getString("user-id");
			grp = (String)jsonObject.get("group-id");
			domain = (String)jsonObject.get("domain");
			
			if(verbose){
				System.out.println("The usr is: " + usr);
				System.out.println("The grp is: " + grp);
				System.out.println("The domain is: " + domain);
				
			}
			
			JSONArray providers = jsonObject.getJSONArray("content­-list-­by­provider");
			if(providers == null){
				error = true;
				output = "{res: 0, msg:\"no providers defined\"}";
			}else{
				Node root = new Node();
				String readingIds = "";
				for (int i = 0; i < providers.length(); ++i) {
				    JSONObject prov = providers.getJSONObject(i);
				    JSONArray provContent = prov.getJSONArray("content-­list");
				    for (int j = 0; j < provContent.length(); ++j) {
				    	readingIds += provContent.getString(j)+",";
				    }
				    
				    //System.out.println(name);
				    
				}
				db = new ReadingDBInterface(cm.dbstring,cm.dbuser,cm.dbpass);
				db.openConnection();
				if(readingIds.length()>0) readingIds = readingIds.substring(0, readingIds.length()-1); // remove the last comma 
				// Make a simple structure
				if(verbose) System.out.println(readingIds);
				ArrayList<Reading> rs = db.getRedingsInfo(readingIds); // this method is the key!!!
				for(Reading r : rs){
					Node n = new Node(r,root);
					root.children.add(n);
				}
				
				// GET ACTIVITY FROM DB
				ArrayList<PageActivity> activity = db.getActivityByFile(usr);
//				for(PageActivity p : activity){
//					System.out.println(p.readingIds + " " + p.getPageLoads());
//				}
				
				// 3. MAKE A PLAIN LIST OF READINGS AND FILL ACTIVITY OF EACH OF THEM
				HashMap<String,Reading> readingMap = new HashMap<String,Reading>();
				HashMap<String,Integer> annotationCounts = db.getAnnotationCount();
				
				db.closeConnection();
				
				fillRadingHashMap(root, readingMap,annotationCounts);
				for(PageActivity p : activity){
					for(String rId : p.readingIds){
						Reading r = readingMap.get(rId);
						if(r != null) r.activity.add(p);
					}
				}
				
				computeEachReadingProgress(readingMap,"simple"); // compute progress of each reading among its own pages and activity tracked
				
				
				// 4. PROPAGATE PROGRESS THROUGH THE TREE
				propagateLevels(root);
				//printTree(root,"  ");
				
				// 5. GENERATE JSON
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = new Date();
				
				output = generateJSONFromTree(root,"aggregate");
				output = output.substring(0, output.length()-1);
				
//				double avgProgress = 0.0;
//				if(root.children.size()>0){
//					for(Node n: root.children) avgProgress += n.reading.getProgress();
//					avgProgress = avgProgress / root.children.size();					
//				}
				
				
				output = "{\n  \"user-id\":\""+usr+"\",\n  \"group-id\":\""+grp+"\",\n" 
						+"  \"content-list\":["+output+"]\n}";	
				
				
				
			}		
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			output = "{res: 0, msg:\"error\"}";
		}
		Common.writeOutput(out,callback,output);
	}
	
	
	
	// METHODS
	public String generateJSONFromTree(Node r, String format){
		String res = "";
		if(r.reading != null) res += generateJSONFromNode(r, format)+",";
		if(r.children != null && r.children.size() > 0){
			for(Node c : r.children){
				res += generateJSONFromTree(c,format);
			}
		}
		
		return res;
	}
	
	public String generateJSONFromNode(Node n, String format){
		if(n.reading == null) return "";
		if(format.equalsIgnoreCase("RC")){
			return "{ \"readingid\":\""+n.reading.getReadingId()+"\", \"k\":"+n.getAggKnowledge()+", \"a\":"+n.reading.getAnnotationCount()+", \"p\":"+n.getAggProgress()+", \"c\":"+n.getAggPConfidence()+"}";
		}
		// TODO compute time spent!!!
		else if(format.equalsIgnoreCase("aggregate")){
			return "{ \"content-id\":\""+n.reading.getReadingId()+"\", \"progress\":"+n.getAggProgress()+", \"attempts\":"+n.reading.getLoadCount()+", \"success-rate\":"+0.0+", \"annotation-count\":"+n.reading.getAnnotationCount()+",  \"like-count\":"+0+", \"time-spent\":-1, \"sub-activities\":"+0+"}";

		}else {
			return "{ \"readingid\":\""+n.reading.getReadingId()+"\", \"k\":"+n.getAggKnowledge()+", \"a\":"+n.reading.getAnnotationCount()+", \"p\":"+n.getAggProgress()+", \"c\":"+n.getAggPConfidence()+"}";
		}
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
			//System.out.println(offSet+n.reading.getReadingId()+" "+n.getAggProgress()+"   "+(n.reading != null ? "R:"+n.reading.getProgress() : "0.0"));
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
	


}
