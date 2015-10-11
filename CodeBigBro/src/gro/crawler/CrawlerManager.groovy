package gro.crawler
import gro.*;


class CrawlerManager {
	static ArrayList<GnomeCrawler> crawlerPool;
	static crawlerNamelist;
	static String base="C:\\Users\\jiacheliu3\\git\\projects\\CodeBigBro\\data\\";
	
	static String resultOutput=base+"resources\\";

	public static void init(){
		crawlerPool=new ArrayList<>();

		//crawlerNamelist=["情感","宗教","宠物","摄影","美食","动漫","音乐","创业","健康","体育","旅游","财经","贷款","彩票","股票","住房","社会","时评","公益"];
		crawlerNamelist=["宠物","财经"];
		crawlerNamelist.each{
			File input=new File(base+"${it}train.txt");
			File output=new File(base+"${it}inspector.txt");
			GnomeCrawler crawler=new GnomeCrawler("crawl",true,it,input);
			crawler.outputFile=output;

			crawler.setThreads(1);
			crawler.setTopN(100);
			//crawler.setResumable(true);
			crawlerPool.add(crawler);
			crawler.start(1);
		}
	}
	//read the contents
	public static void read(){
		crawlerPool.each{

			File input=new File(base+"${it.name}inspector.txt");
			ArrayList<String> contents=input.readLines();
			ArrayList<String> keywords=SepManager.getSepManager().mash(contents);
			it.addKeywords(keywords);
			
			File output=new File(base+"${it.name}keywords.txt");
			output.append(keywords);
			println "Keywords output to file.";
		}

	}
	public static void main(String[] args){
		File log=new File(base+"log");
		def out=System.out;
		System.setOut(new PrintStream(new FileOutputStream(log)));
		
		//initialize crawler pool
		init();
		/*1 stage training*/
		//read training materials
		read();

		//extract keywords and store them in memory
		//extractKeywords();

		/*2 stage training*/
		//read seed website urls and crawl away, output the doc
		crawlerPool.each{
			println "Crawling out for ${it.name}";
			//output to corresponding folder
			String outputPath=resultOutput+it.name;
			File folder=new File(outputPath);
			if(!folder.exists())
				folder.mkdirs();
			it.outputFile=new File(outputPath+"\\");
			it.start(10);
		}
		
		//extract features from doc
		crawlerPool.each{
			it.extractFeatures();
		}
		
		

		System.setOut(out);
	}
	
}
