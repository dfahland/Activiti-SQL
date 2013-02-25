package org.activiti.explorer.ui.form;

import java.util.List;
import java.util.Map;

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
public class SQLFieldPropertyChangeListener extends SQLPropertyChangeListener<AbstractField, String> {
	
	private static final long serialVersionUID = 1L;

	public SQLFieldPropertyChangeListener(AbstractField property, String query, Map<String, Field> fields) {
		super(property, query, fields);
	}
	
    @Override
    protected String transformQueriedValuesToCurrentValue(List<String> queriedValues) {
    	if (queriedValues == null || queriedValues.size() == 0) return "<unknown>";
    	else return queriedValues.get(0);
    }
    
    @Override
    public void updateTargetField() {
		Field targetField = getTargetField();
		boolean readOnly = targetField.isReadOnly();
		targetField.setReadOnly(false);
		String currentValue = getCurrentValue();
		System.out.println("setting "+currentValue);
		targetField.setValue(currentValue);
		targetField.setReadOnly(readOnly);
    }
	
}