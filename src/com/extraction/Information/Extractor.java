package com.extraction.Information;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.ansj.library.UserDefineLibrary;

import com.extraction.until.FileUntil;
import com.sun.jna.Library;
import com.sun.jna.Native;

public class Extractor {
	
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

		public static String transString(String aidString, String ori_encoding,
				String new_encoding) {
			try {
				return new String(aidString.getBytes(ori_encoding), new_encoding);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return null;
		}

		// main函数

		public static void main(String[] args) throws IOException, ParseException,SQLException {

			UserDefineLibrary.insertWord("阿里巴巴ipo", "nw", 15867);

			UserDefineLibrary.insertWord("ipo", "nw", 15867);

			UserDefineLibrary.insertWord("马云", "nw", 15867);

			UserDefineLibrary.insertWord("赢家", "nw", 15867);
			
			String file_folder = "数据集/马航/";

			List<String> file_path_list;

			// 存储微博解析出来的内容，包括content,retreet ,comment,time等
			List<Weibo_info> info_list = new ArrayList<Weibo_info>();

			Parserx parser = new Parserx();

			parser.traverseFolder(file_folder);

			file_path_list = parser.getFile_path_list();

			System.out.println(file_path_list);

			int count = 1;

			for (String filepath : file_path_list) {

				System.out.println(filepath);

				String page = parser.getContent(filepath);

				List<String> list = parser.parse2unit(page);

				for (String unit : list) {

					Weibo_info info = parser.parse2weibo(unit);

					count++;
					info_list.add(info);
				}
			}
			//打印短语
			String filepath = "./数据集/解析文本/珠海航展.txt" ;
			FileUntil until = new FileUntil();
			for(Weibo_info info :info_list){
				info.print_phrases();
//				String content = info.getText();
//				until.write2file(filepath, content);
				
			}
		}

}
