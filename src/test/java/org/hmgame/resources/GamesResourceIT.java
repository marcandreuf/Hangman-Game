/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.hmgame.resources;


import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.response.Response;
import javax.ws.rs.core.MediaType;
import org.codehaus.jettison.json.JSONObject;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author marc
 */
public class GamesResourceIT extends RestAssuredTestBase {
    
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
    public void setUpGamesResourceIt(){
        super.setUpRestAssuredHostWS();
    }
    
    
    @Test
    public void shouldGetAListOfJsonGameObjectsInPlayingState() {    
        given()
            .request()
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonTestObject.toString())
            .then()
            .post("/games/game");        
        
        String resp = expect()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .get("/games/query?state=all")
                .asString();
        
        System.out.println("Resp: "+resp);
        assertTrue(resp.contains("list"));
    }

}
