package org.camunda.bpm.cockpit.plugin.sample.progress;

import java.util.List;

public interface SimpleMathProcess {
    public abstract List<String> getActivities();
    public abstract String getFieldName(String activityId);
}
