package gro

class JsonWrapper {
	public ArrayList<JsonObject> cargo;
	
	public JsonWrapper(){
		cargo=new ArrayList<>();
	}
	public void add(JsonObject j){
		cargo.add(j);
	}
}
