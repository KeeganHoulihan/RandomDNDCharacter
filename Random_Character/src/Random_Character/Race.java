
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Race {
	private String race;
    List<String> races = new ArrayList<>();
	private Random random = new Random();
	
	public Race() throws IOException
	{
		
		races = loadRaces();
		race = races.get(random.nextInt(races.size()));
	}
	
	public void setRace(String setRace)
	{
		race = setRace;
	}
	
	public String getRace()
	{
		return race;
	}
	
	public List<String> loadRaces() throws IOException 
	{
	    List<String> races = new ArrayList<>();
	    try (BufferedReader reader = new BufferedReader(new FileReader("RaceList")))
	    {
	        String line;
	        while ((line = reader.readLine()) != null) 
	        {
	            races.add(line);
	        }
	    }
	    return races;
	}
	@Override
	public String toString() {
	    return race;
	}
}
