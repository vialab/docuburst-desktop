package net.didion.jwnl.princeton.file;

import java.util.HashMap;

public class FileLengths {

	static HashMap<String, Integer> map = new HashMap<String, Integer>();
	
	static {
		map.put("index.adv2.1",167160);
		map.put("index.adj2.1",850570);
		map.put("index.noun2.1",4751490);
		map.put("index.verb2.1",521464);
		map.put("adv.exc2.1",85);
		map.put("adj.exc2.1",18954);
		map.put("noun.exc2.1",27644);
		map.put("verb.exc2.1",30744);
		map.put("exc.adv2.1",85);
		map.put("exc.adj2.1",18954);
		map.put("exc.noun2.1",27644);
		map.put("exc.verb2.1",30744);
		map.put("data.adv2.1",519839);
		map.put("data.adj2.1",3239918);
		map.put("data.noun2.1",15099702);
		map.put("data.verb2.1",2746988);
		
		map.put("index.adv3.0",162816);
		map.put("index.adj3.0",824127);
		map.put("index.noun3.0",4786655);
		map.put("index.verb3.0",523980);
		map.put("index.sense3.0",7294043);
		map.put("adv.exc3.0",85);
		map.put("adj.exc3.0",23019);
		map.put("noun.exc3.0",38301);
		map.put("verb.exc3.0",38033);
		map.put("exc.adv3.0",85);
		map.put("exc.adj3.0",23019);
		map.put("exc.noun3.0",38301);
		map.put("exc.verb3.0",38033);
		map.put("data.adv3.0",516696);
		map.put("data.adj3.0",3155426);
		map.put("data.noun3.0",15300280);
		map.put("data.verb3.0",2772517);
	}
	
	public final static int get(String filename) {
		return map.get(filename);
	}
}
