package com.extraction.Information;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parserx {
	
	private List<String> file_path_list;
	private static int count = 1;

	public Parserx() {
		file_path_list = new ArrayList<String>();
	}

	public Weibo_info parse2weibo(String unit) throws ParseException {

		String content = "";
		String text = "";
		int retweet = 0;
		int comment = 0;
		String time = "";
		String url = "";
		List<String> phrase_list = new ArrayList<String>() ;

		String regex_content = "<p class=\"comment_txt\"([\\s\\S]*?)</p>";

		String regex_text1 = "(<[\\s\\S]*?>)";

		String regex_text2 = "([http|https]+[://]+[0-9A-Za-z:/[-]_#[?][=][.][&]]*)";

		String regex_alt = "((分享自|分享自 |（来自|来自|【来自)?@[\\s\\S]*(:|-|）))";//微博用户名

		String regex_time = "title=\"([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2})";

		String regex_retweet = "(转发(<em>)?[0-9]*(</em>)?)";//转发<em>10</em>

		String regex_comment = "(评论(<em>)?[0-9]*(</em>)?)";

		String regex_url = "(<a title=\"[http|https]+[://]+[0-9A-Za-z:/[-]_#[?][=][.][&]]*)";

		// 提取微博内容
		Pattern p0 = Pattern.compile(regex_content);

		Matcher m0 = p0.matcher(unit);

		if (m0.find() == true) {

			content = m0.group();

		}
		content = content.replaceAll("<p class=\"comment_txt\" node-type=\"feed_list_content\">", "");
		content = content.replaceAll("<p class=\"comment_txt\"", "");
		content = content.replaceAll("</p>", "");
		content = content.trim();
		// System.out.println("content =" + content);

		// 提取微博时间
		Pattern p2 = Pattern.compile(regex_time);

		Matcher m2 = p2.matcher(unit);

		if (m2.find() == true) {

			time = m2.group();

		}
		time = time.replaceAll("title=\"", "");

		long timeStamp = Date2TimeStamp(time);
		// System.out.println("time = "+ time);

		// 提取转发次数
		Pattern p1 = Pattern.compile(regex_retweet);

		Matcher m1 = p1.matcher(unit);

		String temp = "";
		if (m1.find() == true) {

			temp = m1.group();

		}
		retweet = convert2number(temp);

		// System.out.println("retweet = " + retreet);

		// 提取评论次数
		Pattern p3 = Pattern.compile(regex_comment);

		Matcher m3 = p3.matcher(unit);

		temp = "";
		if (m3.find() == true) {

			temp = m3.group();

		}

		comment = convert2number(temp);

		// System.out.println("comment =" + comment);

		// 提取url

		Pattern p4 = Pattern.compile(regex_url);

		Matcher m4 = p4.matcher(content);

		if (m4.find() == true) {

			url = m4.group();

		}

		if (!url.equals(""))
			url = url.replaceAll("<a title=\"", "");
		else
			url = "没有新闻链接";
		// System.out.println("url = " + url);

		// 提取微博去除链接等之后的文本

		text = content.replaceAll(url, "");

		temp = text;

		Pattern p5 = Pattern.compile(regex_text1);

		Matcher m5 = p5.matcher(text);

		while (m5.find()) {

			String text1 = m5.group();
			// System.out.println("text１ = "+text1);
			temp = temp.replace(text1, "");
			// System.out.println("temp = "+temp);
		}

		text = temp;

		Pattern p6 = Pattern.compile(regex_text2);

		Matcher m6 = p6.matcher(text);

		while (m6.find()) {

			String text2 = m6.group();

			// System.out.println("text2 = " +text2);

			temp = temp.replace(text2, "");

			// System.out.println("temp = "+temp);

		}
		text = temp + ":";

		Pattern p7 = Pattern.compile(regex_alt);

		Matcher m7 = p7.matcher(text);

		while (m7.find()) {

			String text3 = m7.group();

			temp = text.replace(text3, "");

		}

		text = temp.trim();

//		System.out.println("text = " + text);
		phrase_list = get_phrases(content);
		
		System.out.println("ID:" + count);
		System.out.println("内容:" + content);
		System.out.println("纯文本:" + text);
		System.out.println("发表时间:" + time);
		System.out.println("时间戳:" + timeStamp);
		System.out.println("转发:" + retweet);
		System.out.println("评论:" + comment);
		System.out.println("url:" + url);

		Weibo_info info = new Weibo_info(count, content, text, retweet,
				comment, time, timeStamp, url,phrase_list);
		count = count + 1;
		// 输出每条微博的内容

		return info;
	}

	private List<String> get_phrases(String content) {
		// TODO Auto-generated method stub
		List<String> phrase_list = new ArrayList<String>();
		
		String regex1 = "(<em class=\"red\">([\\s\\S]*?)</em>)+"  ;//解析<span style="color:red;">阿里巴巴IPO</span>
		
		String regex2 = "(《([\\s\\S]*?)》)" ; 
		
		String regex3 = "(【([\\s\\S]*?)】)" ;
		
		String regex4 = "(“([\\s\\S]*?)”)" ;
		
		//匹配加红的关键词
		
		Pattern p1 = Pattern.compile(regex1);
		
		Matcher m1 = p1.matcher(content);
		while(m1.find()){
			String phrase = m1.group();
			phrase = phrase.replaceAll("<em class=\"red\">", "");
			phrase = phrase.replaceAll("</em>", "");
			phrase_list.add(phrase);
		
		}
		
		//匹配书名号
		
		Pattern p2 = Pattern.compile(regex2);
		
		Matcher m2 = p2.matcher(content);
		
		while(m2.find()){
			String phrase = m2.group();
			phrase = phrase.replaceAll("<em class=\"red\">", "");
			phrase = phrase.replaceAll("</em>", "");
			phrase = phrase.replaceAll("《", "");
			phrase = phrase.replaceAll("》", "");
			phrase_list.add(phrase);
		}
	
		//匹配【fdfd】
			Pattern p3 = Pattern.compile(regex3);
			
			Matcher m3 = p3.matcher(content);
			
			while(m3.find()){
				String phrase = m3.group();
				phrase = phrase.replaceAll("<em class=\"red\">", "");
				phrase = phrase.replaceAll("</em>", "");
				phrase = phrase.replaceAll("【", "");
				phrase = phrase.replaceAll("】", "");
				phrase_list.add(phrase);
			}
			
			//匹配引号
			Pattern p4 = Pattern.compile(regex4);
			
			Matcher m4 = p4.matcher(content);
			
			while(m4.find()){
				String phrase = m4.group();
				phrase = phrase.replaceAll("<em class=\"red\">", "");
				phrase = phrase.replaceAll("</em>", "");
				phrase = phrase.replaceAll("“", "");
				phrase = phrase.replaceAll("”", "");
				phrase_list.add(phrase);
			}
		return phrase_list;
	}
	
	public List<String> parse2unit(String page) {

		List<String> list = new ArrayList<String>();
		
		String regex_content = "(<div class=\"WB_cardwrap S_bg2 clearfix\" >([\\s\\S]*?)<div node-type=\"feed_list_repeat\" class=\"WB_feed_repeat S_bg1\" style=\"display:none;\"></div>)";

		Pattern p = Pattern.compile(regex_content);
		Matcher m = p.matcher(page);
		int count = 0;
		String temp = "";
		while (m.find() == true) {
			count++;
			temp = m.group();
			// System.out.println("count = " + count);
			// System.out.println(temp);
			list.add(temp);
		}
		return list;
	}

	public long Date2TimeStamp(String time) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		// String time="1970-01-06 11:45:55";

		Date date = format.parse(time);

		long stamp = date.getTime() / 1000;

		// System.out.print("Format To times:"+date.getTime());

		return stamp;
	}

	public int convert2number(String str) {
		int number = 0;

		String regex = "([0-9]*)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		String temp = "";

		while (m.find()) {
			temp += m.group();
		}

		if (!temp.equals("")) {

			// byte b = Byte.parseByte( temp );
			// short t = Short.parseShort( temp );
			number = Integer.parseInt(temp);
		}

		else
			number = 0;
		// System.out.println("temp =" + temp);

		return number;
	}

	public String getContent(String filepath) throws IOException {
		String content = "";
		File file = new File(filepath);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		String line;

		while ((line = reader.readLine()) != null) {

			content += line + "\n";
			// System.out.println(line);
		}
		// System.out.println(content);
		return content;

	}

	public List<String> getFile_path_list() {
		
		return file_path_list;
		
	}

	public void traverseFolder(String path) {

		File folder = new File(path);
		if (folder.exists()) {
			File[] files = folder.listFiles();
			if (files.length == 0) {
				System.out.println("文件夹是空的!");
				return;
			} else {
				for (File file2 : files) {
					if (file2.isDirectory()) {
						System.out.println("文件夹:" + file2.getAbsolutePath());
						traverseFolder(file2.getAbsolutePath());
					} else {
						System.out.println("文件:" + file2.getAbsolutePath());
						file_path_list.add(file2.getAbsolutePath());
					}
				}
			}
		} else {
			System.out.println("文件不存在!");
		}
	}

}
