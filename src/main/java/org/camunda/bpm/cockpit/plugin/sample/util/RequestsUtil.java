package org.camunda.bpm.cockpit.plugin.sample.util;

import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.db.QueryService;
import org.camunda.bpm.cockpit.plugin.sample.db.ProcessInstanceFromRootDto;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableInstance;

import java.util.*;

public class RequestsUtil {

    public static List<HistoricProcessInstance> getProcessFromVariable(String varName, String varValue, Date before, Date after, ProcessEngine processEngine){
        HistoricProcessInstanceQuery query = processEngine.getHistoryService().createHistoricProcessInstanceQuery();
        query.variableValueEquals(varName, varValue);
        if(before != null)
            query.executedActivityBefore(before);
        if(after != null)
            query.executedActivityAfter(after);

        return query.list();
    }

    public static List<HistoricProcessInstance> getProcessFromVariable(String varName, String varValue, ProcessEngine processEngine){
        return getProcessFromVariable(varName, varValue, null, null, processEngine);
    }

    public static List<String> getAllRootProcesses(List<HistoricProcessInstance> list){
        Set<String> set = new LinkedHashSet<>();
        for(HistoricProcessInstance p : list)
            set.add(p.getRootProcessInstanceId());
        return new ArrayList<>(set);
    }

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


    public static List<HistoricVariableInstance> getAllVariablesSorted(String processId, ProcessEngine processEngine){
        List<HistoricVariableInstance> vars = processEngine.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(processId).list();
        Collections.sort(vars, Comparator.comparing(HistoricVariableInstance::getCreateTime));

        return vars;
    }

    public static List<HistoricActivityInstance> getAllActivitiesSorted(String processId, ProcessEngine processEngine){
        List<HistoricActivityInstance> activities = processEngine.getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(processId).list();
        Collections.sort(activities, Comparator.comparing(HistoricActivityInstance::getStartTime));

        return activities;
    }

    public static List<HistoricVariableInstance> filterVariables(List<HistoricVariableInstance> variables, List<String> filter){
        List<HistoricVariableInstance> result = new ArrayList<>(variables);
        result.removeIf(historicVariableInstance -> !filter.contains(historicVariableInstance.getName()));
        return result;
    }

}
