package test

class SohuMama {
	public static void main(String[] args){
		File out= new File("C:\\Users\\Tassadar\\Desktop\\Course\\weibo\\crawler\\Éç»átrain.txt");
		for (int i in 5787..5885){
			out.append("http://news.sohu.com/shehuixinwen_${i}.shtml\n");
			
		}
		println "Done"
	}
}
