package org.camunda.bpm.cockpit.plugin.sample.util;

import be.yelido.camunda.module.data.ids.AuditableT1Id;
import be.yelido.camunda.module.data.ids.CamundaObjectIdentifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.cockpit.plugin.sample.progress.IntegrationAuditableT0State;
import org.camunda.bpm.cockpit.plugin.sample.progress.IntegrationAuditableT1State;
import org.camunda.bpm.cockpit.plugin.sample.progress.IntegrationVaccinationState;
import org.camunda.bpm.cockpit.plugin.sample.progress.SimpleMathProcess;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;

import java.util.*;

public class MatchingUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T extends SimpleMathProcess> T matchSimpleProcess(List<HistoricActivityInstance> activities, Class<T> c){
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


//    public static IntegrationAuditableT1State matchAuditableT1IntegrationState(List<HistoricActivityInstance> activities){
//        List<String> activitiesNames = IntegrationAuditableT1State.IntegrationAuditableT1Enum.getActivities();
//        Set<String> activitiesNamesSet = new HashSet<>(activitiesNames);
//        Map<String, Boolean> map = new HashMap<>();
//
//        for(HistoricActivityInstance act : activities){
//            if(activitiesNamesSet.contains(act.getActivityId()))
//                map.put(IntegrationAuditableT1State.IntegrationAuditableT1Enum.getFieldName(act.getActivityId()), act.getEndTime() != null);
//        }
//
//        return objectMapper.convertValue(map, IntegrationAuditableT1State.class);
//    }
//
//    public static IntegrationAuditableT0State matchAuditableT0IntegrationState(List<HistoricActivityInstance> activities){
//        List<String> activitiesNames = IntegrationAuditableT0State.IntegrationAuditableT0Enum.getActivities();
//        Set<String> activitiesNamesSet = new HashSet<>(activitiesNames);
//        Map<String, Boolean> map = new HashMap<>();
//
//        for(HistoricActivityInstance act : activities){
//            if(activitiesNamesSet.contains(act.getActivityId()))
//                map.put(IntegrationAuditableT0State.IntegrationAuditableT0Enum.getFieldName(act.getActivityId()), act.getEndTime() != null);
//        }
//
//        return objectMapper.convertValue(map, IntegrationAuditableT0State.class);
//    }
//
//    public static IntegrationVaccinationState matchVaccinationState(List<HistoricActivityInstance> activities){
//        List<String> activitiesNames = IntegrationVaccinationState.IntegrationVaccinationStateEnum.getActivities();
//        Set<String> activitiesNamesSet = new HashSet<>(activitiesNames);
//        Map<String, Boolean> map = new HashMap<>();
//
//        for(HistoricActivityInstance act : activities){
//            if(activitiesNamesSet.contains(act.getActivityId()))
//                map.put(IntegrationVaccinationState.IntegrationVaccinationStateEnum.getFieldName(act.getActivityId()), act.getEndTime() != null);
//        }
//
//        return objectMapper.convertValue(map, IntegrationVaccinationState.class);
//    }

//    public static AuditableT1Id matchVariablesAuditableT1(List<HistoricVariableInstance> variables){
//        AuditableT1Id sample = new AuditableT1Id();
//        Map<String, String> map = objectMapper.convertValue(sample, Map.class);
//
//        for(HistoricVariableInstance var : variables)
//            if(map.containsKey(var.getName()))
//                map.put(var.getName(), (String)var.getValue());
//
//        return objectMapper.convertValue(map, AuditableT1Id.class);
//    }

//    public static List<AuditableT1Id> matchVariablesAuditableT1(List<HistoricVariableInstance> variables, ObjectMapper objectMapper){
//        AuditableT1Id sample = new AuditableT1Id();
//        List<String> varNames = sample.getAllVariablesNames(objectMapper);
//
//        Map<String, Integer> mapCounter = new LinkedHashMap<>();
//        for(String s : varNames)
//            mapCounter.put(s, 0);
//
//        List<Map<String, String>> list = new ArrayList<>();
//        for(HistoricVariableInstance var : variables){
//            if(!mapCounter.containsKey(var.getName()))
//                continue;
//
//            int index = mapCounter.get(var.getName());
//            if(index >= list.size())
//                list.add(objectMapper.convertValue(sample, Map.class));
//
//            list.get(index).put(var.getName(), (String)var.getValue());
//            mapCounter.put(var.getName(), index + 1);
//        }
//
//        List<AuditableT1Id> auditableT1Ids = new ArrayList<>();
//        for(Map<String, String> m : list){
//            AuditableT1Id id = objectMapper.convertValue(m, AuditableT1Id.class);
//            auditableT1Ids.add(id);
//        }
//
//        return auditableT1Ids;
//    }

//    public static List<IntegrationAuditableT1State> matchAuditableT1IntegrationState(List<HistoricActivityInstance> activities, ObjectMapper objectMapper){
//        IntegrationAuditableT1State sample = new IntegrationAuditableT1State();
//        List<String> activitiesNames = IntegrationAuditableT1Enum.getActivities();
//
//        Map<String, Integer> mapCounter = new LinkedHashMap<>();
//        for(String s : activitiesNames)
//            mapCounter.put(s, 0);
//
//        List<Map<String, Boolean>> list = new ArrayList<>();
//        for(HistoricActivityInstance act : activities){
//            if(!mapCounter.containsKey(act.getActivityId()))
//                continue;
//
//            int index = mapCounter.get(act.getActivityId());
//            if(index >= list.size())
//                list.add(objectMapper.convertValue(sample, Map.class));
//
//            list.get(index).put(IntegrationAuditableT1Enum.getFieldName(act.getActivityId()), act.getEndTime() != null);
//            mapCounter.put(act.getActivityId(), index + 1);
//        }
//
//        List<IntegrationAuditableT1State> states = new ArrayList<>();
//        for(Map<String, Boolean> m : list){
//            IntegrationAuditableT1State id = objectMapper.convertValue(m, IntegrationAuditableT1State.class);
//            states.add(id);
//        }
//
//        return states;
//    }

}
