import java.io.InputStream;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;


public class ConfigManager {
	public String dbstring;
	public String dbuser;
	public String dbpass;
	
	private static String config_string = "./WEB-INF/config.xml";
	
	public ConfigManager(HttpServlet servlet){
		try{
			ServletContext context = servlet.getServletContext();
			//System.out.println(context.getContextPath());
			InputStream input = context.getResourceAsStream(config_string);
			if (input != null){
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(input);
				doc.getDocumentElement().normalize();

				dbstring = doc.getElementsByTagName("dbstring").item(0).getTextContent();
				dbuser = doc.getElementsByTagName("dbuser").item(0).getTextContent();
				dbpass = doc.getElementsByTagName("dbpass").item(0).getTextContent();
				
			}else{
				System.out.println("config not found!");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
