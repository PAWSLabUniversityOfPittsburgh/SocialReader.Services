import java.util.ArrayList;


public class PageActivity {
	private String fileUrl;
	private int page;
	public ArrayList<String> readingIds;
	private int pageLoads;
	private int clicks;
	private int annotations;
	private int likes;
	private int scrolls;
	private double time;
	
	public PageActivity(String fileUrl, int page) {
		super();
		this.fileUrl = fileUrl;
		this.page = page;
		this.readingIds = new ArrayList<String>();
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public ArrayList<String> getReadingIds() {
		return readingIds;
	}

	public void setReadingIds(ArrayList<String> readingIds) {
		this.readingIds = readingIds;
	}

	public int getPageLoads() {
		return pageLoads;
	}

	public void setPageLoads(int pageLoads) {
		this.pageLoads = pageLoads;
	}

	public int getClicks() {
		return clicks;
	}

	public void setClicks(int clicks) {
		this.clicks = clicks;
	}

	public int getAnnotations() {
		return annotations;
	}

	public void setAnnotations(int annotations) {
		this.annotations = annotations;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public int getScrolls() {
		return scrolls;
	}

	public void setScrolls(int scrolls) {
		this.scrolls = scrolls;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}
	
	public void parseReadingIds(String readingIds){
		String[] ids = readingIds.split("]");
		for(int i =0; i<ids.length;i++){
			ids[i] = ids[i].replaceAll("\\[", "").replaceAll("\\]", "");
			this.readingIds.add(ids[i]);
		}
		
	}
	
}
