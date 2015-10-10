package jav;

import java.util.regex.Pattern;

import cn.edu.hfut.dmic.webcollector.crawler.BreadthCrawler;
import cn.edu.hfut.dmic.webcollector.model.Links;
import cn.edu.hfut.dmic.webcollector.model.Page;

import org.jsoup.nodes.Document;

public class GameCrawler extends BreadthCrawler{

	int counter=0;
	
    public GameCrawler(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);
        /*start page*/
        this.addSeed("http://game.hao123.com/");

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
    
    @Override
    public void visit(Page page, Links nextLinks) {
        String url = page.getUrl();
        /*if page is news page*/
        if (counter<99) {
            /*we use jsoup to parse page*/
            Document doc = page.getDoc();

            /*extract title and content of news by css selector*/
            String title = doc.title();
            String content = doc.html();

            System.out.println(counter+"th crawl");
            System.out.println("URL:\n" + url);
            System.out.println("title:\n" + title);
            System.out.println("content:\n" + content);

            /*If you want to add urls to crawl,add them to nextLinks*/
            /*WebCollector automatically filters links that have been fetched before*/
            /*If autoParse is true and the link you add to nextLinks does not match the regex rules,the link will also been filtered.*/
            // nextLinks.add("http://xxxxxx.com");
            nextLinks.add(url);
            counter++;
        }
        else
        	return;
    }
    
    public static void main(String[] args) throws Exception{
    	GameCrawler crawler=new GameCrawler("crawl",true);
    	crawler.setThreads(5);
        crawler.setTopN(100);
        //crawler.setResumable(true);
        /*start crawl with depth of 4*/
        crawler.start(4);
    }
}
