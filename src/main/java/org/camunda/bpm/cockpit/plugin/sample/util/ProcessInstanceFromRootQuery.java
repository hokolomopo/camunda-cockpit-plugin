package org.camunda.bpm.cockpit.plugin.sample.util;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderMethodName  = "createQuery")
public class ProcessInstanceFromRootQuery {
    private String rootProcessId;
    private String variableName;
    private String variableValue;
    private String processDefinitionKey;

}
