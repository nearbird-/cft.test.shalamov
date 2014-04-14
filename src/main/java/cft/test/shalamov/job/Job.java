package cft.test.shalamov.job;

/**
 * 
 * @author Shalamov
 *
 */
public class Job{

	private Integer groupId;
	private Integer id;
	
	public Job(Integer groupId) {
		this.groupId = groupId;
	}
	
	/****** Getters/Setters *********/
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getGroupId() {
		return groupId;
	}

}
