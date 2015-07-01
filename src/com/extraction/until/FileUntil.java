package com.extraction.until;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUntil {

	public String readfile(String filepath) throws IOException {
		String content = " ";
		File file = new File(filepath);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = "";

		while ((line = br.readLine()) != null) {
			content += line;
		}
		return content;
	}

	public void write2file(String filepath, String content) throws IOException {

		File file = new File(filepath);
		FileWriter fw = new FileWriter(file, true);
		PrintWriter pw = new PrintWriter(fw);

		// pw.write(content);
		pw.append(content);
		pw.append("\n");
		pw.flush();
		pw.close();
	}

	public ArrayList<String> parse(String content) throws IOException {

		ArrayList<String> tweet_list = new ArrayList<String>();

		String regex = "(\"content\":([\\s\\S]*?)\"createTime\")";

		Pattern p = Pattern.compile(regex);

		Matcher m = p.matcher(content);
		String temp = " ";
		SplitWords split = new SplitWords();
		while (m.find()) {
			temp = m.group();
			System.out.println(temp);
			temp = temp.replace("\"content\":\"", "");
			temp = temp.replace("\",\"createTime\"", "");
			temp = " 	" + temp;
			temp = split.split(temp);

			System.out.println("temp =" + temp);
			tweet_list.add(temp);
		}

		return tweet_list;
	}

}
