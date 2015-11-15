import java.util.ArrayList;

public class QueryExecution {
	
	public QueryExecution(){
		Relation city = new Relation("city","");
		Relation country = new Relation("country","");
		ArrayList<String> JoinResult = new ArrayList<String>(); 
		Join J = new Join(city, country);
		J.open();
		while(true){
			ArrayList<String> temp = new ArrayList<String>();
			temp = J.getNext();
			if(temp == null){
				J.Close();
				break;
			}

			for(int i=0;i < temp.size();i++){
				JoinResult.add(temp.get(i));
			}
		}
		
		System.out.println("Result Set length of Join operator" + JoinResult.size());
		
		ArrayList<String> SelectionResult = new ArrayList<String>(); 
		Selection S = new Selection(JoinResult);
		S.Open();
		while(true){
			String temp = S.getNext();
			if(temp.equals(Constants.NOT_FOUND)){
				S.Close();
				break;
			}
			if(temp != "")
				SelectionResult.add(temp);
		}
		
		System.out.println("Result Set length of Selection operator" + SelectionResult.size());
		
		Projection P = new Projection(SelectionResult);
		P.Open();
		System.out.println("Name of cities having population greater than 40 percent of the country population are :");
		while(true){
			String temp = P.getNext();
			if(temp.equals(Constants.NOT_FOUND)){
				P.Close();
				break;
			}
			System.out.println(temp);
		}
	}
}
