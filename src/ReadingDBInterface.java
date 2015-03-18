import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReadingDBInterface extends DBInterface {
	public ReadingDBInterface(String connurl, String user, String pass) {
		super(connurl, user, pass);
	}
	
	// get information about a particular reading, including the previous and next readings
	public Reading getRedingInfo(String readingId) {
		try {
			Reading r = null;
			stmt = conn.createStatement();
			String query = "SELECT readingid,bookid,title,authors,spage,epage,url,files,format,COALESCE(nextreading,'') as nextreading, "
					+ " COALESCE((select readingid from ent_reading where nextreading = '"+readingId+"'),'') as prevreading FROM ent_reading "
					+ " WHERE readingid = '"+readingId+"';";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				r = new Reading( rs.getString("readingid").trim(),
						rs.getString("title").trim(),
						rs.getString("bookid").trim(),
						rs.getString("authors").trim(),
						rs.getInt("spage"),
						rs.getInt("epage"),
						rs.getString("url").trim(),
						rs.getString("files").trim(),
						rs.getString("format").trim(),
						rs.getString("prevreading").trim(),
						rs.getString("nextreading").trim());
			}
			return r;
		}
		catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			return null;
		}
		finally {
			this.releaseStatement(stmt, rs);
		}

	}
	
	// gets a list of all readings (readingid) that include a particular page
	// this works for 
	// - IMAGE BASED BOOKS WHERE EPAGE AND SPAGE ARE NUMBERED
	//   ABSOLUTELY ACCORDING TO THE WHOLE BOOK. IN THIS CASE 
	//   PROVIDE bookId, page (IN ABSOLUTE COUNT), format,
	//   fileName = null
	// - PDF BASED BOOK, WHERE SAME FILE IS SHARE ACROSS DIFFERENT
	//   READINGS, AND A PAGE COULD BE SHARED BY CONSECUTIVE READINGS. 
	//   PROVIDE bookId = null, page, fileName, format=pdf
	public ArrayList<String> getAllRadingForAPage(String bookId, int page, 
			String fileUrl, String format) {
		ArrayList<String> r = new ArrayList<String>();
		String query = "";
		if(Reading.isImage(format)){
			query = "SELECT readingid FROM ent_reading "
					+ " WHERE bookid = '"+bookId+"' AND "+page+" >= spage AND "+page+" <= epage;";
		}else{
			query = "SELECT readingid FROM ent_reading "
					+ " WHERE url = '"+fileUrl+"' AND "+page+" >= spage AND "+page+" <= epage;";
		}
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				r.add(rs.getString("readingid").trim());
			}
			
		}
		catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		finally {
			this.releaseStatement(stmt, rs);
		}
		return r;
	}

	public ArrayList<Annotation> getAnnotations(String fileUrl, int page, String usr, String grp) {
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		ArrayList<Annotation> replies = new ArrayList<Annotation>();
		String query = "SELECT id, annotationtype, userid, groupid, sessionid, page, position, text, parentid, "
				+ "fileurl, created, updated, quote, consumer, tags, permissions, links ";
		query 	    += "FROM ent_annotation WHERE ";
		query		+= " (fileurl = '"+fileUrl+"' OR parentid in (select id from ent_annotation WHERE fileurl = '"+fileUrl+"'))";
//		if(usr != null && grp != null){
//			query 	    += " AND (userid = '"+usr+"' OR groupid = '"+grp+"')";
//		}else if(usr != null){
//			query 	    += " AND userid = '"+usr+"'";
//		}else if(grp != null){
//			query 	    += " AND groupid = '"+grp+"'";
//		}
		if(usr != null){
			query 	    += " AND userid = '"+usr+"'";
		}
		if(grp != null){
			query 	    += " AND groupid = '"+grp+"'";
		}
		if(page > 0){
			query 	    += " AND page = "+page;
		}
		query 	    += " ORDER BY parentid, page, created ASC;"; // will get first non reply annotations
		
		// @@@@
		//System.out.println(query);
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				String type = rs.getString("annotationtype");
				Annotation a;
				if (type.equalsIgnoreCase("reply")){ // if it is a reply, add to the corresponding annotation
					a = new Annotation(rs.getLong("id"), type, "",
							"", -1, rs.getString("text"), rs.getString("userid"), rs.getString("groupid"), rs.getString("sessionid"),
							rs.getString("created"), rs.getString("updated"), "",
							rs.getString("tags"), rs.getString("links"), rs.getString("consumer"),
							rs.getString("permissions"));
					
					/*
	Annotation(long id, String annotationType, String fileUrl,
			String position, String text, String usr, String grp,
			String createdDate, String updatedDate, String quote, String tags, 
			String links, String consumer, String permissions) 
					 */
					long parentId = rs.getLong("parentid");
					for(Annotation parent : annotations){
						if(parent.getId() == parentId){
							parent.replies.add(a);
						}
					}
				}else{
					a = new Annotation(rs.getLong("id"), type, rs.getString("fileurl"),
							rs.getString("position"), rs.getInt("page"), rs.getString("text"), rs.getString("userid"), rs.getString("groupid"), rs.getString("sessionid"),
							rs.getString("created"), rs.getString("updated"), rs.getString("quote"),
							rs.getString("tags"), rs.getString("links"),rs.getString("consumer"),
							rs.getString("permissions"));
					annotations.add(a);
				}
			}
		}
		catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			ex.printStackTrace();
		}
		finally {
			this.releaseStatement(stmt, rs);
		}
		return annotations;
	}

	
	// Insert the track action. ReadingIds should contain all reading that has the page 
	// where the action is tracked.
    public boolean insertTrackAction(String usr, String grp, String sid,
            String bookId, String readingIds, String fileUrl, int page, 
            String actionType, String action, String comment) {
        String query = "";
        try {
            stmt = conn.createStatement();
            query = "INSERT INTO ent_tracking (actiondate, userid, groupid, sessionid, bookid, readingids, fileurl, page, actiontype, action, comment) values ("
                    + "now(), '" + usr + "','" + grp + "','" + sid + "','" 
                    + bookId + "','" + readingIds + "','" + fileUrl + "'," + page + ",'" 
                    + actionType + "','" + action + "','" + comment + "');";
            //System.out.println(query);
            stmt.executeUpdate(query);
            
            this.releaseStatement(stmt, rs);
            // System.out.println(query);
            return true;    
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            releaseStatement(stmt, rs);
            return false;
        }
        
    }

    
    public long insertAnnotation(String annotationType, String usr, String grp, String sid,
            String readingIds, String fileUrl, int page, String position, String text, int parentAnnotationId,
            String quote, String consumer, String permissions, String tags, String links) {
        String query = "";
        long key = -1;
        try {
            //stmt = conn.createStatement();
            query = "INSERT INTO ent_annotation (annotationtype,userid,groupid,sessionid,readingids,fileurl,page,position,text,parentid,created,updated,quote,consumer,permissions,tags,links) values ("
                    + "'" + annotationType + "','" + usr + "','" + grp + "','" + sid + "','"
                    + readingIds + "','" + fileUrl + "'," + page + ",'" + position + "','" 
                    + text + "'," + parentAnnotationId + ",now(), now(),"
                    + "'" + quote+ "','"+ consumer+ "','" + permissions + "','"+ tags+ "','" + links +"');";
            //System.out.println(query);
            
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            
            stmt.executeUpdate(query);
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                key = generatedKeys.getLong(1);
            }            
            this.releaseStatement(stmt, rs);
            return key;    
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            releaseStatement(stmt, rs);
            return -1;
        }
        
    }

    public boolean updateAnnotation(int annotationId, String position, String text) {
        String query = "";
        try {
            stmt = conn.createStatement();
            query = "UPDATE ent_annotation SET ";
            if(position != null && position.length()>0) query +=  " position = '" + position + "' AND ";
            query +=  " text = '" + text + "'";
            query +=  " WHERE id = "+annotationId+";";
            
            //System.out.println(query);
            stmt.executeUpdate(query);
            
            this.releaseStatement(stmt, rs);
            // System.out.println(query);
            return true;    
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            releaseStatement(stmt, rs);
            return false;
        }
        
    }

	/*
	public HashMap<String, User> getActivity(String grp, String[] non_students,
			String[] non_sessions, HashMap<String, String> topic_map,
			HashMap<String, String> activityname_map) {

		String nonStudents = csvFromArray(non_students);
		String nonSessions = csvFromArray(non_sessions);

		try {
			HashMap<String, User> res = new HashMap<String, User>();
			stmt = conn.createStatement();
			// String query =
			// "SELECT AppId, UserId, ActivityId, Result, `Session`, DateNTime, DateNTimeNS,SVC, AllParameters FROM archive_user_activity WHERE GroupId = (select userid from ent_user where login='"+grp+"');";
			String query = "SELECT UA.AppId, UA.UserId, U.Login, UA.ActivityId, UA.Result, UA.`Session`, UA.DateNTime, UA.DateNTimeNS, UA.SVC, UA.AllParameters "
					// +
					// " FROM ent_user U, archive_user_activity UA left join rel_activity_activity RAA on (RAA.ChildActivityId = UA.ActivityId or RAA.ParentActivityId = UA.ActivityId) "
					+ " FROM ent_user U, archive_user_activity UA "
					+ " WHERE "
					+ " GroupId = (select userid from ent_user where login='"
					+ grp
					+ "') " + " and U.UserId = UA.UserId ";
			if (nonStudents != null)
				query += " and U.Login not in (" + nonStudents + ") ";
			if (nonSessions != null)
				query += " and UA.`Session` not in (" + nonSessions + ") ";
			// get rid of all sessions that look like 'test'
			query += " and UA.`Session` not like '%TEST%' and UA.`Session` not like '%test%'";
			// admins out of the picture
			query += " and UA.UserId not in (SELECT userId from rel_user_user WHERE GroupId = 68) ";
			query += " and UA.AllParameters not like '%usr=undefined%' and UA.AllParameters not like '%sid=undefined%' ";
			query += " order by UA.UserId, UA.DateNTime asc;";
			rs = stmt.executeQuery(query);
			System.out.println("UM QUERY:\n    " + query);
			// String content_name = "";
			// ArrayList<String[]> c_c = null;
			User currentUser = null;
			int user = -1;
			String login = null;
			int count = 0;
			Set<String> activityWithNullTopic = new HashSet<String>();
			while (rs.next()) {
				count++;
				user = rs.getInt("UserId");
				login = rs.getString("Login");
				// first user in the logs
				if (currentUser == null)
					currentUser = new User(user, login);
				// when detecting a new user, add the current user to 'res' and create
				// another user object
				if (currentUser.getUserId() != user) {
					res.put(currentUser.getUserLogin(), currentUser);
					currentUser = new User(user, login);
				}
				int appId = rs.getInt("AppId");
				String allParameters = rs.getString("AllParameters");
				String activityName = "";
				String parentName = "";
				String topicName = "";
				// map
				// for WEBEX and ANIMATED_EXAMPLES
				// (3) activityName (line clicked) and parentName (example) are the act
				// and sub in AllParameters
				// for QUIZJET (25) activityName (question) the sub parameter in
				// AllParameters. activityParent does not exist
				if (appId == 3 || appId == 25 || appId == 35 || appId == 8) {
					// if( false ){
					String[] all_params = allParameters.split(";");
					if (all_params != null) {
						for (String _p : all_params) {
							if (_p.length() > 4) {
								// System.out.println(label+' '+_p);
								String param = _p.trim().substring(0, 3);
								String value = _p.trim().substring(4);
								if (param.equalsIgnoreCase("act")) {
									switch (appId) {
									case 3:
									case 35: // WEBEX and AE the act is the example
										parentName = value;
										if (topic_map != null && topic_map.containsKey(value)) {
											topicName = topic_map.get(value);
										}
										break;
									case 25: // QUIZJET has the topic in the act parameter
										if (topicName.length() == 0) {
											// e.g. act=9198
											if (activityname_map.containsKey(value)) {
												activityName = activityname_map.get(value);
												parentName = activityName; // hy added
												if (topic_map != null
														&& topic_map.containsKey(activityName))
													topicName = topic_map.get(activityName);
											}
											else
												topicName = value;
										}
										break;
									case 8: // KT has the activity in the act
										activityName = value;
										break;
									}

								}
								else if (param.equalsIgnoreCase("sub")) {
									// System.out.println("  sub: "+value+ " !!!!!");
									switch (appId) {
									case 3:
									case 35: // WEBEX and AE the sub is the line
										activityName = value;
										break;
									case 25: // QUIZJET report the activity in sub
										if (activityName.length() == 0) {
											activityName = value;
											parentName = activityName; // hy added
											if (topicName.length() == 0 && topic_map != null
													&& topic_map.containsKey(activityName))
												topicName = topic_map.get(activityName);
										}
										break;
									case 8: // KT has the activity in the act
										break;
									}
								}
							}

						}
					}
				}
				if (topicName.equals("null") || topicName.equals("")
						|| topicName.length() == 0) {
					activityWithNullTopic.add(parentName);
				}
				LoggedActivity act = new LoggedActivity(rs.getInt("AppId"),
						rs.getString("Session"), rs.getInt("ActivityId"), activityName,
						activityName, parentName, topicName, rs.getDouble("Result"),
						rs.getString("DateNTime"), rs.getLong("DateNTimeNS"),
						rs.getString("SVC"), allParameters);
				// (int appId, String session, String label,
				// int activityId, int parent, double result, Date date, long dateNS,
				// String svc, String allParameters)
				currentUser.addLoggedActivity(act);
			}
			if (currentUser != null)
				res.put(login, currentUser);
			this.releaseStatement(stmt, rs);
			System.out.println("#activities=" + count);
			System.out.println("activities with null topic:");
			for (String s : activityWithNullTopic) {
				System.out.print(s + ",");
			}
			System.out.println();
			return res;
		}
		catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt, rs);
			return null;
		}
		catch (Exception ex) {
			System.out.println("Exception while getting activities from UM2: "
					+ ex.getMessage());
			this.releaseStatement(stmt, rs);
			return null;
		}
	}

	// returns the user information given the username
	public String[] getUsrInfo(String usr) {
		try {
			String[] res = null;
			stmt = conn.createStatement();
			String query = "select U.name, U.email from ent_user U where U.login = '"
					+ usr + "';";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				res = new String[2];
				res[0] = "";
				res[1] = "";
				res[0] = rs.getString("name").trim();
				res[1] = rs.getString("email").trim();
			}
			return res;
		}
		catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			return null;
		}
		finally {
			this.releaseStatement(stmt, rs);
		}

	}

	// returns the activity of the user in content of type example
	// TODO animated examples support @@@@
	public HashMap<String, String[]> getUserExamplesActivity(String usr) {
		try {
			HashMap<String, String[]> res = new HashMap<String, String[]>();
			stmt = conn.createStatement();
			String query = "select A.activity, "
					+ "AA.parentactivityid, count(UA.activityid) as nactions,  "
					+ "count(distinct(UA.activityid)) as distinctactions, "
					+ "(select count(AA2.childactivityid) from rel_activity_activity AA2 where AA2.parentactivityid = AA.parentactivityid) as totallines "
					+ "from archive_user_activity UA, rel_activity_activity AA, ent_activity A "
					+ " where (UA.appid=3 OR UA.appid=35) and UA.userid = (select userid from ent_user where login='"
					+ usr
					+ "') "
					+ " and AA.parentactivityid=A.activityid and AA.childactivityid=UA.activityid "
					+ "group by AA.parentactivityid " + "order by AA.parentactivityid;";
			// System.out.println(query);
			rs = stmt.executeQuery(query);
			// System.out.println(query);

			boolean noactivity = true;
			while (rs.next()) {
				noactivity = false;
				String[] act = new String[4];
				act[0] = rs.getString("activity");
				act[1] = rs.getString("nactions");
				act[2] = rs.getString("distinctactions");
				act[3] = rs.getString("totallines");
				res.put(act[0], act);
				// System.out.println(act[0]+" actions: "+act[2]+", "+act[3]+"/"+act[4]);
			}
			this.releaseStatement(stmt, rs);
			if (noactivity)
				return null;
			else
				return res;
		}
		catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			return null;
		}
		finally {
			this.releaseStatement(stmt, rs);
		}

	}

	public HashMap<String, String[]> getUserQuestionsActivity(String usr) {
		try {
			HashMap<String, String[]> res = new HashMap<String, String[]>();
			stmt = conn.createStatement();
			String query = "(select AC.activity, count(UA.activityid) as nattempts,  sum(UA.Result) as nsuccess from um2.archive_user_activity UA, um2.ent_activity AC where UA.appid=25 and UA.userid = (select userid from um2.ent_user where login='"
					+ usr
					+ "') and AC.activityid=UA.activityid and UA.Result != -1 group by UA.activityid) \n";
			query += " UNION ALL \n";
			query += "(select QN.content_name as activity, count(UA.activityid) as nattempts,  sum(UA.Result) as nsuccess "
					+ " from um2.archive_user_activity UA, um2.sql_question_names QN where UA.appid=23 and "
					+ " UA.userid = (select userid from um2.ent_user where login='"
					+ usr
					+ "') and "
					+ " QN.activityid=UA.activityid and UA.Result != -1  "
					+ " group by UA.activityid); ";

			// System.out.println(query);
			rs = stmt.executeQuery(query);
			boolean noactivity = true;
			while (rs.next()) {
				noactivity = false;
				String[] act = new String[3];
				act[0] = rs.getString("activity");
				act[1] = rs.getString("nattempts");
				act[2] = rs.getString("nsuccess");
				if (act[0].length() > 0)
					res.put(act[0], act);
			}
			this.releaseStatement(stmt, rs);
			if (noactivity)
				return null;
			else
				return res;
		}
		catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			return null;
		}
		finally {
			this.releaseStatement(stmt, rs);
		}
	}

	public ArrayList<String[]> getClassList(String grp) {

		try {
			ArrayList<String[]> res = new ArrayList<String[]>();
			stmt = conn.createStatement();
			String query = "select U.userid, U.login, U.name, U.email "
					+ "from ent_user U, rel_user_user UU "
					+ "where UU.groupid = (select userid from ent_user where login='"
					+ grp + "' and isgroup=1) " + "and U.userid=UU.userid";
			// System.out.println(query);
			rs = stmt.executeQuery(query);
			int i = 0;
			while (rs.next()) {
				String[] act = new String[3];
				act[0] = rs.getString("login");
				act[1] = rs.getString("name").trim();
				act[2] = rs.getString("email").trim();
				res.add(act);
				// System.out.println(act[0]+" "+act[1]+" "+act[2]+" "+act[3]);
				i++;
			}
			this.releaseStatement(stmt, rs);
			return res;
		}
		catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			return null;
		}
		finally {
			this.releaseStatement(stmt, rs);
		}
	}

	// get a list with the content and for each content item, all the concepts in
	// an array list with
	public ArrayList<String[]> getContentConcepts(String domain) {
		try {
			// HashMap<String, ArrayList<String[]>> res = new HashMap<String,
			// ArrayList<String[]>>();
			ArrayList<String[]> res = new ArrayList<String[]>();
			stmt = conn.createStatement();
			String query = "SELECT CC.content_name, "
					+ " group_concat(CC.concept_name , ',', cast(CONVERT(CC.weight,DECIMAL(10,3)) as char ), ',' , cast(CC.direction as char) order by CC.weight separator ';') as concepts "
					+ " FROM agg_content_concept CC  " + " WHERE CC.domain = '" + domain
					+ "'" + " group by CC.content_name order by CC.content_name;";
			rs = stmt.executeQuery(query);
			// System.out.println(query);
			// String content_name = "";
			// ArrayList<String[]> c_c = null;
			while (rs.next()) {
				String[] data = new String[2];
				data[0] = rs.getString("content_name");
				data[1] = rs.getString("concepts");

				res.add(data);
			}
			this.releaseStatement(stmt, rs);
			return res;
		}
		catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			this.releaseStatement(stmt, rs);
			return null;
		}
	}

	public static String csvFromArray(String[] values) {
		String res = "";
		if (values != null && values.length > 0) {
			for (String v : values) {
				res += "'" + v + "',";
			}
			res = res.substring(0, res.length() - 1);
		}
		else
			return null;
		return res;
	}
*/

}
