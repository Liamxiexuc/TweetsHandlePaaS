

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;



public class DataServer {
	
	public static void main(String[] args) throws Exception {
		ConcurrentLinkedQueue<Task> lowPriority = new ConcurrentLinkedQueue<Task>();
		ConcurrentLinkedQueue<Task> highPriority = new ConcurrentLinkedQueue<Task>();
		// Track Store Info 
		Map<String, String> tweetIndex = new ConcurrentHashMap<String, String>();
		// Store query result send back from worker 
		Map<String, String> result = new ConcurrentHashMap<String, String>();
		// userData is to store the user account and password
		Map<String,String> userData = new ConcurrentHashMap<String,String>();
		//TODO need a another hash map to store index
		ServerSocket userRequestServer = null;
		try {
			userRequestServer = new ServerSocket(8888);

			int counter = 0;
			
			//for ReceiveTweets
			Socket tweetClientSocket=new Socket("localhost", 9000);
			TweetClientThread tweetClientThread = new TweetClientThread(tweetClientSocket, tweetIndex); 
			tweetClientThread.start();
			//For worker TODO Elastic
			System.out.println("Worker Server Started ....");

			//create a new UserRequestThread
			WorkerThread workerThread = new WorkerThread(lowPriority, highPriority, tweetIndex, result);
			workerThread.start();
			
			while (true) {
				// TODO Elastic

				
				//For client 
				System.out.println("User Request Server Started ....");
				counter++;
				Socket userRequestSocket = userRequestServer.accept(); 
				// server accept the client connection request 
				System.out.println(" >> " + "Client No:" + counter + " started!");
				//create a new UserRequestThread
				UserRequestThread userRequestThread = new UserRequestThread(userRequestSocket, userData, lowPriority, highPriority, result);
				userRequestThread.start(); 
			}	 
		} catch (Exception e) {
			System.out.println(e);
		} finally{
			//userRequestServer.close();
		}	
	}
}

class WorkerThread extends Thread {
	ConcurrentLinkedQueue<Task> lowPriority;
	ConcurrentLinkedQueue<Task> highPriority;
	Map<String, String> tweetIndex;
	Map<String, String> resultMap;
	
	WorkerThread(ConcurrentLinkedQueue<Task> inLowPriority, ConcurrentLinkedQueue<Task> inHighPriority, Map<String, String> inTweetIndex, Map<String, String> inResult){
		lowPriority = inLowPriority;
		highPriority = inHighPriority;
		tweetIndex = inTweetIndex;
		resultMap = inResult;
	}

