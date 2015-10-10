package gro

class User {
	def name;
	def id;
	def url;
	//store URL of user face image
	def face;
	//store personal info such as address,phone,email etc
	def info;
	//store interested areas
	def interest;
	//store IDs of friends
	def friends;
	//store other key words that are not yet categorized
	LinkedHashMap<String,Double> keywords;
	boolean keysNeedSort;
	//store users that appear in this user's content(share same weibo or forwarding)
	HashSet<User> relatedUsers;
	HashSet<String> relatedUserNames;
	//store weibo of the user
	HashSet<Weibo> weiboList;

	public File findWeiboFile(){
		return new File("C:/Users/Tassadar/Desktop/Course/weibo/userFiles/"+name+".txt");
	}
	//add user with name s to this user's relation list, create the user if not existing
	public void addToRelatedUsers(String s){
		if(relatedUserNames==null)
			relatedUserNames=new HashSet<>();
		//Ignore if weibo is his own
		if(s==name)
			return ;
		//If relation is not yet known
		if(relatedUserNames.add(s)){
			println "Establised relation ${name}-"+s;
			//add this user to target list
			Bootstrap.targetUsers.add(s);
		}
		else
			println "Relation already established ${name}-"+s;

	}
	//increase the weight by 1 by default
	public void addToKeywords(String str){
		if(keywords==null)
			keywords=new LinkedHashMap<>();
		if(keywords.containsKey(str)){
			keywords[str]++;
		}else
			keywords.put(str,1);
		//println "Keywords added to ${name}\n"+str;

	}
	//controlled increase of weight
	public void addToKeywords(String str,def weight){
		if(keywords==null)
			keywords=new LinkedHashMap<>();
		if(keywords.containsKey(str)){
			keywords[str]+=weight;
		}else
			keywords.put(str,weight);
		//println "Keywords added to ${name}\n"+str;

	}
	public void addToKeywords(Collection strs){
		if(keywords==null)
			keywords=new LinkedHashMap<>();
		strs.each {addToKeywords(it)}
		//Need to sort keyword map by frequency
		keysNeedSort=true;

	}
	public void addToKeywords(Collection strs,def weight){
		if(keywords==null)
			keywords=new TreeMap<>();
		strs.each {addToKeywords(it,weight)}
		//Need to sort keyword map by frequency
		keysNeedSort=true;

	}
	//new method to use: use multiple weighted keyword extractors
	public void addToKeywords(Map map,int weight){
		if(keywords==null)
			keywords=new LinkedHashMap<>();
		
		for(def e:map){
			if(!keywords.containsKey(e.key)){
				println "New key ${e.key}";
				keywords.put(e.key, (double)e.value*weight);
			}else{
				println "Existing key ${e.key}";
				double v=e.value*weight;
				keywords[e.key]+=v;
			}
		}
		if(map.size()<=1)
			return;
		keysNeedSort=true;
	}
	public ArrayList<String> getKeywords(int n){
		//Can check if sorted for efficiency!

		if(keysNeedSort){
			keywords=keywords.sort{a,b->
				//sort the map desc by value
				b.value <=> a.value;
			}
			keysNeedSort=false;
		}
		//println keywords;
		if(n>keywords.size()){
			n=keywords.size();
		}
		def result=keywords.entrySet().toList()[0..<n];
		//println result;
		//print the result for record
		//result.each{ println "${it} has ${keywords[it]}" }

		return result;

	}

	public ArrayList<String> getKeywords(){
		return getKeywords(10);
	}

	public ArrayList<String> getAllKeywords(){
		println keywords.keySet();
		return keywords.keySet();
	}

	//use the top 20 keywords to calculate average pagerank
	public double getPageRank(){
		ArrayList<String> keywords=getKeywords(20);
		double sum=0;
		keywords.each {
			sum+=KeywordRepo.getPageRank(it.key);
		}
		double avg=sum/keywords.size();
	}
	public double getPageRank(int i){

	}
	@Override
	public String toString(){
		"User ${id} ${name} has keywords ${keywords}"
	}
	@Override
	public int hashCode(){
		name.hashCode()
	}
	//testing purpose only
	public static void main(String[] args){
		User u=new User(name:'aaaaa');
		u.addToKeywords(['b', 'b', 'b', 'a', 'c', 'c', 'fff']);

		u.getKeywords(3);
	}

}
