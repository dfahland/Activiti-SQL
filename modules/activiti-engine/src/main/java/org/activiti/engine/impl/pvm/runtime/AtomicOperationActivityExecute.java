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
package org.activiti.engine.impl.pvm.runtime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.bpmn.behavior.ManualTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.PvmException;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

import de.hpi.uni.potsdam.bpmnToSql.DataObject;


/**
 * @author Tom Baeyens
 */
public class AtomicOperationActivityExecute implements AtomicOperation {
  
  private static Logger log = Logger.getLogger(AtomicOperationActivityExecute.class.getName());

  private Thread sqlWaitThread = null;

  public boolean isAsync(InterpretableExecution execution) {
    return false;
  }

  public void execute(InterpretableExecution execution) {
    ActivityImpl activity = (ActivityImpl) execution.getActivity();
    
    ActivityBehavior activityBehavior = activity.getActivityBehavior();
    if (activityBehavior==null) {
      throw new PvmException("no behavior specified in "+activity);
    }
    
	if (enterWaitStateForData(execution)) {
		log.fine(execution+" enters waitstate for "+activity+": "+activityBehavior.getClass().getName());
		return;
	}
    
//    // TODO BPMN_SQL start
//    if(BpmnParse.getInputData().containsKey(activity.getId())) { //true if activity reads a data object
//    	for (DataObject item : BpmnParse.getInputData().get(activity.getId())) {
//    		while(!dbConnection(item, execution.getProcessInstanceId()).equalsIgnoreCase(item.getState())) { //wait for correct data state
//            	Thread waiter = new Thread();
//            	waiter.start();
//            	try {
//					waiter.sleep(10000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//    			System.out.println("next while");
//            	System.out.println("object: " + item.getName());
//            	System.out.println("state: " + item.getState());
//            	System.out.println("PID: " + execution.getProcessInstanceId());
//            }
//		}
////    	while(!dbConnection(readHashMap.get(activity.getId())).equalsIgnoreCase(readHashMap.get(activity.getId()).get(1))) { //wait for correct datat state
////        	System.out.println("next while");
////        	System.out.println("state: " + readHashMap.get(activity.getId()).get(1));
////        }
//    }
//    // BPMN_SQL end

    log.fine(execution+" executes "+activity+": "+activityBehavior.getClass().getName());
    
    try {
      activityBehavior.execute(execution);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new PvmException("couldn't execute activity <"+activity.getProperty("type")+" id=\""+activity.getId()+"\" ...>: "+e.getMessage(), e);
    }
  }
  
  // TODO BPMN_SQL start	
	public boolean enterWaitStateForData(ActivityExecution execution) {
	    if(BpmnParse.getInputData().containsKey(execution.getActivity().getId())) { //true if activity reads a data object
	    	
	    	final String activityId = execution.getActivity().getId();
	    	final String instanceId = execution.getProcessInstanceId();
	    	final AtomicOperationActivityExecute taskBehavior = this;
	    	final ActivityExecution pendingExecution = execution;
	    	final RuntimeService myRuntime = execution.getEngineServices().getRuntimeService();
	    	
	    	if (isInputDataAvailable(activityId, instanceId)) {
	    		return false;
	    	} else {
		    	if (sqlWaitThread == null) {
		    		System.out.println("starting new thread for "+activityId);
			    	sqlWaitThread = new Thread("SQL wait thread") {
			    		@Override
			    		public void run() {
			    	    	while (!isInputDataAvailable(activityId, instanceId)) {
			    	    		System.out.println("waiting for data for "+activityId);
		    	            	try {
		    	            		Thread.sleep(1000);
		    					} catch (InterruptedException e) {
		    						e.printStackTrace();
		    					}
			    			}
			    	    	// input data is available: execute task
			    	    	try {
			    	    		taskBehavior.sqlWaitThread = null;
			    	    		System.out.println("continuing execution");
								//taskBehavior.execute(pendingExecution);

			    	    		myRuntime.signalResumeForData(pendingExecution.getId());
								//System.out.println("finished thread");
							} catch (Exception e) {
								e.printStackTrace();
							}
			    		}
			    		
			    		@Override
			    		public void interrupt() {
			    			System.out.println("interrupting for "+activityId);
			    			super.interrupt();
			    		}
			    		
			    		@Override
			    		public synchronized void start() {
			    			System.out.println("starting");
			    			super.start();
			    		}
			    	};
			    	sqlWaitThread.start();
		    	}
		    	return true;
	    	}
	    } else {
	    	return false;
	    }
	}
	
	public boolean isInputDataAvailable(String activityId, String instanceId) {
		boolean missingInputData = false;
		for (DataObject item : BpmnParse.getInputData().get(activityId)) {
			String currentState = dbConnection(item, instanceId);
			String expectedState = item.getState();
			if (!currentState.equalsIgnoreCase(expectedState))
			{
				missingInputData = true;
          	System.out.println("not found: object: " + item.getName() + " for PID: "+instanceId+" in state " + item.getState()+", found "+currentState);
			} else {
				System.out.println("found object: " + item.getName() + " for PID: "+instanceId+" in expected state " + item.getState()+"="+currentState);
			}
		}
		return missingInputData == false;
	}
	
	
	  // TODO BPMN_SQL
	  public String dbConnection(DataObject dataObj, String instanceId) {
		  Connection con = null;
	      Statement st = null;
	      ResultSet rs = null;
	      String state = new String();
	      

	      String url = "jdbc:mysql://localhost:3306/testdb";
	      String user = "testuser";
	      String password = "test623";

	      try {
	          con = DriverManager.getConnection(url, user, password);
	          st = con.createStatement();
	          String selectQuery = "SELECT `state` FROM `"+dataObj.getName()+"` WHERE `"+dataObj.getPkey()+"` =" + instanceId;
	          System.out.println(selectQuery);
	          rs = st.executeQuery(selectQuery);
	          if (rs.next()) {
	              state = rs.getString(1);
	          }

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
	      System.out.println(state);
	      return state;
	  }

}
