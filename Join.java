import java.util.ArrayList;

public class Join {
	private Relation city;
	private Relation country;
	private ArrayList<String> ResulSet;

	public Join(Relation city, Relation country){
		this.city = city;
		this.country = country;
	}
	
	public void open(){
		city.Open();
	}
	
	public ArrayList<String> getNext(){
		String cityTuple = city.GetNext();

		ResulSet = new ArrayList<String>();
		String countryTuple = "";

		if(!cityTuple.equals(Constants.NOT_FOUND)){
			country.Open();
			countryTuple = country.GetNext();
			while(!(countryTuple.equals(Constants.NOT_FOUND))){
				boolean result = checkCondition(cityTuple, countryTuple);
				if(result == true)
					ResulSet.add(cityTuple + "," + countryTuple);
				countryTuple = country.GetNext();
			}
			country.Close();
			return ResulSet;
		} else
			return null;
			
	}
	
	public void Close(){
		
	}
	
	public boolean checkCondition(String cityTuple, String countryTuple){
		boolean result = false;
		String[] valueCity = cityTuple.split(",");
		String[] valueCountry = countryTuple.split(",");
		if(valueCity[2].equals(valueCountry[0])){
			result = true;
		}
		return result;
	}
}
