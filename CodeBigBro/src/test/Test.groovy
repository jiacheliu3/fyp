package test

import Jama.Matrix
import java.util.regex.*;

class Test {
	public static void main(String[] args){
		def s="aaaaa";
		int num=Math.ceil(Math.sqrt(s.length()));
		println num;
		
		Map m=["a":"A","b":"B","c":"C"];
		for(def e:m){
			println e.value;
		}
		
//		String logPath="C:/Users/Tassadar/Desktop/Course/weibo/logs/";
//		def folder=new File(logPath);
//		if(!folder.exists())
//			folder.mkdir();
//		def time=new Date().format("YYYY-MM-dd_HHmmss");;
//		File log=new File(logPath+time+".log");
//		if(!log.exists()){
//			log.write("Logging: ${time}");
//			println "Done"
//		}
		String str=".,:;不要过滤我。。。abc012";
		String x=str.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z\\-_0-9]", "");
		println x
		
		Map mmm=new HashMap();
		println mmm instanceof Collection;
	}
}
