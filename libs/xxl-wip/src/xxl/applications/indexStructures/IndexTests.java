/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.applications.indexStructures;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import xxl.core.collections.containers.CounterContainer;
import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.Cursor;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.indexStructures.Descriptor;
import xxl.core.io.converters.MeasuredConverter;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.util.Interval1D;

public class IndexTests {
	/**
	 * A class to represent data elements for testing purposes. Each instance
	 * contains an integer key value and a byte array representing the data
	 * content.
	 */

	public static class TestDataElement implements Comparable {
		protected int key;
		protected byte[] data;
		
		protected TestDataElement(int key, byte[] data) {
			if (data.length != dataSize)
				throw new IllegalArgumentException();

			this.key = key;
			this.data = data;
		}

		protected TestDataElement(int key) {
			this.key = key;
			this.data = new byte[dataSize];
		}

		public int getKey() {
			return key;
		}

		@Override
		public int hashCode() {
			return key;
		}

		@Override
		public boolean equals(Object obj) {
			return key == ((TestDataElement)obj).key;
		}

		public int compareTo(Object obj) {
			int key_ = ((TestDataElement)obj).key;
			if (key > key_) return +1;
			if (key < key_) return -1;
			return 0;
		}

		@Override
		public String toString() {
			return "" + key;
		}

		protected static int dataSize;

		public static int getDataSize() {
			return dataSize;
		}

		public static void setDataSize(int dataSize) {
			TestDataElement.dataSize = dataSize;
		}

		/**
		 * This <tt>Function</tt> extracts the key value of a TestDataElement.
		 */
		public static final Function<TestDataElement,Integer> GET_KEY =
		new AbstractFunction<TestDataElement,Integer>() {
			public Integer invoke(TestDataElement elem) {
				return elem.key;	
			}
		};

		public static final Function<TestDataElement,Descriptor> GET_DESCRIPTOR =
		new AbstractFunction<TestDataElement,Descriptor> () {
			public Descriptor invoke (TestDataElement elem) {
				return new Interval1D(elem.key);
			}

			public Descriptor invoke (TestDataElement from, TestDataElement to) {
				return new Interval1D(from.key, to.key);
			}
		};

		/**
		 * A default <tt>Converter</tt> to serialize TestDataElements.
		 */
		public static final MeasuredConverter<TestDataElement> DEFAULT_CONVERTER =
		new MeasuredConverter<TestDataElement>() {
			public TestDataElement read(DataInput input, TestDataElement elem) throws IOException {
				int key = input.readInt();
				byte[] data = new byte[input.readInt()];
				input.readFully(data);
				return new TestDataElement(key, data);
			}

			public void write(DataOutput output, TestDataElement elem) throws IOException {
				output.writeInt(elem.key);
				output.writeInt(elem.data.length);
				output.write(elem.data);
			}
			
			public int getMaxObjectSize() {
				return 2 * Integer.SIZE / 8 + dataSize;
			}
		};
		
		/**
		 * A <tt>Converter</tt> to serialize the keys of TestDataElements.
		 */
		public static final MeasuredConverter<Integer> KEY_CONVERTER =
		new MeasuredConverter<Integer>() {
			public Integer read(DataInput input, Integer object) throws IOException {
				return input.readInt();
			}	

			public void write(DataOutput output, Integer object) throws IOException {
				output.writeInt(object);
			}

			public int getMaxObjectSize() {
				return Integer.SIZE / 8;
			}
		};
	}	

	public static class TestDataHelper {
		private static int[] createKeyArray(int size) {
			int[] arr = new int[size];
			for (int i = 0; i < size; i++)
				arr[i] = i;
			for (int i = 0; i < size; i++) {
				int p1 = (int)(Math.random() * size);
				int p2 = (int)(Math.random() * size);
				int temp = arr[p1];
				arr[p1] = arr[p2];
				arr[p2] = temp;
			}
			return arr;	
		}

