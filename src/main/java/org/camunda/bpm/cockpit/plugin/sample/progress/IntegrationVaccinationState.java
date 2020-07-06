package org.camunda.bpm.cockpit.plugin.sample.progress;

import be.yelido.camunda.module.data.ids.VaccinationDTOId;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class IntegrationVaccinationState extends IntegrationState implements SimpleMathProcess{
    private VaccinationDTOId vaccinationDTOId = null;
    private Date processingDate = null;

    private boolean done = false;

    @Override
    public void updateMergeState() {
        fullyMerged = done;
    }

    @Override
    public List<String> getActivities() {
        List<String> list = new ArrayList<>();
        for(IntegrationVaccinationStateEnum i : IntegrationVaccinationStateEnum.values())
            list.add(i.activityId);
        return list;
    }

    @Override
    public String getFieldName(String activityId) {
        for(IntegrationVaccinationStateEnum i : IntegrationVaccinationStateEnum.values())
            if(i.activityId.equals(activityId))
                return i.fieldName;
        return null;
    }

    public enum IntegrationVaccinationStateEnum {
        DONE("Vaccinnet_Controller", "done");

        public String activityId;
        public String fieldName;

        IntegrationVaccinationStateEnum(String activityId, String fieldName) {
            this.activityId = activityId;
            this.fieldName = fieldName;
        }
    }

}
