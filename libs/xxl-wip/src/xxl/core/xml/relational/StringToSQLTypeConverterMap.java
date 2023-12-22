/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.relational;

import java.util.HashMap;
import java.util.Map;

/**
	This class consists of a map which maps SQL-Types to StringToSQLTypeConverters.
	<br> <br>
	The method getObject(String S, String SQLTypeName) tries to convert a string into a java-object
	with the specified SQLType.
	<br>
	The following table shows the default mapping for various common SQL data types.
	<br> <br>

	<table BORDER COLS=2 >
	<tr CLASS="TableHeadingColor" >
	<th><b>SQL type</b></th>
	<th><b>Java type</b></th>
	</tr>

	<tr>
	<td>CHAR</td>
	<td>String</td>
	</tr>

	<tr CLASS="TableSubHeadingColor">
	<td>VARCHAR</td>
	<td>String</td>
	</tr>

	<tr>
	<td>LONGVARCHAR</td>
	<td>String</td>
	</tr>

	<tr CLASS="TableSubHeadingColor">
	<td>NUMERIC</td>
	<td>java.math.BigDecimal</td>
	</tr>

	<tr>
	<td>DECIMAL</td>
	<td>java.math.BigDecimal</td>
	</tr>

	<tr CLASS="TableSubHeadingColor">
	<td>BIT</td>
	<td>Boolean</td>
	</tr>

	<tr>
	<td>TINYINT</td>
	<td>Byte</td>
	</tr>

	<tr CLASS="TableSubHeadingColor">
	<td>SMALLINT</td>
	<td>Short</td>
	</tr>

	<tr>
	<td>INTEGER</td>
	<td>Integer</td>
	</tr>

	<tr CLASS="TableSubHeadingColor">
	<td>BIGINT</td>
	<td>Long</td>
	</tr>

	<tr>
	<td>REAL</td>
	<td>Float</td>
	</tr>

	<tr CLASS="TableSubHeadingColor">
	<td>FLOAT</td>
	<td>Double</td>
	</tr>

	<tr>
	<td>DOUBLE</td>
	<td>Double</td>
	</tr>

	<tr CLASS="TableSubHeadingColor">
	<td>BINARY</td>
	<td>byte[]</td>
	</tr>

	<tr>
	<td>VARBINARY</td>
	<td>byte[]</td>
	</tr>

	<tr CLASS="TableSubHeadingColor">
	<td>LONGVARBINARY</td>
	<td>byte[]</td>
	</tr>

	<tr>
	<td>DATE</td>
	<td>java.sql.Date</td>
	</tr>

	<tr CLASS="TableSubHeadingColor">
	<td>TIME</td>
	<td>java.sql.Time</td>
	</tr>

	<tr>
	<td>TIMESTAMP</td>
	<td>java.sql.Timestamp</td>
	</tr>

	<tr CLASS="TableSubHeadingColor">
	<td>COUNTER</td>
	<td>java.lang.Integer</td>
	</tr>

	</table>

	<br><br>
*/



public class StringToSQLTypeConverterMap {

	/**
		Converter Interface. <br><br>
		An object of this class should be able to convert strings to other java objects.
		Every converter hat its own SQL type in which the strings will be converted.
	*/
	public static interface StringToSqlTypeConverter {
		/**
		 * Converts the string into a java object (converter specific).
		 * @param S String to be converted
		 * @return a new converter specific java object.
		 */
		public Object convert(String S);

		/**
		 * Returns the java classname for this converter.
		 * @return the java classname for object created by this converter.
		 */
		public String getJavaClassName();

		/**
		 * Returns the java.sql.Types int value for this converter.
		 * @return The Type.
		 */
		public int getSQLType();
	}

	/**
		A map, which maps SQLTypeNames to converters.
	*/
	private Map reservoir;
	private Map map;

