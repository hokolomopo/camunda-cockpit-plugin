package org.camunda.bpm.cockpit.plugin.sample.state;

import be.yelido.camunda.module.data.ids.AuditableT0Id;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class IntegrationAuditableT0State extends IntegrationState implements ProcessState {
    private AuditableT0Id auditableT0Id = null;
    private Date processingDate = null;


    private boolean auditableMerged = false;
    private boolean daoMerged = false;
    private boolean postitUpdated = false;

    @Override
    public void updateMergeState() {
        fullyMerged = auditableMerged && daoMerged && postitUpdated;
    }

    @Override
    public List<String> getActivities() {
        List<String> list = new ArrayList<>();
        for(IntegrationAuditableT0Enum i : IntegrationAuditableT0Enum.values())
            list.add(i.activityId);
        return list;
    }

    @Override
    public String getFieldName(String activityId) {
        for(IntegrationAuditableT0Enum i : IntegrationAuditableT0Enum.values())
            if(i.activityId.equals(activityId))
                return i.fieldName;
        return null;
    }

    public enum IntegrationAuditableT0Enum {
        MERGE_AUDITABLE("AuditableT0_Merge", "auditableMerged"),
        MERGE_DAO("AuditableT0_MergeDAO", "daoMerged"),
        UPDATE_POSTITS("AuditableT0_UpdatePostits", "postitUpdated");

        public String activityId;
        public String fieldName;

        IntegrationAuditableT0Enum(String activityId, String fieldName) {
            this.activityId = activityId;
            this.fieldName = fieldName;
        }
    }

}
