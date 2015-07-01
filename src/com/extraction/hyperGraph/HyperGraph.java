package com.extraction.hyperGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;

import com.extraction.Information.*;



public class HyperGraph {
	
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
	private List<Weibo_info> info_list = new ArrayList<Weibo_info>();
	Hashtable<String,String> wordmap = new Hashtable<String ,String>();//存储单词和词性
	Hashtable<String,Integer> Unigram = new Hashtable<String,Integer>();//一元组
	Hashtable<String,Integer> Bigram = new Hashtable<String ,Integer>();//二元组
	Hashtable<String ,Integer> Trigram = new Hashtable<String ,Integer>();//三元组
	Hashtable<String ,Integer> Fourgram = new Hashtable<String ,Integer>();//四元组
	Hashtable<String ,Float> tf = new Hashtable<String ,Float>();
	Hashtable<String ,Float> tf_idf = new Hashtable<String ,Float>();
	Hashtable<String,Integer> topicWords = new Hashtable<String ,Integer>();
	List<String> stopwords = new ArrayList<String>();
	Hashtable<String,Vertex> hgStruct = new Hashtable<String ,Vertex>();
	List<String> wordlist = new ArrayList<String>();
	List<List<String>> contentlist = new ArrayList<List<String>>();
	
	public HyperGraph() throws IOException{
		   // jdk bug : Exception in thread "main" java.lang.IllegalArgumentException: Comparison method violates its general contract!
    	this.stopwords = readStopwords();
    	this.topicWords = readtopicalwords();
    	System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
	}

	public void weiboParser() throws IOException, ParseException{
		
		String file_folder = "数据集/阿里巴巴/";

		List<String> file_path_list;

		// 存储微博解析出来的内容，包括content,retreet ,comment,time等
		

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

	}
	
	public void print_weiboInfo(){
		
		for(Weibo_info info :info_list){
			System.out.println(info.getText());
		}
	}
	
	public void print_bigram(){
		List<Map.Entry<String, Integer>> unigram = ranking_map(Unigram);
		List<Map.Entry<String, Integer>> bigram = ranking_map(Bigram);
		List<Map.Entry<String, Integer>> trigram = ranking_map(Trigram);
		List<Map.Entry<String, Integer>> fourgram = ranking_map(Fourgram);
	       int count = 1 ;
	       String str = "68" ;
	       for (int i = 0;i< unigram.size() ; ++i)
	       {
	       		String keyword = unigram.get(i).toString();
//	       		String temp[] = keyword.split(" ");
	       		if(keyword.equals(str))
	       			System.out.println(keyword);
	      }
	       
	       for (int i = 0;i< bigram.size() ; ++i)
	       {
	       		String keyword = bigram.get(i).toString();
	       		String temp[] = keyword.split(" ");
	       		if(temp[0].equals(str))
	       			System.out.println(keyword);
	      }
	       
	       for (int i = 0;i< trigram.size() ; ++i)
	       {
	       		String keyword = trigram.get(i).toString();
	       		String temp[] = keyword.split(" ");
	       		if(temp[0].equals(str))
	       			System.out.println(keyword);
	      }
	       for (int i = 0;i< fourgram.size() ; ++i)
	       {
	       		String keyword = fourgram.get(i).toString();
	       		String temp[] = keyword.split(" ");
	       		if(temp[0].equals(str))
	       			System.out.println(keyword);
	      }
	}
	
	List<Map.Entry<String, Integer>> ranking_map(Hashtable map){
		List<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String,Integer>>(map.entrySet());
	       Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>()
	       {
	           @Override
	           public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
	           {
	               return (o1.getValue() - o2.getValue() > 0 ? -1 : 1);
	           }
	       });
	       
