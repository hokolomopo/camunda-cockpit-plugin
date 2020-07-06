package org.camunda.bpm.cockpit.plugin.sample.db;

import lombok.Data;

@Data
public class ProcessInstanceFromRootDto {
    private String id;
    private String processDefinitionKey;

    @Override
    public String toString() {
        return "ProcessInstanceFromRootDto{" +
                "id='" + id + '\'' +
                ", processDefinitionKey='" + processDefinitionKey + '\'' +
                '}';
    }
}
