package org.camunda.bpm.cockpit.plugin.sample.state;

import lombok.Data;

@Data
public abstract class IntegrationState {
    protected boolean fullyMerged = false;

    protected String processId;

    public abstract void updateMergeState();
}
