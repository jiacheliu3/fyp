package gro.crawler
import gro.*;


class CrawlerManager {
	static ArrayList<GnomeCrawler> crawlerPool;
	static crawlerNamelist;
	static String base="C:\\Users\\jiacheliu3\\git\\projects\\CodeBigBro\\data\\";
	
	static String resultOutput=base+"resources\\";

	public static void init(){
		crawlerPool=new ArrayList<>();

		//crawlerNamelist=["���","�ڽ�","����","��Ӱ","��ʳ","����","����","��ҵ","����","����","����","�ƾ�","����","��Ʊ","��Ʊ","ס��","���","ʱ��","����"];
		crawlerNamelist=["����","�ƾ�"];
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
