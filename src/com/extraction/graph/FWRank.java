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
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TextRank关键词提取
 * @author hankcs
 */
public class FWRank
{
    public int nKeyword = 50;
    /**
     * 阻尼系数（Damping factor），一般取值为0.85
     */
    static  double d = 0.85f;
    /**
     * 最大迭代次数
     */
    static final int max_iter = 200;//最大迭代次数，达到最大迭代次数之后停止迭代
    static final double min_diff = 0.00001f;//当变化小于min_diff时停止迭代
    List<String> stopwords = new ArrayList<String>();
    Map<String, Hashtable<String ,Integer>>   words = new HashMap<String, Hashtable<String ,Integer>>();
	List<String> keywords = new ArrayList<String>();
	Hashtable<String,Integer> twords = new Hashtable<String ,Integer>();
	Hashtable<String ,Float> tf = new Hashtable<String ,Float>();
	Hashtable<String ,Float> tf_idf = new Hashtable<String ,Float>();
	Map<String, Double> score = new HashMap<String, Double>();
	
    public FWRank() throws IOException//构造函数
    {
        // jdk bug : Exception in thread "main" java.lang.IllegalArgumentException: Comparison method violates its general contract!
    	this.stopwords = readStopwords();
    	this.twords = readtopicalwords();
    	System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }
    
    public FWRank(int nKeyword) throws IOException//构造函数
    {
    	this.nKeyword = nKeyword ;
    	
        // jdk bug : Exception in thread "main" java.lang.IllegalArgumentException: Comparison method violates its general contract!
    	this.stopwords = readStopwords();
    	this.twords = readtopicalwords();
    	System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }

    public String getKeyword(String title, String content) throws IOException
    {
        List<String> wordlist = splitWords(title + content);//分词得到词列表
        System.out.println(wordlist);
 
        Queue<String> que = new LinkedList<String>();
        for (String w : wordlist)
        {
        	if(!tf.containsKey(w)){
        		tf.put(w, (float) 1);
        	}
        	else tf.put(w, tf.get(w)+(float)1);
        	
            if (!words.containsKey(w))
            {
                words.put(w, new Hashtable<String ,Integer>());
            }
            que.offer(w);//队列里面增加一个w
            if (que.size() > 2)//当队列长度大于2时，队头元素出队
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
                    if(!words.get(w1).containsKey(w2))
                    	words.get(w1).put(w2, 1);
                    else words.get(w1).put(w2, words.get(w1).get(w2)+1);
//                    words.get(w2).add(w1);
                }
            }
        }
        
        //对tf进行规范化
//        for(Map.Entry<String, Float> entry : tf.entrySet()){
//        	entry.setValue(entry.getValue()/words.size());
//        }
        //求tf_idf
        float max_tf_idf = 0;
        for(Map.Entry<String, Float> entry : tf.entrySet()){
        	String word = entry.getKey();
        	if(twords.containsKey(word)){
        		Float idf = (float) Math.log(101/twords.get(word));
        		tf_idf.put(word, idf*tf.get(word));
        	}
        	else tf_idf.put(word, (float) (tf.get(word)*Math.log(101/5)));
        	
        	if(tf_idf.get(word)>max_tf_idf)
        		max_tf_idf = tf_idf.get(word);
        }
        
        //对tf_idf规范化
        for(Map.Entry<String, Float> entry : tf_idf.entrySet()){
        	entry.setValue(entry.getValue()/max_tf_idf);
        	System.out.println("规范化的tf_idf:"+entry.getKey()+":"+entry.getValue());
        }

        for (int i = 0; i < max_iter; ++i)//迭代
        {
            Map<String, Double> m = new HashMap<String, Double>();
            double max_diff = 0;
            for (Map.Entry<String, Hashtable<String ,Integer>> entry : words.entrySet())
            {
                String key = entry.getKey();//顶点
                if(twords.containsKey(key)){
                	if(tf.get(key)>=10.0)
                		d = 1-(float)twords.get(key)/101 ;
                	else 
                		d= 0.9;
                }
                
                else d = 0.9 ;
                Hashtable<String ,Integer> edges = entry.getValue();//边和权重
                Set<String> edgeSet = edges.keySet();//边
//                *tf_idf.get(key)
                m.put(key, (1 - d));//先把公式中的第一部分放入计分部分
                
                for (String edge : edgeSet)//接下来计算后面部分分数,每个edge就是一条边
                {
                    int size = words.get(edge).size();//就是edge这条边的出度数目
                    if (key.equals(edge) || size == 0) continue;
                    int co_occur = edges.get(edge);
                    int total = 0;
                   Hashtable table = words.get(edge);
                   Set<String> set = table.keySet();
                   for(String k:set)
                	   total +=(int) table.get(k);
                   double weight = (double)co_occur/(double)total ;
                    m.put(key, m.get(key) + d *weight * (score.get(edge) == null ? 0 : score.get(edge)));
                	}
                max_diff = Math.max(max_diff, Math.abs(m.get(key) - (score.get(key) == null ? 0 : score.get(key))));
//                System.out.println("关键词:"+key + ":"+ "d=" +d);
            }
            
            System.out.println("第"+i+"轮迭代max_diff :" + max_diff);
            score = m;
            if (max_diff <= min_diff) break;
        }
        List<Map.Entry<String, Double>> entryList = new ArrayList<Map.Entry<String, Double>>(score.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String,Double>>()
        {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2)
            {
                return (o1.getValue() - o2.getValue() > 0 ? -1 : 1);
            }
        });
