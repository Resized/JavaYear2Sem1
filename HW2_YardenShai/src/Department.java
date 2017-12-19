import java.io.Serializable;

public class Department implements Serializable {
	private String depName;
	private String depHead;

	public Department(String depName, String depHead) {
		this.depName = depName;
		this.depHead = depHead;
	}
	
	@Override
	public String toString() {
		return String.format("%-40s %-10s", depName, depHead);
	}

	public String getDepName() {
		return depName;
	}


	public String getDepHead() {
		return depHead;
	}
	
}