	public void run() {
		try {
			ServerSocket worker1Socket = new ServerSocket(9001);
			Socket ss1=worker1Socket.accept();
			ServerSocket workerSocket2 = new ServerSocket(9002);
			Socket ss2=workerSocket2.accept(); 
			DataInputStream worker1In=new DataInputStream(ss1.getInputStream());
			DataOutputStream worker1Out=new DataOutputStream(ss1.getOutputStream());
			DataInputStream worker2In=new DataInputStream(ss2.getInputStream());
			DataOutputStream worker2Out=new DataOutputStream(ss2.getOutputStream());
			
			while(true) {		
				// run task
				boolean val=true;
                if(!highPriority.isEmpty()){ 
                	Task task = this.highPriority.poll();
        			if(task == null) break;
        			//get TaskInfo
        			String taskId = task.getTaskId();
        			String query = task.getQuery();
        			String queryType = task.getQueryType();
        			String requestString = taskId + "," + queryType + "," + query;
        			if(queryType.equals("1") || queryType.equals("4")) {
        				// Seacher which worker have this tweet
        				String worker = tweetIndex.get(query);

        				if(worker == null) {
        					resultMap.put(taskId, "Tweet are not found>0");
        				} else {
        					if(worker.equals("worker1")) {
            					worker1Out.writeUTF(requestString);
            					String resultString = worker1In.readUTF();
            					// "result content > bill < meomoInfo"
            					String[] resultArray = resultString.split("<");
            					
            					// Print Worker1 Health
            					String memoryInfo = resultArray[1];
            					String cpuUsage1 = resultArray[2];
            					String cpuTime1 = resultArray[3];
            					System.out.println("worker1: ");
            					System.out.println(memoryInfo);
            					System.out.println("CPU Usage: " + cpuUsage1);
            					System.out.println(cpuTime1);
            					
            					BigDecimal bd = new BigDecimal(cpuUsage1);
            					if(bd.doubleValue()>70 && val) {
            						System.out.println("CPU Usage: " +bd.doubleValue());
            						createinstanceThread ct = new createinstanceThread();
            						ct.start();
            						val=false;
            					}
            					
            					resultMap.put(taskId, resultArray[0]);
            					
            				} else if (worker.equals("worker2")) {
            					worker2Out.writeUTF(requestString);
            					String result = worker2In.readUTF();
            					// "result content > bill < meomoInfo"
            					String[] resultArray = result.split("<");
            					
            					// Print Worker2 Health
            					String memoryInfo = resultArray[1];
            					String cpuUsage1 = resultArray[2];
            					String cpuTime1 = resultArray[3];
            					System.out.println("worker2: ");
            					System.out.println(memoryInfo);
            					System.out.println("CPU Usage: " + cpuUsage1);
            					System.out.println(cpuTime1);
            					
            					BigDecimal bd = new BigDecimal(cpuUsage1);
            					if(bd.doubleValue()>70 && val) {
            						System.out.println("CPU Usage: " +bd.doubleValue());
            						createinstanceThread ct = new createinstanceThread();
            						ct.start();
            						val=false;
            					}
            					
            					resultMap.put(taskId, resultArray[0]);
            				}            
        				}
        				                                                                                                                                                                                                                                                                                                  
        			} else {
        				// Send task to all the worker
        				worker1Out.writeUTF(requestString);
        				worker2Out.writeUTF(requestString);
        				
        				String resultString1 = worker1In.readUTF();
        				String[] resultArray1 = resultString1.split("<");
        				String queryResult1 = resultArray1[0];
        				
        				// Print Worker1 Health
    					String memoryInfo1 = resultArray1[1];
    					String cpuUsage1 = resultArray1[2];
    					String cpuTime1 = resultArray1[3];
    					System.out.println("worker1: ");
    					System.out.println(memoryInfo1);
    					System.out.println("CPU Usage: " + cpuUsage1);
    					System.out.println(cpuTime1);
    					
    					BigDecimal bd = new BigDecimal(cpuUsage1);
    					if(bd.doubleValue()>70 && val) {
    						System.out.println("CPU Usage: " +bd.doubleValue());
    						createinstanceThread ct = new createinstanceThread();
    						ct.start();
    						val=false;
    					}
    					
    					String[] queryResultArray1 = queryResult1.split(">");
    					String result1 = queryResultArray1[0];
    					String bill1 = queryResultArray1[1];
    					
    					String resultString2 = worker2In.readUTF();
        				String[] resultArray2 = resultString2.split("<");
        				String queryResult2 = resultArray2[0];
        				
        				// Print Worker2 Health
    					String memoryInfor2 = resultArray2[1];
    					String cpuUsage2 = resultArray2[2];
    					String cpuTime2 = resultArray2[3];
    					System.out.println("worker2: ");
    					System.out.println(memoryInfor2);
    					System.out.println("CPU Usage: " + cpuUsage2);
    					System.out.println(cpuTime2);
    					val=true;
    					BigDecimal bd2 = new BigDecimal(cpuUsage2);
    					if(bd.doubleValue()>70 && val) {
    						System.out.println("CPU Usage: " +bd2.doubleValue());
    						createinstanceThread ct2 = new createinstanceThread();
    						ct2.start();
    						val=false;
    					}
    					
    					String[] queryResultArray2 = queryResult2.split(">");
    					String result2 = queryResultArray2[0];
    					String bill2 = queryResultArray2[1];
    					
    					
        				int intResult = Integer.parseInt(result1) + Integer.parseInt(result2);
        				int intBill = Integer.parseInt(bill1) + Integer.parseInt(bill2);
        				String result = String.valueOf(intResult);
        				String bill = String.valueOf(intBill);
        				String valueString = result + ">" + bill;
        				resultMap.put(taskId, valueString);
        			}
                }
                if(!lowPriority.isEmpty()){ 
                	Task task = this.lowPriority.poll();
        			if(task == null) break;
        			//get TaskInfo
        			String taskId = task.getTaskId();
        			String query = task.getQuery();
        			String queryType = task.getQueryType();
        			String requestString = taskId + "," + queryType + "," + query;
        			if(queryType.equals("1") || queryType.equals("4")) {
        				// Seacher which worker have this tweet
        				String worker = tweetIndex.get(query);
        				
        				if(worker == null) {
        					resultMap.put(taskId, "Tweet are not found>0");
        				} else {
        					if(worker.equals("worker1")) {
            					worker1Out.writeUTF(requestString);
            					String resultString = worker1In.readUTF();
            					// "result content > bill < meomoInfo"
            					String[] resultArray = resultString.split("<");
            					
            					// Print Worker1 Health
            					String memoryInfo = resultArray[1];
            					String cpuUsage1 = resultArray[2];
            					String cpuTime1 = resultArray[3];
            					System.out.println("worker1: ");
            					System.out.println(memoryInfo);
            					System.out.println("CPU Usage: " + cpuUsage1);
            					System.out.println(cpuTime1);
            					
            					BigDecimal bd = new BigDecimal(cpuUsage1);
            					if(bd.doubleValue()>70 && val) {
            						System.out.println("CPU Usage: " +bd.doubleValue());
            						createinstanceThread ct = new createinstanceThread();
            						ct.start();
            						val=false;
            					}
            					
            					resultMap.put(taskId, resultArray[0]);
            					
            				} else if (worker.equals("worker2")) {
            					worker2Out.writeUTF(requestString);
            					String result = worker2In.readUTF();
            					// "result content > bill < meomoInfo"
            					String[] resultArray = result.split("<");
            					
            					// Print Worker2 Health
            					String memoryInfo = resultArray[1];
            					String cpuUsage1 = resultArray[2];
            					String cpuTime1 = resultArray[3];
            					System.out.println("worker2: ");
            					System.out.println(memoryInfo);
            					System.out.println("CPU Usage: " + cpuUsage1);
            					System.out.println(cpuTime1);
            					
            					BigDecimal bd = new BigDecimal(cpuUsage1);
            					if(bd.doubleValue()>70 && val) {
            						System.out.println("CPU Usage: " +bd.doubleValue());
            						createinstanceThread ct = new createinstanceThread();
            						ct.start();
            						val=false;
            					}
            					
            					resultMap.put(taskId, resultArray[0]);
            				}                 
        				}
        				                                                                                                                                                                                                                                                                                       
        			} else {
        				// Send task to all the worker
        				worker1Out.writeUTF(requestString);
        				worker2Out.writeUTF(requestString);
        				
        				String resultString1 = worker1In.readUTF();
        				String[] resultArray1 = resultString1.split("<");
        				String queryResult1 = resultArray1[0];
        				
        				// Print Worker1 Health
    					String memoryInfo1 = resultArray1[1];
    					String cpuUsage1 = resultArray1[2];
    					String cpuTime1 = resultArray1[3];
    					System.out.println("worker1: ");
    					System.out.println(memoryInfo1);
    					System.out.println("CPU Usage: " + cpuUsage1);
    					System.out.println(cpuTime1);
    					
    					BigDecimal bd = new BigDecimal(cpuUsage1);
    					if(bd.doubleValue()>70 && val) {
    						System.out.println("CPU Usage: " +bd.doubleValue());
    						createinstanceThread ct = new createinstanceThread();
    						ct.start();
    						val=false;
    					}
    					
    					String[] queryResultArray1 = queryResult1.split(">");
    					String result1 = queryResultArray1[0];
    					String bill1 = queryResultArray1[1];
    					
    					String resultString2 = worker2In.readUTF();
        				String[] resultArray2 = resultString2.split("<");
        				String queryResult2 = resultArray2[0];
        				
        				// Print Worker2 Health
    					String memoryInfor2 = resultArray2[1];
    					String cpuUsage2 = resultArray2[2];
    					String cpuTime2 = resultArray2[3];
    					System.out.println("worker2: ");
    					System.out.println(memoryInfor2);
    					System.out.println("CPU Usage: " + cpuUsage2);
    					System.out.println(cpuTime2);
    					val=true;
    					BigDecimal bd2 = new BigDecimal(cpuUsage2);
    					if(bd2.doubleValue()>70 && val) {
    						System.out.println("CPU Usage: " +bd2.doubleValue());
    						createinstanceThread ct2 = new createinstanceThread();
    						ct2.start();
    						val=false;
    					}
    					
    					String[] queryResultArray2 = queryResult2.split(">");
    					String result2 = queryResultArray2[0];
    					String bill2 = queryResultArray2[1];
    					
        				int intResult = Integer.parseInt(result1) + Integer.parseInt(result2);
        				int intBill = Integer.parseInt(bill1) + Integer.parseInt(bill2);
        				String result = String.valueOf(intResult);
        				String bill = String.valueOf(intBill);
        				String valueString = result + ">" + bill;
        				resultMap.put(taskId, valueString);
        			}
                } 
			}
		} catch(Exception e)
		{System.out.println(e);}  
	}
}

