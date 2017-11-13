package io.github.rubinsoft.pengrad.openshift;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnector {
	public static Statement getStatement(String dbName) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		return getStatement(dbName, "", ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
	}
	
	public static Statement getStatement(String dbName, String dbAttribute) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		return getStatement(dbName, dbAttribute, ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
	}
	
	public static Statement getStatement(String dbName, String dbAttribute, int arg0, int arg1) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:mysql://127.6.211.2/"+dbName+dbAttribute,
					"adminq1HeHLQ","5dBUip1aD2Wh");
			return conn.createStatement(arg0, arg1);
	}
	
	public static String getParam(String dbName, String paramName) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		Statement statement = DBConnector.getStatement(dbName);
		ResultSet rsParam = statement.executeQuery("SELECT * FROM parameter WHERE name = '"+paramName+"'");
		if(!rsParam.first())
			throw new SQLException("param "+paramName+" not found on DB "+dbName);
		return rsParam.getString("value");
	}
	
	public static void setParam(String dbName, String paramName, String value) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		Statement statement = DBConnector.getStatement(dbName);
		ResultSet rsParam = statement.executeQuery("SELECT * FROM parameter WHERE name = '"+paramName+"'");
		if(!rsParam.first())
			throw new SQLException("param "+paramName+" not found on DB "+dbName);
		rsParam.absolute(1);
		rsParam.updateString("value", value);
	}
}
