package gro

import exception.*;
import jav.*;

class UserRepo {
	static HashSet<User> users=new HashSet<>();;
	//keep a list of names for quicker search
	static TreeSet<String> userNames=new TreeSet<>();
	static String path="C:/Users/Tassadar/Desktop/Course/weibo/userFiles/";

	public static boolean add(User u){
		users.add(u);
		userNames.add(u.name);
	}
	public static boolean remove(User u){
		userNames.remove(u.name);
		users.remove(u);
	}

	public static User find(String s){
		//if user with the name is already in Repo
		if(userNames.contains(s)){
			User target=null;
			users.each {
				if(it.name==s)
					target=it;
			}
			if(target==null)
				throw new UserNameListNotSynchroException();
			return target;
		}
		//if not in repo
		File userFile=new File(path+s+'.txt');
		if(userFile.exists()){
			//file exists but the user was not loaded, load it then
			User foundUser=loadUser(s);
			
		}//if the user file does not exist then the user must be unavailable
		else{
			//user is not in list and file is not available, must be the problem of name
			throw new UserNotFoundException();
		}
	}
	public static User loadUser(String name){
		User u=new User(name:name);
		Bootstrap.targetUsers.add(name);
		return u;
	}
	//directly load a user from file
	public static User loadUser(File userFile){
		//dummy function for now
		
	}
	//report the numbder of user
	public static void report(){
		println "${users.size()} users in UserRepo\n";
	}
	//for each user report the page rank
	public static void reportPageRank(){
		for(User it:users) {
			if(it!=null&&it.name!=null)
				println "User ${it.name} has "+it.getPageRank();
			else
				println "User is null"
		}
	}
	public static getList(){
		// return user list for temp solution
		return users;
	}
}
