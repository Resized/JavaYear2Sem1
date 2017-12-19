import java.io.Serializable;

public class Worker<T> implements Serializable {
	private String name;
	private T dep;
	private int salary;

	public Worker(String name, T dep, int salary) {
		this.name = name;
		this.dep = dep;
		this.salary = salary;
	}

	@Override
	public String toString() {
		return String.format("	%-20s %-40s %-20d", name, dep, salary);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDep(T dep) {
		this.dep = dep;
	}

	public void setSalary(int salary) {
		this.salary = salary;
	}

	public int getSalary() {
		return salary;
	}
	
	public T getDep() {
		return dep;
	}

}
