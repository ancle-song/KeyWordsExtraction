package com.extraction.hyperGraph;

import java.util.ArrayList;
import java.util.List;

public class Vertex {
	
	String word ;
	double weight = 1 ;
	List<HyperEdge> edges = new ArrayList<HyperEdge>();
	
	public Vertex(){
		
	}
	
	public Vertex(String word){
		this.word = word ;
	}
	
	public void addEdge(HyperEdge edge){
		this.edges.add(edge);
	}
	
	public List<HyperEdge> getEdges() {
		return edges;
	}
}
