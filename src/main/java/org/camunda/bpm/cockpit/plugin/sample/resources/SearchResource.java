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
package org.camunda.bpm.cockpit.plugin.sample.resources;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import be.yelido.camunda.module.data.ids.*;
import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import org.camunda.bpm.cockpit.plugin.sample.db.ProcessInstanceCountDto;
import org.camunda.bpm.cockpit.plugin.sample.db.ProcessInstanceFromRootDto;
import org.camunda.bpm.cockpit.plugin.sample.progress.IntegrationAuditableT0State;
import org.camunda.bpm.cockpit.plugin.sample.progress.IntegrationAuditableT1State;
import org.camunda.bpm.cockpit.plugin.sample.progress.IntegrationLabRequestState;
import org.camunda.bpm.cockpit.plugin.sample.progress.IntegrationVaccinationState;
import org.camunda.bpm.cockpit.plugin.sample.util.Convertor;
import org.camunda.bpm.cockpit.plugin.sample.util.MatchingUtil;
import org.camunda.bpm.cockpit.plugin.sample.util.ProcessInstanceFromRootQuery;
import org.camunda.bpm.cockpit.plugin.sample.util.RequestsUtil;
import org.camunda.bpm.cockpit.plugin.sample.progress.JioIntegrationState;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;

/**
 *
 * @author nico.rehwaldt
 */
public class SearchResource extends AbstractCockpitPluginResource {

  public SearchResource(String engineName) {
    super(engineName);
  }

  @GET
  public List<ProcessInstanceCountDto> getProcessInstanceCounts() {

    return getQueryService()
        .executeQuery(
          "cockpit.sample.selectProcessInstanceCountsByProcessDefinition",
          new QueryParameters<ProcessInstanceCountDto>());
  }

  // After = FINISHED after
  // Before = BEGAN before
  @GET
  @Path("/searchVariable")
  @Produces("application/json")
  public ArrayList<JioIntegrationState> searchFromVariable(@QueryParam("varName") String varName, @QueryParam("varValue") String varValue,
                                                           @QueryParam("before") Date before, @QueryParam("after") Date after) {

    ArrayList<JioIntegrationState> replyList = new ArrayList<>();

    // Get all processes instances with matching variables
    List<HistoricProcessInstance> processInstanceList = RequestsUtil.getProcessFromVariable(varName, varValue, before, after, getProcessEngine());

    // Get the root processes for all the processes instances
    List<String> rootProcesses = RequestsUtil.getAllRootProcesses(processInstanceList);

    for(String rootProcessId : rootProcesses) {
      JioIntegrationState jioIntegrationState = new JioIntegrationState();
      jioIntegrationState.setProcessId(rootProcessId);

      replyList.add(jioIntegrationState);

      // Get all the sub-processes for this root process
      ProcessInstanceFromRootQuery processInstanceFromRootQuery = ProcessInstanceFromRootQuery.createQuery().rootProcessId(rootProcessId).build();
      List<ProcessInstanceFromRootDto> processInstanceFromRootList = RequestsUtil.getProcessInstanceFromRootAndVar(processInstanceFromRootQuery, getQueryService());

      //State of JIO Upload process
      getJioUploadState(processInstanceFromRootList, jioIntegrationState);

      //State of Vaccines process
      getVaccinationStates(processInstanceFromRootList, jioIntegrationState);

      //State of Lab Requests
      getEgelStates(processInstanceFromRootList, jioIntegrationState);


      jioIntegrationState.updateMergeState();
    }

    return replyList;
  }

