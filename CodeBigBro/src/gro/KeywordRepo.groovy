package gro

import Jama.Matrix;
class KeywordRepo {

	static HashSet<String> keywords=new HashSet<>();
	static Map<String,Set<String>> edgeMap=new HashMap<>();
	static Map<Integer,String> orderMap=new TreeMap<>();
	static Map<String,Double> rankMap=new LinkedHashMap<>();
	static double dampening=0.85;
	//by default visualize 1000 top keywords
	static int outputSize=1000;
	static LinkedHashMap<String,Set<String>> relationOfTop;

	public static void add(String s){
		keywords.add(s);
	}
	public static void add(Collection c){
		keywords.addAll(c);
	}
	public static void report(){
		println "${keywords.size()} keywords in the KeywordRepo"
	}
	//establish relation within a set of keywords
	public static void relate(Collection<String> c){
		println "Establish relationship within collection of ${c.size()} keywords."
		//for each string in c, establish the relation from it to every other string in c
		c.each{
			//initialize the linklist if not done
			if(!edgeMap.containsKey(it))
				edgeMap.put(it,new HashSet<String>() );
			for(String s:c){
				if(s!=it)
					edgeMap[it].add(s);
			}
		}
	}
	public static void constructMatrix(){
		//check the size of keywords and relationMap
		int n=keywords.size();
		int m=edgeMap.size();
		println "Keyword count: ${n}\nNumber of keywords with relationship recorded ${m}"

		//decide the order of getting items from set
		int i=0;
		keywords.each {
			orderMap.put(i, it);
			i++;
		}
		//initialize the matrix with order decided
		println "Initializing connection matrix"
		Matrix H=new Matrix(n,n);
		orderMap.each {r->
			/*decide the row number*/
			int row=r.key;
			int num=edgeMap[r.value].size();
			//initialize the weight of each cell of this row
			double val;
			if(num==0)
				val=1/n;
			else
				val=1/num;
			//r.value is the keyword, get it's link list
			edgeMap[r.value].each{c->
				/*decide the column number*/
				//set the corresponding cell in matrix
				def column=orderMap.find{key,value->value==c};
				int col=column.key;
				H.set(row, col, val);

			}
			if(row%100==0)
				println "Finished line ${row}"

		}
		Matrix tempH=H.getMatrix(1, 20, 1, 20);
		tempH.print(20, 3);

		//calculate the pagerank
		println "Calculating pagerank";
		int iter = ((int) Math.abs(Math.log((double) n)
				/ Math.log((double) 10))) + 1;
		println "Calculate for ${iter} iterations";

		Matrix I=new Matrix(n,n,1.0);
		Matrix G;
		
		G=H.times(dampening).plus(I.times(1-dampening));
		
		Matrix tempG=G.getMatrix(1, 20, 1, 20);
		//tempG.print(20,3);
		
		//pi[0]=[1,1,1,...];
		Matrix pi=new Matrix(1,n,1);
		for(int j in 0..iter){
			println "${j} iteration:"
			pi=pi.times(G);
			//normalize pi
			pi=normalize(pi);
			//just for display purpose
			Matrix tempPi=pi.getMatrix(0, 0, 1, 20);
			tempPi.print(20, 3);
		}

		println "Assign the final Pi";
		orderMap.each{key,value->
			rankMap.put(value, pi.get(0, key));
		}
		//sort rankMap, bigger pagerank first
		rankMap=rankMap.sort{a,b->
			b.value <=> a.value;
		}
		//find max pagerank
		println rankMap;



	}
	public static double getPageRank(String s){
		try{
			double ans=rankMap[s];
			if(ans==null){
				println "No pagerank calculated yet!"
				return 0;
			}
			return ans;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/*@author Eric Eaton (EricEaton@umbc.edu)
	 *         University of Maryland Baltimore County
	 * @version 0.1
	 * */
	public static Matrix normalize(Matrix m) {
		int numRows = m.getRowDimension();
		int numCols = m.getColumnDimension();

		// compute the sum of the matrix
		double sum = 0;
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				sum += m.get(i, j);
			}
		}

		// normalize the matrix
		Matrix normalizedM = new Matrix(numRows, numCols);
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				normalizedM.set(i, j, m.get(i, j) / sum);
			}
		}

		return normalizedM;
	}

	public static void setOutputSize(int i){
		println "Setting the wanted pagerank size as ${i}"
		if(i>rankMap.size()){
			println "Required size greater than existing keyword count! Setting size as current keyword size."
			outputSize=rankMap.size();
			return;
		}

		outputSize=i;
		println "Done!"
	}
	public static void recordRelationsToJson(File file){
		PrintWriter writer=new PrintWriter(file);
		try{
			//write beginning of json
			writer.println( """{
"nodes":[""");
			//for each keyword to write
			/*"name": "The Boys Of Summer",
			 "artist": "Don Henley",
			 "id": "the_boys_of_summer_don_henley",
			 "playcount": 924750
			 * */
			LinkedHashMap<String,Double> content=getTopRank(outputSize);
			int c=1;//count the number of output
			content.each{key,value->
				double m=Math.random().round(3);
				String s="""{
					"name":"${key}",	
					"artist":"${key}",
					"match":${m},
					"id":"${key}",
					"playcount":${Math.round(value*1000000)}
					}""";
				writer.println(s);
				if(c<content.size())
					//print , as separator if not the last element
					writer.println(',');
				c++;
			}
			//write end of items
			writer.println("],\n");
			//write start of links
			writer.println("\"links\": [");
			//for each link
			LinkedHashMap<String,Set<String>> relations=getRelationOfTop(content.keySet());
			/*
			 * "source": "uptown_girl_billy_joel",
			 "target": "tell_her_about_it_billy_joel"
			 */
			int d=1;//count till the end of output
			int num=1;
			relations.each{key,value->
				//println "${key} has edges ${value}"
				//d becomes 1000 on the 999th iter
				d++;
				//value is a Set<String>
				Iterator<String> iter = value.iterator();
				while(iter.hasNext()){

					String s="""{
						"source":"${key}",
						"target":"${iter.next()}"
}"""
					writer.println(s);
					//if not the last element to print, print , as separator
					if(d>=relations.size()&&!iter.hasNext()){
						//print nothing if already reach the end of print
					}
					else{
						writer.println(',');
					}
					num++;

				}

			}
			println "${num} edges kept at last"

			//write end of json
			writer.println("]\n}");

		}
		catch(FileNotFoundException e){
			println "File is not found!"
			e.printStackTrace()

		}
		finally{writer.close();}
	}
	//record info to .gexf file for Gephi
	public static void recordRelationsToGexf(File file){
		PrintWriter writer=new PrintWriter(file);
		try{
			//write beginning of json
			writer.println( """<?xml version="1.0" encoding="UTF-8"?>
<gexf xmlns="http://www.gexf.net/1.1draft"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.gexf.net/1.1draft http://www.gexf.net/1.1draft/gexf.xsd"
    version="1.1">
			<graph mode="static" defaultedgetype="undirected">
				<nodes>""");
			//for each keyword to write
			//<node id="4941" label="YBR236C"/>
			LinkedHashMap<String,Double> content=getTopRank(outputSize);
			int c=1;//count the number of output
			content.each{key,value->
				double m=Math.random().round(3);
				String s="""<node id="${key}" label="${key}"/>""";
				writer.println(s);

			}
			//write end of items
			writer.println("</nodes>");
			//write start of links
			writer.println("<edges>");
			//for each link
			LinkedHashMap<String,Set<String>> relations=getRelationOfTop(content.keySet());
			/*
			 * <edge id="13188" source="4941" target="4941"/>
			 */

			int num=1;
			relations.each{key,value->
				println "${key} has edges ${value}"

				//value is a Set<String>
				value.each{

					String s="""<edge id="${num}" source="${key}" target="${it}"/>"""
					writer.println(s);
					num++;
				}

			}
			println "${num} edges kept at last"

			//write end of json
			writer.println("""</edges>
			</graph>
</gexf>""");

		}
		catch(FileNotFoundException e){
			println "File is not found!"
			e.printStackTrace()

		}
		finally{writer.close();}
	}
	//return top rank k elements
	public static LinkedHashMap<String,Double> getTopRank(int i){
		//store top k elements
		LinkedHashMap<String, Double> result=new LinkedHashMap<>();
		int count=0;
		//use the static rankMap in the class
		for(def e in rankMap){
			result.put(e.key, e.value);
			count++;
			if(count>=i){
				println "Time to return"
				return result;
			}
		}
		//in case the loop does not work
		return result;
	}

	//return the relations of top rank elements
	public static LinkedHashMap<String,Set<String>> getRelationOfTop(Set<String> s){
		//store their relations
		HashMap<String,Set<String>> relations=new HashMap<>();
		s.each{
			//only keep the relations among left keywords
			Set kept=edgeMap[it].intersect(s);
			relations.put(it, kept);
		}
		println "After filtering on the edges, ${relations.size()} keywords have edges left. "
		return relations;
	}

	//return data for display in http
	public static getData(boolean displayAll){
		if(displayAll)
			return rankMap;
		else
			return getTopRank(500);
	}
	public static getData(int i){
		return getTopRank(i);
	}
}
