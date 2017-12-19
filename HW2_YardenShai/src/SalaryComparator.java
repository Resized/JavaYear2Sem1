import java.util.Comparator;

public class SalaryComparator<T extends Worker<?>> implements Comparator<T> {
	
	@Override
	public int compare(T w1, T w2) {
		return w1.getSalary() - w2.getSalary();
	}

}
