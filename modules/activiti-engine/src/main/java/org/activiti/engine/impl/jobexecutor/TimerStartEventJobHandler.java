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
package org.activiti.engine.impl.jobexecutor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.cmd.StartProcessInstanceCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.form.DefaultFormHandler;
import org.activiti.engine.impl.form.FormPropertyHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.sql.SQLExpression;
import org.activiti.engine.repository.ProcessDefinition;


public class TimerStartEventJobHandler implements JobHandler {

  private static Logger log = Logger.getLogger(TimerStartEventJobHandler.class.getName());

  public static final String TYPE = "timer-start-event";

  public String getType() {
    return TYPE;
  }
  
  public void execute(JobEntity job, String configuration, ExecutionEntity execution, CommandContext commandContext) {
    DeploymentCache deploymentCache = Context
            .getProcessEngineConfiguration()
            .getDeploymentCache();
    
    ProcessDefinition processDefinition = deploymentCache.findDeployedLatestProcessDefinitionByKey(configuration);
    try {
      if(!processDefinition.isSuspended()) {
    	  
    	// TODO: BPMN_ERP added

   	  System.out.println(">>>>>>>>>>>>>> TIMER checking for new process instance");
    	  
    	StartFormData data = ProcessEngines.getDefaultProcessEngine().getFormService().getStartFormData(processDefinition.getId());
    	for (FormProperty prop : data.getFormProperties()) {
    		if (prop.getId().equals("sql_trigger")) {
				String result = prop.getValue();
				if (result == null) {
					log.fine("no result on trigger");
					return; // do not start anything
				} else {
					if (result.equals("0")) {
						log.fine("trigger condition not satisfied");
						return; // query returned 0 or false
					} else {
						log.fine("trigger condition satisfied");						
					}
				}
    		}
    	}
    	System.out.println(">>>>>>>>>>>>>> TIMER starting new process instance");
    	
    	// BPMN_ERP end
    	  
        new StartProcessInstanceCmd(configuration, null, null, null).execute(commandContext);
      } else {
        log.log(Level.FINE, "ignoring timer of suspended process definition " + processDefinition.getName());
      }
    } catch (RuntimeException e) {
      log.log(Level.SEVERE, "exception during timer execution", e);
      throw e;
    } catch (Exception e) {
      log.log(Level.SEVERE, "exception during timer execution", e);
      throw new ActivitiException("exception during timer execution: " + e.getMessage(), e);
    }
  }
}
