package com.rhomobile.hsqldata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class HsqlData {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		try {
			String sqliteFile = args[0];
			String hsqlFile = args[1];
			Class.forName("org.sqlite.JDBC");
			Connection sqliteConn = DriverManager.getConnection("jdbc:sqlite:"+ sqliteFile);
			Connection hsqlConn = hsqlOpen(hsqlFile);
			
			hsqlConn.createStatement().execute("SET PROPERTY \"hsqldb.default_table_type\" \'cached\'");
			hsqlConn.createStatement().execute("SET PROPERTY \"hsqldb.nio_data_file\" FALSE");
			
			hsqlConn.createStatement().execute(loadSchemaFromSqlite(sqliteConn));
			
			copyAllTables(sqliteConn, hsqlConn);

			hsqlConn.createStatement().execute("SHUTDOWN COMPACT");
			
			sqliteConn.close();
			hsqlConn.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private static Connection hsqlOpen(String fileName) throws Exception {
		Class.forName("org.hsqldb.jdbcDriver");
		return DriverManager.getConnection("jdbc:hsqldb:file:" + fileName,
				"SA", "");
	}
/*
	private static String loadSchema(String schemaFile) throws Exception {
		StringBuffer buffer = new StringBuffer();
		FileInputStream fis = new FileInputStream(schemaFile);
		int b;
		while ((b = fis.read()) != -1) {
			buffer.append((char)b);
		}
		return buffer.toString();
	}
*/
	private static String loadSchemaFromSqlite(Connection sqliteConn) throws Exception 
	{
		Statement sqliteStat = sqliteConn.createStatement();
		ResultSet rsTables = sqliteStat.executeQuery("SELECT sql FROM sqlite_master WHERE type='table' OR type='index'");

		String strSql = "";
		
		while (rsTables.next()) 
		{
			String str = rsTables.getString("sql");
			if ( str != null && str.length() > 0)
			{
				strSql += str;
				strSql += ";\r\n";
			}
		}
		
		rsTables.close();
		sqliteStat.close();
		
		return strSql;
	}
	
	private static String createInsertStatement(ResultSet rsRows, String tableName) throws Exception
	{
		String strInsert = "INSERT INTO ";
		
		strInsert += tableName;
		strInsert += "(";
		String strQuest = ") VALUES(";
		for (int i = 1; i <= rsRows.getMetaData().getColumnCount(); i++ )
		{
			if ( i > 1 )
			{
				strInsert += ",";
				strQuest += ",";
			}
			
			strInsert += "\"" + rsRows.getMetaData().getColumnName(i) + "\"";
			strQuest += "?";
		}
		
		strInsert += strQuest + ")"; 
		return strInsert;
	}
	
	private static void copyTable(String strTable, Connection sqliteConn, Connection hsqlConn) throws Exception
	{
		Statement sqliteStat = sqliteConn.createStatement();
		ResultSet rsRows = sqliteStat.executeQuery("SELECT * FROM " + strTable);
		while (rsRows.next())
		{
			PreparedStatement hsqlPrep = null;
			int nCount = 0;
			
			while (nCount < 10000 && rsRows.next()) 
			{
		    	if ( hsqlPrep == null )
		    	{
		    		String strInsert = createInsertStatement(rsRows, strTable);
		    		hsqlPrep = hsqlConn.prepareStatement(strInsert);
		    	}
	
		    	for ( int i = 1; i <= rsRows.getMetaData().getColumnCount(); i++)
		    		hsqlPrep.setObject(i,rsRows.getObject(i));
		    	
		    	nCount++;
	    		hsqlPrep.addBatch();
			}
			
			if ( hsqlPrep != null )
			{
				hsqlConn.setAutoCommit(false);
				hsqlPrep.executeBatch();
				hsqlConn.setAutoCommit(true);
				
				hsqlPrep.close();
			}
		}
		
		rsRows.close();
		sqliteStat.close();
	}
	
	private static void copyAllTables(Connection sqliteConn, Connection hsqlConn) throws Exception
	{
		Statement sqliteStat = sqliteConn.createStatement();
		ResultSet rsTables = sqliteStat.executeQuery("SELECT name FROM sqlite_master WHERE type='table' ");
		
		while (rsTables.next()) 
		{
			String strTable = rsTables.getString("name");
			
			copyTable(strTable, sqliteConn, hsqlConn);
		}
		
		rsTables.close();
		sqliteStat.close();
	}
/*	
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
	*/
}