  private void getJioUploadState(List<ProcessInstanceFromRootDto> processes, JioIntegrationState jioState){

    ProcessInstanceFromRootDto jioUploadInstance = processes.stream().filter(p -> p.getProcessDefinitionKey().equals("JIO_Upload")).findFirst().orElse(null);

    if(jioUploadInstance == null)
      return;

    List<HistoricVariableInstance> jioUploadVariables = RequestsUtil.getAllVariablesSorted(jioUploadInstance.getId(), getProcessEngine());
    List<HistoricActivityInstance> jioUploadActivities = getProcessEngine().getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(jioUploadInstance.getId()).list();

    //---------------- Process AuditableT0 ----------------//

    // Get variables of the AuditableT0
    AuditableT0Id t0Id = Convertor.convert(jioUploadVariables, AuditableT0Id.class);

    // Get state of integration of the AuditableT0
    HistoricActivityInstance integrateT0RootActivity = jioUploadActivities.stream().filter(a -> a.getActivityId().equals("AuditableT0_Integrate_process")).findFirst().get();
    List<HistoricActivityInstance> integrateT0Activities = jioUploadActivities.stream().filter(a -> a.getParentActivityInstanceId().equals(integrateT0RootActivity.getId())).collect(Collectors.toList());
    IntegrationAuditableT0State integrationT0State = MatchingUtil.matchSimpleProcess(integrateT0Activities, IntegrationAuditableT0State.class);

    integrationT0State.setAuditableT0Id(t0Id);
    integrationT0State.setProcessingDate(integrateT0RootActivity.getStartTime());
    integrationT0State.setProcessId(jioUploadInstance.getId());
    integrationT0State.updateMergeState();

    jioState.setAuditableT0State(integrationT0State);

    //---------------- Process AuditableT1s ----------------//
    // Get all sub-processes integrating AuditableT1s
    List<HistoricActivityInstance> integrateT1RootActivities = jioUploadActivities.stream().filter(a -> a.getActivityId().equals("AuditableT1_Integrate_process")).collect(Collectors.toList());
    for (HistoricActivityInstance act : integrateT1RootActivities) {

      // Get variables for this process instance
      List<HistoricVariableInstance> integrateT1Variables = jioUploadVariables.stream().filter(v -> v.getActivityInstanceId().equals(act.getId())).collect(Collectors.toList());
      AuditableT1Id id = Convertor.convert(integrateT1Variables, AuditableT1Id.class);

      // Get state of integration for this process instance
      List<HistoricActivityInstance> integrateT1Activities = jioUploadActivities.stream().filter(a -> a.getParentActivityInstanceId().equals(act.getId())).collect(Collectors.toList());
      IntegrationAuditableT1State state = MatchingUtil.matchSimpleProcess(integrateT1Activities, IntegrationAuditableT1State.class);
      state.setAuditableT1Id(id);
      state.setProcessingDate(act.getStartTime());
      state.setProcessId(act.getProcessInstanceId());
      state.updateMergeState();

      jioState.getAuditableT1States().add(state);
    }
  }

  private void getVaccinationStates(List<ProcessInstanceFromRootDto> processes, JioIntegrationState jioState){
    List<ProcessInstanceFromRootDto> vaccineProcesses = processes.stream().filter(p -> p.getProcessDefinitionKey().equals("Vaccinnet_Processor")).collect(Collectors.toList());

    for(ProcessInstanceFromRootDto vaccineProcess : vaccineProcesses){
      List<HistoricVariableInstance> vaccineProcessVariables = RequestsUtil.getAllVariablesSorted(vaccineProcess.getId(), getProcessEngine());
      List<HistoricActivityInstance> vaccineProcessActivities = getProcessEngine().getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(vaccineProcess.getId()).list();

      VaccinationDTOId id = Convertor.convert(vaccineProcessVariables, VaccinationDTOId.class);

      // Get state of integration for this process instance
      IntegrationVaccinationState state = MatchingUtil.matchSimpleProcess(vaccineProcessActivities, IntegrationVaccinationState.class);
      state.setVaccinationDTOId(id);
      state.setProcessingDate(vaccineProcessActivities.get(0).getStartTime());
      state.setProcessId(vaccineProcess.getId());
      state.updateMergeState();

      jioState.getVaccinationStates().add(state);

    }

  }

  private void getEgelStates(List<ProcessInstanceFromRootDto> processes, JioIntegrationState jioState){

    ProcessInstanceFromRootDto egeProcessorInstance = processes.stream().filter(p -> p.getProcessDefinitionKey().equals("Egel_Processor")).findFirst().orElse(null);

    if(egeProcessorInstance == null)
      return;

    List<HistoricVariableInstance> egeProcessorVariables = RequestsUtil.getAllVariablesSorted(egeProcessorInstance.getId(), getProcessEngine());
    List<HistoricActivityInstance> egeProcessorActivities = getProcessEngine().getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(egeProcessorInstance.getId()).list();

    ProcessInstanceFromRootDto processWorkerInstance = processes.stream().filter(p -> p.getProcessDefinitionKey().equals("Process_Worker")).findFirst().orElse(null);
    List<HistoricVariableInstance> processWorkerVariables = RequestsUtil.getAllVariablesSorted(processWorkerInstance.getId(), getProcessEngine());

    //---------------- Process AuditableT1s ----------------//
    // Get all sub-processes integrating AuditableT1s
    List<HistoricActivityInstance> createLabRequestActivities = egeProcessorActivities.stream().filter(a -> a.getActivityId().equals("New_Lab_Request")).collect(Collectors.toList());
    for (HistoricActivityInstance act : createLabRequestActivities) {

      // Get variables for this process instance
      List<HistoricVariableInstance> newLabRequestVariables = egeProcessorVariables.stream().filter(v -> v.getActivityInstanceId().equals(act.getId())).collect(Collectors.toList());
      LabRequestDTOId id = Convertor.convert(newLabRequestVariables, LabRequestDTOId.class);

      // Get state of integration for this process instance
      List<HistoricActivityInstance> newLabRequestActivities = egeProcessorActivities.stream().filter(a -> a.getParentActivityInstanceId().equals(act.getId())).collect(Collectors.toList());
      IntegrationLabRequestState state = new IntegrationLabRequestState();
      HistoricActivityInstance createNewLabRequestActivity = newLabRequestActivities.stream().filter(a -> a.getActivityId().equals("Create_new_lab_request")).findFirst().orElse(null);
      if(createNewLabRequestActivity == null)
        state.setState("NOT CREATED");
      else{
        HistoricVariableInstance labRequestUuidVariable = processWorkerVariables.stream().filter(v -> v.getValue().equals(id.getLabRequestUuid())).findFirst().orElse(null);
        HistoricVariableInstance labRequestStatusVariable = processWorkerVariables.stream()
                .filter(v -> v.getName().equals("labRequestStatus") && v.getActivityInstanceId().equals(labRequestUuidVariable.getActivityInstanceId()))
                .findFirst().orElse(null);
        state.setState((String)labRequestStatusVariable.getValue());
      }

      state.setLabRequestDTOId(id);
      state.setProcessingDate(act.getStartTime());
      state.setProcessId(act.getProcessInstanceId());
      state.updateMergeState();

      jioState.getLabRequestStates().add(state);
    }
  }


