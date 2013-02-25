/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import java.util.Map.Entry;
import java.util.logging.Level;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.form.EnumFormType;
import org.activiti.engine.impl.sql.SQLUtil;
import org.activiti.explorer.Messages;

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Select;

import de.hpi.uni.potsdam.bpmnToSql.DataObject;

/**
 * @author Frederik Heremans
 */
public class EnumFormPropertyRenderer extends AbstractFormPropertyRenderer {

  public EnumFormPropertyRenderer() {
    super(EnumFormType.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Field getPropertyField(FormProperty formProperty) {
	  AbstractSelect comboBox;
	  if (formProperty.isWritable()) {
		  comboBox = new ComboBox(getPropertyLabel(formProperty));
	  } else {
		  comboBox = new ListSelect(getPropertyLabel(formProperty));
		  comboBox.setReadOnly(true);
		  comboBox.setNullSelectionAllowed(false);
	  }
	  
	  //ComboBox comboBox = new ComboBox(getPropertyLabel(formProperty)); // BPMN_EPR removed
	  comboBox.setRequired(formProperty.isRequired());
	  comboBox.setRequiredError(getMessage(Messages.FORM_FIELD_REQUIRED, getPropertyLabel(formProperty)));
	  //comboBox.setEnabled(formProperty.isWritable()); //BPMN_ERP removed

	  comboBox.setImmediate(true);
	  
	  Map<String, String> values = (Map<String, String>) formProperty.getType().getInformation("values");
	  
	  System.out.println(values+" "+formProperty.getValueUiSqlQuery());
			  
	  if (values != null) {
		  // TODO BPMN_ERP
		  if (formProperty.getValueUiSqlQuery() != null) {
			  String query = formProperty.getValueUiSqlQuery();
			  
			  Map<String, Field> fields = SQLPropertyChangeListener.getFieldMapping(query, this);
			  SQLListPropertyChangeListener listener = new SQLListPropertyChangeListener(comboBox, query, fields, values);
			  for (Field f : fields.values()) {
				  f.addListener(listener);
			  }
			  listener.updateTargetField();
		  } else {
		  // BPMN_ERP end
			  for (Entry<String, String> enumEntry : values.entrySet()) {
				  // Add value and label (if any)
				  comboBox.addItem(enumEntry.getKey());
				  if (enumEntry.getValue() != null) {
					  comboBox.setItemCaption(enumEntry.getKey(), enumEntry.getValue());
				  }
			  }
		  }
	  }
	  return comboBox;
  }
  

}
