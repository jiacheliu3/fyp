package gro

class PythonCaller {

	public static call(String input,String output){
		String inSnowNLP="C:\\Users\\Tassadar\\Desktop\\Course\\weibo\\temp\\snowInput.txt";
		String outSnowNLP="C:\\Users\\Tassadar\\Desktop\\Course\\weibo\\temp\\snowOutput.txt";
		String inJieba="C:\\Users\\Tassadar\\Desktop\\Course\\weibo\\temp\\jiebaInput.txt";
		String outJieba="C:\\Users\\Tassadar\\Desktop\\Course\\weibo\\temp\\jiebaOutput.txt";
		
		println "Input file is ${input}";
		//snow nlp
		Process proc0=Runtime.getRuntime().exec("python C:\\Users\\Tassadar\\eclipse\\CodeBigBroSub\\snowNLP.py ${input} ${output}")
		proc0.waitFor();
		println "Finished snowNLP"
		
		//jieba
		Process proc1=Runtime.getRuntime().exec("python C:\\Users\\Tassadar\\eclipse\\CodeBigBroSub\\jiebaSeg.py ${input} ${output}")
		proc1.waitFor();
		println "Finished jieba"
		//combine the keyword from 2 segregations
		
		File snowResult= new File(outSnowNLP);
	
		println "Done with python script";
		return snowResult.readLines();
		
	}
	
	public static main(String[] args){
		String i="C:\\Users\\Tassadar\\Desktop\\Course\\weibo\\temp\\tempInput.txt";
		String o="C:\\Users\\Tassadar\\Desktop\\Course\\weibo\\temp\\output.txt";
		def result=call(i,o);
		println result;
	}
	
}