//        System.out.println(entryList);
        //读取主题词和背景词
        
//        Hashtable<String , Integer> twords = new Hashtable<String , Integer>();
//        twords = readtopicalwords();
//        List<String> bgwords = new ArrayList<String>();
//        bgwords = readbgwords();
        String result = "";
        String resultx = "";
        int count = 1 ;
        for (int i = 0;i< 30 ; ++i)
        {
//        	entryList.size()
        		String keyword = entryList.get(i).getKey();
        	resultx+= keyword + ":"+entryList.get(i).getValue()+"	";
        	result+= keyword +"	";
//        	if((!twords.containsKey(keyword))&&(!bgwords.contains(keyword))&&count<=nKeyword){
//        		result += keyword + '	';
//        		count++ ;
//        	}
//        	else if(twords.containsKey(keyword)){
//        		if(twords.get(keyword)<40&(!bgwords.contains(keyword))&&count<=nKeyword){
//        		result += keyword + "	";
//        		count++ ;
//        	}
//        }
       }
        String[] listwords = result.split("	");
        for(String keyword:listwords){
        	keywords.add(keyword);
        }
        return result;
    }
    
    public List<Entry<String, Float>> getphrases(String[] wordlist) throws IOException{
    	
    	Map<String, Float> phrase_map = new HashMap<String, Float>();
    	for(String keyword:wordlist){
//    		System.out.println("keyword:"+ keyword);
    		Set<String> edges = words.get(keyword).keySet();
//    		String content = this.getContent();
    		
    		for(String edge:edges){
    			if(keywords.contains(edge)){
    				String phrase = keyword + "-" +edge ;
    				float rank_score = (float) (Math.log(score.get(keyword))+Math.log(score.get(edge)));
    				phrase_map.put(phrase, rank_score);
//    				if(str.length>0)
//    				System.out.println(phrase+":"+(str.length-1));
    		}
    	}
    }
    	
    	List<Map.Entry<String, Float>> infoIds = new ArrayList<Map.Entry<String, Float>>(phrase_map.entrySet());
    		//排序前
//    		for (int i = 0; i < infoIds.size(); i++) {
//    		    String id = infoIds.get(i).toString();
//    		    System.out.println(id);
//    		}
    	
    		//排序
    	Collections.sort(infoIds, new Comparator<Map.Entry<String, Float>>() {   
    	    public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {      
    	        	
    	    	return (int)(o2.getValue() - o1.getValue()); 
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
    	 String filepath = "./数据集/test/康定地震.txt" ;
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
        String filepath = "./数据集/解析文本/康定地震.txt" ;
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
               
        FWRank fw = new FWRank(30); 
        String keywords = fw.getKeyword("", content) ;
        System.out.println(keywords);
        String[] listwords = keywords.split("	");
//        ne.getphrases(listwords);

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
        	if(!twords.containsKey(record[0].replaceAll(" ", ""))){
        		twords.put(record[0].replaceAll(" ", ""), 1);
        	}
        	else twords.put(record[0].replaceAll(" ", ""), twords.get(record[0].replaceAll(" ", ""))+1);
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
