package org.camunda.bpm.cockpit.plugin.sample.progress;

import be.yelido.camunda.module.data.ids.AuditableT1Id;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class IntegrationAuditableT1State extends IntegrationState implements SimpleMathProcess{

    private AuditableT1Id auditableT1Id = null;
    private Date processingDate = null;


    private boolean auditableMerged = false;
    private boolean daoMerged = false;
    private boolean postitUpdated = false;
    private boolean reportsUpdated = false;
    private boolean reintegrationController = false;
    private boolean synchroController = false;

    @Override
    public void updateMergeState() {
        fullyMerged = auditableMerged && daoMerged && postitUpdated && reportsUpdated && reintegrationController && synchroController;
    }

    @Override
    public List<String> getActivities() {
        List<String> list = new ArrayList<>();
        for(IntegrationAuditableT1Enum i : IntegrationAuditableT1Enum.values())
            list.add(i.activityId);
        return list;
    }

    @Override
    public String getFieldName(String activityId) {
        for(IntegrationAuditableT1Enum i : IntegrationAuditableT1Enum.values())
            if(i.activityId.equals(activityId))
                return i.fieldName;
        return null;
    }

    public enum IntegrationAuditableT1Enum {
        MERGE_AUDITABLE("AuditableT1_Merge", "auditableMerged"),
        MERGE_DAO("AuditableT1_MergeDAO", "daoMerged"),
        UPDATE_POSTITS("AuditableT1_UpdatePostits", "postitUpdated"),
        UPDATE_REPORTS("AuditableT1_UpdateReports", "reportsUpdated"),
        REINTEGRATION_CONTROLLER("AuditableT1_ReintegrationController", "reintegrationController"),
        SYNCHRO_ASSIGNMENT("AuditableT1_CheckSynchroAssignement", "synchroController");

        public String activityId;
        public String fieldName;

        IntegrationAuditableT1Enum(String activityId, String fieldName) {
            this.activityId = activityId;
            this.fieldName = fieldName;
        }
    }

}
