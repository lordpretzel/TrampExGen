package org.vagabond.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.xmlmodel.ConnectionInfoType;

/**
 * Singleton for conveniently creating database connections.
 * 
 * @author Boris Glavic
 *
 */
public class ConnectionManager {

	static Logger log = LogProviderHolder.getInstance().getLogger(ConnectionManager.class);
	
	private static ConnectionManager instance;
	
	private Connection con = null;
	
	private ConnectionManager () throws ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
	}
	
	public static ConnectionManager getInstance () throws ClassNotFoundException {
		if (instance == null) {
			instance = new ConnectionManager ();
		}
		
		return instance;
	}
	
	public Connection getConnection (MapScenarioHolder map) throws SQLException {
		ConnectionInfoType info = map.getScenario().getConnectionInfo();
		return getConnection(info.getHost(), info.getDB(), info.getUser(), info.getPassword(), info.getPort());
	}
	
	public Connection getConnection (String URL, String dbName, 
			String user, String password, int port) throws SQLException {
		closeCon();
		con =  DriverManager.getConnection(
				"jdbc:postgresql://" + URL + ":" + port + "/" + dbName, user, password);
		return con;
	}
	
	public Connection getConnection () {
		return con;
	}
	
	public void setConnection (Connection con) {
		this.con = con;
	}
	
	public void closeCon () throws SQLException {
		if (con != null && !con.isClosed()) {
			con.close();
			con = null;
		}
	}
	
	public Statement getSt () throws SQLException {
		return con.createStatement();
	}
	
	public void closeRs (ResultSet rs) throws SQLException {
		rs.getStatement().close();
		rs.close();
	}
	
	public ResultSet execQuery (String query) throws SQLException {
		return execQuery(con, query);
	}
	
	public ResultSet execQuery (Connection userCon, String query) throws SQLException {
		ResultSet rs;
		Statement st;
		
		st = userCon.createStatement();
		
		rs = st.executeQuery(query);
		
		return rs;
	}
	
	public int execUpdate(String query) throws SQLException {
		return execUpdate(con, query);
	}
	
	public int execUpdate (Connection userCon, String query) throws SQLException {
		Statement st;
		int numRowsAff;
		
		st = userCon.createStatement();
		
		numRowsAff = st.executeUpdate(query);
		return numRowsAff;
	}
	
}
