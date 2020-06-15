

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.*;  
import java.net.*;

public class TweetGen {
	public static void main(String[] args) throws IOException, InterruptedException {
    	ServerSocket ss=new ServerSocket(9000);
    	int counter = 0;
		System.out.println("Server Started ....");
    	Socket s=ss.accept();//establishes connection
    	
    	DataOutputStream out=new DataOutputStream(s.getOutputStream());  
    	  
    	
        String csvFile =  "D:\\KIT418\\Tweets.txt";
        String line = "";
        String cvsSplitBy = "\t";
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {
            	if(counter>0)
            		out.writeUTF(line);
                counter++;
                 Thread.sleep(100);
                out.flush();
                    
            }
            s.close();
            ss.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally{
        	ss.close();
		}

    }

}
