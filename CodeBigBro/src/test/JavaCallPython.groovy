package test

	Process proc=Runtime.getRuntime().exec("python C:\\Users\\Tassadar\\eclipse\\PyTest\\slow\\apiTest.py")
	proc.waitFor();
	
	println "Done"

