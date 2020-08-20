package org.camunda.bpm.cockpit.plugin.sample;

import org.camunda.bpm.cockpit.plugin.sample.resources.SearchResource;
import org.camunda.bpm.cockpit.plugin.sample.util.TestRequestUtils;
import org.camunda.bpm.cockpit.plugin.sample.state.JioIntegrationState;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.junit.Test;

import java.text.ParseException;
import java.util.List;

public class TestRequests extends AbstractCockpitPluginTest {

    @Test
    public void testSearchRequest() throws ParseException {
        SearchResource searchResource = new SearchResource("engine");

        List<String> rrnrValues = TestRequestUtils.getAllVarValue("rrnr", getProcessEngine());

//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//
//        String sDate1 = "26-06-2020 15:13:50";
//        Date before = dateFormat.parse(sDate1);
//
//        String sDate2 = "26-06-2020 15:13:50";
//        Date after = dateFormat.parse(sDate2);
//        after = null;

        List<JioIntegrationState> states = searchResource.searchFromVariable("rrnr", rrnrValues.get(0), null, null);

        System.out.println(states);
    }
}
