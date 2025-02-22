
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Alignment {
	private String alignment;
    List<String> alignments = new ArrayList<>();
	private Random random = new Random();
	
	public Alignment() throws IOException
	{
		alignments = loadAlignments();
		alignment = alignments.get(random.nextInt(alignments.size()));
	}
	
	public void setAlignment(String setAlignment)
	{
		alignment = setAlignment;
	}
	
	public String getAlignment()
	{
		return alignment; //Returns the characters alignment
	} 
	
	public List<String> loadAlignments() throws IOException 
	{
	    List<String> alignments = new ArrayList<>();
	    try (BufferedReader reader = new BufferedReader(new FileReader("AlignmentList")))
	    {
	        String line;
	        while ((line = reader.readLine()) != null) 
	        {
	        	alignments.add(line);
	        }
	    }
	    return alignments;
	}
	@Override
	public String toString() {
	    return alignment;
	}
}
