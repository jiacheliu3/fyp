package gro.crawler;
import gro.*;
import java.util.regex.Matcher
import java.util.regex.Pattern;

import cn.edu.hfut.dmic.webcollector.crawler.BreadthCrawler;
import cn.edu.hfut.dmic.webcollector.model.Links;
import cn.edu.hfut.dmic.webcollector.model.Page;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.jsoup.Jsoup
import java.math.RoundingMode;

public class GnomeCrawler extends BreadthCrawler{

	static String base="C:\\Users\\jiacheliu3\\git\\projects\\CodeBigBro\\data\\";
	int counter=0;
	File inputFile;
	File outputFile;
	String name;

	TreeSet<String> wordList;
	HashSet<String> stopwords;

	public GnomeCrawler(String crawlPath, boolean autoParse) {
		super(crawlPath, autoParse);
		/*start page*/
		this.addSeed("http://ol.gamersky.com/");
		this.addSeed("http://www.gamersky.com/pcgame/");
		this.addSeed("http://shouyou.gamersky.com/");
		this.addSeed("http://tv.gamersky.com/");

		/*fetch url like http://news.yahoo.com/xxxxx*/
		this.addRegex("-http://www.hao123.com/.*");
		/*do not fetch url like http://news.yahoo.com/xxxx/xxx)*/
		this.addRegex("-http://\\.*.hao123.com/.+/.*");
		/*do not fetch jpg|png|gif*/
		this.addRegex("-.*\\.(jpg|png|gif).*");
		/*do not fetch url contains #*/
		//this.addRegex("-.*#.*");
		System.out.println("Crawler initialized");
	}

	public GnomeCrawler(String crawlPath,boolean autoParse,String name,File src){
		super(crawlPath,autoParse);
		//initialize the relevant word list
		wordList=new TreeSet<String>();
		stopwords=new HashSet<String>();
		initStopWords();

		inputFile=src;
		this.name=name;

		for (String line in inputFile.readLines()){
			if(line.startsWith("http"))
				this.addSeed(line);
		}

		/*do not fetch files*/
		this.addRegex("-.*\\.(jpg|png|gif|pdf|xml|mp4|mp3).*");
		/*do not fetch url contains #*/
		//this.addRegex("-.*#.*");
		System.out.println("Crawler initialized");
	}

