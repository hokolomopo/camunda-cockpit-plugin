package org.camunda.bpm.cockpit.plugin.sample.resources;

import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginRootResource;
import org.camunda.bpm.cockpit.plugin.sample.SamplePlugin;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("plugin/" + SamplePlugin.ID)
public class SamplePluginRootResource extends AbstractPluginRootResource {

  public SamplePluginRootResource() {
    super(SamplePlugin.ID);
  }

  @Path("{engineName}/search")
  public SearchResource getProcessInstanceResource(@PathParam("engineName") String engineName) {
    return subResource(new SearchResource(engineName), engineName);
  }
}
