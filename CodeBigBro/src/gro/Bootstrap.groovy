package gro

import jav.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import groovy.time.*

public class Bootstrap {
	static HashSet<String> userSet=new HashSet<>();
	static String lastUserName='';
	static StringBuilder itemHolder=new StringBuilder();
	//store the list of target users
	static ArrayList<String> targetUsers=new ArrayList<>();
	//data path
	static String dirPath="C:/Users/Tassadar/Desktop/Course/weibo/userFiles";
	//static String path = "C:/Users/Tassadar/Desktop/Course/weibo/test5.txt";
	static String path = "C:/Users/Tassadar/Desktop/Course/weibo/UserWeibos201201";
	//static String path = "C:/Users/Tassadar/Desktop/Course/weibo/UserWeibos201502";
	static int exitState=-1;
	//static String output="C:/Users/Tassadar/eclipse/CodeBigBro/visualization/data/vis.json";
	static String output="C:/Users/Tassadar/eclipse/CodeBigBro/visualization/data/vis.gexf";

	
	public static void main(String[] args) {

		def result=start();
		if(result instanceof Integer)
			exitState=result;
		println "Exit with state ${exitState}";

	}
	//react to options and generate a http request as return
	public static start(String option){

		//set output to a file as log
		String logPath="C:/Users/Tassadar/Desktop/Course/weibo/logs/";
		def folder=new File(logPath);
		if(!folder.exists())
			folder.mkdir();
		def time=new Date().format("YYYY-MM-dd_HHmmss");;
		File log=new File(logPath+time+".log");
		def out=System.out;
		System.out = new PrintStream(new BufferedOutputStream(new FileOutputStream(log)));
		
		def result;
		switch(option){
			case '1':
				result=studyFile();
				break;

			case '2':
				result=scanAll();
				break;
			case '3':
				result=pageRank();
				break;
			case '4':
				result=visualize();
				break;
			case '5':
				result=studyUser();
				break;
			case '9':
				return new Integer(0);

			default:
				println "Invalid option! Input option again:"

		}
		
		//reset system output
		System.out=out;
		
		
		return result;


	}
	public static int start(){
		println """Input 1 for data file mining,
				2 to scan all user files,
				3 for keyword ranking,
				4 for visualization,
				5 for user mining(only available after 1 has been carried out once to generate user files.)
				9 to quit""";

		String option=System.in.newReader().readLine();
		for(;;){
			start(option);
			println """Input 1 for data file mining,
				2 to scan all user files,
				3 for keyword ranking,
				4 for visualization,
				5 for user mining(only available after 1 has been carried out once to generate user files.)
				9 to quit"""
			option=System.in.newReader().readLine();
			if(option=='9')
				return 0;

		}

	}

	private static visualize(){
		GephiManager.run();
		return ["nodeCount":KeywordRepo.outputSize];
	}
	private static studyUser() {
		def timeStart = new Date();
		PinPoint pinpoint=new PinPoint();
		def result=pinpoint.init();
		def timeEnd=new Date();
		println TimeCategory.minus(timeEnd, timeStart);
		return result;
	}

