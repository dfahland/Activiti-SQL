package org.activiti.explorer.ui.form;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.sql.SQLUtil;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Field;

/**
 * BPMN_ERP added
 * 
 * A value change listener that updates the value of a property if the value of an input field changes.
 * The new value is computed through evaluating an SQL query.
 * 
 * @author dfahland
 *
 */
public abstract class SQLPropertyChangeListener<F extends Field, T extends Object> implements ValueChangeListener {
	
	private static final long serialVersionUID = 1L;
	private String query;
	private Map<String, Field> sourceFields;
	private Map<String, Object> sourceValues;
	
	private T currentValue;
	private F targetField;
	
	private boolean firstQuery = true;
	
	public SQLPropertyChangeListener(F property, String query, Map<String, Field> fields) {
		this.targetField = property;
		this.query = query;
		this.sourceFields = fields;
		this.sourceValues = new HashMap<String, Object>();
		for (String fieldName : sourceFields.keySet()) {
			sourceValues.put(fieldName, null);
		}
	}
	
	private boolean updateSourceValues() {
		boolean updated = false;
		for (String fieldName : sourceFields.keySet()) {
			Field field = sourceFields.get(fieldName);
			if (field != null) {
				Object newValue = field.getValue();
				if (newValue != null && !newValue.equals(sourceValues.get(fieldName))) {
					updated = true;
					sourceValues.put(fieldName, newValue);
				}
			}
		}
		return updated;
	}
	
	private List<String> queryNewValues() {
		String iQuery = query;
		for (String fieldName : sourceValues.keySet()) {
			Object fieldValue = sourceValues.get(fieldName);
			if (fieldValue != null) {
				iQuery = iQuery.replace("&{"+fieldName+"}", fieldValue.toString());    						
			} else {
				return null;
			}
		}
  		List<String> values = dbConnection(iQuery, 1);
  		return values;
	}
	
	/**
	 * Select from a given list of queried values one value that shall be set for the {@link #targetField} 
	 * @param queriedValues
	 * @return
	 */
	protected abstract T transformQueriedValuesToCurrentValue(List<String> queriedValues);

	/**
	 * @return current value returned by the query based on values in input fields
	 */
	protected T getCurrentValue() {
		if (updateSourceValues()) {
			currentValue = transformQueriedValuesToCurrentValue(queryNewValues());
		} else if (firstQuery) {
			firstQuery = false;
			currentValue = transformQueriedValuesToCurrentValue(queryNewValues());
		}
		return currentValue;
	}
	
	/**
	 * Update properties of {@link #getTargetField()} based on queried values
	 */
	public abstract void updateTargetField();
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		updateTargetField();
	}
	
	/**
	 * @return field to update
	 */
	protected F getTargetField() {
		return targetField;
	}
	
	/**
	 * Resolve placeholder variables for fields to the actual fields. The fields
	 * have to be known to the {@link FormPropertyRenderer} which knows the
	 * parent form containing all fields that can be accessed for evaluating the
	 * query.
	 * 
	 * @param query
	 * @param renderer
	 * @return
	 */
	public static Map<String, Field> getFieldMapping(String query, FormPropertyRenderer renderer) { 
		Map<String, Field> fields = new HashMap<String, Field>();
		int index = 0;
		while ((index = query.indexOf("&{", index)) != -1) {
			int endIndex = query.indexOf("}", index);
			if (endIndex != -1) {
				String fieldName = query.substring(index+2,endIndex);
				System.out.println("field: "+fieldName+" = "+renderer.getFieldInParentForm(fieldName));
				fields.put(fieldName, renderer.getFieldInParentForm(fieldName));
			}
			index++; // move one on
		}
		return fields;
	}
	
	public static List<String> dbConnection(String selectQuery, int column) {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		String url = SQLUtil.getURL();
		String user = SQLUtil.getUser();
		String password = SQLUtil.getPassword();

		List<String> query_values = new LinkedList<String>();

		try {
			con = DriverManager.getConnection(url, user, password);
			st = con.createStatement();
			System.out.println(selectQuery);
			rs = st.executeQuery(selectQuery);

			try {
				while (rs.next()) {
					query_values.add(rs.getObject(column).toString());
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

		} catch (SQLException ex) {
			// log.log(Level.SEVERE, ex.getMessage(), ex);

		} finally {
			try {
				if (st != null) {
					st.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException ex) {
				// log.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
		return query_values;
	}
}