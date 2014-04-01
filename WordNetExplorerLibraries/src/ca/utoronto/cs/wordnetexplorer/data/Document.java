package ca.utoronto.cs.wordnetexplorer.data;

import java.util.ArrayList;

public class Document {

	public ArrayList<Section> sections = new ArrayList<Section>();
	
	private String title;
	
	public Document() {
		super();
		title = "";
	}
	
	public Document(String title) {
		super();
		setTitle(title);
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void addSection(Section s) {
		sections.add(s);
	}
	
	public Section getSection(int s) {
		return sections.get(s);
	}
	
	public int length() {
		return sections.size();
	}
	
	public String toXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<document title=\"");
		sb.append(title);
		sb.append("\">\n");
		for (Section s : sections) {
			sb.append(s.toXML());
		}
		sb.append("</document>");
		return sb.toString();
	}
	
}
