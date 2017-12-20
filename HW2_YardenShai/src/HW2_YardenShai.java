
// Yarden Shai 309920767
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class HW2_YardenShai {
	private static Scanner s = new Scanner(System.in);
	final static String WORKERS_FNAME = "workers.bin";
	final static String NAMES_FNAME = "names.bin";
	private static final int WRITE_NAME_SIZE = 20;
	private static final int WRITE_DEP_NAME_SIZE = 40;
	private static final int WRITE_DEP_HEAD_SIZE = 10;
	private static final int WRITE_SALARY_SIZE = 10;
	private static final int WRITE_DEP_NAME_LONG_SIZE = WRITE_DEP_NAME_SIZE + WRITE_DEP_HEAD_SIZE;
	private static final int WORKER_SIZE = WRITE_NAME_SIZE + WRITE_DEP_NAME_SIZE + WRITE_DEP_HEAD_SIZE + WRITE_SALARY_SIZE;
	private static final int WORKER_SIZE_BIN = WORKER_SIZE * Character.BYTES;
	private static final int FALSE = -1;
	private static final int TRUE = 1;

	public static void main(String[] args) {
		System.out.println("Press 1 for dep as Department, any other for dep as class as String:");
		int answer;
		try {
			answer = s.nextInt();
			if (answer != 1)
				answer = FALSE;
		} catch (Exception e) {
			answer = FALSE;
		}
		ArrayList<Worker<?>> arrList = createWorkers(answer);
		printArrList(arrList);
		Map<Integer, Worker<?>> map = createMap(arrList);
		System.out.println("\nMap content backward, order by worker's name: ");
		//printMapBackWard(map);

		try {
			saveMapToFile(map, WORKERS_FNAME, answer);

			System.out.println("\nFile Content: ");
			readFile(WORKERS_FNAME, answer);

			sortFileBySalary(WORKERS_FNAME, answer);

			System.out.println("\nFile Content after sorting: ");
			readFile(WORKERS_FNAME, answer);

			ListIterator<Worker<?>> iterator = listIterator(0, WORKERS_FNAME, answer);

			System.out.println("\ncheckIterator: ");

			checkIterator(iterator);
			iterator.add(new Worker<Department>("Shalom", new Department("Science", "Boss2"), 6000));
			iterator.previous();

			//checkIterator(iterator);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Prints all workers on file with iterator going forwards and backwards
	 * 
	 * @param <T
	 *            extends Worker<?>>
	 */
	private static <T extends Worker<?>> void checkIterator(ListIterator<T> iterator) {
		System.out.println("\nFile content FORWARDS with ListIterator: ");
		while (iterator.hasNext()) {
			T worker = iterator.next();
			System.out.println(worker);
		}

		System.out.println("\nFile content BACKWARDS with ListIterator: ");
		while (iterator.hasPrevious()) {
			T worker = iterator.previous();
			System.out.println(worker);
		}

	}

	/**
	 * Sorts all workers on file by salary
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void sortFileBySalary(String workersBin, int answer) throws FileNotFoundException, IOException {
		try (RandomAccessFile f = new RandomAccessFile(workersBin, "rw")) {
			SalaryComparator<Worker<?>> c = new SalaryComparator<>();
			long pos1, pos2;
			int size = getNumOfWorkersInFile(f);
			for (int i = 0; i < size; i++) {
				for (int j = 1; j < size - i; j++) {
					pos1 = f.getFilePointer();
					Worker<?> w1 = readWorker(f, answer);
					pos2 = f.getFilePointer();
					Worker<?> w2 = readWorker(f, answer);

					if (c.compare(w1, w2) > 0) {
						f.seek(pos1);
						writeWorker(w2, f, answer);
						writeWorker(w1, f, answer);
					}
					f.seek(pos2);
				}
				f.seek(0);
			}
		}
	}

	/**
	 * Returns the number of workers in given file
	 * 
	 * @throws IOException
	 */
	private static int getNumOfWorkersInFile(RandomAccessFile f) throws IOException {
		return (int) (f.length() / WORKER_SIZE_BIN);
	}

	/**
	 * Writes a single worker string in fixed length to a given file
	 * 
	 * @throws IOException
	 */
	private static void writeWorker(Worker<?> w, DataOutput f, int answer) throws IOException {
		FixedLengthStringIO.writeFixedLengthString(w.getName(), WRITE_NAME_SIZE, f);
		if (answer == TRUE) {
			FixedLengthStringIO.writeFixedLengthString(((Department) w.getDep()).getDepName(), WRITE_DEP_NAME_SIZE, f);
			FixedLengthStringIO.writeFixedLengthString(((Department) w.getDep()).getDepHead(), WRITE_DEP_HEAD_SIZE, f);
		} else {
			FixedLengthStringIO.writeFixedLengthString((String) w.getDep(), WRITE_DEP_NAME_LONG_SIZE, f);
		}
		FixedLengthStringIO.writeFixedLengthString(String.valueOf(w.getSalary()), WRITE_SALARY_SIZE, f);
	}

	/**
	 * Reads a single worker from a given file
	 * 
	 * @throws IOException
	 */
	private static Worker<?> readWorker(DataInput f, int answer) throws IOException {
		String name, depName, depHead = null;
		int salary;
		name = FixedLengthStringIO.readFixedLengthString(WRITE_NAME_SIZE, f).trim();
		if (answer == TRUE) {
			depName = FixedLengthStringIO.readFixedLengthString(WRITE_DEP_NAME_SIZE, f).trim();
			depHead = FixedLengthStringIO.readFixedLengthString(WRITE_DEP_HEAD_SIZE, f).trim();
		} else
			depName = FixedLengthStringIO.readFixedLengthString(WRITE_DEP_NAME_LONG_SIZE, f).trim();
		salary = Integer.parseInt(FixedLengthStringIO.readFixedLengthString(WRITE_SALARY_SIZE, f).trim());
		Department dep = new Department(depName, depHead);
		if (answer == TRUE)
			return new Worker<Department>(name, dep, salary);
		return new Worker<String>(name, depName, salary);
	}

	/**
	 * Prints all workers in a given file
	 * 
	 * @throws IOException
	 */
	private static void readFile(String workersBin, int answer) throws IOException {
		try (DataInputStream i = new DataInputStream(new BufferedInputStream(new FileInputStream(workersBin)))) {
			while (i.available() > 0) {
				Worker<?> w = readWorker(i, answer);
				System.out.println(w);
			}
		}
	}

	/**
	 * Saves the given Map of workers to a file
	 * 
	 * @param <T
	 *            extends Worker<?>>
	 * @throws IOException
	 */
	private static <T extends Worker<?>> void saveMapToFile(Map<Integer, T> map, String workersBin, int answer)
			throws IOException {
		Set<Integer> key = map.keySet();
		try (DataOutputStream o = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(workersBin)))) {
			for (Integer integer : key) {
				T w = map.get(integer);
				writeWorker(w, o, answer);
			}
		}
	}

	/**
	 * Prints a given map from end to start
	 * 
	 * @param <T>
	 */
	private static <T> void printMapBackWard(Map<Integer, T> map) {
		Set<Integer> keys = map.keySet();
		ArrayList<Integer> list = new ArrayList<>(keys);
		ListIterator<Integer> iterator = list.listIterator(list.size());

		while (iterator.hasPrevious()) {
			Integer key = iterator.previous();
			T value = map.get(key);
			System.out.println(key + ": " + value);
		}
	}

	/**
	 * Creats a map of workers sorted by name
	 * 
	 * @param <T
	 *            extends Worker<?>>
	 */
	private static <T extends Worker<?>> Map<Integer, T> createMap(ArrayList<T> arrList) {
		Set<T> set = new TreeSet<>(new Comparator<T>() {
			@Override
			public int compare(T t1, T t2) {
				int res = t1.getName().compareToIgnoreCase(t2.getName());
				return res != 0 ? res : 1;
			}
		});
		set.addAll(arrList);
		Map<Integer, T> treeMap = new TreeMap<>();
		int index = 1;
		for (T t : set)
			treeMap.put(index++, t);
		return treeMap;
	}

	/** Prints all elements in given ArrayList */
	private static void printArrList(ArrayList<Worker<?>> createWorkers) {
		for (Worker<?> worker : createWorkers)
			System.out.println(worker.toString());
	}

	/** Creates an ArrayList of workers with detailed/non-detailed save */
	private static ArrayList<Worker<?>> createWorkers(int answer) {
		final String[] aNames = { "Elvis", "Samba", "Bamba", "Bisli", "Kinder Bueno", "Elvis" };
		final String[] aDepNames = { "Software Engineering", "Mechanical Engineering",
				"Industrial And Medical Engineering", "Electrical Engineering", "Electrical Engineering",
				"Software Engineering" };
		final String[] aDepHeads = { "Boss1", "Boss2", "Boss3", "Boss4", "Boss4", "Boss1" };
		final int[] aSalaries = { 1000, 2000, 3000, 4000, 1000, 9999 };

		ArrayList<Worker<?>> arrList = new ArrayList<>();

		for (int i = 0; i < aNames.length; i++) {
			if (answer == TRUE)
				// dep as class Department
				arrList.add(
						new Worker<Department>(aNames[i], new Department(aDepNames[i], aDepHeads[i]), aSalaries[i]));
			else
				// dep as String
				arrList.add(new Worker<String>(aNames[i], aDepNames[i], aSalaries[i]));
		}

		return arrList;
	}

	/**
	 * Returns MyFileListIterator on a given file name starting in given index
	 * 
	 * @throws FileNotFoundException
	 */
	public static ListIterator<Worker<?>> listIterator(int index, String workersBin, int answer)
			throws FileNotFoundException {
		return new MyFileListIterator(index, new RandomAccessFile(workersBin, "rw"), answer);
	}

	/**
	 * MyFileListIterator implements ListIterator methods on a given file Allows to
	 * traverse the file by workers
	 */
	private static class MyFileListIterator implements ListIterator<Worker<?>> {
		private int cursor = 0; // Indicates element to be retrieved
		private int lastElementPos = -1; // Indicates element to be removed
		private RandomAccessFile f;
		private int answer;

		public MyFileListIterator(int index, RandomAccessFile workersFile, int answer) {
			this.cursor = index;
			this.f = workersFile;
			this.lastElementPos = -1;
			this.answer = answer;
		}

		@Override
		public boolean hasNext() {
			// boolean ans = false;
			try {
				// ans = cursor * WORKER_SIZE_BIN < f.length();
				f.seek(cursor * WORKER_SIZE_BIN);
				readWorker(f, answer);
			} catch (IOException e) {
				return false;
			}
			return true;
		}

		@Override
		public boolean hasPrevious() {
			// boolean res = false;
			try {
				// res = cursor > 0 && f.length() > 0;
				f.seek((cursor - 1) * WORKER_SIZE_BIN);
				readWorker(f, answer);
			} catch (IOException e) {
				return false;
			}
			return true;
		}

		@Override
		public Worker<?> next() {
			Worker<?> w = null;
			try {
				if (!hasNext())
					throw new NoSuchElementException();
				f.seek(cursor * WORKER_SIZE_BIN);
				w = readWorker(f, answer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			lastElementPos = cursor;
			cursor++;
			return w;
		}

		@Override
		public int nextIndex() {
			try {
				if (cursor * WORKER_SIZE_BIN == f.length())
					return (int) (f.length() / WORKER_SIZE_BIN);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return cursor + 1;
		}

		@Override
		public void add(Worker<?> w) {
			try {
				if (f.length() == 0)
					writeWorker(w, f, answer);
				else {
					long currentFilePosition = cursor * WORKER_SIZE_BIN;
					long fileLength = f.length();
					for (long i = fileLength - WORKER_SIZE_BIN; i >= currentFilePosition; i -= WORKER_SIZE_BIN) {
						f.seek(i);
						writeWorker(readWorker(f, answer), f, answer);
					}
					f.seek(currentFilePosition);
					writeWorker(w, f, answer);
				}
				cursor++;
				lastElementPos = -1;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public Worker<?> previous() {
			Worker<?> w = null;
			try {
				if (!hasPrevious())
					throw new NoSuchElementException();
				f.seek(--cursor * WORKER_SIZE_BIN);
				w = readWorker(f, answer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			lastElementPos = cursor;
			return w;
		}

		@Override
		public int previousIndex() {
			if (cursor == 0)
				return -1;
			return cursor - 1;
		}

		@Override
		public void remove() {
			if (lastElementPos == -1)
				throw new IllegalStateException();
			try {
				if (lastElementPos * WORKER_SIZE_BIN == f.length() - WORKER_SIZE_BIN)
					f.setLength(lastElementPos * WORKER_SIZE_BIN);
				else {
					long currentFilePosition = lastElementPos * WORKER_SIZE_BIN;
					long fileLength = f.length();
					Worker<?> tempWorker;
					for (long i = currentFilePosition + WORKER_SIZE_BIN; i < fileLength; i += WORKER_SIZE_BIN) {
						f.seek(i);
						tempWorker = readWorker(f, answer);
						f.seek(i - WORKER_SIZE_BIN);
						writeWorker(tempWorker, f, answer);
					}
					f.setLength(fileLength - WORKER_SIZE_BIN);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			cursor = lastElementPos;
			lastElementPos = -1;
		}

		@Override
		public void set(Worker<?> w) {
			if (lastElementPos == -1)
				throw new IllegalStateException();
			try {
				f.seek(lastElementPos * WORKER_SIZE_BIN);
				writeWorker(w, f, answer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			lastElementPos = -1;
		}

	}

}
