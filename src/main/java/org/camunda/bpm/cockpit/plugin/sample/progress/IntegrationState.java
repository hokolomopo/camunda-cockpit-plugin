package org.camunda.bpm.cockpit.plugin.sample.progress;

import lombok.Data;

import java.util.List;

@Data
public abstract class IntegrationState {
    protected boolean fullyMerged = false;

    protected String processId;

    public abstract void updateMergeState();
}
