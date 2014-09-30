/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.hmgame.resources;

import com.jayway.restassured.RestAssured;

/**
 *
 * @author marc
 */
public class RestAssuredTestBase {
    
    
    public void setUpRestAssuredHostWS() {
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/hangmangame/rest";
    }
    
}
