import java.util.ArrayList;

public class Selection {
	private ArrayList<String> Table;
	private int tableIterator;
	
	public Selection(ArrayList<String> JoinResult){
		Table = JoinResult;
	}
	public void Open(){
		tableIterator = 0;
	}
	
	public String getNext(){
		String result = "";

		if(tableIterator >= Table.size()){
			result = Constants.NOT_FOUND;
		}
		else {
			String[] valueCityCountry = Table.get(tableIterator).split(",");
			int cityPopulation = 0;
			int countryPopulation = 0;
			
			if(!valueCityCountry[4].equals("NULL")){
				cityPopulation = Integer.parseInt(valueCityCountry[4]);
			}
			
			if(!valueCityCountry[11].equals("NULL")){
				countryPopulation = Integer.parseInt(valueCityCountry[11]);
			}
			
			if(cityPopulation > 0.4 * countryPopulation)
				result =  Table.get(tableIterator);
			tableIterator++;
		}
		
		return result;
	}
	
	public void Close(){
		
	}
}
