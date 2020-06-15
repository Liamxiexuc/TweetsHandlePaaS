
public class Task {
	private String taskId;
	private String queryType;
	private String query;
	
	public Task(String taskId, String queryType, String query) {
		this.taskId = taskId;
		this.queryType = queryType;
		this.query = query;
	}
	
	public String getTaskId() {
		return this.taskId;
	}
	
	public String getQueryType() {
		return this.queryType;
	}
	
	public String getQuery() {
		return this.query;
	}
	

}
