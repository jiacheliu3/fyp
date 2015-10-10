package test

class OrganizeTest {
	LinkedHashMap<String,Double> keywordOdds;
	HashSet<String> toDelete=new HashSet<>();
	public void improveResult(){
		HashSet<String> improvedHolder=new HashSet<>();
		//if A+B and A both exists, add weight of A to A+B
		keywordOdds.each{key,value->
			int l=key.length();

			ArrayList<String> comb=combination(key);
			for(String s in comb){

				String left=key.substring(0,key.indexOf(s));
				String right=key-left-s;
				Boolean deleteL;
				Boolean deleteM;
				Boolean deleteR;
				//println "L:${left} R:${right}";
				if(left==""||keywordOdds[left]!=null){
					deleteL=true;
					if(left!=""&&!improvedHolder.contains(left)){
						keywordOdds[key]+=keywordOdds[left];
						improvedHolder.add(left);
					}
				}
				if(keywordOdds[s]!=null){
					deleteM=true;
					if(!improvedHolder.contains(s)){
						keywordOdds[key]+=keywordOdds[s];
						improvedHolder.add(s);
					}
				}
				if(right==""||keywordOdds[right]!=null){
					deleteR=true;
					if(right!=""&&!improvedHolder.contains(right)){
						keywordOdds[key]+=keywordOdds[right];
						improvedHolder.add(right);
					}
				}
				if(deleteL&&deleteM&&deleteR){
					toDelete.add(left);
					toDelete.add(s);
					toDelete.add(right);
						
				}
				
			}

		}
		
		toDelete.each{
			if(it!=null&&it!="")
				keywordOdds.remove(it);
		}
	}
	public ArrayList<String> combination(String s){
		ArrayList<String> results=new ArrayList<>();
		if(s.length()==1)
			results.add(s);
		else{
			for(int i in 2..s.length()){
				for(int j in 0..s.length()-i+1){
					results.add(s.substring(j, j+i-1));
				}
			}
		}
		println results;
		return results;
	}
	public static void main(String[] args){
		OrganizeTest t=new OrganizeTest();
		t.keywordOdds=["abc":3,"bc":1,"a":1,"abcdef":1];

		t.improveResult();
		print(t.keywordOdds);
	}
}
