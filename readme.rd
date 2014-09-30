Exercise:

The challenge is to build a simple version of a 'hangman' game as a web-app (look & feel is not important).

The app should be built with 'ant' and produce a war file that can be deployed in a tomcat (Done)
The app should use jquery and ajax queries for interaction. (Done)
The app should keep the current game state persistent across server and browser re-starts. (Done)
The app should have a 'management' page that shows a summary of the state of all games that are currently being played (Done)

Add this line to the .htaccess of root server folder.
'AddType text/cache-manifest .appcache'



Solution implemented:

1. Setup project in netbeans 7.4:
	http://www.mulesoft.com/tcat/tomcat-eclipse
	http://www.pretechsol.com/2013/06/java-restful-web-services-simple-example.html#.Ux9E33Vf3AQ
	

2. Implement build.xml ant script. http://ant.apache.org/
    2.1 Add ant task to run junit tests.


3. Implement [Game] Stateless Application. The state of the games is kept in a Json object on the client side.
 This game object is sent to back end to be stored into the DB, but the Web Services do not hold any state at
 any given time.

    3.1 Object that represents the state. Json object serialized.
    3.2 Service to create a new object state with auto increment id from DB.
        3.2.1 New object with auto increment id.
        https://github.com/jankotek/MapDB/blob/master/src/test/java/examples/SQL_Auto_Incremental_Unique_Key.java
        https://github.com/jankotek/MapDB/blob/master/src/test/java/examples/Secondary_Key.java
        Map 1: Map_objects. <Long, Json String / Serialized>
        Map 2: Index state, calculated from the Json value.

        3.2.6 Singleton of MapDB instance of the mapdb file,
                and implement JUnit implement jUnit tests for PersistenceService.
                Create a MapDB instance factory which to get the db instance from.

        3.2.2 Save instance.
        3.2.3 Load instance.
        3.2.4 Store secondary map to index by status.
        3.2.5 Search all objects by state attribute.
        


4. Implement Jersey WS Resource for Games
http://docs.oracle.com/cd/E19776-01/820-4867/6nga7f5o5/index.html

    4.1 POST a Game /hangmangame/games/newGame to create a new game
    4.2 PUT a Game /hangmangame/games/{gameId} to update the game state
    4.2 Get a Game /hangmangame/games/{gameId} to get the game state
    4.3 Get list of Games /hangmangame/games
    4.4 Delete a Game /hangmangame/games/{gameId}


5. Implement html5 UI based on http://www.adobe.com/devnet/html5/articles/hangman-part-1.html
    5.1 Refactor hangman.js to make it stateless and work with Game WS Resources.


6. Store and load game as json string from browser local storage.
   http://www.w3schools.com/html/html5_webstorage.asp




Resources used to implement this exercise:

Back end REST with Jersey
https://jersey.java.net/

Persistence:
Client side using html5 sql 
Server side using nonsql document solution, MapDB.

REST style:
https://restful-api-design.readthedocs.org/en/latest/intro.html

