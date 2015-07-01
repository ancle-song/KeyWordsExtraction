package com.extraction.Information;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.extraction.until.FileUntil;

public class Weibo_info {

	int id;
	String content;
	String text;// text是微博原文本中取出链接等之后的文本
	// String hashTag ;
	List<String> phrase_list = new ArrayList<String>() ;
	int retreet;
	int comment;
	String time;
	long timeStamp;
	String url;

	public Weibo_info() {

	}

	public Weibo_info(int id, String content, String text, int retreet,
			int comment, String time, long timeStamp, String url,List<String> phrase_list) {

		this.id = id;
		this.content = content;
		this.text = text;
		// this.hashTag = hashTag ;
		this.retreet = retreet;
		this.comment = comment;
		this.time = time;
		this.timeStamp = timeStamp;
		this.url = url;
		this.phrase_list = phrase_list ;
	}

	public String getContent() {
		return content;
	}

	public String getText() {

		return text;
	}

	public int getId() {
		return id;
	}

	public int getRetreet() {
		return retreet;
	}

	public String getTime() {
		return time;
	}

	public int getComment() {
		return comment;
	}

	public long getTimeStamp() {
		return timeStamp;
	}
	
	public void print_phrases() throws IOException{
		System.out.println(id);
		System.out.println(content);
		FileUntil until = new FileUntil() ;
		String filepath = "./数据集/短语.txt" ;
		for(String phrase : phrase_list){
			System.out.println(phrase);
			until.write2file(filepath, phrase);
		}
	}

}
