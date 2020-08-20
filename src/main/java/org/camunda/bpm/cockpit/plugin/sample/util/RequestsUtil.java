package org.camunda.bpm.cockpit.plugin.sample.util;

import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.db.QueryService;
import org.camunda.bpm.cockpit.plugin.sample.db.ProcessInstanceFromRootDto;
import org.camunda.bpm.cockpit.plugin.sample.db.ProcessInstanceFromRootQuery;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableInstance;

import java.util.*;

public class RequestsUtil {

    /**
     * Get process instances that contains the given variable.
     *
     * @param varName       the variable name
     * @param varValue      the variable value
     * @param before        the date before which the process must have be executed
     * @param after         the date after which the process must have be executed
     * @param processEngine the process engine
     * @return the list of processes containing the variable
     */
    public static List<HistoricProcessInstance> getProcessFromVariable(String varName, String varValue, Date before, Date after, ProcessEngine processEngine){
        HistoricProcessInstanceQuery query = processEngine.getHistoryService().createHistoricProcessInstanceQuery();
        query.variableValueEquals(varName, varValue);
        if(before != null)
            query.executedActivityBefore(before);
        if(after != null)
            query.executedActivityAfter(after);

        return query.list();
    }

    /**
     * Get process instances that contains the given variable.
     *
     * @param varName       the variable name
     * @param varValue      the variable value
     * @param processEngine the process engine
     * @return the list of processes containing the variable
     */
    public static List<HistoricProcessInstance> getProcessFromVariable(String varName, String varValue, ProcessEngine processEngine){
        return getProcessFromVariable(varName, varValue, null, null, processEngine);
    }

    /**
     * Get the set of all the root process of the given processes instances.
     *
     * @param list the list of process instances
     * @return the list of root processes of these process instances
     */
    public static List<String> getAllRootProcesses(List<HistoricProcessInstance> list){
        Set<String> set = new LinkedHashSet<>();
        for(HistoricProcessInstance p : list)
            set.add(p.getRootProcessInstanceId());
        return new ArrayList<>(set);
    }

    /**
     * Get process instances that contains the given variables and that are children of the given root process.
     *
     * @param query        the query parameters
     * @param queryService the query service
     * @return the list of processes instances
     */
    public static List<ProcessInstanceFromRootDto> getProcessInstanceFromRootAndVar(ProcessInstanceFromRootQuery query, QueryService queryService){
        HashMap<String, String> p = new HashMap<>();
        if(query.getVariableName() != null)
            p.put("varName", query.getVariableName());
        if(query.getVariableValue() != null)
            p.put("varValue", query.getVariableValue());
        if(query.getRootProcessId() != null)
            p.put("rootId", query.getRootProcessId());
        if(query.getProcessDefinitionKey() != null)
            p.put("processKey", query.getProcessDefinitionKey());

        QueryParameters<ProcessInstanceFromRootDto> parameters = new QueryParameters<>();
        parameters.setParameter(p);
        List<ProcessInstanceFromRootDto> instanceCounts =
                queryService
                        .executeQuery(
                                "cockpit.sample.selectProcessInstanceByRootAndVariableHistory",
                                parameters);

        return instanceCounts;
    }


    /**
     * Get all the variables of a process instance sorted by creation time.
     *
     * @param processId     the process instance id
     * @param processEngine the process engine
     * @return the sorted list of variables of the process instance
     */
    public static List<HistoricVariableInstance> getAllVariablesSorted(String processId, ProcessEngine processEngine){
        List<HistoricVariableInstance> vars = processEngine.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(processId).list();
        Collections.sort(vars, Comparator.comparing(HistoricVariableInstance::getCreateTime));

        return vars;
    }

    /**
     * Get all the activities of a process instance sorted by execution time.
     *
     * @param processId     the process instance id
     * @param processEngine the process engine
     * @return the sorted list of activities of the process instance
     */
    public static List<HistoricActivityInstance> getAllActivitiesSorted(String processId, ProcessEngine processEngine){
        List<HistoricActivityInstance> activities = processEngine.getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(processId).list();
        Collections.sort(activities, Comparator.comparing(HistoricActivityInstance::getStartTime));

        return activities;
    }

}
