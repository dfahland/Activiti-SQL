package org.activiti.engine.impl.sql;

import java.lang.Character.UnicodeBlock;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLUtil {
	
	private static Logger log = Logger.getLogger(SQLUtil.class.getName());
	
	public static String getURL() {
		return "jdbc:mysql://localhost:3306/acme";
	}
	
	public static String getUser() {
		return "acme_activiti";
	}
	
	public static String getPassword() {
		return "acme_password";
	}
	
	private static Connection getConnection() throws SQLException {
		String url = getURL();
		String user = getUser();
		String password = getPassword();

		Connection con = DriverManager.getConnection(url, user, password);
		return con;
	}
	
	public static String escapeString(String s) {
		if (s == null) return s;
		
		String escaped = s;
		escaped = escaped.replace("`", "%U+0060");
		escaped = escaped.replace("\'", "%U+0027");
		escaped = escaped.replace("\"", "%U+0022");
		return escaped;
	}
	
	public static void logQuery(Connection con, String query, String processID, String caseID, String resource, String event, String result) {
		Statement log_st = null;
		try {
			log_st = con.createStatement();
			String escapedQuery = escapeString(query);
			String escapedResult = escapeString(result);
			log_st.executeUpdate("INSERT INTO `log` (`query`, `resource`, `event`, `caseID`, `processID`, `result`) VALUES ('"+escapedQuery+"','"+resource+"','"+event+"','"+caseID+"','"+processID+"','"+escapedResult+"');");
			
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Could not log query. "+e.getMessage(), e);
		} finally {
			try {
				log_st.close();
			} catch (SQLException e) {
				log.log(Level.WARNING, e.getMessage(), e);
			}
		}
	}
	
	public static int executeUpdate(String query, String processID, String caseID, String resource, String event) {

		Statement st = null;
		int rs = -1;

		Connection con = null;
		try {
			con = getConnection();
			st = con.createStatement();
			System.out.println(query);
			rs = st.executeUpdate(query);
			
			if (processID != null && caseID != null) logQuery(con, query, processID, caseID, resource, event, null);

		} catch (SQLException ex) {
			log.log(Level.SEVERE, ex.getMessage(), ex);

		} finally {
			try {
				if (st != null) {
					st.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException ex) {
				log.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
      return rs;
	}
	
	public static List<String> executeSelect(String query, String processID, String caseID, String resource, String event) {

		List<String> query_values = new LinkedList<String>();
		
		Statement st = null;
		ResultSet rs = null;

		Connection con = null;
		try {
			con = getConnection();
			st = con.createStatement();
			System.out.println(query);
			rs = st.executeQuery(query);
			while (rs.next()) {
				query_values.add(rs.getObject(1).toString());
			}
			
			if (processID != null && caseID != null) logQuery(con, query, processID, caseID, resource, event, query_values.toString());

		} catch (SQLException ex) {
			log.log(Level.SEVERE, ex.getMessage(), ex);

		} finally {
			try {
				if (st != null) {
					st.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException ex) {
				log.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
      return query_values;
	}

}