class UserRequestThread extends Thread {
	Socket userRequestSocket;
	Map<String,String> userData;
	ConcurrentLinkedQueue<Task> lowPriority;
	ConcurrentLinkedQueue<Task> highPriority;
	Map<String, String> result;

	UserRequestThread(Socket inSocket, Map<String,String> inMap, ConcurrentLinkedQueue<Task> inLowPriority, ConcurrentLinkedQueue<Task> inHighPriority, Map<String, String> inResult){
		userRequestSocket = inSocket;
		userData = inMap;
		lowPriority = inLowPriority;
		highPriority = inHighPriority;
		result = inResult;
	}
	
	public void run() {
		try{
			String userInput;
			DataInputStream userIn=new DataInputStream(userRequestSocket.getInputStream());
			DataOutputStream userOut=new DataOutputStream(userRequestSocket.getOutputStream());
			
			Boolean mainFlag = true;
			while (mainFlag) {
				userOut.writeUTF("Welcome to Data Server (aka platform), Type any of the following options: 1.Login "
		    			+ "2.Register  3. Exit");
				
				String option = (String)userIn.readUTF();
				
				if (option.equals("1"))
				{
						userOut.writeUTF("Please type your Username: ");
						String username = (String)userIn.readUTF();
						userOut.writeUTF("Please type your Password: ");
						String password = (String)userIn.readUTF();
						
						String storedPassword = userData.get(username);
						if(password.equals(storedPassword))
						{
							System.out.println("Login Success");
							userOut.writeUTF("Login Success");
							Boolean flag = true;
							while (flag) {
								userOut.writeUTF("Please select your query options (type query number only):\r\n 1. Search text of a tweet given its id\r\n" + 
										"2. Search number of tweets containing a specific words\r\n" + 
										"3. Search number of tweets from a specific airline\r\n" + 
										"4. Find the most frequent character used in a tweet given its id\r\n" +
										"5. Find the status of my query\r\n" + 
										"6. Cancel query request\r\n" +
										"7. Exit");
								
								String queryOption = (String)userIn.readUTF();

								switch (queryOption) {
									case "1" :
										userOut.writeUTF("Please type a Tweet ID:_______");
										String tweetId = (String)userIn.readUTF();
										userOut.writeUTF("Please type your query priority || 0 for normal: 1 for urgent");
										String priority1 = (String)userIn.readUTF();
										//generate a taskId 
										int taskId1 = (int)((Math.random()*9+1)*100000);
										String queryId1 =String.valueOf(taskId1); 
										// Create a New Task, then add to taskQueue based on the priority
										Task task1 = new Task(queryId1, queryOption, tweetId);
										if(priority1.equals("1")) {
											highPriority.add(task1);
										} else {
											lowPriority.add(task1);
										}
										result.put(queryId1, "processing");
										userOut.writeUTF("Searching.. Your query Id is: " + queryId1);
										break;
									case "2" :
										userOut.writeUTF("Please type your word:_______");
										String word = (String)userIn.readUTF();
										userOut.writeUTF("Please type your query priority || 0 for normal: 1 for urgent");
										String priority2 = (String)userIn.readUTF();
										//generate a taskId 
										int taskId2 = (int)((Math.random()*9+1)*100000);
										String queryId2 =String.valueOf(taskId2); 
										// Create a New Task, then add to taskQueue based on the priority
										Task task2 = new Task(queryId2, queryOption, word);
										if(priority2.equals("1")) {
											highPriority.add(task2);
										} else {
											lowPriority.add(task2);
										}
										result.put(queryId2, "processing");
										userOut.writeUTF("Searching.. Your query Id is: " + queryId2);
										break;
									case "3" :
										userOut.writeUTF("Please type desired airline:_______");
										String airline = (String)userIn.readUTF();

										userOut.writeUTF("Please type your query priority || 0 for normal: 1 for urgent");
										String priority3 = (String)userIn.readUTF();
										//generate a taskId 
										int taskId3 = (int)((Math.random()*9+1)*100000);
										String queryId3 =String.valueOf(taskId3); 
										// Create a New Task, then add to taskQueue based on the priority
										Task task3 = new Task(queryId3, queryOption, airline);
										if(priority3.equals("1")) {
											highPriority.add(task3);
										} else {
											lowPriority.add(task3);
										}
										result.put(queryId3, "processing");
										userOut.writeUTF("Searching.. Your query Id is: " + queryId3);
										break;
									case "4" :
										userOut.writeUTF("Please type tweet ID:_______");
										String tweetId4 = (String)userIn.readUTF();
										userOut.writeUTF("Please type your query priority || 0 for normal: 1 for urgent");
										String priority4 = (String)userIn.readUTF();
										// Generate a taskId 
										int taskId4 = (int)((Math.random()*9+1)*100000);
										String queryId4 =String.valueOf(taskId4); 
										// Create a New Task, then add to taskQueue based on the priority
										Task task4 = new Task(queryId4, queryOption, tweetId4);
										if(priority4.equals("1")) {
											highPriority.add(task4);
										} else {
											lowPriority.add(task4);
										}
										result.put(queryId4, "processing");
										userOut.writeUTF("Searching.. Your query Id is: " + queryId4);
										break;
									case "5" :
										userOut.writeUTF("Please type your query ID:_______");
										String queryId = (String)userIn.readUTF();
										String queryResult = result.get(queryId);
										if (queryResult == null) {
											userOut.writeUTF("The query ID: " + queryId + " does not exist!");
											break;
										}
										if(queryResult.equals("processing")) {
											userOut.writeUTF("Your query is still on processing, please wait.");
											break;
										} else {
											String[] resultArray = queryResult.split(">");
											String result = resultArray[0];
											String bill = resultArray[1];
											userOut.writeUTF("Result of Query Id - "+queryId+" is: "+ result + "\r\n Bill: " + bill + "ns");
											break;
										}
									case "6" :
										userOut.writeUTF("Please type your query ID:_______");
										String queryId6 = (String)userIn.readUTF();
										String result6 = result.get(queryId6);
										if(result6 == null) {
											userOut.writeUTF("The query ID: " + queryId6 + " does not exist!");
											break;
										}
										if(result6.equals("processing")) {
											// Delete task from two TaskQueue
											Iterator<Task> highPriorityIterator = highPriority.iterator();
									        while (highPriorityIterator.hasNext()){
									        	Task task = highPriorityIterator.next();
									            String taskId = task.getTaskId();
									            if (taskId.equals(queryId6)) {
									            	highPriorityIterator.remove();
									            }
									        }
									        Iterator<Task> lowPriorityIterator = lowPriority.iterator();
									        while (lowPriorityIterator.hasNext()){
									        	Task task = lowPriorityIterator.next();
									            String taskId = task.getTaskId();
									            if (taskId.equals(queryId6)) {
									            	lowPriorityIterator.remove();
									            }
									        }
									        userOut.writeUTF("Your query ID: " + queryId6 + " has been canceled!");
									        break;
										} else {
											String[] resultArray = result6.split(">");
											String result = resultArray[0];
											String bill = resultArray[1];
											userOut.writeUTF("Your query ID: " + queryId6 + " is already been executed, The result is: " + result + "\r\n Bill: " + bill + "ns");
											break;
										}
									case "7" :
										flag = false;
										break;
									default :
										//any other string will be ask to retype
										break;
								}
							}
							
						} else 
						{
							userOut.writeUTF("Wrong username or password!");
							break;
						}
					
					
					
				} else if (option.equals("2"))
				{
					userOut.writeUTF("Please type your Username: ");
					String username = (String)userIn.readUTF();
					int secret = (int)((Math.random()*9+1)*1);
					String password = String.valueOf(secret);
					//store acct info
					userData.put(username, password);
					userOut.writeUTF("Your password is: " + password);
					//TODO
					
				} else if (option.equals("3"))
				{
					userOut.writeUTF("You have exit.");
					mainFlag = false;
				} else
			    {
					userOut.writeUTF("INVALID OPTION!! PLEASE SPECIFY OPTION AGAIN.");
			    }
			}

			userIn.close();
			userOut.close();
			userRequestSocket.close();
		
		} catch(Exception e)
			{System.out.println(e);}  
	}  
}

