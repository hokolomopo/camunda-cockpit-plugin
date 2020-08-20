package org.camunda.bpm.cockpit.plugin.sample.resources;

import be.yelido.camunda.module.data.ids.*;
import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import org.camunda.bpm.cockpit.plugin.sample.db.ProcessInstanceFromRootDto;
import org.camunda.bpm.cockpit.plugin.sample.db.ProcessInstanceFromRootQuery;
import org.camunda.bpm.cockpit.plugin.sample.state.*;
import org.camunda.bpm.cockpit.plugin.sample.util.Converter;
import org.camunda.bpm.cockpit.plugin.sample.util.MatchingClass;
import org.camunda.bpm.cockpit.plugin.sample.util.RequestsUtil;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SearchResource extends AbstractCockpitPluginResource {

  /**
   * Instantiates a new Search resource.
   *
   * @param engineName the name of the BPMN engine
   */
  public SearchResource(String engineName) {
    super(engineName);
  }


  /**
   * Search for a variable in the Camunda database and return the state of all the processes that contains this variable.
   *
   * @param varName      the variable name
   * @param varValue     the variable value
   * @param beforeString the lower bound date for the search as a String
   * @param afterString  the upper bound date for the search as a String
   * @return the state of the processes instances that contain the variable searched
   */
  @GET
  @Path("/searchVariable")
  @Produces("application/json")
  public ArrayList<JioIntegrationState> searchFromVariable(@QueryParam("varName") String varName, @QueryParam("varValue") String varValue,
                                                           @QueryParam("before") String beforeString, @QueryParam("after") String afterString) throws ParseException {

    ArrayList<JioIntegrationState> replyList = new ArrayList<>();

    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd yyyy", Locale.ENGLISH);

    Date before = null;
    if(beforeString != null)
      before = dateFormat.parse(beforeString);

    Date after = null;
    if(afterString != null)
      after = dateFormat.parse(afterString);

    // Get all processes instances with matching variables
    List<HistoricProcessInstance> processInstanceList = RequestsUtil.getProcessFromVariable(varName, varValue,
            before, after, getProcessEngine());

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

      // State of Legacy Processor
      getLegacyProcessorState(processInstanceFromRootList, jioIntegrationState);

      jioIntegrationState.updateMergeState();
    }

    return replyList;
  }

  // Get the state of the JIO Upload service
  private void getJioUploadState(List<ProcessInstanceFromRootDto> processes, JioIntegrationState jioState){

    ProcessInstanceFromRootDto jioUploadInstance = processes.stream().filter(p -> p.getProcessDefinitionKey().equals("JIO_Upload")).findFirst().orElse(null);

    if(jioUploadInstance == null)
      return;

    List<HistoricVariableInstance> jioUploadVariables = RequestsUtil.getAllVariablesSorted(jioUploadInstance.getId(), getProcessEngine());
    List<HistoricActivityInstance> jioUploadActivities = getProcessEngine().getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(jioUploadInstance.getId()).list();

    //---------------- Process AuditableT0 ----------------//

    // Get variables of the AuditableT0
    AuditableT0Id t0Id = Converter.convert(jioUploadVariables, AuditableT0Id.class);

    // Get state of integration of the AuditableT0
    HistoricActivityInstance integrateT0RootActivity = jioUploadActivities.stream().filter(a -> a.getActivityId().equals("AuditableT0_Integrate_process")).findFirst().get();
    List<HistoricActivityInstance> integrateT0Activities = jioUploadActivities.stream().filter(a -> a.getParentActivityInstanceId().equals(integrateT0RootActivity.getId())).collect(Collectors.toList());
    IntegrationAuditableT0State integrationT0State = MatchingClass.matchSimpleProcess(integrateT0Activities, IntegrationAuditableT0State.class);

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
      AuditableT1Id id = Converter.convert(integrateT1Variables, AuditableT1Id.class);

      // Get state of integration for this process instance
      List<HistoricActivityInstance> integrateT1Activities = jioUploadActivities.stream().filter(a -> a.getParentActivityInstanceId().equals(act.getId())).collect(Collectors.toList());
      IntegrationAuditableT1State state = MatchingClass.matchSimpleProcess(integrateT1Activities, IntegrationAuditableT1State.class);
      state.setAuditableT1Id(id);
      state.setProcessingDate(act.getStartTime());
      state.setProcessId(act.getProcessInstanceId());
      state.updateMergeState();

      jioState.getAuditableT1States().add(state);
    }
  }

  // Get the state of the Vaccinnet Processor service
  private void getVaccinationStates(List<ProcessInstanceFromRootDto> processes, JioIntegrationState jioState){
    List<ProcessInstanceFromRootDto> vaccineProcesses = processes.stream().filter(p -> p.getProcessDefinitionKey().equals("Vaccinnet_Processor")).collect(Collectors.toList());

    for(ProcessInstanceFromRootDto vaccineProcess : vaccineProcesses){
      List<HistoricVariableInstance> vaccineProcessVariables = RequestsUtil.getAllVariablesSorted(vaccineProcess.getId(), getProcessEngine());
      List<HistoricActivityInstance> vaccineProcessActivities = getProcessEngine().getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(vaccineProcess.getId()).list();

      VaccinationDTOId id = Converter.convert(vaccineProcessVariables, VaccinationDTOId.class);

      // Get state of integration for this process instance
      IntegrationVaccinationState state = MatchingClass.matchSimpleProcess(vaccineProcessActivities, IntegrationVaccinationState.class);
      state.setVaccinationDTOId(id);
      state.setProcessingDate(vaccineProcessActivities.get(0).getStartTime());
      state.setProcessId(vaccineProcess.getId());
      state.updateMergeState();

      jioState.getVaccinationStates().add(state);

    }

  }

  // Get the state of the Egel Processor Service
  private void getEgelStates(List<ProcessInstanceFromRootDto> processes, JioIntegrationState jioState){

    ProcessInstanceFromRootDto egeProcessorInstance = processes.stream().filter(p -> p.getProcessDefinitionKey().equals("Egel_Processor")).findFirst().orElse(null);

    if(egeProcessorInstance == null)
      return;

    List<HistoricVariableInstance> egeProcessorVariables = RequestsUtil.getAllVariablesSorted(egeProcessorInstance.getId(), getProcessEngine());
    List<HistoricActivityInstance> egeProcessorActivities = getProcessEngine().getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(egeProcessorInstance.getId()).list();

    // Get all sub-processes "Create new Request"
    List<HistoricActivityInstance> createLabRequestActivities = egeProcessorActivities.stream().filter(a -> a.getActivityId().equals("New_Lab_Request")).collect(Collectors.toList());
    for (HistoricActivityInstance act : createLabRequestActivities) {

      // Get variables for this process instance
      List<HistoricVariableInstance> newLabRequestVariables = egeProcessorVariables.stream().filter(v -> v.getActivityInstanceId().equals(act.getId())).collect(Collectors.toList());
      LabRequestDTOId id = Converter.convert(newLabRequestVariables, LabRequestDTOId.class);

      // Get state of integration for this process instance
      List<HistoricActivityInstance> newLabRequestActivities = egeProcessorActivities.stream().filter(a -> a.getParentActivityInstanceId().equals(act.getId())).collect(Collectors.toList());
      IntegrationLabRequestState state = new IntegrationLabRequestState();
      HistoricActivityInstance createNewLabRequestActivity = newLabRequestActivities.stream().filter(a -> a.getActivityId().equals("Create_new_lab_request")).findFirst().orElse(null);

      if(createNewLabRequestActivity == null || id == null)
        state.setState("NOT CREATED");
      else{

        // There will be 2 labRequestUuid variables : one in the "Create New Request" sub-process, and one in the "Egel request" sub-process, we want the one from the Egel
        // Request process, thus the filter on the activity Id
        HistoricVariableInstance labRequestUuidVariable = egeProcessorVariables.stream().filter(v -> v.getValue().equals(id.getLabRequestUuid()) && !v.getActivityInstanceId().equals(act.getId())).findFirst().orElse(null);
        if(labRequestUuidVariable == null)
          state.setState("Unknown");
        else{
          HistoricVariableInstance labRequestStatusVariable = egeProcessorVariables.stream()
                  .filter(v -> v.getName().equals("labRequestStatus") && v.getActivityInstanceId().equals(labRequestUuidVariable.getActivityInstanceId()))
                  .findFirst().orElse(null);
          if(labRequestStatusVariable == null)
            state.setState("Unknown");
          else
            state.setState((String)labRequestStatusVariable.getValue());
        }
      }

      state.setLabRequestDTOId(id);
      state.setProcessingDate(act.getStartTime());
      state.setProcessId(act.getProcessInstanceId());
      state.updateMergeState();

      jioState.getLabRequestStates().add(state);
    }
  }

  // Get the state of the Legacy Processor
  private void getLegacyProcessorState(List<ProcessInstanceFromRootDto> processes, JioIntegrationState jioState){

    List<ProcessInstanceFromRootDto> legacyProcesses = processes.stream().filter(p -> p.getProcessDefinitionKey().equals("Legacy_Processor")).collect(Collectors.toList());

    for(ProcessInstanceFromRootDto legacyProcess : legacyProcesses){
      List<HistoricVariableInstance> legacyProcessVariables = RequestsUtil.getAllVariablesSorted(legacyProcess.getId(), getProcessEngine());
      List<HistoricActivityInstance> legacyProcessActivities = getProcessEngine().getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(legacyProcess.getId()).list();

      ToProcessLegacyDTOId id = Converter.convert(legacyProcessVariables, ToProcessLegacyDTOId.class);

      // Get state of integration for this process instance
      IntegrationLegacyProcessorState state = MatchingClass.matchSimpleProcess(legacyProcessActivities, IntegrationLegacyProcessorState.class);
      state.setToProcessLegacyDTOId(id);
      state.setProcessingDate(legacyProcessActivities.get(0).getStartTime());
      state.setProcessId(legacyProcess.getId());
      state.updateMergeState();

      jioState.getLegacyProcessorStates().add(state);

    }
  }


}
