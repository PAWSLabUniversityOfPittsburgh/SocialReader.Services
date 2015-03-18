import java.sql.SQLException;


public class RCDBInterface extends DBInterface {
	public RCDBInterface(String connurl, String user, String pass) {
		super(connurl, user, pass);
	}
	
//	public Structure getCourseInfo(String readingId) {
//		try {
//			Reading r = null;
//			stmt = conn.createStatement();
//			String query = "SELECT C.courseid, C.domain, C.title FROM rc_course C, groups G where G.grp='".$grp."' and G.courseid=C.courseid;";
//			rs = stmt.executeQuery(query);
//			while (rs.next()) {
//				r = new Reading( rs.getString("readingid").trim(),
//						rs.getString("title").trim(),
//						rs.getString("bookid").trim(),
//						rs.getString("authors").trim(),
//						rs.getInt("spage"),
//						rs.getInt("epage"),
//						rs.getString("url").trim(),
//						rs.getString("files").trim(),
//						rs.getString("format").trim(),
//						rs.getString("prevreading").trim(),
//						rs.getString("nextreading").trim());
//			}
//			return r;
//		}
//		catch (SQLException ex) {
//			System.out.println("SQLException: " + ex.getMessage());
//			System.out.println("SQLState: " + ex.getSQLState());
//			System.out.println("VendorError: " + ex.getErrorCode());
//			return null;
//		}
//		finally {
//			this.releaseStatement(stmt, rs);
//		}
//
//	}
}
