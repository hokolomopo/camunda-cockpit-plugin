package org.camunda.bpm.cockpit.plugin.sample.state;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class JioIntegrationState extends IntegrationState {
    private IntegrationAuditableT0State auditableT0State;
    private List<IntegrationAuditableT1State> auditableT1States = new ArrayList<>();
    private List<IntegrationLabRequestState> labRequestStates = new ArrayList<>();
    private List<IntegrationVaccinationState> vaccinationStates = new ArrayList<>();
    private List<IntegrationLegacyProcessorState> legacyProcessorStates = new ArrayList<>();

    @Override
    public void updateMergeState() {
        fullyMerged = auditableT0State.isFullyMerged();
        for(IntegrationAuditableT1State s : auditableT1States)
            fullyMerged &= s.isFullyMerged();
        for(IntegrationLabRequestState s : labRequestStates)
            fullyMerged &= s.isFullyMerged();
        for(IntegrationVaccinationState s : vaccinationStates)
            fullyMerged &= s.isFullyMerged();
        for(IntegrationLegacyProcessorState s : legacyProcessorStates)
            fullyMerged &= s.isFullyMerged();
    }
}
