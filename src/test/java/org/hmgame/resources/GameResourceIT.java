package org.hmgame.resources;


import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.response.Response;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import org.codehaus.jettison.json.JSONObject;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author marc
 */
public class GameResourceIT extends RestAssuredTestBase{
    
    private static final Logger logger = Logger.getLogger(GameResourceIT.class.getName());
   
    private static JSONObject jsonTestObject; 
    
    
    @BeforeClass
    public static void initialiseTestData() throws Exception{
       jsonTestObject = new JSONObject();
       jsonTestObject.put("id", "");
       jsonTestObject.put("wordToGuess", "hello");
       jsonTestObject.put("attempts", "att");
       jsonTestObject.put("state", "new");
    }
    
    @Before
    public void setUpGameResourceIt(){
        super.setUpRestAssuredHostWS();
    }
    
    
    @Test
    public void shouldGetGameInstanceFromPostResponseHeaderLocation() 
    throws Exception {        
        Response postResp = given()
                                .request()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(jsonTestObject.toString())
                                .then()
                                .post("/games/game");
        String postResponseLocation = postResp.getHeader("Location");
                        
        Response getResp = expect()
                                .statusCode(200)
                                .contentType(MediaType.APPLICATION_JSON)
                                .when()
                                .get(postResponseLocation);        
        JSONObject jsonObjeGetResponse = new JSONObject(getResp.asString());
        
        assertTrue(jsonTestObject.getString("wordToGuess")
                   .equals(jsonObjeGetResponse.getString("wordToGuess")));
    }
    
    @Test
    public void shouldCreateANewGameAndGetTheNewGameInstance() throws Exception{        
       
       Response resp =  given()
               .request()
               .contentType(MediaType.APPLICATION_JSON)
               .body(jsonTestObject.toString())
               .expect()
               .statusCode(201)
               .contentType(MediaType.APPLICATION_JSON)
               .body(containsString("\"id\":"),
                     containsString("hello"), 
                     containsString("att"), 
                     containsString("\"attempts\":"))
               .when()
               .post("/games/game");
              
       String responseLocation = resp.getHeader("Location");
       System.out.println("Location: "+responseLocation);
       
       assertTrue(!responseLocation.isEmpty());       
    }
    
    
    
    @Test
    public void shouldReturnWithErrorStatausCodeWhilePostingWrongContent() {
        given()
            .request()
            .contentType(MediaType.APPLICATION_JSON)
            .body("worngJsonContent")
            .then()
            .expect()
            .statusCode(400)
            .post("/games/game");
    }
    
}
