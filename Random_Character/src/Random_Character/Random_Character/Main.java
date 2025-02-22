package Random_Character;

import java.util.Scanner;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
	{
		Scanner scan = new Scanner(System.in); //Scanner
		System.out.print("What level is your character?: "); //Ask user what level the character they want is
		int inLevel = scan.nextInt(); //Scans for next input
		while(inLevel > 20 || inLevel < 1)
		{
			System.out.print("Sorry, please input a level between 1 and 20: ");
			inLevel=scan.nextInt();
		}
		System.out.print("How many classes would you like?: "); //Ask user what level the character they want is
		int inMaxClass = scan.nextInt(); //Scans for next input
		while(inMaxClass > 13 || inMaxClass < 1)
		{
			System.out.print("Sorry, please input a number of classes between 1 and 13: ");
			inMaxClass=scan.nextInt();
		}
		Character character = new Character(inLevel, inMaxClass);
		System.out.println(character.getStats());
		System.out.println(character.getRace());
		System.out.println(character.getCharClass());
		System.out.println(character.getBackground());
		System.out.println(character.getAlignment());
		scan.close();
	}
}
}