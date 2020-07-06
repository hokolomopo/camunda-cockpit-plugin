/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.cockpit.plugin.sample;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import be.yelido.camunda.module.data.ids.AuditableT1Id;
import be.yelido.camunda.module.data.ids.VaccinationDTOId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.db.QueryService;
import org.camunda.bpm.cockpit.plugin.sample.db.ProcessInstanceFromRootDto;
import org.camunda.bpm.cockpit.plugin.sample.progress.IntegrationAuditableT1State;
import org.camunda.bpm.cockpit.plugin.sample.util.*;
import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
*
* @author nico.rehwaldt
*/
@Ignore
public class TestFunctionality extends AbstractCockpitPluginTest {

  @Test
  public void testPluginDiscovery() {
    CockpitPlugin samplePlugin = Cockpit.getRuntimeDelegate().getPluginRegistry().getPlugin("sample-plugin");

    Assert.assertNotNull(samplePlugin);
  }


  @Test
  public void testQueryFromRootRuntime(){
    QueryService queryService = getQueryService();
    ProcessEngine processEngine = getProcessEngine();

    HashMap<String, String> p = new HashMap<>();
    p.put("varName", "rrnr");
    //p.put("varValue", "1749913543");
//    p.put("rootId", "be209f0f-a4c6-11ea-a947-0a0027000004");

    QueryParameters<ProcessInstanceFromRootDto> parameters = new QueryParameters<>();
    parameters.setParameter(p);
    List<ProcessInstanceFromRootDto> instanceCounts =
            queryService
                    .executeQuery(
                            "cockpit.sample.selectProcessInstanceByRootAndVariableRuntime",
                            parameters);

    System.out.println(instanceCounts);

  }

  @Test
  public void testQueryFromRootHistory(){
    QueryService queryService = getQueryService();
    ProcessEngine processEngine = getProcessEngine();

    HashMap<String, String> p = new HashMap<>();
    p.put("varName", "rrnr");
    p.put("varValue", "1749913543");
    p.put("rootId", "be209f0f-a4c6-11ea-a947-0a0027000004");

    QueryParameters<ProcessInstanceFromRootDto> parameters = new QueryParameters<>();
    parameters.setParameter(p);
    List<ProcessInstanceFromRootDto> instanceCounts =
            queryService
                    .executeQuery(
                            "cockpit.sample.selectProcessInstanceByRootAndVariableHistory",
                            parameters);

    System.out.println(instanceCounts);

  }

  @Test
  public void testRrnrSearch(){
    List<String> rrnrValues = TestRequestUtils.getAllVarValue("rrnr", getProcessEngine());
    System.out.println("RrNr values : " + rrnrValues);
    ObjectMapper o = new ObjectMapper();
//    List<String> rootProcessInstances = RequestsUtil.getRootProcessIdsFromVariable("rrnr", rrnrValues.get(0), getProcessEngine());
//    System.out.println(rootProcessInstances);

    String searchedRrnr = rrnrValues.get(0);

    List<HistoricProcessInstance> processInstanceList = RequestsUtil.getProcessFromVariable("rrnr",searchedRrnr , getProcessEngine());
    List<String> rootProcesses = RequestsUtil.getAllRootProcesses(processInstanceList);
    System.out.println("Root processes : " + rootProcesses);

    for(String rootProcessId : rootProcesses){
      ProcessInstanceFromRootQuery processInstanceFromRootQuery = ProcessInstanceFromRootQuery.createQuery().rootProcessId(rootProcessId).variableName("rrnr").variableValue(searchedRrnr).build();
      List<ProcessInstanceFromRootDto> processInstanceFromRootList = RequestsUtil.getProcessInstanceFromRootAndVar(processInstanceFromRootQuery, getQueryService());
      System.out.println("Number of Sub processes : " + processInstanceFromRootList.size());
      for(ProcessInstanceFromRootDto p : processInstanceFromRootList){
        System.out.println(p);
      }

      //JIO Upload State
      String jioUploadInstanceId = processInstanceFromRootList.stream().filter(p -> p.getProcessDefinitionKey().equals("JIO_Upload")).findFirst().orElse(null).getId();

      AuditableT1Id auditableT1Id = new AuditableT1Id();
      List<HistoricVariableInstance> jioUploadVariables = RequestsUtil.getAllVariablesSorted(jioUploadInstanceId, getProcessEngine());
      jioUploadVariables.get(0).getActivityInstanceId();

      List<HistoricActivityInstance> jioUploadActivities = getProcessEngine().getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(jioUploadInstanceId).list();
      List<HistoricActivityInstance> integrateT1RootActivities = jioUploadActivities.stream().filter(a -> a.getActivityId().equals("AuditableT1_Integrate_process")).collect(Collectors.toList());
      for(HistoricActivityInstance act : integrateT1RootActivities){
        List<HistoricVariableInstance> integrateT1Variables = jioUploadVariables.stream().filter(v -> v.getActivityInstanceId().equals(act.getId())).collect(Collectors.toList());
        AuditableT1Id id = Convertor.convert(integrateT1Variables, AuditableT1Id.class);
        System.out.println("AuditableT1Id : " + id);

        List<HistoricActivityInstance> integrateT1Activities = jioUploadActivities.stream().filter(a -> a.getParentActivityInstanceId().equals(act.getId())).collect(Collectors.toList());
        IntegrationAuditableT1State state = MatchingUtil.matchSimpleProcess(integrateT1Activities, IntegrationAuditableT1State.class);
        System.out.println("State : " + state);

      }

      //Vaccines
      List<ProcessInstanceFromRootDto> vaccinesProcesses = processInstanceFromRootList.stream().filter(p -> p.getProcessDefinitionKey().equals("Vaccinnet_Processor")).collect(Collectors.toList());
      for(ProcessInstanceFromRootDto vaccineProcess : vaccinesProcesses){
        List<HistoricVariableInstance> vaccineVariables = getProcessEngine().getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(vaccineProcess.getId()).list();
        VaccinationDTOId id = Convertor.convertToVaccinationDTO(vaccineVariables);
        System.out.println("Vaccination id : " + id);
      }

      //LabRequests
      List<ProcessInstanceFromRootDto> egelProcesses = processInstanceFromRootList.stream().filter(p -> p.getProcessDefinitionKey().equals("Egel_Processor")).collect(Collectors.toList());
      for(ProcessInstanceFromRootDto egelProcess : egelProcesses){
        List<HistoricVariableInstance> egelVariables = getProcessEngine().getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(egelProcess.getId()).list();
//        for(HistoricVariableInstance var : egelVariables)
//          System.out.println(String.format("Variable {%s, %s} created at %s in activity %s", var.getName(), var.getValue(), var.getCreateTime(), var.getActivityInstanceId()));
      }


    }
  }

}
