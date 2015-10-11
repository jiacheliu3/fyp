package gro

import com.google.gson.Gson

import java.io.StringReader;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern
import org.lionsoul.jcseg.core.ADictionary
import org.lionsoul.jcseg.core.ISegment
import org.lionsoul.jcseg.core.IWord
import org.lionsoul.jcseg.core.*;



//import org.lionsoul.jcseg.core.*;


class SepManager {
	String configPath;
	JcsegTaskConfig config;
	ADictionary dic;
	ISegment seg;

	String pyInput;
	String pyOutput;

	//Singleton
	private static SepManager manager;

	static String base="C:\\Users\\jiacheliu3\\git\\projects\\CodeBigBro\\";
	String uselessWordPath=base+"StopWords.txt";
	TreeSet<String> uselessWords=new TreeSet<>();

	HashSet<String> toDelete=new HashSet<>();

	LinkedHashMap<String,Double> keywordOdds;

	HashSet<String> negation1=["不", "别", "难", "反"];
	HashSet<String> negation2=["不是", "不要", "不能", "没有", "不会"];
	HashSet<String> negation3=["不可能"];

	private SepManager(){
		//use jcseg to segment sentences
		prepareJcseg();
		
		pyInput=base+"temp\\tempInput.txt";
		pyOutput=base+"temp\\tempOutput.txt";

		keywordOdds=new LinkedHashMap<>();
		
		//load useless words
		File stopWords=new File(uselessWordPath);
		String[] useless=stopWords.readLines();
		useless.each {
			uselessWords.add(it);
		}
		
		println "Separation Manager initialized with ${uselessWords.size()} stop words."

	}
	public static SepManager getSepManager(){
		if(manager==null)
			manager=new SepManager();
		return manager;
	}
	//entrance of separation
	public separate(String s){
		//filter out the usernames
		s=s.replaceAll("@[\\u4e00-\\u9fa5a-zA-Z\\-_0-9]+", "");
		//the expected number of keywords is around the sqrt of length
		int num=Math.ceil(Math.sqrt(s.length()));
		if(num<=2){
			println "String is too short for any keywords!"
			return [:];
		}

		//renew the map
		keywordOdds=new LinkedHashMap<>();

		//python part: snowNLP and jieba
		//prepare input file for python script
		File input=new File(pyInput);
		input.withPrintWriter {w-> w<<s; }
		//output to another log file for temp reason
		File temp=new File(base+"temp\\tempRecord.txt");
		temp.append(s);
		temp.append("&end&\n");
		//pass to python script
		PythonCaller.call(pyInput, pyOutput);




		File jiebaTDIFD=new File(base+"temp\\jiebaTDIFD.txt");
		jiebaTDIFD.readLines().each {
			callIncrement(it,0.483);
		}
		File jiebaTextrank=new File(base+"temp\\jiebaTextRank.txt");
		jiebaTextrank.readLines().each{
			callIncrement(it,0.506);
		}


		File snowNLP=new File(base+"temp\\snowOutput.txt");
		snowNLP.readLines().each{
			callIncrement(it,0.345);
		}
		//java part:
		//fnlp
		//fnlp is good at detecting new words with length>2 but not so at short words
		//so add only 0.3 to short ones and 0.6 to longer ones
		ArrayList<String> r=FnlpManager.separate(s);
		String fnlpOutput=base+"temp\\fnlpOutput.txt";
		File fOut=new File(fnlpOutput);
		fOut.append(r+"\n");
		r.each {
			if(it.size()>2){
				callIncrement(it,0.6);
			}
			else{
				callIncrement(it,0.3);
			}
		}



		//ansj not activated due to memory limitation




		//read from output file
		//		File output=new File(pyOutput);
		//		ArrayList<String> result=output.readLines();
		println "Raw keywords without organizing."
		println keywordOdds;
		//filter out stop words
		HashSet<String> useless=new HashSet<>();
		keywordOdds.each{
			if(uselessWords.contains(it.key))
				useless.add(it.key);
		}
		useless.each{
			println "Filter out useless keyword "+it;
			keywordOdds.remove(it);
		}
		//return only the top results
		println "Sorting"
		keywordOdds=keywordOdds.sort { a, b -> b.value <=> a.value };

		//check if negation words exist before return
		HashMap<String,Double> toRemove=new HashMap<>();
		HashMap<String,Double> toAdd=new HashMap<>();
		def results=getResults(num);
		results.each{key,value->
			//find all occurrence of keyword in string
			for (int index = s.indexOf(key);
			index >= 0;
			index = s.indexOf(key, index + 1))
			{
				//check if 1-char negation exists
				if(index>=1){
					String n1=s.substring(index-1,index);
					//replace the keyword with its opposite
					if(negation1.contains(n1)){

						toRemove.put(key,value);
						toAdd.put(n1+key,value);
					}
				}
				//check if 2-char negation exists
				if(index>=2){
					String n2=s.substring(index-2,index);
					//replace the keyword with its opposite
					if(negation2.contains(n2)){

						toRemove.put(key,value);
						toAdd.put(n2+key,value);
					}
				}
				//check if 3-char negation exits
				if(index>=3){
					String n3=s.substring(index-3,index);
					//replace the keyword with its opposite
					if(negation3.contains(n3)){
						toRemove.put(key,value);
						toAdd.put(n3+key,value);
					}
				}
			}

		}
		toRemove.each{
			results.remove(it.key);
		}
		toAdd.each{
			results.put(it.key, it.value);
		}
		return results;
	}
	public LinkedHashMap<String,Double> getResults(int number){
		//improve the final result
		improveResult();

		int i=1;
		LinkedHashMap<String,Double> result=new LinkedHashMap<>();
		for(def e:keywordOdds){
			result.put(e.key,e.value);
			if(i>=number){
				println "Final keywords: "+result;
				return result;

			}
			i++;
		}
		return result;
	}
	public void improveResult(){


		HashSet<String> improvedHolder=new HashSet<>();
		//if A+B and A both exists, add weight of A to A+B
		keywordOdds.each{key,value->
			int l=key.length();

			ArrayList<String> comb=combination(key);
			for(String s in comb){

				String left=key.substring(0,key.indexOf(s));
				String right=key-left-s;
				Boolean deleteL;
				Boolean deleteM;
				Boolean deleteR;
				//println "L:${left} R:${right}";
				if(left==""||keywordOdds[left]!=null){
					deleteL=true;
					if(left!=""&&!improvedHolder.contains(left)){
						keywordOdds[key]+=keywordOdds[left];
						improvedHolder.add(left);
					}
				}
				if(keywordOdds[s]!=null){
					deleteM=true;
					if(!improvedHolder.contains(s)){
						keywordOdds[key]+=keywordOdds[s];
						improvedHolder.add(s);
					}
				}
				if(right==""||keywordOdds[right]!=null){
					deleteR=true;
					if(right!=""&&!improvedHolder.contains(right)){
						keywordOdds[key]+=keywordOdds[right];
						improvedHolder.add(right);
					}
				}
				if(deleteL&&deleteM&&deleteR){
					toDelete.add(left);
					toDelete.add(s);
					toDelete.add(right);

				}

			}

		}

		toDelete.each{
			if(it!=null&&it!="")
				keywordOdds.remove(it);
		}
	}
	public ArrayList<String> combination(String s){
		ArrayList<String> results=new ArrayList<>();
		if(s.length()==1)
			results.add(s);
		else{
			for(int i in 2..s.length()){
				for(int j in 0..s.length()-i+1){
					results.add(s.substring(j, j+i-1));
				}
			}
		}
		//println results;
		return results;
	}
	//
	public void callIncrement(String str,double value){
		if(str==""||str=="\n"||str==" "||str=="\t"){
			//do nothing
		}else if(str.matches(Patterns.PUNC.value())){
			//skip punctuation
		}
		//not null
		else{

			increment(str,value);
		}
	}
	public void increment(String key,double value){
		//closure to modify value of key

		if(keywordOdds[key]!=null)
			keywordOdds[key]+=value;
		else
			keywordOdds.put(key, value);

	}
	public ArrayList<String> jcseg(String s){
		
		//first filter out the @usernames in content to avoid noise
		//String content=str.replaceAll("@[\\u4e00-\\u9fa5a-zA-Z\\-_0-9]+", '');

		//Pass string to string smasher
		seg?.reset(new StringReader(s));

		ArrayList<String> result=new ArrayList<>();
		IWord word=null;
		while ( (word = seg.next()) != null ) {
			//if(isNeeded(word))
				result.add(word.getValue());
		}
		println "Segmentation complete with "+result.size+" segments.";
		return result;
	}
	//segment a string into words
	public ArrayList<String> segment(String s){
		println "Use jcseg to segement sentence"
		//use jcseg to segment sentences
		ArrayList<String> r=jcseg(s);
		
		return r;
	}
	//get raw segments 
	public ArrayList<String> segment(Collection<String> c){
		println "Use jcseg to segement sentences"
		ArrayList<String> results=new ArrayList<>();
		c.each{
			results.addAll(jcseg(it));
		}
		
		return results;
		
	}
	//get keywords 
	public ArrayList<String> mash(Collection c){
		return FnlpManager.mashCollection(c);
	}
	public ArrayList<String> separate(ArrayList<String> src){
		StringBuilder sb=new StringBuilder();
		src.each {
			sb.append(it+'\n');
		}
		return separate(sb.toString());
	}
	//obsolete with Jcseg

