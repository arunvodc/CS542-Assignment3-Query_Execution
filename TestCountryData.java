
public class TestCountryData {

	public static void main(String[] args) {
		Relation country = new Relation("country", "");
		country.Open();

		String countryTuple = country.GetNext();
		while(!(countryTuple.equals(Constants.NOT_FOUND))){
			String[] valueCountry = countryTuple.split(",");
			System.out.println("Country Name :: "+ valueCountry[1] + "   Population :: " + valueCountry[6]);
			
			countryTuple = country.GetNext();
		}
		country.Close();
	}

}