	/**
	 * Constructs a standard ConverterMap.
	 */
	public StringToSQLTypeConverterMap() {
		reservoir = new HashMap();

		/* ------------- COUNTER*/
		reservoir.put("COUNTER",new StringToSqlTypeConverter() {
						public Object convert(String S) {return new String(S);}
						public String getJavaClassName() {return "java.lang.Integer";}
						public int getSQLType() {return java.sql.Types.INTEGER;}
					}
		);

		/* ------------- LONGCHAR*/
		reservoir.put("LONGCHAR",new StringToSqlTypeConverter() {
						public Object convert(String S) {return new String(S);}
						public String getJavaClassName() {return "java.lang.String";}
						public int getSQLType() {return 12;}
					}
		);

		/* ------------- NUMERIC*/
		reservoir.put("NUMERIC",new StringToSqlTypeConverter() {
						public Object convert(String S) { return new java.math.BigDecimal(S);}
						public String getJavaClassName() {return "java.math.BigDecimal";}
						public int getSQLType() {return java.sql.Types.NUMERIC;}
					}
		);

		/* ------------- INTEGER*/
		reservoir.put("INTEGER",new StringToSqlTypeConverter() {
						public Object convert(String S) { return new Integer(S);	}
						public String getJavaClassName() {return "java.lang.Integer";}
						public int getSQLType() {return java.sql.Types.INTEGER;}
					}
		);

		/* ------------- DECIMAL*/
		reservoir.put("DECIMAL",new StringToSqlTypeConverter() {
						public Object convert(String S) { return new java.math.BigDecimal(S);}
						public String getJavaClassName() {return "java.math.BigDecimal";}
						public int getSQLType() {return java.sql.Types.DECIMAL;}
					}
		);

		/* ------------- SMALLINT*/
		reservoir.put("SMALLINT",new StringToSqlTypeConverter() {
						public Object convert(String S) { return new Short(S);}
						public String getJavaClassName() {return "java.lang.Short";}
						public int getSQLType() {return java.sql.Types.SMALLINT;}
					}
		);

		/* ------------- BYTE*/
		reservoir.put("BYTE",new StringToSqlTypeConverter() {
						public Object convert(String S) { return new Byte(S);}
						public String getJavaClassName() {return "java.lang.Byte";}
						public int getSQLType() {return java.sql.Types.OTHER;}
					}
		);
		/* ------------- CHAR*/
		reservoir.put("CHAR",new StringToSqlTypeConverter() {
						public Object convert(String S) { return new String(S);}
						public String getJavaClassName() {return "java.lang.String";}
						public int getSQLType() {return java.sql.Types.CHAR;}
					}
		);

		/* ------------- VARCHAR*/
		reservoir.put("VARCHAR",new StringToSqlTypeConverter() {
						public Object convert(String S) { return new String(S);}
						public String getJavaClassName() {return "java.lang.String";}
						public int getSQLType() {return java.sql.Types.VARCHAR;}
					}
		);

		/* ------------- LONGVARCHAR*/
		reservoir.put("LONGVARCHAR",new StringToSqlTypeConverter() {
						public Object convert(String S) { return new String(S);}
						public String getJavaClassName() {return "java.lang.String";}
						public int getSQLType() {return java.sql.Types.LONGVARCHAR;}
					}
		);

		/* ------------- BIT*/
		reservoir.put("BIT",new StringToSqlTypeConverter() {
						public Object convert(String S) {	return new Boolean(S);	}
						public String getJavaClassName() {return "java.lang.Boolean";}
						public int getSQLType() {return java.sql.Types.BIT;}
					}
		);

		/* ------------- TINYINT*/
		reservoir.put("TINYINT",new StringToSqlTypeConverter() {
						public Object convert(String S) {return new Byte(S);}
						public String getJavaClassName() {return "java.lang.Byte";}
						public int getSQLType() {return java.sql.Types.TINYINT;}
					}
		);

		/* ------------- BIGINT*/
		reservoir.put("BIGINT",new StringToSqlTypeConverter() {
						public Object convert(String S) {return new Long(S);}
						public String getJavaClassName() {return "java.lang.Long";}
						public int getSQLType() {return java.sql.Types.BIGINT;}
					}
		);

		/* ------------- REAL*/
		reservoir.put("REAL",new StringToSqlTypeConverter() {
						public Object convert(String S) {return new Float(S);}
						public String getJavaClassName() {return "java.lang.Float";}
						public int getSQLType() {return java.sql.Types.REAL;}
					}
		);

		/* ------------- FLOAT*/
		reservoir.put("FLOAT",new StringToSqlTypeConverter() {
						public Object convert(String S) {return new Double(S);}
						public String getJavaClassName() {return "java.lang.Double";}
						public int getSQLType() {return java.sql.Types.FLOAT;}
					}
		);

		/* ------------- DOUBLE*/
		reservoir.put("DOUBLE",new StringToSqlTypeConverter() {
						public Object convert(String S) {return new Double(S);}
						public String getJavaClassName() {return "java.lang.Double";}
						public int getSQLType() {return java.sql.Types.DOUBLE;}
					}
		);

		/* ------------- BINARY*/
		reservoir.put("BINARY",new StringToSqlTypeConverter() {
						public Object convert(String S) {return  S.getBytes();}
						public String getJavaClassName() {return "byte[]";}
						public int getSQLType() {return java.sql.Types.BINARY;}
					}
		);

		/* ------------- VARBINARY*/
		reservoir.put("VARBINARY",new StringToSqlTypeConverter() {
						public Object convert(String S) {return  S.getBytes();}
						public String getJavaClassName() {return "byte[]";}
						public int getSQLType() {return java.sql.Types.VARBINARY;}
					}
		);

		/* ------------- LONGVARBINARY*/
		reservoir.put("LONGVARBINARY",new StringToSqlTypeConverter() {
						public Object convert(String S) {return  S.getBytes();}
						public String getJavaClassName() {return "byte[]";}
						public int getSQLType() {return java.sql.Types.LONGVARBINARY;}
					}
		);

		/* ------------- DATE*/
		reservoir.put("DATE",new StringToSqlTypeConverter() {
						public Object convert(String S) {return  java.sql.Date.valueOf(S);}
						public String getJavaClassName() {return "java.sql.Date";}
						public int getSQLType() {return java.sql.Types.DATE;}
					}
		);

		/* ------------- TIME*/
		reservoir.put("TIME",new StringToSqlTypeConverter() {
						public Object convert(String S) {return  java.sql.Time.valueOf(S);}
						public String getJavaClassName() {return "java.sql.Time";}
						public int getSQLType() {return java.sql.Types.TIME;}
					}
		);

		/* ------------- TIMESTAMP*/
		reservoir.put("TIMESTAMP",new StringToSqlTypeConverter() {
						public Object convert(String S) {return  java.sql.Timestamp.valueOf(S);}
						public String getJavaClassName() {return "java.sql.Timestamp";}
						public int getSQLType() {return java.sql.Types.TIMESTAMP;}
					}
		);

		map = reservoir;
	}

