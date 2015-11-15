import java.util.ArrayList;

public class Projection {
	private ArrayList<String> Table;
	private int tableIterator;
	
	public Projection(ArrayList<String> SelectionResult){
		Table = SelectionResult;
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
			result =  valueCityCountry[1];
			tableIterator++;
		}
		
		return result;
	}
	
	public void Close(){
		
	}
}
