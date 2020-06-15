

import java.net.*;
import java.io.*;

public class User {
	public static void main(String[] args) throws Exception {
		 try{
		     BufferedReader keyboard=new BufferedReader(new InputStreamReader(System.in));
		     Socket s =new Socket("127.0.0.1",8888);
		     DataInputStream in=new DataInputStream(s.getInputStream());
	    	 DataOutputStream out=new DataOutputStream(s.getOutputStream());
		     while(true){
					
				 System.out.println(in.readUTF());
				   
				   // user type option
				 String requestOption = keyboard.readLine();
				 out.writeUTF(requestOption);

				 if (requestOption.equals("3")) {
					 System.out.println(in.readUTF());
					 break;
				 };
				   
				 if (requestOption.equals("1"))
				 {
							   System.out.println(in.readUTF());
							   String username = keyboard.readLine();
							   out.writeUTF(username);
							   System.out.println(in.readUTF());
							   String password = keyboard.readLine();
							   out.writeUTF(password);
							   
							   //verify log in result
							   String loginResult = in.readUTF();
							   System.out.println(loginResult);
							   
							   if (loginResult.equals("Login Success"))
							   {
								   Boolean flag = true;
								   
								   while (flag) {
									   System.out.println(in.readUTF());
									   String queryOption = keyboard.readLine();
									   out.writeUTF(queryOption);

										   switch (queryOption) {
											case "1" :
												System.out.println(in.readUTF());
												String tweetID = keyboard.readLine();
												out.writeUTF(tweetID);
												System.out.println(in.readUTF());
												String priority1 = keyboard.readLine();
												out.writeUTF(priority1);
												System.out.println("Your query ID is "+in.readUTF());
												break;
											case "2" :
												System.out.println(in.readUTF());
												String w = keyboard.readLine();
												out.writeUTF(w);
												System.out.println(in.readUTF());
												String priority2 = keyboard.readLine();
												out.writeUTF(priority2);
												System.out.println("Your query ID is "+in.readUTF());
												break;
											case "3" :
												System.out.println(in.readUTF());
												String air = keyboard.readLine();
												out.writeUTF(air);
												System.out.println(in.readUTF());
												String priority3 = keyboard.readLine();
												out.writeUTF(priority3);
												System.out.println("Your query ID is "+in.readUTF());
												break;
											case "4" :
												System.out.println(in.readUTF());
												String TweetID = keyboard.readLine();
												out.writeUTF(TweetID);
												System.out.println(in.readUTF());
												String priority4 = keyboard.readLine();
												out.writeUTF(priority4);
												System.out.println("Your query ID is "+in.readUTF());
												break;
											case "5" :
												System.out.println(in.readUTF());
												String queryID = keyboard.readLine();
												out.writeUTF(queryID);
												System.out.println(in.readUTF());
												break;
											case "6" :
												System.out.println(in.readUTF());
												String queryID6 = keyboard.readLine();
												out.writeUTF(queryID6);
												System.out.println(in.readUTF());
												break;
											case "7" :
												flag = false;									
												System.out.println("You have exited!");
												break;
											default :									
												System.out.println("Option does not valid, please type again!");
												break;
										}
									   
								   }
								   
							   } else
							   {
								   System.out.println("Connection Terminal!");
								   System.out.println(in.readUTF());
								   //s.close();
								   break;
							   }		   
					   
				   } else if (requestOption.equals("2") ) 
				   {
					   System.out.println(in.readUTF());
					   String username = keyboard.readLine();
					   out.writeUTF(username);
					   System.out.println(in.readUTF());
					   System.out.println("You can login now!");
				   } else {
					   System.out.println(in.readUTF());
				   }

		  }
		 }catch(Exception e){
		   System.out.println(e);
		 }
		 }
}
