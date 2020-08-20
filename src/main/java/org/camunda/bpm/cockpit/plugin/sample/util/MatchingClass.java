package org.camunda.bpm.cockpit.plugin.sample.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.cockpit.plugin.sample.state.ProcessState;
import org.camunda.bpm.engine.history.HistoricActivityInstance;

import java.util.*;

public class MatchingClass {
    private static final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * Match a process state with a list of Activities from Camunda.
     *
     * @param activities the activities
     * @param c          the type of state class extending ProcessState
     * @return the matched ProcessState
     */
    public static <T extends ProcessState> T matchSimpleProcess(List<HistoricActivityInstance> activities, Class<T> c){
        T sample;
        try {
            sample = c.newInstance();
        } catch (Exception e) {
            return null;
        }

        List<String> activitiesNames = sample.getActivities();
        Set<String> activitiesNamesSet = new HashSet<>(activitiesNames);
        Map<String, Boolean> map = new HashMap<>();

        for(HistoricActivityInstance act : activities){
            if(activitiesNamesSet.contains(act.getActivityId()))
                map.put(sample.getFieldName(act.getActivityId()), act.getEndTime() != null);
        }

        return objectMapper.convertValue(map, c);
    }

}
