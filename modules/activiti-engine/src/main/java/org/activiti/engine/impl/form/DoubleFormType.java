package org.activiti.engine.impl.form;

/**
 * TODO BPMN_ERP
 * 
 * Double form type
 * 
 * @author dfahland
 *
 */
public class DoubleFormType extends AbstractFormType {

	public String getName() {
		return "double";
	}

	public String getMimeType() {
		return "plain/text";
	}

	public Object convertFormValueToModelValue(String propertyValue) {
		if (propertyValue == null || "".equals(propertyValue)) {
			return null;
		}
		return new Double(propertyValue);
	}

	public String convertModelValueToFormValue(Object modelValue) {
		if (modelValue == null) {
			return null;
		}
		return modelValue.toString();
	}
}