	/**
		Returns a StringToSQLTypeConverterMap supporting the specified SQL-Types.
		@param SQLTypes a String[]
	*/
	public StringToSQLTypeConverterMap(String []SQLTypes) {
		this();
		map = new HashMap();
		for (int i=0;i<SQLTypes.length;i++) {
			map.put (SQLTypes[i],this.getConverter(SQLTypes[i]));
		}
	}

	/**
	 * Returns a converter which converts strings to objects with this SQLType.
	 * @param SQLType The SQLType.
	 * @return The Converter.
	 */
	public StringToSqlTypeConverter getConverter(String SQLType) {
		return ((StringToSqlTypeConverter) map.get (SQLType));

	}

	/**
	 * Converts this string to an object which has this SQLType.
	 * @param s The String which is converted.
	 * @param SQLType The SQLType.
	 * @return The Object.
	 */
	public Object getObject(String s,String SQLType) {
		return getConverter(SQLType).convert(s);
	}

	/**
	 * Returns the java.sql.Types int value for this SQLType string.
	 * @param SQLType The SQLType as String
	 * @return The SQL Type.
	 */
	public int getSQLType (String SQLType) {
		return getConverter(SQLType).getSQLType();
	}

	/**
	 * Returns the Java Type for this SQL Type.
	 * @param SQLType The SQLType as String.
	 * @return the Java Type for this SQL Type
	 */
	public String getJavaClassName (String SQLType) {
		return getConverter(SQLType).getJavaClassName();
	}

	/**
		Returns the map in which the converters are stored.
		@return the map in which the converters are stored
	*/
	public Map getMap() {
		return map;
	}

	/**
		Sets the map in which the converters are stored (private access).
		@param map new map
	*/
	public void setMap(Map map) {
		this.map = map;
	}
}
