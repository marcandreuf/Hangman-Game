package org.hmgame.persistence;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author marcandreuf
 */
public class PersistenceServiceTest {
    
    private PersistenceService persistenceService;
    private final String jsonString1 = 
            "{\"id\":\"1\",\"wordToGuess\":\"salubrious\",\"attempts\":"
            +"\"sa\",\"state\":\"new\",\"score\":{\"win\":1,\"lost\":1}}";
    private final String jsonString2 = 
           "{\"id\":\"2\",\"wordToGuess\":\"mendacity\",\"attempts\":"
           +"\"dacy\",\"state\":\"playing\",\"score\":{\"win\":1,\"lost\":1}}";
    private final String jsonString3 = 
           "{\"id\":\"3\",\"wordToGuess\":\"pernicious\",\"attempts\":"
           +"\"peio\",\"state\":\"playing\",\"score\":{\"win\":1,\"lost\":1}}";
    
    @Before
    public void setUpPersistenceService() throws Exception{
       persistenceService = ConcurrentPersistenceService.getDBInstance();
    }
    
    
    @Test
    public void shouldCreateASingletonInstanceOfTheDB() 
    throws InterruptedException{
        PersistenceService instance1 = ConcurrentPersistenceService.getDBInstance();
        ThreadLocalRandom thread = ThreadLocalRandom.current();
        Thread.sleep(thread.nextInt(1, 5)*10);
        PersistenceService instance2 = ConcurrentPersistenceService.getDBInstance();
        PersistenceService instance3 = ConcurrentPersistenceService.getDBInstance();
        assertTrue(instance1 == instance2 && instance2 == instance3);
    }
    
    
    @Test
    public void shouldSaveAGivenObjecAndReturnAnAutoIncrementalLongValue() 
    throws Exception {
        Long keyId = 0L;
        keyId = persistenceService.save(jsonString1);       
        assertTrue(keyId > 0L);        
    }
    
    @Test
    public void souldSaveAndLoadAGivenValueByKeyId() throws Exception {
        String testValue = jsonString2;
        Long keyId = persistenceService.save(testValue);
        String loadedValue = persistenceService.load(keyId);
        assertTrue(loadedValue != null && loadedValue.equals(testValue));
    }       
    
    @Test
    public void shouldSaveTheGivenObjectsAndIndexThemForSearchByState() 
    throws Exception {        
        persistenceService.save(jsonString1);
        persistenceService.save(jsonString2);
        persistenceService.save(jsonString3);
        
        List<String> lstGames = persistenceService.queryByState("playing");               
        assertTrue(lstGames.size() == 2);
        
        lstGames = persistenceService.queryByState("all");               
        assertTrue(lstGames.size() == 3);
    }
    
    
    @Test 
    public void shouldUpdateAnExistingObjectByIndex() throws Exception {        
        String simpleContent = "{\"id\":\"\"}";
        Long keyId = persistenceService.save(simpleContent);
        
        simpleContent = "{\"id\":\"0001\"}";
        persistenceService.update(keyId,simpleContent);
        
        String storedValue = persistenceService.load(keyId);
        
        assertTrue(storedValue.contains("\"id\":\"0001\""));
    }
    
    
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    
    @Test
    public void shouldSaveANonExistingObjectWhileDoingAnUpdateByIndex(){
        expectedEx.expect(IndexOutOfBoundsException.class);
        expectedEx.expectMessage("Key id not found in the DB.");
        Long nonExistingKey = 10L;
        persistenceService.update(nonExistingKey,jsonString1);
    }
    
    
    @Test
    public void shouldRemoveAllContentFromDB(){
        persistenceService.save(jsonString1);
        
        List<String> lstGames = persistenceService.queryByState("all");
        assertTrue(lstGames != null && lstGames.size() > 0);
        
        persistenceService.resetDB();
        
        lstGames = persistenceService.queryByState("all");
        assertTrue(lstGames != null && lstGames.isEmpty());
    }
     
    
    
    @After
    public void removeAllContenctFromDB() throws Exception {
        persistenceService.resetDB();
        Thread.sleep(1000);
    }
    
}
