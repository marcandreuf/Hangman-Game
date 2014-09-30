package org.hmgame.resources;


import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hmgame.persistence.ConcurrentPersistenceService;
import org.hmgame.persistence.PersistenceService;


/**
 * REST Web Service which represents the collection of games.
 *
 * @author marc
 */
@Path("games")
public class GamesResource {
   
    @Context
    private UriInfo context;   
    
     //TODO: This should be injected through a dependency injection tool.
    // Under investigation how to get this. For now we use DBFactory.
    private final PersistenceService persistenceService;
    private static final Logger logger = 
            Logger.getLogger(GamesResource.class.getName());

    
    public GamesResource() throws Exception {
        this.persistenceService = ConcurrentPersistenceService.getDBInstance();
    }
    
    public GamesResource(ConcurrentPersistenceService persistenceService) 
    throws Exception {
        this.persistenceService = persistenceService; 
    }
    

    @GET
    @Path("/query")
    @Produces("application/json")
    public Response getListOfGames(@QueryParam("state") String state) {
        JSONObject listOfGames = new JSONObject();
        logger.log(Level.INFO, "Query param state: {0}", state);
        List<String> games = persistenceService.queryByState(state);
        for(String game : games) {          
            try {
                listOfGames.append("list", game);
            } catch (JSONException ex) {
                logger.log(Level.SEVERE, 
                        "Not able to append the "+game
                        +" to the list of results.", ex);
            }
        }
        return Response.ok().entity(listOfGames).build();
    }

}
