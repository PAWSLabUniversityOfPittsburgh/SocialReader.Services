
public class Question {
	private int id;
	private String title;
	private String text;
	private String type;
	private String[] options;
	private int[] correctOptions;
	
	private int[] answerChoices;
	private double score;
	private String answerDate;
	
	public Question(int id, String type, String title, String text, String options, String correctOptions) {
		super();
		this.id = id;
		this.title = title;
		this.type = type;
		this.text = text;
		this.options = options.split(";");
		String[] temp = correctOptions.split(";");
		this.correctOptions = new int[temp.length];
		for(int i=0; i<temp.length;i++) this.correctOptions[i] = Integer.parseInt(temp[i]);
		
	}
	
	public String jsonFormat(){
		String json = "{\"id\":\""+id+"\",\"title\":\""+title+"\",\"text\":\""+text+"\",\"type\":\""+type+"\""+
							"\"options\":[";
		int i = 1;
		for(String o : options){
			json += "{\"" + i + "\":\"" + o + "\", \"correct\":"+Common.contains(correctOptions, i)+"},";
			i++;
		}
		json = json.substring(0, json.length()-1);
		json += "]";
		
		if(answerChoices != null && answerChoices.length > 0){
			json += ",\"user-answer\":{\"score\":"+score+",\"answer-date\":\""+answerDate+"\",\"answer-choices\":[";
			for(int c : answerChoices){
				json += "\"" + c + "\",";
				i++;
			}
			json = json.substring(0, json.length()-1);
			json += "]}";
		}
		
		
		return json;
	}

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String[] getOptions() {
		return options;
	}

	public void setOptions(String[] options) {
		this.options = options;
	}

	public int[] getCorrectOptions() {
		return correctOptions;
	}

	public void setCorrectOptions(int[] correctOptions) {
		this.correctOptions = correctOptions;
	}

	public int[] getAnswerChoices() {
		return answerChoices;
	}

	public void setAnswerChoices(int[] answerChoices) {
		this.answerChoices = answerChoices;
	}
	
	public void setAnswerChoices(String answerChoices) {
		String[] temp = answerChoices.split(";");
		this.answerChoices = new int[temp.length];
		for(int i=0; i<temp.length;i++) this.answerChoices[i] = Integer.parseInt(temp[i]);

	}

	
	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getAnswerDate() {
		return answerDate;
	}

	public void setAnswerDate(String answerDate) {
		this.answerDate = answerDate;
	}
	
	
	
}
