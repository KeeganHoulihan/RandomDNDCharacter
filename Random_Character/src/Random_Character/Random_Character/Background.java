package Random_Character;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Background {
	private String background;
    List<String> backgrounds = new ArrayList<>();
	private Random random = new Random();
	
	public Background() throws IOException
	{
		
		backgrounds = loadBackgrounds();
		background = backgrounds.get(random.nextInt(backgrounds.size()));
	}
	
	public void setBackground(String setBackground)
	{
		background = setBackground;
	}
	
	public String getBackground()
	{
		return background;
	}
	
	public List<String> loadBackgrounds() throws IOException 
	{
	    List<String> backgrounds = new ArrayList<>();
	    try (BufferedReader reader = new BufferedReader(new FileReader("BackgroundList")))
	    {
	        String line;
	        while ((line = reader.readLine()) != null) 
	        {
	            backgrounds.add(line);
	        }
	    }
	    return backgrounds;
	}
	@Override
	public String toString() {
	    return background;
	}
}