	       return entryList ;
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
    public List<String> splitWords(String content){
    	List<String> wordlist = new ArrayList<String>();
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
    	String regex = "((\\s*[_@a-zA-Z0-9\u4e00-\u9fa5]+(\\/n[srtlgwz]?))|(\\s*[_@a-zA-Z0-9\u4e00-\u9fa5]+(\\/m))|(\\s*[_@a-zA-Z0-9\u4e00-\u9fa5]+(\\/a[ng]?))|(\\s*[_@a-zA-Z0-9\u4e00-\u9fa5]+(\\/v[indl]?)))";
		String regex1 = "(s*[_@a-zA-Z0-9\u4e00-\u9fa5]+(\\/))";
    	Pattern p = Pattern.compile(regex);

		// Pattern p=
		// Pattern.compile("(\\s*[0-9\u4e00-\u9fa5 ]+((\\/n[srtlgwz]?))\\s*)");

		Matcher m = p.matcher(content);

		String temp = "";

		while (m.find()) {

			String str = m.group();

//			System.out.print("str :" + str + "|");

			if (!str.contains("@")) {
				
//				System.out.print(str+"| ");

//				str = str.replaceAll("((\\/t)|(\\s)|(\\/n[srtlgwz]?)|(\\/a[ng]?)|(\\/v[indl]?))"," ");

				str = str.replaceAll(" ", "");
				int index = str.indexOf("/");
				String word = str.substring(0, index) ;
				String pos = str.substring(index+1, str.length()) ;
				if ((word.replaceAll(" ", "").length() >= 2)&&(!stopwords.contains(word.replaceAll(" ", "")))) {
					wordlist.add(word);
					wordmap.put(word, pos);
				}
			}

		}
		
		//产生所有的一元组
		
		for(int i=0;i<wordlist.size();i++){
			String uni_gram = wordlist.get(i);
			if(!Unigram.containsKey(uni_gram)){
				Unigram.put(uni_gram, 1);
			}
			else if(Unigram.containsKey(uni_gram)){
				Unigram.put(uni_gram, Unigram.get(uni_gram)+1);
			}
		}
		//产生所有的二元组
		for(int i=0;i<wordlist.size()-1;i++){
			String bi_gram = wordlist.get(i)+" "+wordlist.get(i+1);
			if(!Bigram.containsKey(bi_gram)){
				Bigram.put(bi_gram, 1);
			}
			else if(Bigram.containsKey(bi_gram)){
				Bigram.put(bi_gram, Bigram.get(bi_gram)+1);
			}
		}
		
		//产生所有的三元组
		for(int i=0;i<wordlist.size()-2;i++){
			String tri_gram = wordlist.get(i)+" "+wordlist.get(i+1)+" "+ wordlist.get(i+2);
			if(!Trigram.containsKey(tri_gram)){
				Trigram.put(tri_gram, 1);
			}
			else if(Trigram.containsKey(tri_gram)){
				Trigram.put(tri_gram, Trigram.get(tri_gram)+1);
			}
		}
		//产生所有的四元组
				for(int i=0;i<wordlist.size()-3;i++){
					String four_gram = wordlist.get(i)+" "+wordlist.get(i+1)+" "+ wordlist.get(i+2)+" " +wordlist.get(i+3);
					if(!Fourgram.containsKey(four_gram)){
						Fourgram.put(four_gram, 1);
					}
					else if(Fourgram.containsKey(four_gram)){
						Fourgram.put(four_gram, Fourgram.get(four_gram)+1);
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
    
   public void creatGraph(){
	   
	   for(Weibo_info info:info_list){
		   String content = info.getText();
		  int retweet = info.getRetreet();
		  double edgeWeight = 0;
		  if(retweet==0) 
			  edgeWeight = 0.5 ;
		  else if(retweet>1&& retweet<10)
			  edgeWeight = 0.7 ;
		  else edgeWeight = 1;
		   List<String> elements = this.splitWords(content);
		   contentlist.add(elements);
		   HyperEdge edge = new HyperEdge(elements ,edgeWeight);
		   for(String word:elements){
			   if(!wordlist.contains(word)){
				   wordlist.add(word);
			   }
			   if(!hgStruct.containsKey(word)){
				   Vertex vertex = new Vertex(word);
				   vertex.addEdge(edge);
				   hgStruct.put(word, vertex);
			   }
			   else if(hgStruct.containsKey(word)){
				   Vertex vertex = hgStruct.get(word);
				   vertex.addEdge(edge);
				   hgStruct.put(word, vertex);
//				   hgStruct.get(word).addEdge(edge);
			   }
		   }
	   }
   }
   
   public void RandomWalk(){
	   
	   for(String word:wordlist){
		   if(!tf.containsKey(word)){
       		tf.put(word, (float) 1);
       	}
       	else 
       		tf.put(word, tf.get(word)+(float)1);
	   }
	 //对tf进行规范化
       for(Map.Entry<String, Float> entry : tf.entrySet()){
       	entry.setValue(entry.getValue()/wordlist.size());
       }
       //求tf_idf
       float max_tf_idf = 0;
       for(Map.Entry<String, Float> entry : tf.entrySet()){
       	String word = entry.getKey();
       	if(topicWords.containsKey(word)){
       		Float idf = (float) Math.log(101/topicWords.get(word));
       		tf_idf.put(word, idf*tf.get(word));
       	}
       	else tf_idf.put(word, (float) (tf.get(word)*Math.log(101/5)));
       	
       	if(tf_idf.get(word)>max_tf_idf)
       		max_tf_idf = tf_idf.get(word);
       }
       
       //对tf_idf规范化
       for(Map.Entry<String, Float> entry : tf_idf.entrySet()){
       	entry.setValue(entry.getValue()/max_tf_idf);
//       	System.out.println("规范化的tf_idf:"+entry.getKey()+":"+entry.getValue());
       }
       
	   Map<String, Double> score = new HashMap<String, Double>();
       for (int i = 0; i < max_iter; ++i)//迭代
       {
           Map<String, Double> m = new HashMap<String, Double>();
           double max_diff = 0;
           for (Map.Entry<String, Vertex> entry : hgStruct.entrySet())
           {
               String key = entry.getKey();
               Vertex vertex = entry.getValue();
               m.put(key, (double) (1 - d));//先把公式中的第一部分放入计分部分
              double totalEdgeWeight = 0;
               for (HyperEdge edge : vertex.getEdges()){
            	   totalEdgeWeight+=edge.getWeight(); 
               }
               for (HyperEdge edge : vertex.getEdges())//接下来计算后面部分分数
               {
            	   int edgeSize = edge.getEdgesize();
            	   
            	   if(edgeSize==0) continue ;
            	   
            	   
            	   for(String element:edge.getElements()){
            		   int eleSize = hgStruct.get(element).getEdges().size();
            		   double edgeWeight = 1/edgeSize ;
            		   double wordWeight = 1/eleSize ;
            		   m.put(key, (double) (m.get(key) + d/(edgeSize*eleSize)* (score.get(element) == null ? 0 : score.get(element))));
            	   }
               }
               max_diff = Math.max(max_diff, Math.abs(m.get(key) - (score.get(key) == null ? 0 : score.get(key))));
           }
           score = m;
           if (max_diff <= min_diff) break;
        }
       
       List<Map.Entry<String, Double>> entryList = new ArrayList<Map.Entry<String, Double>>(score.entrySet());
       Collections.sort(entryList, new Comparator<Map.Entry<String, Double>>()
       {
           @Override
           public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2)
           {
               return (o1.getValue() - o2.getValue() > 0 ? -1 : 1);
           }
       });
       
       int count = 1 ;
       for (int i = 0;i< 100 ; ++i)
       {
       		String keyword = entryList.get(i).toString();
       		System.out.print(keyword+" |");
      }
       System.out.println("\n");
   }
   
   public void similarity(){
	   String str1 = "上市";
	   String str2 = "融资";
//	   String str3 = "发行价";
	   
	   int count1 = 0;
	   int count2 = 0;
	   int count3 = 0;
	   int count4 = 0;
	   for(List<String> list :contentlist){
		   if(list.contains(str2)){
			   count1++ ;
			   if(list.contains(str1))
				   count2++;
		   }
		   if(list.contains(str1)){
			   count3++ ;
			   if(list.contains(str2))
				   count4++;
		   }
	   }
	   float similarity = 0;
	   float propotion = 0;
	   if(count1<=count3){
		   similarity = (float)count2/count1 ;
		   propotion = (float)count2/count3 ;
		   System.out.println(str1+":"+str2 +"="+count2);
		   System.out.println(str1 +"="+count3);
		   System.out.println(str2 +"="+count1);
	   }
	   else {
		   similarity = (float)count4/count3 ;
		   propotion = (float)count4/count1 ;
		   System.out.println(str1+":"+str2 +"="+count4);
		   System.out.println(str1 +"="+count3);
		   System.out.println(str2 +"="+count1);
	   }
	   
	   
	   System.out.println(str1+":"+str2+" 结合度为：" + similarity);
	   System.out.println(str1+":"+str2+" 贡献度为：" + propotion);
   }
   
   public void overlap(){
	   String str1 = "阿里巴巴";
	   String str2 = "确定";
	   String str3 = "IPO";
	   int count1 = 0 ;
	   int count2 = 0;
	   for(List<String> list :contentlist){
		   if(list.contains(str1)&&list.contains(str2)){
			   count1++ ;
			   if(list.contains(str3))
				   count2++ ;
		   }
	   }
	   float similarity = (float)count2/count1 ;
	   System.out.println(str1+str2+":"+count1);
	   System.out.println(str1+str2+str3+":"+count2);
	   System.out.println("相似度："+similarity);
   }
	public static void main(String[] args) throws IOException, ParseException {
		
		HyperGraph graph = new HyperGraph();
		graph.weiboParser();
		graph.print_weiboInfo();
		graph.creatGraph();
		graph.RandomWalk();
		graph.print_bigram();
		graph.similarity();
//		graph.overlap();
		
	}

}
