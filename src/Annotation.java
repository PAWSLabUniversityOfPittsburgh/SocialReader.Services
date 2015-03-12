import java.util.ArrayList;


public class Annotation {
	private long id;
	private String annotationType;
	private String fileUrl;
	private String position;
	private String text;
	private String usr;
	private String grp;
	private String sid;
	
	private String created;
	private String updated;
	private String quote;
	private String tags;
	private String links;
	private String consumer;
	private String permissions;
	private int page;
	
	public ArrayList<Annotation> replies;
	public Annotation(long id, String annotationType, String fileUrl,
			String position, int page, String text, String usr, String grp, String sid, 
			String createdDate, String updatedDate, String quote, String tags, 
			String links, String consumer, String permissions) {
		super();
		this.id = id;
		this.annotationType = annotationType;
		this.fileUrl = (fileUrl != null && !fileUrl.equalsIgnoreCase("null")?fileUrl:"");
		this.position = (position != null && !position.equalsIgnoreCase("null")?position:"");
		this.page = page;
		this.text = text;
		this.usr = usr;
		this.grp = grp;
		this.sid = sid;
		this.created = createdDate;
		this.updated = (updatedDate != null && !updatedDate.equalsIgnoreCase("null")?updatedDate:"");
		this.quote = (quote != null && !quote.equalsIgnoreCase("null")?quote:"");
		this.tags = (tags != null && !tags.equalsIgnoreCase("null")?tags:"");
		this.links = (links != null && !links.equalsIgnoreCase("null")?links:"");
		this.consumer = (consumer != null && !consumer.equalsIgnoreCase("null")?consumer:"");
		this.permissions = (permissions != null && !permissions.equalsIgnoreCase("null")?permissions:"");
		replies = new ArrayList<Annotation>();
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getAnnotationType() {
		return annotationType;
	}
	public void setAnnotationType(String annotationType) {
		this.annotationType = annotationType;
	}
	public String getFileUrl() {
		return fileUrl;
	}
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	public String getPosition() {
		return position;
	}

	public String getHTMLPositions(String offset) {
		String r = "";
		String[] ranges = position.split(",");
		if(ranges.length > 3){
			for(int i=0; i<ranges.length;i++){
				if(i%4 == 0) r += "\n"+offset+"{\n"+offset+"  \"start\":\""+ranges[i]+"\",";
				if(i%4 == 1) r += "\n"+offset+"  \"end\":\""+ranges[i]+"\",";
				if(i%4 == 2) r += "\n"+offset+"  \"startOffset\":\""+ranges[i]+"\",";
				if(i%4 == 3) r += "\n"+offset+"  \"endOffset\":\""+ranges[i]+"\n"+offset+"},";
			}
		}
		r = r.substring(0,r.length()-1);
		return r;
	}
	
	
	public void setPosition(String position) {
		this.position = position;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getUsr() {
		return usr;
	}
	public void setUsr(String usr) {
		this.usr = usr;
	}
	public String getGrp() {
		return grp;
	}
	public void setGrp(String grp) {
		this.grp = grp;
	}
	
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public String getCreated() {
		return created;
	}
	public void setCreated(String created) {
		this.created = created;
	}
	public String getUpdated() {
		return updated;
	}
	public void setUpdated(String updated) {
		this.updated = updated;
	}
	public String getQuote() {
		return quote;
	}
	public void setQuote(String quote) {
		this.quote = quote;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public String getLinks() {
		return links;
	}
	public void setLinks(String links) {
		this.links = links;
	}
	public String getConsumer() {
		return consumer;
	}
	public void setConsumer(String consumer) {
		this.consumer = consumer;
	}
	public String getPermissions() {
		return permissions;
	}
	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}
	public ArrayList<Annotation> getReplies() {
		return replies;
	}
	public void setReplies(ArrayList<Annotation> replies) {
		this.replies = replies;
	}
	
	
	public String jsonFormat(){
		String json = "{";
		
		json += "\"annotationid\":"+getId()+",";
		json += "\"annotationtype\":\""+getAnnotationType()+"\",";
		json += "\"fileurl\":\""+getFileUrl()+"\",";
		json += "\"position\":\""+getPosition()+"\",";
		json += "\"userid\":\""+getUsr()+"\",";
		json += "\"groupid\":\""+getGrp()+"\",";
		json += "\"datetime\":\""+getCreated()+"\",";
		json += "\"replies\":[ \n";
		for(Annotation r : replies){
			json += "    "+r.jsonFormat()+",\n";
		}
		json = json.substring(0, json.length()-2);
		if(replies.size()>0) json += "\n  ";
		json += "],";
		json += "\n      \"text\":\""+getText()+"\"}";
		return json;
	}

	public String jsonAnnotatorJS(String offset){
		String json = offset+"{\n";
		
		json += offset+"  \"id\":"+getId()+",\n";
		json += offset+"  \"uri\":\""+getFileUrl()+"\",\n";
		json += offset+"  \"annotationtype\":\""+getAnnotationType()+"\",\n";
		json += offset+"  \"page\":"+getPage()+",\n";
		json += offset+"  \"usr\":\""+getUsr()+"\",\n";
		json += offset+"  \"grp\":\""+getGrp()+"\",\n";
		json += offset+"  \"sid\":\""+getSid()+"\",\n";
		json += offset+"  \"created\":\""+getCreated()+"\",\n";
		json += offset+"  \"updated\":\""+getUpdated()+"\",\n";
		json += offset+"  \"consumer\":\""+getConsumer()+"\",\n";
		
		if(getPermissions() != null && getPermissions().length()>1 && getPermissions().charAt(0) == '{')
			json += offset+"  \"permissions\":"+getPermissions()+",\n";
		else
			json += offset+"  \"permissions\":{"+getPermissions()+"},\n";
		
		if(getTags() != null && getTags().length()>1 && getTags().charAt(0) == '[')
			json += offset+"  \"tags\":"+getTags()+",\n";
		else
			json += offset+"  \"tags\":["+getTags()+"],\n";	
		
		if(getLinks() != null && getLinks().length()>1 && getLinks().charAt(0) == '[')
			json += offset+"  \"links\":"+getLinks()+",\n";
		else
			json += offset+"  \"links\":["+getLinks()+"],\n";
		
		//json += "\"position\":\""+getPosition()+"\",";
//		if(getAnnotationType().equalsIgnoreCase("inhtmltext")){
//			json += offset+"  \"ranges\":[\n"+getHTMLPositions(offset+"  ")+"\n"+offset+"  ],";
//		}else{
//			json += "\"ranges\":\""+getPosition()+"\",";	
//		}
		if(getPosition() != null && getPosition().length()>1 && getPosition().charAt(0) == '[')
			json += offset+"  \"ranges\":"+getPosition()+",\n";
		else
			json += offset+"  \"ranges\":["+getPosition()+"],\n";	
		
		
		json += offset+"  \"replies\":[ \n";
		for(Annotation r : replies){
			json += offset+"    "+r.jsonAnnotatorJS(offset+"    ")+",\n";
		}
		json = json.substring(0, json.length()-2);
		if(replies.size()>0) json += offset+"  ],";
		else json += "\n"+offset+"  ],";
		json += "\n"+offset+"  \"text\":\""+getText()+"\"\n"+offset+"}";
		
		return json;
	}
	
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}

	
}