class TweetClientThread extends Thread {
	Socket tweetClientSocket;
	Map<String, String> tweetIndex;
	int count=0;
	int count1=0;

	TweetClientThread(Socket inSocket, Map<String, String> inTweetIndex){
		tweetClientSocket = inSocket;
		tweetIndex = inTweetIndex;
	  }
	public void run() {
		try{
			ServerSocket workerSocket = new ServerSocket(9003);
			Socket ss=workerSocket.accept();
			ServerSocket workerSocket2 = new ServerSocket(9004);
			Socket ss2=workerSocket2.accept(); 
			Boolean b=true;
			Boolean a=false;

			DataInputStream in=new DataInputStream(tweetClientSocket.getInputStream());
			DataOutputStream out=new DataOutputStream(ss.getOutputStream());
			DataInputStream in1=new DataInputStream(ss.getInputStream());
			DataOutputStream out2=new DataOutputStream(ss2.getOutputStream());
			DataInputStream in2=new DataInputStream(ss2.getInputStream());
		while(true) {
			while(b) {
				String  str=(String)in.readUTF();
	            String[] tweet = str.split("\t");
	            String storeString = tweet[0] + ">" + tweet[1] + ">" + tweet[5] + ">" + tweet[10] + ">" + tweet[12];       
	            count++;
	            tweetIndex.put(tweet[0], "worker1");
	        	out.writeUTF(storeString);       	
	        	String ad=(String)in1.readUTF();
	        	if(ad.equals("200")) {
	        		System.out.println("Worker 1 limit reached");
	        		a=true;
	        		b=false;
	        	}
            }
            while(a) {
            	String  str=(String)in.readUTF();
            	String[] tweet = str.split("\t");
            	 System.out.println(tweet[0] + " " + tweet[1] + " " + tweet[5] + " " + tweet[10] + " " + tweet[12]);
                 String storeStrings = tweet[0] + ">" + tweet[1] + ">" + tweet[5] + ">" + tweet[10] + ">" + tweet[12];
                 count++;
                 tweetIndex.put(tweet[0], "worker2");
                 out2.writeUTF(storeStrings);
            }
		}
 
		}catch(Exception e){System.out.println(e);}  
		}  
}

class createinstanceThread extends Thread{
	public void run() {
		try {			
			System.out.println("Inside creating instance");

			Instance openstack=new Instance(); //Build the openstac client and authenticate
			String serverid= openstack.createServer(); // Creating a new VM
			System.out.println("Successfully Created Virtual Machine (VM) with server id"+serverid +" and temp folder inside VM Please log in to nectar cloud to verify");
			try {
				Thread.sleep(300000); // wait for 5 minutes to create VM
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
			// openstack.deleteServer(serverid);// Delete the created server
			// System.out.println("Successfully deleted Virtual Machine (VM) of server id"+serverid);		
		}catch(Exception e) {
			System.out.println(e);
		}
	}
	
}
