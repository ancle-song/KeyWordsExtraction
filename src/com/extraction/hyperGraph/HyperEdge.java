package com.extraction.hyperGraph;

import java.util.ArrayList;
import java.util.List;

public class HyperEdge {
	
	private List<String> elements = new ArrayList<String>() ;

	private double edgeWeight ;
	
	public HyperEdge(){
		
	}
	
	public HyperEdge(List<String> words){
		for(String word:words){
			this.elements.add(word);
		}
	}
	
	public HyperEdge(List<String> words ,double edgeWeight){
		this.edgeWeight = edgeWeight+1 ;
		for(String word:words){
			this.elements.add(word);
		}
	}
	
	int getEdgesize(){
		return elements.size();
	}
	
	double getWeight(){
		return edgeWeight ;
	}
	
	public List<String> getElements() {
		return elements;
	}

}
