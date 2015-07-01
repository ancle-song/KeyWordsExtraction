package com.extraction.graph;

import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;

import com.extraction.until.SplitWords.CLibrary;
import com.sun.jna.Library;
import com.sun.jna.Native;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TextRank关键词提取
 * @author hankcs
 */
public class TextRankKeyword
{
    public int nKeyword = 50;
    /**
     * 阻尼系数（Damping factor），一般取值为0.85
     */
    static final float d = 0.85f;
    /**
     * 最大迭代次数
     */
    static final int max_iter = 200;//最大迭代次数，达到最大迭代次数之后停止迭代
    static final float min_diff = 0.00001f;//当变化小于min_diff时停止迭代
    List<String> stopwords = new ArrayList<String>();
    Map<String, Set<String>>   words = new HashMap<String, Set<String>>();
	List<String> keywords = new ArrayList<String>();
	

	

    public TextRankKeyword() throws IOException//构造函数
    {
        // jdk bug : Exception in thread "main" java.lang.IllegalArgumentException: Comparison method violates its general contract!
    	this.stopwords = readStopwords();
    	System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }
    
    public TextRankKeyword(int nKeyword) throws IOException//构造函数
    {
    	this.nKeyword = nKeyword ;
        // jdk bug : Exception in thread "main" java.lang.IllegalArgumentException: Comparison method violates its general contract!
    	this.stopwords = readStopwords();
    	System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }

    public String getKeyword(String title, String content) throws IOException
    {
        List<String> wordlist = splitWords(title + content);//分词得到词列表
        System.out.println(wordlist);
 
        Queue<String> que = new LinkedList<String>();
        for (String w : wordlist)
        {
            if (!words.containsKey(w))
            {
                words.put(w, new HashSet<String>());
            }
            que.offer(w);//队列里面增加一个w
            if (que.size() > 5)//当队列长度大于5时，队头元素出队
            {
                que.poll();
            }

            for (String w1 : que)
            {
                for (String w2 : que)
                {
                    if (w1.equals(w2))
                    {
                        continue;
                    }

                    words.get(w1).add(w2);
                    words.get(w2).add(w1);
                }
            }
        }
//        System.out.println(words);
        Map<String, Float> score = new HashMap<String, Float>();
        for (int i = 0; i < max_iter; ++i)//迭代
        {
            Map<String, Float> m = new HashMap<String, Float>();
            float max_diff = 0;
            for (Map.Entry<String, Set<String>> entry : words.entrySet())
            {
                String key = entry.getKey();
                Set<String> value = entry.getValue();
                m.put(key, 1 - d);//先把公式中的第一部分放入计分部分
                
                for (String other : value)//接下来计算后面部分分数
                {
                    int size = words.get(other).size();
                    if (key.equals(other) || size == 0) continue;
//                    if(i==1)
//                    System.out.println("第"+(i+1)+"轮迭代" + score.get(other));
                    m.put(key, m.get(key) + d / size * (score.get(other) == null ? 0 : score.get(other)));
                }
              
                max_diff = Math.max(max_diff, Math.abs(m.get(key) - (score.get(key) == null ? 0 : score.get(key))));
//            System.out.println("第"+i+"轮迭代max_diff :" + max_diff);
            }
            score = m;
            if (max_diff <= min_diff) break;
        }
        
        List<Map.Entry<String, Float>> entryList = new ArrayList<Map.Entry<String, Float>>(score.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, Float>>()
        {
            @Override
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2)
            {
                return (o1.getValue() - o2.getValue() > 0 ? -1 : 1);
            }
        });
//        System.out.println(entryList);
        //读取主题词和背景词
        String result = "";
        Hashtable<String , Integer> twords = new Hashtable<String , Integer>();
        twords = readtopicalwords();
        List<String> bgwords = new ArrayList<String>();
        bgwords = readbgwords();
       
