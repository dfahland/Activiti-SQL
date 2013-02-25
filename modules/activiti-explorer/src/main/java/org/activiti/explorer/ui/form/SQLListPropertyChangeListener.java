package org.activiti.explorer.ui.form;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Field;
import com.vaadin.ui.Select;

/**
 * BPMN_ERP added
 * 
 * A value change listener that updates the value of a property if the value of an input field changes.
 * The new value is computed through evaluating an SQL query.
 * 
 * @author dfahland
 *
 */
public class SQLListPropertyChangeListener extends SQLPropertyChangeListener<AbstractSelect, List<String>> {

	private static final long serialVersionUID = 1L;
	
	private final Map<String, String> formValues;
	public SQLListPropertyChangeListener(AbstractSelect property, String query, Map<String, Field> fields, Map<String, String> formValues) {
		super(property, query, fields);
		this.formValues = formValues;
	}
	
	@Override
	protected List<String> transformQueriedValuesToCurrentValue(List<String> queriedValues) {
		if (queriedValues == null) return new LinkedList<String>();
		else return queriedValues;
	}
	
	@Override
	public void updateTargetField() {
//		if (formValues.containsKey("sql_ui")) {
//			String query = formValues.get("sql_ui");

			List<String> query_values = getCurrentValue();
			Map<String, String> updatedValues = new HashMap<String, String>();
			for (String value : query_values) {
				updatedValues.put(value, value);
			}
//			updatedValues.put("sql_ui", query);

			formValues.clear();
			formValues.putAll(updatedValues);
//		}

		AbstractSelect s = getTargetField();
		s.removeAllItems();

		for (Entry<String, String> enumEntry : formValues.entrySet()) {
			// Add value and label (if any)
			if (enumEntry.getKey().equals("sql_ui")) continue;

			s.addItem(enumEntry.getKey());
			if (enumEntry.getValue() != null) {
				s.setItemCaption(enumEntry.getKey(), enumEntry.getValue());
			}
		}
	}

}