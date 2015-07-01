package com.extraction.until;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ansj.library.UserDefineLibrary;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class SplitWords {

	List<String> stopwords = new ArrayList<String>();

	public SplitWords() throws IOException {
		this.stopwords = readStopwords();
	}

	private List<String> readStopwords() throws IOException {
		List<String> list = new ArrayList<String>();

		String filepath = "./library/stopwords";
		File file = new File(filepath);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = "";

		while ((line = br.readLine()) != null) {
			list.add(line);
		}
		return list;
	}

	// 定义接口CLibrary，继承自com.sun.jna.Library
	public interface CLibrary extends Library {
		// 定义并初始化接口的静态变量
		CLibrary Instance = (CLibrary) Native
				.loadLibrary(

						"/home/songyajun/Java/program/query_auto_completion/lib/linux32/libNLPIR.so",
						CLibrary.class);

		public int NLPIR_Init(String sDataPath, int encoding,
				String sLicenceCode);

		public String NLPIR_ParagraphProcess(String sSrc, int bPOSTagged);

		public String NLPIR_GetKeyWords(String sLine, int nMaxKeyLimit,
				boolean bWeightOut);

		public String NLPIR_GetFileKeyWords(String sLine, int nMaxKeyLimit,
				boolean bWeightOut);

		public int NLPIR_AddUserWord(String sWord);// add by qp 2008.11.10

		public int NLPIR_DelUsrWord(String sWord);// add by qp 2008.11.10

		public String NLPIR_GetLastErrorMsg();

		public void NLPIR_Exit();
	}

	// 对微博文本进行分词，词性标注等处理
	public String split(String content) {

		String argu = "./";
		// String system_charset = "GBK";//GBK----0
		UserDefineLibrary.insertWord("贤妻", "nw", 15867);
		UserDefineLibrary.insertWord("贤妻良母", "nw", 15867);
		UserDefineLibrary.insertWord("破解", "nw", 15867);
		UserDefineLibrary.insertWord("自作多情", "nw", 15867);

		String system_charset = "UTF-8";

		int charset_type = 1;

		int init_flag = CLibrary.Instance.NLPIR_Init(argu, charset_type, "0");

		String nativeBytes = null;

		if (0 == init_flag) {

			nativeBytes = CLibrary.Instance.NLPIR_GetLastErrorMsg();

			System.err.println("初始化失败！fail reason is " + nativeBytes);

			return "";
		}
		if (content != null) {
			content = content.replaceAll("([0-9a-zA-Z]*?)", "");
			
			nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(content, 1);

//			System.out.println(nativeBytes);

			nativeBytes = this.removeNoisy(nativeBytes);

//			System.out.println(nativeBytes);

		}
		return nativeBytes;
	}

	private String removeNoisy(String content) {

		Pattern p = Pattern
				.compile("((\\s*[_@a-zA-Z0-9\u4e00-\u9fa5]+(\\/n[srtlgwz]?))|(\\s*[_@a-zA-Z0-9\u4e00-\u9fa5]+(\\/a[ng]?))|(\\s*[_@a-zA-Z0-9\u4e00-\u9fa5]+(\\/v[indl]?)))");

		// Pattern p=
		// Pattern.compile("(\\s*[0-9\u4e00-\u9fa5 ]+((\\/n[srtlgwz]?))\\s*)");

		Matcher m = p.matcher(content);

		String temp = "";

		while (m.find()) {

			String str = m.group();

//			System.out.print("str :" + str + "|");

			if (!str.contains("@")) {

				str = str.replaceAll("((\\/t)|(\\s)|(\\/n[srtlgwz]?)|(\\/a[ng]?)|(\\/v[indl]?))"," ");

				if ((str.replaceAll(" ", "").length() >= 2)&& (!stopwords.contains(str.replaceAll(" ", "")))) {

					temp += str;

					// System.out.print(str + " ");
				}
			}

		}

		return temp;
	}
}
