package xxl.tests.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import xxl.core.util.DatabaseAccess;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class DatabaseAccess.
 */
public class TestDatabaseAccess {

	/**
	 * Tests a database connection using a specified property file.
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	*/
	public static void main(String args[]) {
		if (args.length==0 || args.length>2) {
			System.out.println("The property-file has to be passed. If a second argument is passed, ");
			System.out.println("then a small write test is performed on the database.");
		}
		else {
			try {
				DatabaseAccess dba = DatabaseAccess.loadFromPropertyFile(args[0]);
				System.out.println(dba);
				Connection con = dba.getConnection();
	
				if (args.length==2) {
					Statement stmt = con.createStatement();
					if (args[1].equalsIgnoreCase("create"))
						stmt.execute("create table test(i integer)");
					else if (args[1].equalsIgnoreCase("drop"))
						stmt.execute("drop table test");
					else if (args[1].equalsIgnoreCase("insert"))
						stmt.execute("insert into test values (42)");
					else {
						stmt.execute("create table test(i integer)");
						stmt.execute("insert into test values (42)");
						stmt.execute("drop table test");
					}
					stmt.close();
				}
				con.close();
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
