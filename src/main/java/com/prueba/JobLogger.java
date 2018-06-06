package com.prueba;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobLogger {
	private static final String LOGGER_NAME = "MyLog"; //Constante para el nombre del logger
	private static boolean logToFile;
	private static boolean logToConsole;
	private static boolean logMessage;
	private static boolean logWarning;
	private static boolean logError;
	private static boolean logToDatabase;
	private static boolean initialized;//se cambia a static
	private static Map<String, String> dbParams;
	private static Logger logger;
	private static Properties connectionProps;
	
	// Se agrega un contructor para evitar la creacion publica
	private JobLogger(){		
	}
	
	public static Properties getConnectionProps() {
		return connectionProps;
	}

	//Se remueve el constructor con parametros que inicializan variables estaticas puesto que es mala practica, en su lugar se crea un metodo que permite configurar el log
	public static void configLogger(boolean logToFileParam, boolean logToConsoleParam, boolean logToDatabaseParam,
			boolean logMessageParam, boolean logWarningParam, boolean logErrorParam, Map dbParamsMap) throws IOException{
		logger = Logger.getLogger(LOGGER_NAME);
		removeHandlers();
		logError = logErrorParam;
		logMessage = logMessageParam;
		logWarning = logWarningParam;
		logToDatabase = logToDatabaseParam;
		logToFile = logToFileParam;
		logToConsole = logToConsoleParam;		
		dbParams = dbParamsMap;
		initialized = true;
		if(logToConsole){
			configToConsole();
		}
		if(logToFile){
			configToFile();
		}
		if(logToDatabase){
			configToDatabase();
		}
	}
	
	//Se agrega metodo para remover los handlers
	private static void removeHandlers(){
		for(Handler handler : logger.getHandlers()) { 
			logger.removeHandler(handler); 
		}
	}
	
	//Se agrega metodo para configurar el handler de console
	private static void configToConsole(){
		ConsoleHandler ch = new ConsoleHandler();
		logger.addHandler(ch);
	}
	
	//Se agrega metodo para configurar el handler de file
	private static void configToFile() throws IOException{
		File logFile = new File(dbParams.get("logFileFolder") + "/logFile.txt");
		boolean existFile = logFile.exists();
		if (!existFile) {			
			existFile = logFile.createNewFile();
		}
		if(existFile){
			FileHandler fh = new FileHandler(dbParams.get("logFileFolder") + "/logFile.txt");
			logger.addHandler(fh);
		}
	}
	
	//Se agrega metodo para configurar las propiedades para la base de datos
	private static void configToDatabase(){
		connectionProps = new Properties();
		connectionProps.put("user", dbParams.get("userName"));
		connectionProps.put("password", dbParams.get("password"));
	}

	//Se remueve el Exception del throws y se crea la clase JobLoggerException
	//Se renombra para el nombre del metodo LogMessage empiece con minuscula
	public static void logMessage(String messageText, boolean message, boolean warning, boolean error) throws JobLoggerException, SQLException {
		// Se hace trim a messageText para comparar su tamaño
		if (messageText == null || messageText.trim().length() == 0) {
			return;
		}
		
		//Se valida el log
		logValidate(message, warning, error);
		
		//Se cambia le lugar el trim y se le asigna el valor a messageText
		messageText = messageText.trim();

		int t = 0;
		if (message && logMessage) {
			t = 1;
		} else if (error && logError) {
			t = 2;
		} else if (warning && logWarning) {
			t = 3;
		}

		// Se remueve la declaracion y asignaciones de la variable "l" puesto que no se usa en el log
		
		// Se junta el log a file y console puesto que en este punto hacen lo mismo y no deberia duplicarse
		if(logToFile || logToConsole) {			
			logger.log(Level.INFO, messageText);
		}
		
		if(logToDatabase) {
			insertToDatabase(messageText, t);
		}
	}
	
	private static void insertToDatabase(String messageText, int t) throws SQLException{
		Connection connection = null;		
		// Se remueve el Statement y se reemplaza por un PreparedStatement
		PreparedStatement pstmt = null;
		//El SQL para el insert tiene error le falta la palabra values
		String query = "insert into Log_Values values (?, ?)";
		try
		{
			String strServerName = dbParams.get("serverName");
			String url = "jdbc:" + dbParams.get("dbms"); 
					
			if(strServerName!=null && strServerName.trim().length()>0){
				url += "://" + strServerName	+ ":" + dbParams.get("portNumber") + "/";
				connection = DriverManager.getConnection(url, connectionProps);		
			}else{
				connection = DriverManager.getConnection(url);		
			}
			
			pstmt = connection.prepareStatement(query);
			//Se cambia la variable message por messageText, puesto que no tiene sentido no poner el texto del mensaje en el log
			pstmt.setString(1, messageText);
			pstmt.setString(2, String.valueOf(t));
			pstmt.executeUpdate();	
		} finally {
			//Se cierra el PreparedStatement y Connection a la base de datos
		    if (pstmt != null) {
		        try {
		        	pstmt.close();
		        } catch (SQLException e) { 
		        	logger.log(Level.INFO, e.getMessage());
		        }
		    }
		    if (connection != null) {
		        try {
		        	connection.close();
		        } catch (SQLException e) { 
		        	logger.log(Level.INFO, e.getMessage());
		        }
		    }
		}
	}
	
	private static void logValidate(boolean message, boolean warning, boolean error) throws JobLoggerException{
		//Se verifica que el configurador fue inicializado
		if(!initialized){
			throw new JobLoggerException("Uninitialized configuration");
		}		
		if (!logToConsole && !logToFile && !logToDatabase) {
			throw new JobLoggerException("Invalid configuration");
		}
		if ((!logError && !logMessage && !logWarning) || (!message && !warning && !error)) {
			throw new JobLoggerException("Error or Warning or Message must be specified");
		}
	}
	
}
