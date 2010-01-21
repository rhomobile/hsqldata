package com.rhomobile.hsqldata;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class HsqlData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String sqliteFile = args[0];
			String hsqlFile = args[1];
			String schemaFile = args[2];
			Class.forName("org.sqlite.JDBC");
			Connection sqliteConn = DriverManager.getConnection("jdbc:sqlite:"+ sqliteFile);
			Connection hsqlConn = hsqlOpen(hsqlFile);
			
			hsqlConn.createStatement().execute("SET PROPERTY \"hsqldb.default_table_type\" \'cached\'");
			hsqlConn.createStatement().execute("SET PROPERTY \"hsqldb.nio_data_file\" FALSE");
			
			hsqlConn.createStatement().execute(loadSchema(schemaFile));
			
			copyObjectValues(sqliteConn, hsqlConn);
			copySources(sqliteConn, hsqlConn);

			hsqlConn.createStatement().execute("SHUTDOWN COMPACT");
			
			sqliteConn.close();
			hsqlConn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Connection hsqlOpen(String fileName) throws Exception {
		Class.forName("org.hsqldb.jdbcDriver");
		return DriverManager.getConnection("jdbc:hsqldb:file:" + fileName,
				"SA", "");
	}

	private static String loadSchema(String schemaFile) throws Exception {
		StringBuffer buffer = new StringBuffer();
		FileInputStream fis = new FileInputStream(schemaFile);
		int b;
		while ((b = fis.read()) != -1) {
			buffer.append((char)b);
		}
		return buffer.toString();
	}

	private static void copyObjectValues(Connection sqliteConn, Connection hsqlConn) throws Exception
	{
		PreparedStatement hsqlPrep = hsqlConn.prepareStatement("insert into object_values (source_id,object,attrib,value,attrib_type) values (?,?,?,?,?);");

		Statement sqliteStat = sqliteConn.createStatement();
		ResultSet rs = sqliteStat.executeQuery("select * from object_values;");
		
		while (rs.next()) 
		{
			hsqlPrep.setInt(1, rs.getInt("source_id"));
			hsqlPrep.setString(2, rs.getString("object"));
			hsqlPrep.setString(3, rs.getString("attrib"));
			hsqlPrep.setString(4, rs.getString("value"));
			hsqlPrep.setString(5, rs.getString("attrib_type"));
			hsqlPrep.addBatch();
		}
		
		hsqlConn.setAutoCommit(false);
		hsqlPrep.executeBatch();
		hsqlConn.setAutoCommit(true);
		
		sqliteStat.close();
		rs.close();
		hsqlPrep.close();
	}
	
	private static void copySources(Connection sqliteConn, Connection hsqlConn) throws Exception
	{
		PreparedStatement hsqlPrep = hsqlConn.prepareStatement("insert into sources (source_id,name,priority,partition,sync_type,source_attribs) values (?,?,?,?,?,?);");

		Statement sqliteStat = sqliteConn.createStatement();
		ResultSet rs = sqliteStat.executeQuery("select * from sources;");
		
		while (rs.next()) {
			hsqlPrep.setInt(1, rs.getInt("source_id"));
			hsqlPrep.setString(2, rs.getString("name"));
			hsqlPrep.setInt(3, rs.getInt("priority"));
			hsqlPrep.setString(4, rs.getString("partition"));
			hsqlPrep.setString(5, rs.getString("sync_type"));
			hsqlPrep.setString(6, rs.getString("source_attribs"));
			hsqlPrep.addBatch();
		}
		
		hsqlConn.setAutoCommit(false);
		hsqlPrep.executeBatch();
		hsqlConn.setAutoCommit(true);
		
		sqliteStat.close();
		rs.close();
		hsqlPrep.close();
	}
	
}