        int count = 1 ;
        for (int i = 0;i< entryList.size() ; ++i)
        {
        		String keyword = entryList.get(i).getKey();
        	
        	if((!twords.containsKey(keyword))&&(!bgwords.contains(keyword))&&count<=nKeyword){
        		result += keyword + '	';
        		count++ ;
        	}
        	else if(twords.containsKey(keyword)){
        		if(twords.get(keyword)<40&(!bgwords.contains(keyword))&&count<=nKeyword){
        		result += keyword + "	";
        		count++ ;
        	}
        }
       }
        String[] listwords = result.split("	");
        for(String keyword:listwords){
        	keywords.add(keyword);
        }
        return result;
    }
    
    public List<Map.Entry<String, Integer>> getphrases(String keyword) throws IOException{
    	Set<String> listwords = words.get(keyword);
    	String content = this.getContent();
    	Map<String, Integer> map = new HashMap<String, Integer>();
    	for(String edge:listwords){
    		if(keywords.contains(edge)){
    			String phrase = keyword + " " +edge ;
    			String[] str = content.split(phrase);
    			map.put(phrase, str.length-1);
//    			if(str.length>0)
//    			System.out.println(phrase+":"+(str.length-1));
    		}
    	}
    	
    	List<Map.Entry<String, Integer>> infoIds = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
    	//排序前
//    	for (int i = 0; i < infoIds.size(); i++) {
//    	    String id = infoIds.get(i).toString();
//    	    System.out.println(id);
//    	}
    	
    	//排序
    	Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() {   
    	    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
    	        return (o2.getValue() - o1.getValue()); 
//    	        return (o1.getKey()).toString().compareTo(o2.getKey());
    	    }
    	}); 

    	//排序后
    	System.out.println("排序后：");
    	for (int i = 0; i < infoIds.size(); i++) {
    	    String id = infoIds.get(i).toString();
    	    
    	    System.out.println(id);
    	 
    	}
    	
    	return infoIds ;
    
    }
    
    public String getContent() throws IOException{
    	String content = "";
    	  String filepath = "./数据集/解析文本/康定地震.txt" ;
         File file = new File(filepath);
//       BufferedReader br = new BufferedReader(new FileReader(file));
         InputStreamReader read = new InputStreamReader (new FileInputStream(file),"utf-8");   
         BufferedReader br = new BufferedReader(read);
         String line ="";
         while((line = br.readLine())!=null){
//         	System.out.println(line);
         	content+=line ;
         }
    	return content ;
    }
    
    
   
    
    public static void main(String[] args) throws IOException
    {
    	  String filepath = "./数据集/解析文本/珠海航展.txt" ;
        File file = new File(filepath);
//      BufferedReader br = new BufferedReader(new FileReader(file));
        InputStreamReader read = new InputStreamReader (new FileInputStream(file),"utf-8");   
        BufferedReader br = new BufferedReader(read);
        String line ="";
        String content = "";
        while((line = br.readLine())!=null){
//        	System.out.println(line);
        	content+=line ;
        }
        
        TextRankKeyword tr= new TextRankKeyword(30); 
        String keywords = tr.getKeyword("", content) ;
        System.out.println(keywords);
//        String[] listwords = keywords.split("	");
//        tr.getphrases(listwords[0]);

    }

    /**
     * 是否应当将这个term纳入计算，词性属于名词、动词、副词、形容词
     * @param term
     * @return 是否应当
     */
    public boolean shouldInclude(Term term)
    {
        if (
                (term.getNatrue().natureStr.startsWith("a") ||term.getNatrue().natureStr.startsWith("n") ||
                term.getNatrue().natureStr.startsWith("v") )&&
                term.getName().length()>=2
//                term.getNatrue().natureStr.startsWith("d") ||
//                term.getNatrue().natureStr.startsWith("a")
                )
        {
            // TODO 你需要自己实现一个停用词表
//            if (!StopWordDictionary.contains(term.getName()))
//            {
                return true;
//            }
        }

        return false;
    }
    
    public Hashtable<String,Integer> readtopicalwords() throws IOException{
    	 Hashtable<String,Integer> twords = new  Hashtable<String,Integer>();
    	 String filepath = "./数据集/主题词.txt" ;
         File file = new File(filepath);
//         BufferedReader br = new BufferedReader(new FileReader(file));
         InputStreamReader read = new InputStreamReader (new FileInputStream(file),"utf-8");   
         BufferedReader br = new BufferedReader(read);
         String line ="";
         String content = "";
         while((line = br.readLine())!=null){
        	 String[] record=line.split("	") ;
        	if(!twords.containsKey(record[0])){
        		twords.put(record[0], 1);
        	}
        	else twords.put(record[0], twords.get(record[0])+1);
         }
    	
    	return twords ;
    }
    public List<String> readbgwords() throws IOException{
    
    	List<String> bgwords  = new ArrayList<String>();
    	 String filepath = "./数据集/背景词.txt" ;
         File file = new File(filepath);
//         BufferedReader br = new BufferedReader(new FileReader(file));
         InputStreamReader read = new InputStreamReader (new FileInputStream(file),"utf-8");   
         BufferedReader br = new BufferedReader(read);
         String line ="";
         String content = "";
         while((line = br.readLine())!=null){
        	 String[] record=line.split("	") ;
        	if(!bgwords.contains(record[0])){
        		bgwords.add(record[0]);
        	}
        
         }
    	
    	return bgwords ;
    }
    public List<String> splitWords(String content){
    	List<String> wordlist = new ArrayList<>();
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

			return null;
		}
		if (content != null) {
			content = content.replaceAll("([0-9a-zA-Z]*?)", "");
			
			nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(content, 1);

//			System.out.println(nativeBytes);

			wordlist = this.removeNoisy(nativeBytes);

//			System.out.println(nativeBytes);

		}
    	return wordlist ;
    }
    
    
    private List<String> removeNoisy(String content) {

    	List<String> wordlist = new ArrayList<String>();
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

				if ((str.replaceAll(" ", "").length() >= 2)&&(!stopwords.contains(str.replaceAll(" ", "")))) {

					wordlist.add(str.replaceAll(" ", ""));

					// System.out.print(str + " ");
				}
			}

		}

		return wordlist;
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
		
			
}
