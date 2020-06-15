

import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class Worker {
	public static void main(String[] args) throws Exception {
		Map<String,String> map = new ConcurrentHashMap<String,String>();
		Socket workerSocket=null;
		Socket querySocket=null;
		try {
			
				workerSocket=new Socket("127.0.0.1", 9003);
				System.out.println("Tweet Worker Started ....");
				WorkerTweetThread workerTweetThread = new WorkerTweetThread(workerSocket, map); //send  the request to a separate thread
				workerTweetThread.start();
				
				querySocket = new Socket("127.0.0.1", 9001);
				System.out.println("Query Worker Started ....");

				WorkerQueryThread workerQueryThread = new WorkerQueryThread(querySocket, map); //send  the request to a separate thread
				workerQueryThread.start();
			
		} catch (Exception e) {
			System.out.println(e);
		} 
	}
	
}

class WorkerTweetThread extends Thread {
	Socket tweetSocket;
	Map<String,String> map;

	WorkerTweetThread(Socket inSocket, Map<String,String> inMap){
		tweetSocket = inSocket;
		map = inMap;
	}
	public void run() {
		try{
			int limit=0;
			while (true) {
				DataInputStream in=new DataInputStream(tweetSocket.getInputStream());
				DataOutputStream out=new DataOutputStream(tweetSocket.getOutputStream());
				String  str=(String)in.readUTF();
				limit++;
				System.out.println("Limit"+limit);
				System.out.println(str);
				String[] tweet = str.split(">");
				String key = tweet[0];
				String value =  tweet[1] + '>' + tweet[2] + '>' + tweet[3] + '>' + tweet[4];
				map.put(key, value);
				if(limit==200) {
					out.writeUTF(("200"));
				}else {
					out.writeUTF("not reached");
				}
			}
		}catch(Exception e){System.out.println(e);}  
		}  
}

class WorkerQueryThread extends Thread {
	Socket querySocket;
	Map<String,String> map;
	static OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

	WorkerQueryThread(Socket inSocket, Map<String,String> inMap){
		querySocket = inSocket;
		map = inMap;
	}	
	
	public void run() {
		try{
			DataInputStream in = new DataInputStream(querySocket.getInputStream());
			DataOutputStream out = new DataOutputStream(querySocket.getOutputStream());
			while (true) {
				
				String cpuTime = printCPUTime();
				String memoryInfo = getMemoryInfo();

				int cpuCount = operatingSystemMXBean.getAvailableProcessors();
				long elapsedStartTime = System.nanoTime();
				
				String queryString = (String)in.readUTF();
				long startTime=System.nanoTime();
				long endTime;

				String[] queryArray = queryString.split(",");
				String taskId = queryArray[0];
				String queryOption = queryArray[1];
				String query = queryArray[2];
				String bill;

				switch (queryOption) {
				case "1" :
					//Search Tweet by id
					String tweetDetails = map.get(query);
					String[] tweet = tweetDetails.split(">");
					endTime=System.nanoTime();
					bill = Long.toString(endTime-startTime);
					
					long totalAvailCPUTime1 = cpuCount * (endTime-elapsedStartTime);
			        long totalUsedCPUTime1 = endTime-startTime;     
			        float per1 = ((float)totalUsedCPUTime1*100)/(float)totalAvailCPUTime1;
			        String cpuUsage1 = per1 + "";
					
					out.writeUTF(tweet[2] + ">" + bill + "<" + memoryInfo + "<" + cpuUsage1 + "<" + cpuTime);

					break;
				case "2" :
					int count=0;
					for (Map.Entry<String, String> entry : map.entrySet()) {
						String[] s = entry.getValue().split(">");
						for (int i = 0; i < s.length; i++){ 
						    if (s[i].contains(query)) { 
						        count++;
						    }
					    }
					}
					endTime=System.nanoTime();
					bill = Long.toString(endTime-startTime);
					
					long totalAvailCPUTime = cpuCount * (endTime-elapsedStartTime);
			        long totalUsedCPUTime = endTime-startTime;     
			        float per = ((float)totalUsedCPUTime*100)/(float)totalAvailCPUTime;
			        String cpuUsage = per + "";
			        
					out.writeUTF(Integer.toString(count) + ">" + bill + "<" + memoryInfo + "<" + cpuUsage + "<" + cpuTime);
					break;
				case "3" :
					//option3
					int number = 0;
					for(String value : map.values()){
						String[] tweet3 = value.split(">");
						String tweetAirline = tweet3[1];
						if(tweetAirline.equals(query))
						{
							number++;
						}
					}
					String optionThreeResult = Integer.toString(number);
					endTime=System.nanoTime();
					bill = Long.toString(endTime-startTime);
					
					long totalAvailCPUTime3 = cpuCount * (endTime-elapsedStartTime);
			        long totalUsedCPUTime3 = endTime-startTime;     
			        float per3 = ((float)totalUsedCPUTime3*100)/(float)totalAvailCPUTime3;
			        String cpuUsage3 = per3 + "";
			        
					out.writeUTF(optionThreeResult + ">" + bill + "<" + memoryInfo + "<" + cpuUsage3 + "<" + cpuTime);
					break;
				case "4" :
					//Search Tweet by id
					String TweetDetails = map.get(query);
					String[] Tweet = TweetDetails.split(">");
					String tweetText=Tweet[2];

					// Find the most frequent char

			        char[] x = tweetText.toCharArray();
					char[] m = {'a','b','c','d','e','f','g','h','i','j','k','l','m',
							'n','o','p','q','r','s','t','u','v','w','x','y','z'};
					int[] n=new int[26];
					for(int i=0;i<x.length;i++)
					{
						for(int j=0;j<26;j++)
						{
							if(x[i]==m[j]) n[j]++;
						}
					}
					int max=0,temp=0;
					for(int i=0;i<26;i++)
						if(n[i]>max) {max=n[i];temp=i;}
					
					String optionFourReult = String.valueOf(m[temp]);
					endTime=System.nanoTime();
					bill = Long.toString(endTime-startTime);
					long totalAvailCPUTime4 = cpuCount * (endTime-elapsedStartTime);
			        long totalUsedCPUTime4 = endTime-startTime;     
			        float per4 = ((float)totalUsedCPUTime4*100)/(float)totalAvailCPUTime4;
			        String cpuUsage4 = per4 + "";

					out.writeUTF(optionFourReult + ">" + bill + "<" + memoryInfo + "<" + cpuUsage4 + "<" + cpuTime);      
					break;
				default :
					break;
			}
				
			}	
		//in.close();  
		//s.close();  
		}catch(Exception e){System.out.println(e);}  
		}
	
	public static String getMemoryInfo() {
		long total, free, used;
		int kb = 1024;
		
		total = Runtime.getRuntime().totalMemory();
		free = Runtime.getRuntime().freeMemory();
		used = total - free;
		String memoryInfo = "Total Memory: " + total / kb + "KB\r\n" + "Memory Used: " +  used / kb + "KB\r\n"
				+ "Memory Free: " +  free / kb + "KB\r\n" + "Percent Used: " + ((double)used/(double)total)*100 + "%\r\n"
						+ "Percent Free: " + ((double)free/(double)total)*100 + "%";
		return memoryInfo;
	}   

   private static String printCPUTime() {
	ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
	String CPUTime = null;
	for(Long threadID : threadMXBean.getAllThreadIds()) {			 
	    CPUTime = String.format("CPU time: %s ns", threadMXBean.getThreadCpuTime(threadID));
	  }
	return CPUTime;
}
}
