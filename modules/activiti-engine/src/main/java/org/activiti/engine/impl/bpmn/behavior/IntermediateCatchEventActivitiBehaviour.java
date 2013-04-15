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
package org.activiti.engine.impl.bpmn.behavior;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.sql.SQLExpression;
import org.activiti.engine.impl.sql.SQLUtil;


public class IntermediateCatchEventActivitiBehaviour extends AbstractBpmnActivityBehavior {

	// TODO BPMN_ERP added
	private static Logger log = Logger.getLogger(AbstractBpmnActivityBehavior.class.getName());
    private Thread sqlWaitThread = null;

	public void execute(ActivityExecution execution) throws Exception {
		// Do nothing: waitstate behavior

		// TODO BPMN_ERP start
		ActivityImpl activity = (ActivityImpl) execution.getActivity();
		List<EventSubscriptionDeclaration> signals = (List<EventSubscriptionDeclaration>) activity
				.getProperty(BpmnParse.PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION);

		final Map<String, String> expressions = new HashMap<String, String>();

		for (EventSubscriptionDeclaration d : signals) {
			if ("signal".equals(d.getEventType()) && d.getExpression() != null
					&& d.getExpression() instanceof SQLExpression) {
				SQLExpression sql = (SQLExpression) d.getExpression();
				String sql_query = sql.instantiateSQLQuery(execution);
				expressions.put(d.getEventName(), sql_query);
			}
		}

		if (expressions.size() > 0) {

			final String activityId = activity.getId();
			final RuntimeService myRuntime = execution.getEngineServices()
					.getRuntimeService();

			System.out.println("INTERMEDIATE EVENT >>>> waiting to execute queries for "+ activityId);

			if (sqlWaitThread == null) {
				System.out.println("starting new thread for " + activityId);
				sqlWaitThread = new Thread("SQL wait thread") {
					@Override
					public void run() {

						int satisfied;
						do {
							satisfied = 0;
							for (String eventName : expressions.keySet()) {
								String query = expressions.get(eventName);
								if (SQLUtil.executeSelect(query, null, null, null, null).size() > 0) {
									satisfied++;
								}
							}
							
							System.out.println("satisfied: "+satisfied);
							
							if (satisfied < expressions.size()) {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									log.log(Level.SEVERE, e.getMessage());
									e.printStackTrace();
								}
							}
						} while (satisfied < expressions.size());

						for (String eventName : expressions.keySet()) {
							myRuntime.signalEventReceived(eventName);
						}
						sqlWaitThread = null;
					}

					@Override
					public void interrupt() {
						System.out.println("interrupting for " + activityId);
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
		}
		// BPMN_ERP end
	}
  

	@Override
	public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
		leave(execution);
	}
}

