package gro.crawler
import gro.*;


class CrawlerManager {
	static ArrayList<GnomeCrawler> crawlerPool;
	static crawlerNamelist;
	static String base="C:\\Users\\Tassadar\\Desktop\\Course\\weibo\\crawler\\";

	public static void init(){
		crawlerPool=new ArrayList<>();

		crawlerNamelist=["八卦","体育","影视","游戏","政治","综艺"];
		crawlerNamelist.each{
			File input=new File("C:\\Users\\Tassadar\\Desktop\\Course\\weibo\\crawler\\${it}train.txt");
			File output=new File("C:\\Users\\Tassadar\\Desktop\\Course\\weibo\\crawler\\test\\${it}inspector.txt");
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

			File input=new File("C:\\Users\\Tassadar\\Desktop\\Course\\weibo\\crawler\\${it.name}inspector.txt");
			ArrayList<String> contents=input.readLines();
			ArrayList<String> keywords=SepManager.getSepManager().mash(contents);
			it.addKeywords(keywords);
			
			File output=new File("C:\\Users\\Tassadar\\Desktop\\Course\\weibo\\crawler\\${it.name}keywords.txt");
			output.append(keywords);
			println "Keywords output to file.";
		}

	}
	public static void main(String[] args){
		File log=new File("C:\\Users\\Tassadar\\Desktop\\Course\\weibo\\crawler\\log");
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
		//read seed website urls and crawl away
		crawlerPool.each{
			println "Crawling out for ${it.name}";
			
			it.outputFile=new File("C:\\Users\\Tassadar\\Desktop\\Course\\weibo\\crawler\\${it.name}crawlResult.txt");
			it.start(4);
		}
		
		//keyword extraction

		System.setOut(out);
	}
}
