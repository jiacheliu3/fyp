package gro

import java.util.regex.*
import exception.*;

class PinPoint {
	User target;
	//Read target name from user
	public init(){
		println("Input the username you want to pinpoint at: ");
		String targetName=System.in.newReader().readLine();
		println "Your target name is ${targetName}";
		target=new User(name:targetName);

		User target=keywordStudy(targetName,true);
		println "Final results:\nUser name:${targetName}\nKeywords: "+target.getKeywords();
		
		Map<String,Object> result=new HashMap<>();
		result.put("userName", target.name);
		result.put("keywords", target.getKeywords());
		return result;

	}
	//only used inside init()
	public User keywordStudy(String userName,boolean goDeeper){
		target=new User(name:userName);

		println "Loading user file"
		//current path
		def cwd="C:/Users/Tassadar/Desktop/Course/weibo/";
		def path="userFiles/"
		try{
			User target=dataMining(target,cwd+path+userName+'.txt');
			if(goDeeper){
				HashSet<String> nextTarget=target.relatedUserNames;
				nextTarget.each {
					//do not go deeper into friend's friends
					User u=keywordStudy(it,false);
					if(u!=null){
						HashSet<String> subKeywords=u.getAllKeywords();
						//for related users that may share opinions, the weight of each keyword is 1
						target.addToKeywords(subKeywords, 1);}
				}
			}
			return target;
		}catch(FileNotFoundException e){
			println "User file not found, probably no data of user "+userName;
		}catch(NullPointerException e){
			//null pointer because file is not loaded
		println "Problem loading user file for "+userName;
		}
	}
	public User dataMining(User user,String filePath){
		//load user file
		File file=new File(filePath);
		ArrayList<String> content=file.readLines();
		//search for basic info
		/* The lines follow such pattern as:
		 * Date: 2012-01-04
		 * Time: 10:24:01
		 * Forward: 0
		 * Comment: 0
		 * User Face URL: http://tp2.sinaimg.cn/2050977825/50/5623245657/1
		 * User ID: 2050977825
		 * In order to find userID it's promising to locate the lines before it.
		 */
		Pattern datePattern=Patterns.DATE.value();
		Pattern timePattern=Patterns.TIME.value();
		Pattern numPattern=Patterns.NUM.value();
		Pattern urlPattern=Patterns.URL.value();
		Pattern idPattern=Patterns.USERID.value();
		Weibo weibo;
		int startOfThis=0;

		for(int i=0;i<content.size-5;i++){
			//Check the location of metadata block
			if(content.get(i).matches(datePattern)
			&&content.get(i+1).matches(timePattern)
			&&content.get(i+2).matches(numPattern)
			&&content.get(i+3).matches(numPattern)
			&&content.get(i+4).matches(urlPattern)){
				def id=content.get(i+5);
				if(content.get(i+5).matches(idPattern)){
					//1st level info-field matching
					user.face=content.get(i+4);
					user.id=id;
					if(user.weiboList==null)
						user.weiboList=new ArrayList<>();
					println "Got user id "+id
					weibo=new Weibo(
							owner:user,
							forwardCount:content.get(i+2),
							commentCount:content.get(i+3),
							url:content.get(i+4))
					weibo.id=content[startOfThis];
					weibo.content=content[startOfThis+2..i-2];
					//weibo.content=content.subList(2,i-1);
					user.weiboList.add(weibo);
				}else{
					println "Fail to match id "+id;
				}
			}
			else if(content.get(i).startsWith("&end&")){

				if(weibo==null||weibo.content==null){
					//If weibo is not initialized there must be problem with metadata locating
					println "Error with weibo item at Line "+i;
					//Still update start of next item
					startOfThis=i+1;
					continue;
				}
				//End of one weibo item, process it as a whole
				sniffContent(weibo,content[startOfThis, i]);
				//Update startOfThis as start of next weibo
				startOfThis=i+1;
			}
		}
		println user;

		//segment into weibo
		//still need this to get whole weibo body
		StringBuilder weiboAll=new StringBuilder();
		content.each {
			if(it.startsWith('&end&')){
				//finer search into each weibo
				sniffContent(weibo,weiboAll.toString());
				weiboAll.setLength(0);
			}
			else{
				weiboAll.append(it+'\n');
			}
		}

		//at the end of data mining, print the userlist related to this user
		println "Related users in list: "+user.relatedUserNames;
		return user;
	}
	public void sniffContent(Weibo weibo,ArrayList<String> wb/*weiboBody*/){
		//process the content of weibo based on its existing content
		//how to decide the seperation between @user and content?
		Pattern atUser=Patterns.AT.value();
		Pattern urlPattern= Patterns.URL.value();

		//println weibo.content;

		Matcher atResult= weibo.content=~atUser;
		if(atResult.find())
			atResult.each {name->
				weibo.owner.addToRelatedUsers(name.toString().substring(1));
			}
		Matcher urlResult= weibo.content=~ urlPattern;
		urlResult.each {
			if(weibo.links==null)
				weibo.links=new ArrayList<>();
			weibo.links.add(it);
		}


		//check whether it's forwarded
		/* Weibo that is not forwarded share one block of content
		 * ת����URL��http://weibo.com/1200256360		
		 ��֤��Ϣ�� δ��֤	
		 ��ת���ˣ�0	
		 ��ת��΢����0		
		 Date: 2014-11-02 
		 Time: 09:27:47
		 */
		Pattern datePattern=Patterns.DATE.value();
		Pattern timePattern=Patterns.TIME.value();
		Pattern numPattern=Patterns.NUM.value();
		Pattern idPattern=Patterns.USERID.value();
		Pattern authPattern=Patterns.AUTH.value();

		for(int i=wb.size-1;i>=13;i--){

			//locate the date and time
			if(wb.get(i).matches(timePattern)&&
			wb.get(i-1).matches(datePattern)){
				//format of original weibo
				if(wb.get(i-2)=='0'&&
				wb.get(i-3)=='0'&&
				wb.get(i-4).matches(authPattern)){
					weibo.hasForward= false;
					weibo.postTime=wb.get(i);
					weibo.postDate=wb.get(i-1);
					weibo.auth=wb.get(i-4);
					weibo.owner.url=wb.get(i-5);

				}
				//format of forwarded weibo
				else if(wb.get(i-2).matches(authPattern)){
					weibo.hasForward=true;
					int contentEnd;
					boolean haveImage;
					//have image in forwarded weibo
					if(wb.get(i-3).matches(urlPattern)){
						println "Image in original weibo!";
						contentEnd=i-4;
						haveImage=true;
					}//no image url in forwarded weibo
					else{
						println "No image in original weibo!";
						contentEnd=i-3;
						haveImage=false;
					}
					weibo.forwardTime=wb.get(i);
					weibo.forwardDate=wb.get(i-1);

					//get the forwarded weibo
					/* The body of forwarded message is after the block:
					 * WeiboID: 3796889403598342
					 * UserID: 1244243145	
					 * WeiboURL: http://weibo.com/1244243145/BEExef65M 
					 * UserURL: http://weibo.com/1244243145	<- where j stops
					 * UserName: ����һ����˪	
					 * CONTENT
					 * */
					for(int j=i-3;j>Math.max(i-13, 2);j--){//10 lines sufficient for locating the content
						if(wb.get(j).matches(urlPattern)&&
						wb.get(j-1).matches(urlPattern)&&
						wb.get(j-2).matches(idPattern)){

							//get poster of this forwarded weibo
							weibo.owner.addToRelatedUsers(wb.get(j+1));

							//the info of orgPoster can be extracted from his own file, don't need to get it here
							//							User orgPoster=UserRepo.find(wb.get(j+1));
							//							orgPoster.url=wb.get(j);
							//							orgPoster.id=wb.get(j-2);

							Weibo orgWeibo=new Weibo(ownerName:wb.get(j+1),forwarder:weibo.owner,	url:wb.get(j-1),id:wb.get(j-3));
							orgWeibo.content=wb[j+2, contentEnd];
							if(haveImage){orgWeibo.imageUrl=wb.get(i-3)};
							//match the orgWeibo to this weibo
							weibo.orgWeibo=orgWeibo;
							weibo.orgWeiboId=orgWeibo.id;
							println "\nFound forwarded weibo:\n"+orgWeibo+"\nat "+orgWeibo.url;

							//dig into keywords of original weibo first
							digKeyword(orgWeibo,weibo.owner,true);
						}
					}
				}
			}
		}

		//then dig into keywords of this weibo and add them into user's keyword list
		digKeyword(weibo,weibo.owner,false);



	}
	public void sniffContent(Weibo weibo,String wb){
		sniffContent(weibo,wb.split('\n').findAll());
	}
	//dig keywords from weibo content and assign to user
	public void digKeyword(Weibo weibo,User user,boolean isOrg){
		SepManager sep=SepManager.getSepManager();

		//if weibo is original, no need to separate into direct and indirect
		if(isOrg){
			def keywords=sep.separate(weibo.content);
			println "Got keywords: "+keywords;
			user.addToKeywords(keywords,2);
		}
		else{
			//separate the weibo message into direct and indirect content(from other users)
			int c=weibo.content.indexOf("//");
			if(c==-1) c=0;
			String direct=weibo.content.substring(0, c);
			String indirect=weibo.content.substring(c);
			println "Direct:"+direct;
			println "Indirect:"+indirect;

			//for direct weibo content, weight is 2
			def directKey=sep.separate(direct);
			def indirectKey=sep.separate(indirect);

			println "Got direct keywords: "+directKey+"\nGot indirect keywords: "+indirectKey;
			user.addToKeywords(directKey,2);
			user.addToKeywords(indirectKey,1);
		}
		//first 10 keywords of user
		def tags=user.getKeywords(10);
		//println tags

	}
	//used as internal method, only to separate string!
	public void digKeyword(String content,User user){
		SepManager sep=SepManager.getSepManager();

		def keywords=sep.separate(content);
		user.addToKeywords(keywords);
	}
	public void digKeyword(def weibo,String userName){
		User user=UserRepo.find(userName);
		if(user==null){
			throw new UserNotFoundException();
		}
		digKeyword(weibo,user);
	}
	public static void main(String[] args){
		//		User u=new User(name:'whatever');
		//		PinPoint p=new PinPoint();
		//		String weibo="""//@ǧ������:
		//Ϊʲô�����ϰ���ֱ��ѡ��������Ա��
		////@������v:
		////@��ͨ��޲�Ǩ��:
		////@��������ǿ��:
		////@������:
		////@��������456:
		//ƨ�������������Ҫ������Ĵ���֯���Ǹ�ɶ�ġ�
		////@���־�ʿ-:
		//��ͨ��޲�Ǩ��:
		//ѡ��һ���ɲ�
		//����һ������??
		//����Դ����ѡ��һ���ɲ�
		//����һ������
		//����Դ��������̸��ƣ�ѡ��һ���ɲ����츣һ�����գ�ѡ��һ���ɲ�������һ�����գ�ѡ��һ���ɲ�������һ�����ա���֯����Ҫ��������ǿ�ĸɲ������ڼ��ԭ�򣬸������ֹ������Բ���֮��͸�������Ҫ�������̡���ͬ����֮�����������."""
		//
		//		Weibo w=new Weibo(content:weibo);
		//		p.digKeyword(w, u,false);
//		Pattern p=Patterns.AT.value();
//		String s="||��Ӱ@ĺ��-����||";
//		Matcher m= s=~p;
//		m.each { println it; }
		
	
	}

}
