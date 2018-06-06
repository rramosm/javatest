package com.prueba.test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.prueba.JobLogger;
import com.prueba.JobLoggerException;

import org.junit.Assert; 

public class JobLoggerTest {

	@Test
	public final void testConfigLoggerFileDirExist() throws IOException {
		boolean logToFileParam = true;
		boolean logToConsoleParam = false;
		boolean logToDatabaseParam = false;
		boolean logMessageParam = true;
		boolean logWarningParam = false;
		boolean logErrorParam = false;
		Map<String, String> dbParamsMap = new HashMap<String, String>();
		// Previamente debe existir y debemos de tener acceso a C:/TEMP
		dbParamsMap.put("logFileFolder", "C:/TEMP");
		dbParamsMap.put("userName", "userNameVal");
		dbParamsMap.put("password", "passwordVal");
		dbParamsMap.put("dbms", "dbmsVal");
		dbParamsMap.put("serverName", "serverNameVal");
		dbParamsMap.put("portNumber", "portNumberVal");
		JobLogger.configLogger(logToFileParam, logToConsoleParam, logToDatabaseParam, logMessageParam, logWarningParam, logErrorParam, dbParamsMap);
	}
	
	@Test(expected=IOException.class)
	public final void testConfigLoggerFileDirNoExist() throws IOException {
		boolean logToFileParam = true;
		boolean logToConsoleParam = false;
		boolean logToDatabaseParam = false;
		boolean logMessageParam = true;
		boolean logWarningParam = false;
		boolean logErrorParam = false;
		Map<String, String> dbParamsMap = new HashMap<String, String>();
		// No existe M:/proyectos
		dbParamsMap.put("logFileFolder", "M:/proyectos");
		dbParamsMap.put("userName", "userNameVal");
		dbParamsMap.put("password", "passwordVal");
		dbParamsMap.put("dbms", "dbmsVal");
		dbParamsMap.put("serverName", "serverNameVal");
		dbParamsMap.put("portNumber", "portNumberVal");
		JobLogger.configLogger(logToFileParam, logToConsoleParam, logToDatabaseParam, logMessageParam, logWarningParam, logErrorParam, dbParamsMap);
	}
	
	@Test
	public final void testConfigLoggerDatabaseTableExist() throws IOException, SQLException {
		configDatabase();
		Assert.assertEquals("userNameVal", JobLogger.getConnectionProps().getProperty("user"));	
		Assert.assertEquals("passwordVal", JobLogger.getConnectionProps().getProperty("password"));
		
	}

	@Test
	public final void testLogMessageDatabase() throws IOException, SQLException, JobLoggerException {
		testConfigLoggerDatabaseTableExist();
		String sURL = "jdbc:derby:memory:myDB;create=true";
	    Connection con = DriverManager.getConnection(sURL);
		Statement stmt = con.createStatement();
		ResultSet rs= stmt.executeQuery("SELECT count(mensaje) FROM Log_Values");
	    rs.next();
	    int countBefore = rs.getInt(1);
		JobLogger.logMessage("Mensaje en database "+new Date().getTime(), true, false, false);
		rs= stmt.executeQuery("SELECT count(mensaje) FROM Log_Values");
		rs.next();
		int countAfter = rs.getInt(1);
		Assert.assertEquals(countAfter,countBefore+1);	
	}
	
	private void configDatabase() throws SQLException, IOException{
		boolean logToFileParam = false;
		boolean logToConsoleParam = false;
		boolean logToDatabaseParam = true;
		boolean logMessageParam = true;
		boolean logWarningParam = false;
		boolean logErrorParam = false;
		Map<String, String> dbParamsMap = new HashMap<String, String>();
		dbParamsMap.put("logFileFolder", "C:/TEMP");
		dbParamsMap.put("userName", "userNameVal");
		dbParamsMap.put("password", "passwordVal");
		dbParamsMap.put("dbms", "derby:memory:myDB");
		dbParamsMap.put("serverName", "");
		dbParamsMap.put("portNumber", "");
		//setupDatabase
	    String sURL = "jdbc:derby:memory:myDB;create=true";
	    Connection con = DriverManager.getConnection(sURL);
	    Statement stmt = con.createStatement();
	    try{
	    	stmt.executeUpdate("CREATE TABLE Log_Values (mensaje varchar(200), level VARCHAR(1))");
	    } catch (Exception e) {
			// En caso ya exista la tabla
		}
	      
		JobLogger.configLogger(logToFileParam, logToConsoleParam, logToDatabaseParam, logMessageParam, logWarningParam, logErrorParam, dbParamsMap);
		con.close();
	}

}
