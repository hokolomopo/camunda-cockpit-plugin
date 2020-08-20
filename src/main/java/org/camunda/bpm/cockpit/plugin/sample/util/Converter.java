package org.camunda.bpm.cockpit.plugin.sample.util;

import be.yelido.camunda.module.data.ids.CamundaObjectIdentifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.history.HistoricVariableInstance;

import java.util.List;
import java.util.Map;

public class Converter {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Convert a list of Camunda Variables into a CamundaObjectIdentifier object
     *
     * @param vars the Camunda variables
     * @param c    the class extending CamundaObjectIdentifier to convert to
     * @return the CamundaObjectIdentifier
     */
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

}
