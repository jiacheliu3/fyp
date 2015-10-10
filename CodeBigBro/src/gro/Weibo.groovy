package gro

class Weibo {
	def id;
	
	def subID;
	User owner;
	String ownerName;
	def url;
	String content;
	def commentCount;
	def comments;
	def forwardCount;
	//authorization info
	def auth;
	//Url of image if any
	def imageUrl;
	//poster and forwarder can be owner
	def poster;
	def postDate;
	def postTime;
	//if the weibo is forwarded then there should be original message
	boolean hasForward;
	Weibo orgWeibo;
	def orgWeiboId;
	def forwarder;
	def forwardDate;
	def forwardTime;
	
	//links in the content
	ArrayList<String> links;
	
	public boolean checkForward(){
		
	}

	@Override
	public String toString(){
		"""ID:${id}(${subID})
			Owner:${ownerName}
			Body:${content}"""
	}
	@Override
	public int hashCode(){
		id.hashCode()+content.hashCode()
	}
}
