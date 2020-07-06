package org.camunda.bpm.cockpit.plugin.sample.util;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableInstance;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TestRequestUtils {
    public static List<String> getAllVarValue(String varName, ProcessEngine processEngine){
        List<HistoricVariableInstance> variableInstances = processEngine.getHistoryService().createHistoricVariableInstanceQuery().variableName(varName).list();

        Set<String> variableValues = new LinkedHashSet<>();
        for(HistoricVariableInstance v : variableInstances)
            variableValues.add((String)v.getValue());

        return new ArrayList<>(variableValues);
    }

}
