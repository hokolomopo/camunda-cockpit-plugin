package org.camunda.bpm.cockpit.plugin.sample.state;

import be.yelido.camunda.module.data.ids.LabRequestDTOId;
import lombok.Data;

import java.util.Date;

@Data
public class IntegrationLabRequestState extends IntegrationState{
    private LabRequestDTOId labRequestDTOId = null;
    private Date processingDate = null;

    private String state = "WAITING";

    @Override
    public void updateMergeState() {
        fullyMerged = state.equals("SENT");
    }
}
