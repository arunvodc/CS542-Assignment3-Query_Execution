import java.util.ArrayList;

public class QueryExecution2 {
	
	public QueryExecution2(){
		Relation city = new Relation("city","");
		Relation country = new Relation("country","");
		ArrayList<String> JoinResult = new ArrayList<String>(); 
		Join2 J = new Join2(city, country);
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
		
		Projection P = new Projection(JoinResult);
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

