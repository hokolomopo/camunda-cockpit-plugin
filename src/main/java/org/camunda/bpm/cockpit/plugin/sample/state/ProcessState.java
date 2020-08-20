package org.camunda.bpm.cockpit.plugin.sample.state;

import java.util.List;

public interface ProcessState {
    List<String> getActivities();
    String getFieldName(String activityId);
}
