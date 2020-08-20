package org.camunda.bpm.cockpit.plugin.sample.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.camunda.bpm.engine.history.HistoricProcessInstance;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessInstanceFromRootDto {
    private String id;
    private String processDefinitionKey;


    public ProcessInstanceFromRootDto(HistoricProcessInstance processInstance) {
        this.id = processInstance.getId();
        this.processDefinitionKey = processInstance.getProcessDefinitionKey();
    }


    @Override
    public String toString() {
        return "ProcessInstanceFromRootDto{" +
                "id='" + id + '\'' +
                ", processDefinitionKey='" + processDefinitionKey + '\'' +
                '}';
    }
}