	//decide whether a word is needed
	//public boolean isNeeded(IWord word){
	/*
	 China,JPanese,Korean words 
	 T_CJK_WORD = 1;
	 chinese and english mix word.		 like B超,SIM卡. 
	 T_MIXED_WORD = 2;
	 chinese last name. 
	 T_CN_NAME = 3;
	 chinese nickname.		 like: 老陈 
	 T_CN_NICKNAME = 4;
	 latain series.		 including the arabic numbers.
	 T_BASIC_LATIN = 5;
	 letter number like 'ⅠⅡ' 
	 T_LETTER_NUMBER = 6;
	 other number like '①⑩⑽㈩' 
	 T_OTHER_NUMBER = 7;
	 pinyin 
	 T_CJK_PINYIN = 8;
	 Chinese numeric   
	 T_CN_NUMERIC = 9;
	 T_PUNCTUATION = 10;
	 useless chars like the CJK punctuation
	 T_UNRECOGNIZE_WORD = 11;
	 * */
	//		def result;
	//		switch(word.getType()){
	//			case 10:case 11:
	//				result=false;
	//				break;
	//			default:result=true;
	//		}
	//if word is needed, continue to check getPosition()
	/* NAME_POSPEECH = {"nr"};張三
	 NUMERIC_POSPEECH = {"m"};300米
	 EN_POSPEECH = {"en"};
	 MIX_POSPEECH = {"mix"};
	 PPT_POSPEECH = {"nz"};垃圾焚燒大躍進
	 PUNCTUATION = {"w"};
	 UNRECOGNIZE = {"urg"};‘ ’，‘-’
	 * 
	 * */
	//		if(result){
	//			switch(word.getPosition()){
	//				case '/w':case 'w':case '[w]':
	//				case 'urg':case '/urg':
	//					result=false;
	//					break;
	//			}
	//		}
	//of the result words, filter out the useless ones
	//		if(uselessWords.contains(word.getValue())){
	//			result=false;
	//			//println "Found useless word "+word.getValue()+"!!"
	//		}
	//		return result;
	//	}
	
