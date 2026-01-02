

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.StringTokenizer;


/**
 * Lazy Coder's Best Friend
 *
 * @author: Halley Nengovhela
 */

public class LazyCoder {
	private static File hostResponse = new File("C:\\LazyCoder\\hostResponse.txt");
	private static File specInput = new File("C:\\LazyCoder\\Lazy.txt");
	private static StringBuffer output = new StringBuffer();
	private static StringBuffer specText = new StringBuffer();
	private static BufferedReader reader = null;
	private static StringTokenizer st;
	private static HashMap<Integer, String> positions = new HashMap<Integer, String>();
	private static HashMap<Integer, String> fieldNames = new HashMap<Integer, String>();
	private static String tempLine = "";
	private static boolean goHome = false;
	private static String hostFile = "";
	private static String chars = "";
	private static String currentResponse = "";
	private static FileWriter outFile;
	private static PrintWriter out;
	private static String temp;
	private static int whiteSpaces = 0;
	private static int repeatBlockStart = 0;
	private static int repeatBlockStop = 0;
	
	public static void main(String[] args) {

		try{
			reader = new BufferedReader(new FileReader(hostResponse));
			hostFile = reader.readLine();
			hostFile = hostFile.substring(hostFile.indexOf("R00000")+6, hostFile.length());
			reader = new BufferedReader(new FileReader(specInput));
			int counter = 0;
			while ((tempLine = reader.readLine()) != null) {
				if(tempLine.trim().length() != 0){
					st = new StringTokenizer(tempLine, "(");
					if(st.countTokens() != 2){
						fieldNames.put(counter, st.nextToken());
						chars = tempLine.substring(tempLine.lastIndexOf("(")+1);
					}else{
						fieldNames.put(counter, st.nextToken());
						chars = st.nextToken();
					}
					positions.put(counter, chars.trim().substring(0, chars.lastIndexOf(")")));
					counter++;
				}
			}
			outFile = new FileWriter("C:\\LazyCoder\\LazyOutput.html");
			currentResponse = hostFile;
			out = new PrintWriter(outFile);
			out.println("<table border='1' width='100%'><tr>");
			out.println("<th colspan='5'><font color='red' size='7'><b>LAZY CODER</b></font></th></tr><tr><p></tr><tr>"); 
			out.println("<th><font color='blue'>&nbsp;Field Name&nbsp;</font><p></th><th><font color='blue'>&nbsp;Value&nbsp;</font><p></th><th><font color='blue'>&nbsp;Spaces&nbsp;</font><p></th><th><font color='blue'>&nbsp;Expected Characters&nbsp;</font><p></th><th><font color='blue'>&nbsp;Valid Characters&nbsp;</font><p></th></tr>");
						
			for(int i=0;i<fieldNames.size(); i++){
				out.print("<tr><td><font color='blue'>"+fieldNames.get(i)+"</font></td>"+"<td><b>"+(currentResponse.substring(0, Integer.parseInt(""+positions.get(i)))).replaceAll(" ", "_")+"</b></td>");
				if(currentResponse.substring(0, Integer.parseInt(""+positions.get(i))).replaceAll(" ", "_").indexOf("_") != -1){
					temp = currentResponse.substring(0, Integer.parseInt(""+positions.get(i)));
					for(int s=0; s<temp.length();s++){
						if(Character.isWhitespace(temp.charAt(s))){
							whiteSpaces++;
						}
					}
					out.println("<td><font color='red'><b>"+whiteSpaces+"</b></font></td><td><b>"+positions.get(i)+"</b></td><td><b>"+(temp.length()-whiteSpaces)+"</b></td>");
					whiteSpaces = 0;
					out.println("</tr>");
				}
				
				currentResponse = currentResponse.substring(Integer.parseInt(""+positions.get(i)), currentResponse.length());
			}
			out.close();
			System.out.println("$$$$$$$$ -  Done  - $$$$$$$$");
		}catch(Exception e){
			System.err.println("$$$$$$$$$Fix your file format already! To make things easier for you, problem's on this line in your spec file: "+tempLine);
			goHome = (tempLine ==null || tempLine.trim().equals(""))?true:false;
			if(goHome){
				System.err.println("Well maybe not, stop fooling around");
			}
			e.printStackTrace();
		}

	}

}