	public void initStopWords(){
		File sw=new File(base+"stopwords.txt");
		String[] words=sw.readLines();

		if(stopwords==null)
			stopwords=new HashSet<>();
		words.each{
			stopwords.add(it.trim());
		}
		println "${words.size()} stop words read.";
	}
	@Override
	public void visit(Page page, Links nextLinks) {

		String url = page.getUrl();


		/*we use jsoup to parse page*/
		Document doc = page.getDoc();

		doc.select("script,.hidden,style,form,span").remove();
		/*extract title and content of news by css selector*/
		String title = doc.title();
		String raw = doc.body().text();

		//if the text is decoded correctly, redo decoding
		if(!isValid(raw)){
			raw=redecode(page);
		}

		//check if the website contains useful info
		if(!isUseful(raw)){
			println "${url} deemed irrelevant!"
			return;
		}
		println "${url} deemed useful!"
		//filter out useless content from the document
		doc=filter(doc);


		System.out.println(counter+"th crawl");
		System.out.println("URL:\n" + url);
		System.out.println("title:\n" + title);
		System.out.println("content:\n" + raw);
		
		if(outputFile.isDirectory()){
			println "Output crawling result to txt.";
			
			String fileName=url.replaceAll("http[s]?://","");
			fileName=fileName.replaceAll("[/\\:*><?\"\'| .]","");
			try{
				
			String p=outputFile.absolutePath+"\\${fileName}.txt";
			File newFile=new File(p);
			newFile.append(raw);
			}
			catch(Exception e){
				println "Error when outputing crawl result";
				e.printStackTrace();
			}
			println "Output complete to ${newFile.canonicalPath}";
		}
		else{
			
			outputFile.append(url+"\n");
			outputFile.append(raw+"\n");
	
		}
		/*If you want to add urls to crawl,add them to nextLinks*/
		/*WebCollector automatically filters links that have been fetched before*/
		/*If autoParse is true and the link you add to nextLinks does not match the regex rules,the link will also been filtered.*/
		// nextLinks.add("http://xxxxxx.com");

		//find href in the page and go
		Elements links=doc.select("a[href]");
		for(Element e:links){
			String s=e.attr("abs:href");
			println "Add ${s}";
			nextLinks.add(s);

		}


		counter++;

	}
	public Document filter(Document doc){
		Elements divs=doc.select("div:not(:has(div))");
		try{
			for(Element e in divs){
				String text=e.text();
				if(containStopWords(text))
					e.remove();
			}
		}catch(Exception e){
			println "Error while filtering divs"
			e.printStackTrace();
		}
		return doc;
	}
	public boolean containStopWords(String s){
		if(s==""||s==" " ||s==null){
			return false;
		}

		for(String word in stopwords){
			if(s.contains(word)){
				println "${s.substring(0,Math.min(20,s.length()))} is likely not needed due to containing ${word}."
				return true;
			}
		}
		return false;
	}
	public boolean isValid(String s){
		Pattern normal=Patterns.TEXT.value();
		if(s.length()==0){
			println "Empty string."
			return true;//since empty string has no use, each code is valid
		}

		try{


			String part=s.substring(0, Math.min(20, s.length()));
			Matcher match=normal.matcher(part);
			BigDecimal count=new BigDecimal("0");//number of meaningful char in the string
			while(match.find()){
				count.add(new BigDecimal("1"));
			}
			//if no meaningful char found in the first part of the string, deem the string as incorrectly decoded
			BigDecimal result=count.divide(part.length(), 4,RoundingMode.HALF_UP);

			if(result<0.6){
				println "Only ${count} meaningful char in the string, failed to decode."
				return false;
			}
			return true;
		}catch(Exception e){
			e.printStackTrace()
		}
	}
	public String redecode(Page p){
		//code to choose from
		String[] codes=["gbk", "ISO8859-1", "utf-8", "utf-16", "gb2312"];
		boolean solved=false;
		int i=0;
		String raw="";
		while(!solved&&i<codes.size()){
			println "Trying to decode with ${codes[i]}!"
			byte[] content=p.getContent();
			String html=new String(content,codes[i]);
			Document doc=Jsoup.parse(html,p.getUrl());
			raw=doc.text();
			solved=isValid(raw);
			if(solved)
				println "Successfully decoded with ${codes[i]}";
			i++;
		}
		if(!solved)
			println "Failed eventually in decoding.";
		return raw;
	}
	public void addKeywords(Collection<String> c){
		wordList.addAll(c);
		println "Learned ${c}"
	}
	//determine whether a website is useful for next step learning
	public boolean isUseful(String content){
		//if the word list is empty then it's in the 1st step, no need to check
		if(wordList==null||wordList.size()==0){
			println "Still studying, no need to check."
			return true;
		}
		try{

			//Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
			//	.timeout(10000).get();
			//String text = doc.body().text();

			return isRelevant(content,0.4);
			//}catch(IllegalArgumentException e){
			//			//if url is nothing at all, no need to display
			//			if(url==null||url==""||url==" ")
			//				return false;
			//			println "An exception occurred when reaching out for ${url}"
			//			//e.printStackTrace();
			//			return false;
		}catch(Exception e){
			//			println "An exception occurred when reaching out for ${url}:"
			//			println e.getClass().getSimpleName();
			e.printStackTrace();
			return false;
		}

	}
	//check the relevance of one website with a certain topic
	public boolean isRelevant(String content, float threshold){

		//segment the content
		SepManager sep=SepManager.getSepManager();
		ArrayList<String> parts=sep.segment(content);

		//accumulate the coverage of this topic in the context
		int match=0;
		parts.each{
			if(wordList.contains(it))
				match+=it.length();
		}

		def coverage=Math.round(match/content.length());
		println "Coverage is ${coverage}";
		return coverage>=threshold;
	}
	public static void main(String[] args) throws Exception{
		def queue=["八卦", "体育", "影视", "游戏", "政治", "综艺"];

		queue.each{
			File input=new File(base+"${it}train.txt");
			File output=new File(base+"${it}inspector.txt");
			GnomeCrawler crawler=new GnomeCrawler("crawl",true,it,input);
			crawler.outputFile=output;

			crawler.setThreads(5);
			crawler.setTopN(100);
			//crawler.setResumable(true);
			/*start crawl with depth of 4*/
			crawler.start(1);



		}


	}
	public void extractFeatures(){
		String source=base+"resources\\${name}\\";
		println "Start to extract features for type ${name}";
		//use all files under the folder as source
		File sourceDir=new File(source);
		Type type= new Type(name:name);
		TypeRepo.types.add(type);
		sourceDir.eachFile {
			println "Processing file ${it.name}";
			ArrayList<String> content=it.readLines();
			ArrayList<String> segments=SepManager.getSepManager().segment(content);
			HashSet<String> words=new HashSet<>();
			words.addAll(segments);
			type.record(it.name,words);
		}
		TypeRepo.startCompute();
	}
}

