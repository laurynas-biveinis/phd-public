package xxl.tests.relational.tuples;

import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData;
import xxl.core.relational.metaData.StoredColumnMetaData;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.relational.tuples.ListTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.relational.tuples.TupleConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class TupleConverter.
 */
public class TestTupleConverter {
	
	/**
	 * This main method contains an example how to use a tuple converter. 
	 * 
	 * @param args the arguments to the main method.
	 * @throws IOException if an I/O error occurs. 
	 * @throws SQLException if an database error occurs. 
	 */
	public static void main(String[] args) throws IOException, SQLException  {
		
		// ********************************************************************
		// * example 1                                                        *
		// ********************************************************************
		
		ColumnMetaDataResultSetMetaData metadata = new ColumnMetaDataResultSetMetaData(
			new StoredColumnMetaData(false, true,  true, false, ResultSetMetaData.columnNullable, false, 10, "stagenm", "stagenm", "",  0, 0, "", "", java.sql.Types.VARCHAR,  false, true, false),
			new StoredColumnMetaData(false, true,  true, false, ResultSetMetaData.columnNullable, false, 10, "birthnm", "birthnm", "",  0, 0, "", "", java.sql.Types.VARCHAR,  false, true, false),
			new StoredColumnMetaData(false, true,  true, false, ResultSetMetaData.columnNullable, false, 10, "firstnm", "firstnm", "",  0, 0, "", "", java.sql.Types.VARCHAR,  false, true, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, false,  5, "dob",     "dob",     "",  5, 0, "", "", java.sql.Types.SMALLINT, false, true, false),
			new StoredColumnMetaData(false, true,  true, false, ResultSetMetaData.columnNullable, false,  5, "origin",  "origin",  "",  0, 0, "", "", java.sql.Types.VARCHAR,  false, true, false),
			new StoredColumnMetaData(false, true,  true, false, ResultSetMetaData.columnNullable, false,  5, "gender",  "gender",  "",  0, 0, "", "", java.sql.Types.VARCHAR,  false, true, false),
			new StoredColumnMetaData(false, false, true, true,  ResultSetMetaData.columnNullable, true,  10, "income",  "income",  "", 10, 5, "", "", java.sql.Types.NUMERIC,  false, true, false)
		);
		System.out.println("Number of columns: " + metadata.getColumnCount());
		
		// create 3 objectarrays a,b,c, that contain data from 3 tuples.
		Object[][] columns = new Object[][] {
			new Object[] {"Willie Aames",  "Aames", "William", (short)1960, "\\Am", "M", new java.math.BigDecimal("12244.2")},
			new Object[] {"Bud Abbott",    "Abott", "William", (short)1895, "\\Am", "M", null},
			new Object[] {"Diahnne Abott", null,    null,      (short)1960, "\\Am", "F", new java.math.BigDecimal("2712244.23333")}
		};
		
		//create array of arraytuples
		Tuple[] tuples = new Tuple[columns.length];
		for (int i = 0; i < tuples.length; i++)
			tuples[i] = ArrayTuple.FACTORY_METHOD.invoke(Arrays.asList(columns[i]));
		
		// output of original tuples
		System.out.println("Tuples constructed");
		for (Tuple tuple : tuples)
			System.out.println(tuple);
		
		// create a tuple converter in oder to read and write the tuples
		TupleConverter tupleConverter = new TupleConverter(true, metadata);
		
		// create a byte array output stream
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		
		// create an output stream
		java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
		
		// writes a tuple on the output stream
		System.out.println("Write tuples");
		for (Tuple tuple : tuples)
			tupleConverter.write(dos, tuple);
		
		// create an input stream on the output stream's output
		java.io.DataInputStream dis = new java.io.DataInputStream(new java.io.ByteArrayInputStream(baos.toByteArray()));
		
		// reads 3 tuples from the input stream
		System.out.println("Read tuples");
		Tuple[] newTuples = new Tuple[tuples.length];
		for (int i = 0; i < newTuples.length; i++)
			newTuples[i] = tupleConverter.read(dis);
		
		// close the streams after use
		dis.close();
		dos.close();
		
		// output of the all tuple
		System.out.println("Output the restored tuples");
		for (Tuple tuple : newTuples)
			System.out.println(tuple);
		
		System.out.println("Test the restored tuples");
		
		for (int i = 0; i < tuples.length; i++)
			if (!tuples[i].equals(newTuples[i])) {
				System.out.println("Tuples are not identical!");
				throw new RuntimeException("Tuples should have been identical");
			}
		
		System.out.println("Tuples successfully reconstructed\n");

		// ********************************************************************
		// * example 2                                                        *
		// ********************************************************************
		
		// create a tuple converter in oder to read and write the tuples
		tupleConverter = new TupleConverter(
			ListTuple.FACTORY_METHOD,
			true,
			xxl.core.io.converters.Converters.getObjectConverter(xxl.core.io.converters.StringConverter.DEFAULT_INSTANCE),
			xxl.core.io.converters.Converters.getObjectConverter(xxl.core.io.converters.StringConverter.DEFAULT_INSTANCE),
			xxl.core.io.converters.Converters.getObjectConverter(xxl.core.io.converters.StringConverter.DEFAULT_INSTANCE),
			xxl.core.io.converters.Converters.getObjectConverter(xxl.core.io.converters.ShortConverter.DEFAULT_INSTANCE),
			xxl.core.io.converters.Converters.getObjectConverter(xxl.core.io.converters.StringConverter.DEFAULT_INSTANCE),
			xxl.core.io.converters.Converters.getObjectConverter(xxl.core.io.converters.StringConverter.DEFAULT_INSTANCE),
			xxl.core.io.converters.Converters.getObjectConverter(xxl.core.io.converters.BigDecimalConverter.DEFAULT_INSTANCE)
		);
		
		// create a byte array output stream
		baos = new java.io.ByteArrayOutputStream();
		
		// create an output stream
		dos = new java.io.DataOutputStream(baos);
		
		// writes a tuple on the output stream
		System.out.println("Write tuples");
		for (Tuple tuple : tuples)
			tupleConverter.write(dos, tuple);
		
		// create an input stream on the output stream's output
		dis = new java.io.DataInputStream(new java.io.ByteArrayInputStream(baos.toByteArray()));
		
		// reads 3 tuples from the input stream
		System.out.println("Read tuples");
		newTuples = new Tuple[tuples.length];
		for (int i = 0; i < newTuples.length; i++)
			newTuples[i] = tupleConverter.read(dis);
		
		// close the streams after use
		dis.close();
		dos.close();
		
		// output of the all tuple
		System.out.println("Output the restored tuples");
		for (Tuple tuple : newTuples)
			System.out.println(tuple);
		
		System.out.println("Test the restored tuples");
		
		for (int i = 0; i < tuples.length; i++)
			if (!tuples[i].equals(newTuples[i])) {
				System.out.println("Tuples are not identical!");
				throw new RuntimeException("Tuples should have been identical");
			}
		
		System.out.println("Tuples successfully reconstructed");
	}

}
