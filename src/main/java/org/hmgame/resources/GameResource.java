package org.hmgame.resources;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hmgame.persistence.ConcurrentPersistenceService;
import org.hmgame.persistence.PersistenceService;


/**
 * REST Web Service to expose the game resource, which represents the state
 * of a game.
 *
 * @author marc
 */
@Path("games/game")
public class GameResource {

    
    //TODO: This should be injected through a dependency injection tool.
    // Under investigation how to get this. For now we use DBFactory.
    private final PersistenceService persistenceService;
    private static final Logger logger = 
            Logger.getLogger(GameResource.class.getName());
    
    @Context
    private UriInfo context;
    
    
    public GameResource() throws Exception {
        this.persistenceService = ConcurrentPersistenceService.getDBInstance();
    }
    
    //This constructor would allow to implmement unit tests for this resource
    // where we can mock the PersistenceService and verify that the resource
    // is calling the expected methods.
    public GameResource(ConcurrentPersistenceService persistenceService) 
    throws Exception {
        this.persistenceService = persistenceService; 
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGameById(@PathParam("id") String id) {        
        String loadedGame = persistenceService.load(Long.valueOf(id));
        return Response.status(Response.Status.OK).entity(loadedGame).build();
    }
    
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postGame(String jsonGame) {      
      System.out.println("Post content parameter: "+jsonGame);
      try {         
         JSONObject jsonObj = new JSONObject(jsonGame);
         saveNewGame(jsonObj);
         return Response.created(URI.create("/"+jsonObj.getString("id")))
                        .entity(jsonObj.toString())
                        .build();
      } catch (Exception ex) {
         return createExceptionResponse(ex);
      }
    }    

    private void saveNewGame(JSONObject jsonObj) throws JSONException {
        Long newId = persistenceService.save(jsonObj.toString());
        updateGameId(jsonObj, newId);
    }

    private void updateGameId(JSONObject jsonObj, Long newId) 
    throws JSONException {
        jsonObj.put("id", String.valueOf(newId));
        persistenceService.update(newId, jsonObj.toString());
    }
    
    private Response createExceptionResponse(Exception e) {
        logger.log(Level.SEVERE, e.getMessage(), e);
        String jsonError = "\"Message\":"+e.getMessage();
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity(jsonError)
                       .build();
    }
    

    @PUT
    @Consumes("application/json")
    public Response updateGame(String jsonGame) {
        System.out.println("Put content: "+jsonGame);
      try {         
         try{
            JSONObject jsonObj = new JSONObject(jsonGame);
            Long keyId = Long.valueOf(jsonObj.getString("id"));
            persistenceService.update(keyId, jsonObj.toString());
         }catch(NumberFormatException|JSONException e){
            logger.log(Level.WARNING, "Unable to update game{0}: Error: {1}", 
                    new Object[]{jsonGame, e.getMessage()});
         }        
         return Response.ok().build();
      } catch (Exception ex) {
         return createExceptionResponse(ex);
      }
        
    }
}
