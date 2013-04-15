package org.activiti.engine.impl.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.task.IdentityLink;

public class SQLExpression implements Expression {
	

	private String query;
	private Map<String, Expression> subExpressions;
	private Map<String, FormProperty> formProperty;
	
	public SQLExpression(String sqlQuery, ExpressionManager em) {
		query = sqlQuery;
		subExpressions = new HashMap<String, Expression>();
		formProperty = new HashMap<String, FormProperty>();
		findSubExpressions(em);
		findFormProperties();
	}
	
	private void findSubExpressions(ExpressionManager em) {
		int index = 0;
		while ((index = query.indexOf("${", index)) != -1) {
			int endIndex = query.indexOf("}", index);
			if (endIndex != -1) {
				String subExpression = query.substring(index,endIndex+1);
				
				Expression e = em.createExpression(subExpression);
				
				subExpressions.put(subExpression, e);
			}
			index++; // move one on
		}
	}
	
	private void findFormProperties() {
		int index = 0;
		while ((index = query.indexOf("&{", index)) != -1) {
			int endIndex = query.indexOf("}", index);
			if (endIndex != -1) {
				String fieldName = query.substring(index+2,endIndex);
				
				formProperty.put(fieldName, null);
			}
			index++; // move one on
		}
	}

	
	public String instantiateSQLQuery(VariableScope variableScope) {
		String iQuery = query;
		for (String var : subExpressions.keySet()) {
			iQuery = iQuery.replace(var, subExpressions.get(var).getValue(variableScope).toString());
		}
		
		for (String form : formProperty.keySet()) {
			String formKey = "&{"+form+"}";
			if (formProperty.get(formKey) != null) {
				FormProperty p = formProperty.get(formKey);
				iQuery.replace(formKey, p.getValue());
			} else {
				return null;
			}
		}
		return iQuery;
	}

	@Override
	public Object getValue(VariableScope variableScope) {
//		for (String var : expressions.keySet()) {
//			Object value = variableScope.getVariable(var);
//			expressions.put(var, value.toString());
//		}
		
		// extract information for logging
		String processID = null;
		String caseID = null;
		String resource = null;
		String event = null;
		
		if (variableScope instanceof DelegateExecution) {
			DelegateExecution e = (DelegateExecution)variableScope;
			event = e.getCurrentActivityName();
			processID = e.getProcessDefinitionId();
			caseID = e.getProcessInstanceId();
		} else if (variableScope instanceof DelegateTask) {
			DelegateTask t = (DelegateTask)variableScope;
			event = t.getName()+"_"+t.getEventName();
			processID = t.getProcessDefinitionId();
			caseID = t.getProcessInstanceId();
			resource = (t.getAssignee() != null) ? t.getAssignee() : t.getOwner();
			if (resource == null) {
				Set<String> resources = new HashSet<String>();
				for (IdentityLink ident : t.getCandidates()) {
					if (ident.getUserId() != null) resources.add(ident.getUserId());
					if (ident.getGroupId() != null) resources.add(ident.getGroupId());
				}
				resource = resources.toString();
			}
		}
		
		String iQuery = instantiateSQLQuery(variableScope);
		if (iQuery == null) return null;
		
		if (iQuery.indexOf("SELECT") >= 0) {
			System.out.println("select");
			List<String> result = SQLUtil.executeSelect(iQuery, processID, caseID, resource, event);
			if (result.size() == 0) return null;
			if (result.size() == 1) return result.get(0);
			else return result;
		} else {
			System.out.println("update");
			return SQLUtil.executeUpdate(iQuery, processID, caseID, resource, event);
		}
	}

	@Override
	public void setValue(Object value, VariableScope variableScope) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getExpressionText() {
		return "sql:"+query;
	}
	
	@Override
	public String toString() {
		return "SQLExpression: "+getExpressionText();
	}

}
