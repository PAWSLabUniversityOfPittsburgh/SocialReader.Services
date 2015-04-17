import java.util.ArrayList;


public class Node {
	public ArrayList<Node> children;
	public Node parent;
	public Reading reading;
	
	private double aggProgress;
	private double aggKnowledge;
	private double aggPConfidence;
	private double aggKConfidence;
	
	public Node(){
		children = new ArrayList<Node>();
		parent = null;
	}
	
	public Node(Reading r, Node parent){
		this.reading = r;
		this.children = new ArrayList<Node>();
		this.parent = parent;
	}
	
	public double getAggProgress() {
		return aggProgress;
	}
	public void setAggProgress(double aggProgress) {
		this.aggProgress = aggProgress;
	}
	public double getAggKnowledge() {
		return aggKnowledge;
	}
	public void setAggKnowledge(double aggKnowledge) {
		this.aggKnowledge = aggKnowledge;
	}
	public double getAggPConfidence() {
		return aggPConfidence;
	}
	public void setAggPConfidence(double aggPConfidence) {
		this.aggPConfidence = aggPConfidence;
	}
	public double getAggKConfidence() {
		return aggKConfidence;
	}
	public void setAggKConfidence(double aggKConfidence) {
		this.aggKConfidence = aggKConfidence;
	}
}