	private static studyFile() {
		def timeStart = new Date();
		println "Target file is by default ${path}, processing..."
		def result=readFile(path);
		def timeEnd=new Date();
		println TimeCategory.minus(timeEnd, timeStart)
		return result;
	}
	//set exit state
	public static void setExitState(int i){
		exitState=i;
	}
	//scan all users' keywords and apply PageRank algo
	public static scanAll(){
		println "Keyword study of all user files!"
		def folder=new File(dirPath);
		def files=folder.listFiles();

		PinPoint p=new PinPoint();
		files.each{
			String name=it.name.tokenize('.')[0];
			println name;
			try{
				User u=p.keywordStudy(name, false)
				//add the user to user list
				UserRepo.add(u);
				//add the keywords to keyword repository and establish relationship among them
				KeywordRepo.add(u.getAllKeywords());
				KeywordRepo.relate(u.getAllKeywords());
				}
			catch(NullPointerException e){
				println "Problem processing user "+name;
			}
		}
		//After scanning all files
		UserRepo.report();
		KeywordRepo.report();

		//get data from repositories
		def users=UserRepo.getList();
		println users.class;
		def keywords=KeywordRepo.getData(false);
		println keywords.class;
		
		Map<String,Object> result=new HashMap<>();
		//users is a Set of all User objects
		result.put("users", users);
		//keywords is a Map containing keyword and pagerank
		result.put("keywords", keywords);
		
		return result;


	}
	//calculate pagerank
	public static pageRank(){
		KeywordRepo.constructMatrix();
		UserRepo.reportPageRank();
		//output to json file for display
		File out=new File(output);
		//rewrite if existing
		if(out.exists())
			out.setText('');
		KeywordRepo.setOutputSize(1000);
		//KeywordRepo.recordRelationsToJson(out);
		KeywordRepo.recordRelationsToGexf(out);
		//display the visualization
		println "Gexf generated at ${output}";
		
		HashMap<String,Object> result=new HashMap<>();
		result.put("pagerank", KeywordRepo.rankMap);

		return result;
	}
	public static readFile(String path) {
		FileInputStream inputStream = null;
		Scanner sc = null;
		def result;
		try {
			inputStream = new FileInputStream(path);
			sc = new Scanner(inputStream, "UTF-8");
			//read weibo content file
			result = recursiveRead(sc);
			// note that Scanner suppresses exceptions
			if (sc.ioException() != null) {
				throw sc.ioException();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (sc != null) {
				sc.close();
			}
			return result;
		}
	}
	//Read only one item. Not caring about the content.
	public static  recursiveRead(Scanner sc){
		int count=0;

		//Initial: the 1st String is empty and next2 looks at the 1st String of the File
		StringBuilder item=new StringBuilder();
		String next1='';
		String next2=sc.next();

		//Target weiboID:'12345XXX||12345XXX'
		for(;;){
			//Look forward
			if(!sc.hasNext()){
				//Need to check if null
				item.append(next1+"\n");
				item.append(next2+"\n");
				try{
					formatItem(item.toString());
					writeToUserFile(lastUserName);

				}
				catch(FileNotFoundException e){
					println "Cannot be written!"
					e.printStackTrace();
				}
				break;
			}
			String next3=sc.next();

			if(next2.endsWith('||')){
				//Deal with '||' appears in weibo content
				if(!next1.contains('||')&&!next3.contains('||')){
					item.append(next1+"\n");
					next1=next2;
					next2=next3;
					continue;
				}

				//Save the last item first
				item.append(next1+'\n');
				try{
					formatItem(item.toString());
				}
				catch(FileNotFoundException e){
					println "Cannot be written!"
					e.printStackTrace();
				}

				println("Item Count:"+ ++count);
				//Renew the item
				item.setLength(0);
				//next2 should be weiboID or userID
				String id=next2.substring(0, next2.length()-2);
				println("weiboID: "+id);

				if(next3=='||'){
					item.append(next2+sc.next()+'\n');
					next1=sc.next();
					next2=sc.next();
				}else{
					item.append(next2+next3.substring(2)+'\n');
					next1=sc.next();
					next2=sc.next();
				}

			}
			else{
				if(next3=='||'){
					String next=sc.next();
					//if(!next2.contains('||')&&!next.contains('||')){
					if(!next2.matches(Patterns.STARTID.value())&&!next.matches(Patterns.ENDID.value())){
						item.append(next1+"\n"+next2+'\n');
						next1=next3;
						next2=next;
						continue;
					}
					//Start of one item and end the last item, next1 will be the end of last item
					item.append(next1+"\n");
					try{
						formatItem(item.toString());
						println("Item Count:"+ ++count);
						//Renew the item
						item.setLength(0);
						//Then next2 should be weiboID or userID
						String id=next2;
						println("weiboID: "+id);
						next1=next2;
						next2=next3;
						if(next2=='||'){
							item.append(next1+'||'+next+'\n');
							next1=sc.next();
							next2=sc.next();
						}else{
							item.append(next1+next+'\n');
							next1=sc.next();
							next2=sc.next();
						}
					}
					catch(FileNotFoundException e){
						println "Cannot be written!"
						e.printStackTrace();
						//dispose the content
						item.setLength(0);
					}
				}else{
					item.append(next1+"\n");
					next1=next2;
					next2=next3;
				}
			}


		}

		//return a Map as result
		def result=["weiboCount":count];
		return result;
	}
	public static void writeToUserFile(String userName){
		println("Writing to "+ userName);

		// Create a File object representing the folder 'A/B'
		def folder = new File( 'C:/Users/Tassadar/Desktop/Course/weibo/userFiles' )

		// If it doesn't exist
		if( !folder.exists() ) {
			// Create all folders up-to and including B
			folder.mkdirs()
		}

		// Then, write to file.txt inside B
		File target=new File( folder, userName+'.txt' )
		try{
			target.withWriterAppend { w ->
				//println 'Test write '+itemHolder.toString().length()<10?itemHolder.toString():itemHolder.toString().substring(0, 10)
				String data=itemHolder.toString();
				if(data.charAt(data.length()-1)!='\n')
					data=data+'\n';
				w << data;
			}
		}
		catch(FileNotFoundException e){
			println "File name is invalid!"
			e.printStackTrace()

		}
		finally{return;}
	}
	//Format the piece of weibo
	public static void formatItem(String weibo){
		if(weibo==null||weibo.length()==0)
			return
		else if(weibo.length()==1&&Character.isWhitespace(weibo.charAt(0)))
			return
		else if(weibo.length()<30)
			println "Formatting weibo"+weibo+'..'
		else
			println("Formatting weibo"+weibo.substring(0, 30))+'...';

		//find userName of weibo
		Scanner reader=new Scanner(weibo)
		//User name at 2nd line
		String weiboID=reader.nextLine();
		String userName=reader.nextLine();
		println userName;

		//If the weibo belongs to same user as last one, delay to write until seeing a new user's weibo
		if(lastUserName==null||lastUserName==''||userName==lastUserName){
			itemHolder.append(weibo+'&end&\n');
			lastUserName=userName;
			return
		}
		else{

			writeToUserFile(lastUserName)
		}

		//Reset string builder after output
		itemHolder.setLength(0);
		itemHolder.append(weibo+'&end&\n');
		lastUserName=userName;
		return
	}


}