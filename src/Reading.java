import java.util.ArrayList;


public class Reading {
	private String readingId; 
	private String title; 
	private String bookId; 
	private String authors; 
	private int sPage; 
	private int ePage;
	private String url; 
	private String[] files; 
	private String format;
	private String prevReadingId;
	private String nextReadingId;
	public ArrayList<PageActivity> activity;
	
	private double progress;
	private double knowledge;
	private double pConfidence;
	private double kConfidence;
	private int loadCount;
	
	private int annotationCount;

	public ArrayList<Question> questions;
	
	public int getAnnotationCount() {
		return annotationCount;
	}
	public void setAnnotationCount(int annotationCount) {
		this.annotationCount = annotationCount;
	}
	public String getReadingId() {
		return readingId;
	}
	public void setReadingId(String readingId) {
		this.readingId = readingId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBookId() {
		return bookId;
	}
	public void setBookId(String bookId) {
		this.bookId = bookId;
	}
	public String getAuthors() {
		return authors;
	}
	public void setAuthors(String authors) {
		this.authors = authors;
	}
	public int getsPage() {
		return sPage;
	}
	public void setsPage(int sPage) {
		this.sPage = sPage;
	}
	public int getePage() {
		return ePage;
	}
	public void setePage(int ePage) {
		this.ePage = ePage;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String[] getImageFiles() {
		return files;
	}
	public void setImageFiles(String[] imageFiles) {
		this.files = imageFiles;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public Reading(String readingId, String title, String bookId,
			String authors, int sPage, int ePage, String url,
			String files, String format, String prev, String next) {
		super();
		this.readingId = readingId;
		this.title = title;
		this.bookId = bookId;
		this.authors = authors;
		this.sPage = sPage;
		this.ePage = ePage;
		this.url = url;
		this.files = files.split("\\|");
		this.format = format;
		this.prevReadingId = prev;
		this.nextReadingId = next;
		activity = new ArrayList<PageActivity>();
		questions = new ArrayList<Question>();
	} 

	public String jsonFormat(){
		String json = "{\n";
		
		json += "  \"readingid\":\""+getReadingId()+"\",\n";
		json += "  \"title\":\""+getTitle()+"\",\n";
		json += "  \"authors\":\""+getAuthors()+"\",\n";
		json += "  \"spage\":\""+getsPage()+"\",\n";
		json += "  \"epage\":\""+getePage()+"\",\n";		
		json += "  \"format\":\""+getFormat()+"\",\n";
		json += "  \"prev\":\""+getPrevReadingId()+"\",\n";
		json += "  \"next\":\""+getNextReadingId()+"\",\n";
		
//		if(getFormat().equalsIgnoreCase("jpg") || getFormat().equalsIgnoreCase("png") 
//				|| getFormat().equalsIgnoreCase("jpeg") || getFormat().equalsIgnoreCase("gif")  
//				|| getFormat().equalsIgnoreCase("img") || getFormat().equalsIgnoreCase("image")){
//			if(imageFiles != null){
//				json += "  \"urls\":[ \n";
//				for(String imgFile : imageFiles){
//					json += "    \""+getUrl()+imgFile+"\",\n";
//
//				}
//				json = json.substring(0, json.length()-2);
//				json += "\n  ]\n";
//				
//			}else{
//				json += "  \"urls\":[]";
//			}
//			
//		}else{
//			json += "  \"urls\":[\""+getUrl()+"\"]";
//		}
		if(files != null){
			json += "  \"urls\":[ \n";
			for(String file : files){
				
				json += "    \""+(getUrl()+file.trim()).trim()+"\",\n";

			}
			json = json.substring(0, json.length()-2);
			json += "\n  ],\n";

		}else{
			json += "  \"urls\":[\""+getUrl()+"\"],\n";
		}
		
		json += "  \"questions\":[ \n";
		if(questions != null){
			for(Question q : questions){
				json += q.jsonFormat() + ",\n";
			}
		}
		json = json.substring(0, json.length()-2);
		json += "\n  ]";
		
		json += "\n}";
		
		return json;
	}
	
	public String getPrevReadingId() {
		return prevReadingId;
	}
	public void setPrevReadingId(String prevReadingId) {
		this.prevReadingId = prevReadingId;
	}
	public String getNextReadingId() {
		return nextReadingId;
	}
	public void setNextReadingId(String nextReadingId) {
		this.nextReadingId = nextReadingId;
	}

	public static boolean isImage(String format){
		return format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("png") 
				|| format.equalsIgnoreCase("jpeg") || format.equalsIgnoreCase("gif")  
				|| format.equalsIgnoreCase("img") || format.equalsIgnoreCase("image");
	}
	
	public ArrayList<String> GetFileUrls(){
		ArrayList<String> r = new ArrayList<String>();
		if(files != null){			
			for(String file : files){
				r.add((getUrl()+file.trim()).trim());
			}
		}
		return r;
	}
	
	
	
	public double getProgress() {
		return progress;
	}
	public void setProgress(double progress) {
		this.progress = progress;
	}
	public double getKnowledge() {
		return knowledge;
	}
	public void setKnowledge(double knowledge) {
		this.knowledge = knowledge;
	}
	public double getPConfidence() {
		return pConfidence;
	}
	public void setPConfidence(double pConfidence) {
		this.pConfidence = pConfidence;
	}
	public double getKConfidence() {
		return kConfidence;
	}
	public void setKConfidence(double kConfidence) {
		this.kConfidence = kConfidence;
	}
	
	
	
	public String[] getFiles() {
		return files;
	}
	public void setFiles(String[] files) {
		this.files = files;
	}
	public double getpConfidence() {
		return pConfidence;
	}
	public void setpConfidence(double pConfidence) {
		this.pConfidence = pConfidence;
	}
	public double getkConfidence() {
		return kConfidence;
	}
	public void setkConfidence(double kConfidence) {
		this.kConfidence = kConfidence;
	}
	
	public int getLoadCount() {
		return loadCount;
	}
	public void setLoadCount(int loadCount) {
		this.loadCount = loadCount;
	}
	
	public void computeSimpleProgress(){
		double p = 0.0;
		double pConfidence = 0.0;
		int nPages = ePage - sPage;
		if(nPages <= 0) nPages = 1;
		int pagesRead = 0;
		for(PageActivity pA : activity){
			if(pA.getPageLoads()>0) pagesRead++;
			if(pA.getPageLoads()>2) pConfidence += (1.0/2)*(1.0/nPages);
			if(pA.getClicks()>3) pConfidence += (1.0/4)*(1.0/nPages);
			if(pA.getScrolls()>5) pConfidence += (1.0/4)*(1.0/nPages);
		}
		loadCount = pagesRead;
		this.setProgress(1.0*pagesRead/nPages);
		this.setPConfidence(pConfidence);
		
	}
	

}
