package org.camunda.bpm.cockpit.plugin.sample.util;

import be.yelido.camunda.module.data.ids.AuditableT1Id;
import be.yelido.camunda.module.data.ids.CamundaObjectIdentifier;
import be.yelido.camunda.module.data.ids.VaccinationDTOId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.history.HistoricVariableInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Convertor {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T extends CamundaObjectIdentifier> T convert(List<HistoricVariableInstance> vars, Class<T> c){
        T sample;
        try {
            sample = c.newInstance();
        } catch (Exception e) {
            return null;
        }
        Map<String, String> map = mapper.convertValue(sample, Map.class);

        for(HistoricVariableInstance var : vars)
            if(map.containsKey(var.getName()))
                map.put(var.getName(), (String)var.getValue());

        return mapper.convertValue(map, c);
    }

    public static AuditableT1Id convertToAuditableT1Id(List<HistoricVariableInstance> variables){
        return convert(variables, AuditableT1Id.class);
    }

    public static VaccinationDTOId convertToVaccinationDTO(List<HistoricVariableInstance> vars){
        VaccinationDTOId id = new VaccinationDTOId();
        List<String> fieldNames = id.getAllVariablesNames(mapper);

        Map<String, String> map = new HashMap<>();
        for(String s : fieldNames)
            map.put(s, null);

        for(HistoricVariableInstance var : vars)
            if(map.containsKey(var.getName()))
                map.put(var.getName(), (String)var.getValue());

        return mapper.convertValue(map, VaccinationDTOId.class);
    }


}
