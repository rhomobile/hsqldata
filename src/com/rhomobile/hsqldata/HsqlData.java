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
			String indexSchemaFile = args[3];
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"
					+ sqliteFile);
			Statement stat = conn.createStatement();

			Connection hsqlConn = hsqlOpen(hsqlFile);
			hsqlConn.createStatement().execute(loadSchema(schemaFile));
			PreparedStatement hsqlPrep = hsqlConn
					.prepareStatement("insert into object_values (source_id,object,attrib,value,attrib_type) values (?,?,?,?,?);");

			ResultSet rs = stat.executeQuery("select * from object_values;");
			while (rs.next()) {
				hsqlInsertRow(hsqlPrep, rs.getString("source_id"), rs
						.getString("object"), rs.getString("attrib"), rs
						.getString("value"), rs.getString("attrib_type"));
			}

			hsqlConn.setAutoCommit(false);
			hsqlPrep.executeBatch();
			hsqlConn.setAutoCommit(true);
			hsqlConn.createStatement().execute(loadSchema(indexSchemaFile));
			hsqlConn.createStatement().execute("SHUTDOWN");

			rs.close();
			conn.close();
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

	private static void hsqlInsertRow(PreparedStatement stmt, String sourceId,
			String object, String attrib, String value, String attribType)
			throws Exception {
		stmt.setString(1, sourceId);
		stmt.setString(2, object);
		stmt.setString(3, attrib);
		stmt.setString(4, value);
		stmt.setString(5, attribType);
		stmt.addBatch();
	}
}