		public static void createKeyFile(String file, int size) {
			try {
				PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
				int[] arr = createKeyArray(size);

				for (int i = 0; i < size; i++) {
					int insVersion = i;
					int delVersion = insVersion + 20;
					out.println(arr[i]+","+insVersion+","+delVersion);
				}

				out.flush();
				out.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		public static void createKeyFile1(String file, int size) {
			try {
				PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
				int[] arr = createKeyArray(size);

				for (int i = 0; i < size; i++) {
					int insVersion = 1;
					int delVersion = insVersion + 20;
					out.println(arr[i]+","+insVersion+","+delVersion);
					
				}
				out.flush();
				out.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public static void createKeyFile_FixedInterval(String file, int size, int window) {
			try {
				PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
				int[] arr = createKeyArray(size);

				for (int i = 0; i < size; i++) {
					int insVersion = i;
					int delVersion = insVersion + window;
					out.println(arr[i]+","+insVersion+","+delVersion);
				}

				out.flush();
				out.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public static void createKeyFile_RandomExpTime(String file, int size, int window) {
			try {
				PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
				int[] arr = createKeyArray(size);

				for (int i = 0; i < size; i++) {
					int insVersion = i;
					int delVersion = insVersion + (int)(Math.random()*window) + 1;
					out.println(arr[i]+","+insVersion+","+delVersion);
				}

				out.flush();
				out.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public static void createSnapshot(String inFile, String outFile, int ts) {
			try {
				Cursor<TemporalObject> it = keyFileCursor(inFile);
				PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)));
				while (it.hasNext()) {
					TemporalObject obj = it.next();
					if (obj.getTimeInterval().contains(ts))
						out.println(((TestDataElement)obj.getObject()).getKey()+","+obj.getStart()+","+obj.getEnd());
					if (obj.getStart() > ts)
						break;
				}
				out.flush();
				out.close();
				it.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		private static void printElement(PrintStream out, TemporalObject obj) {
			out.println(((TestDataElement)obj.getObject()).getKey()+","+obj.getStart()+","+obj.getEnd());
		}

		public static void createRangeFromSnapshot(String inFile, String outFile, int count, int rangeSize, int size) {
			try {
				Cursor<TemporalObject> it = keyFileCursor(inFile);
				List<TemporalObject> list = new ArrayList<TemporalObject>();
				while (it.hasNext())
					list.add(it.next());
				it.close();
				Comparator<TemporalObject> comp = new Comparator<TemporalObject>() {
					public int compare(TemporalObject o1, TemporalObject o2) {
						int key1 = ((TestDataElement)o1.getObject()).getKey();
						int key2 = ((TestDataElement)o2.getObject()).getKey();
						if (key1 > key2) return +1;
						if (key1 < key2) return -1;
						return 0;
					}
				};
				Collections.sort(list, comp);

				PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)));
				for (int i = 0; i < count; i++) {
					int start = (int)(Math.random() * (size - rangeSize - 1));
					printElement(out, list.get(start));
					printElement(out, list.get(start+rangeSize-1));
				}
				out.flush();
				out.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public static Cursor<TemporalObject> keyFileCursor(final String file) {
			return new AbstractCursor<TemporalObject>() {
				BufferedReader in;
				String s;
				
				@Override
				public void open() {
					super.open();
					try {
						in = new BufferedReader(new FileReader(file));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public void close() {
					super.close();
					try {
						in.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				protected boolean hasNextObject() {
					try {
						s = in.readLine();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					return s != null;
				}

				@Override
				protected TemporalObject nextObject() {
					if (s == null) throw new NoSuchElementException();
					StringTokenizer tok = new StringTokenizer(s, ",");
					int key = Integer.parseInt(tok.nextToken());
					int insVersion = Integer.parseInt(tok.nextToken());
					int delVersion = Integer.parseInt(tok.nextToken());
					
					return new TemporalObject<TestDataElement>(
							new TestDataElement(key),
							new TimeInterval(insVersion, delVersion));
				}
			};
		}
	}

	/**
	 * Helper class for counting the number of block accesses.
	 */
	public static class BlockCounter {
		public long inserts, gets, updates, removes, reserves;
		
		public BlockCounter() {
		}

		public BlockCounter(CounterContainer c) {
			inserts = c.inserts;
			gets = c.gets;
			updates = c.updates;
			removes = c.removes;
			reserves = c.reserves;
		}
		
		public void add(CounterContainer c) {
			inserts += c.inserts;
			gets += c.gets;
			updates += c.updates;
			removes += c.removes;
			reserves += c.reserves;
		}

		public void sub(CounterContainer c) {
			inserts -= c.inserts;
			gets -= c.gets;
			updates -= c.updates;
			removes -= c.removes;
			reserves -= c.reserves;
		}

		public void add(BlockCounter c) {
			inserts += c.inserts;
			gets += c.gets;
			updates += c.updates;
			removes += c.removes;
			reserves += c.reserves;
		}
		
		public void sub(BlockCounter c) {
			inserts -= c.inserts;
			gets -= c.gets;
			updates -= c.updates;
			removes -= c.removes;
			reserves -= c.reserves;
		}

		public void neg() {
			inserts = -inserts;
			gets = -gets;
			updates = -updates;
			removes = -removes;
			reserves = -reserves;
		}
		
		public void reset() {
			inserts = gets = updates = removes = reserves = 0;
		}
		
		public String toString() {
			return
				"inserts = "+inserts + ", " +
				"gets = "+gets + ", " +
				"updates = "+updates + ", " +
				"removes = "+removes + ", " +
				"reserves = "+reserves+
				"\tTOTAL: "+(inserts+gets+updates);
		}
	}
}