  @GET
  @Path("/test")
  @Produces("application/json")
  public ArrayList<JioIntegrationState> getTest(@QueryParam("test") String test) {

    ArrayList<JioIntegrationState> list = new ArrayList<>();
//    list.add(getReply());
//    list.add(getReply());

    return list;
//    return "[{\"string\":\"0\"},{\"string\":\"1\"}]";
  }

  //TODO delete this
//  private JioIntegrationState getReply(){
//    JioIntegrationState reply = new JioIntegrationState();
//
//    IntegrationAuditableT0State t0State = new IntegrationAuditableT0State();
//    t0State.setAuditableT0Id(new AuditableT0Id("0", "0", "0", "Jean"));
//    t0State.setProcessingDate(Calendar.getInstance().getTime());
//    t0State.setFullyMerged(true);
//    t0State.setAuditableMerged(true);
//    t0State.setDaoMerged(true);
//    t0State.setPostitUpdated(true);
//    reply.setAuditableT0State(t0State);
//
//    ArrayList<IntegrationAuditableT1State> listT1 = new ArrayList<>();
//
//    IntegrationAuditableT1State state = new IntegrationAuditableT1State();
//    state.setAuditableT1Id(new AuditableT1Id("1", "1", "1", "Jean", "1"));
//    state.setProcessingDate(Calendar.getInstance().getTime());
//    listT1.add(state);
//
//    state = new IntegrationAuditableT1State();
//    state.setAuditableT1Id(new AuditableT1Id("2", "2", "2", "Roger", "2"));
//    state.setProcessingDate(Calendar.getInstance().getTime());
//    listT1.add(state);
//
//    reply.setAuditableT1States(listT1);
//
//
//    ArrayList<IntegrationLabRequestState> listEgel = new ArrayList<>();
//    IntegrationLabRequestState egelState = new IntegrationLabRequestState();
//    egelState.setLabRequestDTOId(new ToProcessEgeld("1", "José"));
//    egelState.setProcessingDate(Calendar.getInstance().getTime());
//    listEgel.add(egelState);
//
//    egelState = new IntegrationLabRequestState();
//    egelState.setLabRequestDTOId(new ToProcessEgeld("1", "André"));
//    egelState.setProcessingDate(Calendar.getInstance().getTime());
//    listEgel.add(egelState);
//
//    reply.setEgelStates(listEgel);
//
//    ArrayList<IntegrationVaccinationState> listVacc = new ArrayList<>();
//    IntegrationVaccinationState vaccinationState = new IntegrationVaccinationState();
//    vaccinationState.setVaccinationDTOId(new VaccinationDTOId("1", "135","Polio"));
//    vaccinationState.setDone(true);
//    vaccinationState.setProcessingDate(Calendar.getInstance().getTime());
//    listVacc.add(vaccinationState);
//
//    vaccinationState = new IntegrationVaccinationState();
//    vaccinationState.setVaccinationDTOId(new VaccinationDTOId("1", "135","Polio"));
//    vaccinationState.setDone(true);
//    vaccinationState.setProcessingDate(Calendar.getInstance().getTime());
//    listVacc.add(vaccinationState);
//
//    reply.setVaccinationStates(listVacc);
//
//    return reply;
//  }

}
