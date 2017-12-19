import java.util.Comparator;

public class NameWithDuplicatesComparator<T extends Worker<?>> implements Comparator<T> {

	@Override
	public int compare(T t1, T t2) {
		int res = t1.getName().compareToIgnoreCase(t2.getName());
		return res != 0 ? res : 1;
	}

}
