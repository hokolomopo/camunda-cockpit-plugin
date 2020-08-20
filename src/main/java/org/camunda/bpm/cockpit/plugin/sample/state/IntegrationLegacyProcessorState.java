package org.camunda.bpm.cockpit.plugin.sample.state;

import be.yelido.camunda.module.data.ids.ToProcessLegacyDTOId;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class IntegrationLegacyProcessorState extends IntegrationState implements ProcessState {
    private ToProcessLegacyDTOId toProcessLegacyDTOId = null;
    private Date processingDate = null;


    private boolean administrativeProcessor = false;
    private boolean hazardProcessor = false;
    private boolean hepatitisProcessor = false;
    private boolean inabilityProcessor = false;
    private boolean inactivityProcessor = false;
    private boolean prestationProcessor = false;
    private boolean serviceProcessor = false;
    private boolean reintegrationProcessor = false;

    @Override
    public void updateMergeState() {
        fullyMerged = administrativeProcessor && hazardProcessor && hepatitisProcessor
                && inabilityProcessor&& inactivityProcessor&& prestationProcessor&& serviceProcessor && reintegrationProcessor;
    }

    @Override
    public List<String> getActivities() {
        List<String> list = new ArrayList<>();
        for(IntegrationLegacyProcessorState.IntegrationLegacyProcessorEnum i : IntegrationLegacyProcessorState.IntegrationLegacyProcessorEnum.values())
            list.add(i.activityId);
        return list;
    }

    @Override
    public String getFieldName(String activityId) {
        for(IntegrationLegacyProcessorState.IntegrationLegacyProcessorEnum i : IntegrationLegacyProcessorState.IntegrationLegacyProcessorEnum.values())
            if(i.activityId.equals(activityId))
                return i.fieldName;
        return null;
    }

    public enum IntegrationLegacyProcessorEnum {
        ADMINISTRATIVE_PROCESSOR("Administrative_Processor", "administrativeProcessor"),
        HAZARD_PROCESSOR("Hazard_Processor", "hazardProcessor"),
        HEPATITIS_PROCESSOR("Hepatitis_Processor", "hepatitisProcessor"),
        INABILITY_PROCESSOR("Inability_Processor", "inabilityProcessor"),
        INACTIVITY_PROCESSOR("Inactivity_Processor", "inactivityProcessor"),
        PRESTATION_PROCESSOR("Prestation_Processor", "prestationProcessor"),
        SERVICE_PROCESSOR("Service_Processor", "serviceProcessor"),
        REINTEGRATION_PROCESSOR("Reintegration_Processor", "reintegrationProcessor");

        public String activityId;
        public String fieldName;

        IntegrationLegacyProcessorEnum(String activityId, String fieldName) {
            this.activityId = activityId;
            this.fieldName = fieldName;
        }
    }

}
