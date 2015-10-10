package gro

public class Facade {

	static result;
	public static getResult(){
		return "Finally!";
	}
	public static getResult(int i){
		return getResult(new Integer(i).toString());
	}
	public static getResult(String s){
		return Bootstrap.start(s);
	}
}