	//obsoleted with jcseg
		public void prepareJcseg(){
			configPath=base+"conf/jcseg.properties";
			config = new JcsegTaskConfig(configPath);
			dic = DictionaryFactory.createDefaultDictionary(config);
			seg = SegmentFactory.createJcseg(JcsegTaskConfig.COMPLEX_MODE, config, dic);
	
			//load useless words
			File stopWords=new File(uselessWordPath);
			String[] useless=stopWords.readLines();
			useless.each {
				uselessWords.add(it);
			}
			println "SeparatorManager initialized with "+uselessWords.size()+" words filtered."
		}

	//obsoleted because of using jcseg
//	public ArrayList<String> obsoleteSeparate(String str){
//
//
//		//first filter out the @usernames in content to avoid noise
//		String content=str.replaceAll("@[\\u4e00-\\u9fa5a-zA-Z\\-_0-9]+", '');
//
//		//Pass string to string smasher
//		seg?.reset(new StringReader(content));
//
//		ArrayList<String> result=new ArrayList<>();
//		IWord word=null;
//		while ( (word = seg.next()) != null ) {
//			if(isNeeded(word))
//				result.add(word.getValue());
//		}
//		println "Segmentation complete with "+result.size+" segments.";
//		return result;
//	}
	//To test and compare the performance of segmentation services
	public static void main(String[] args){
		//		File test=new File("F:/needless.lex");
		//		StringBuilder sb=new StringBuilder();
		//		test.readLines().each {
		//			sb.append(it);
		//		}
		//		String target=sb.toString();
		//
		//		def manager=new SepManager();
		//		manager.separate(target);
		//		return;
		File ts=new File(base+"temp\\sampleSet.txt");
		SepManager sep=new SepManager();
		Gson gson=new Gson();

		File output=new File(base+"temp\\sampleResult.json");
		if(output.exists())
			output.write("");
		def set=ts.readLines();

		JsonWrapper jw=new JsonWrapper();
		set.each {
			if(it=="\n"||it==""||it==" "||it=="\t"){}
			else{
				//filter out names
				String str=it.replaceAll("@[\\u4e00-\\u9fa5a-zA-Z\\-_0-9]+", "");

				def r=sep.separate(str);
				println r;

				JsonObject j=new JsonObject(it,r);
				jw.add(j);
			}
		}
		String temp=gson.toJson(jw);
		output.append(temp);

	}
}
