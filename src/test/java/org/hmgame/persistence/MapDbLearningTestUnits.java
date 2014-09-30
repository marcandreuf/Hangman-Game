package org.hmgame.persistence;


import java.io.File;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.mapdb.Atomic;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;

/**
 *
 * @author marcandreuf
 */
public class MapDbLearningTestUnits {    
    
    private final String dbPath = System.getProperty("user.home")
                                +System.getProperty("file.separator")+"mapdb";
    
    @Test
    @Ignore
    public void shouldSaveAValueWithAutoIncrementId() throws Exception {
        String valueToSave = "{\"name\":\"this is a sample json string for testing.\"}";        
        
        //File dbFile = File.createTempFile("mapdb","db");
        File mapdbFolder = new File(dbPath);
        FileUtils.forceMkdir(mapdbFolder);
        File dbFile = new File(dbPath+System.getProperty("file.separator")+"mapdb.db");
        DB db = DBMaker.newFileDB(dbFile)
                .closeOnJvmShutdown()
                .make();

        BTreeMap<Long, String> primaryMap = db.getTreeMap("primaryMap");
        Atomic.Long primaryMapKeyInc = db.getAtomicLong("primaryMap_keyinc");
        Long autoIncKey = primaryMapKeyInc.incrementAndGet();        
        primaryMap.put(autoIncKey, valueToSave);
        System.out.println("Saved with key: "+autoIncKey);
        
        db.commit();
        db.close();        
    }

    @Test
    @Ignore
    public void shouldLoadTheStoredValueGivenTheAutoIncKey()throws Exception {
        
        File dbFile = new File(dbPath+System.getProperty("file.separator")+"mapdb.db");
        DB db = DBMaker.newFileDB(dbFile)
                .closeOnJvmShutdown()
                .make();
        BTreeMap<Long, String> primaryMap = db.getTreeMap("primaryMap");
        
        String value = primaryMap.get(1L);
        
        System.out.println("Loaded value: "+value);        
        
    }
    
    
    
    @Test
    @Ignore
    public void shouldBindASecondaryMapToIndexTheStateOfPrimaryMap() throws Exception {
        
        File dbFile = new File(dbPath+System.getProperty("file.separator")+"mapdb.db");
        DB db = DBMaker.newFileDB(dbFile)
                .closeOnJvmShutdown()
                .make();     
        
        BTreeMap<Long, String> primary = db.getTreeMap("primaryMap");
        

        NavigableSet<Fun.Tuple2<Integer,Long>> indexedMap =
                new TreeSet<Fun.Tuple2<Integer,Long>>();


        Bind.secondaryKey(primary, indexedMap, new Fun.Function2<Integer, Long, String>() {
            @Override
            public Integer run(Long key, String value) {                
                try {
                    JSONObject jsonObj = new JSONObject(value);
                    return jsonObj.getInt("state");
                } catch (JSONException ex) {
                    Logger.getLogger(MapDbLearningTestUnits.class.getName()).log(Level.SEVERE, null, ex);
                }
                return -1;
            }
        });
        
        String jsonString1 = "{\"name\":\"simple test json1\", \"state\":1}";
        Atomic.Long primaryMapKeyInc = db.getAtomicLong("primaryMap_keyinc");
        Long autoIncKey = primaryMapKeyInc.incrementAndGet();        
        primary.put(autoIncKey, jsonString1);
        System.out.println("Saved with key1: "+autoIncKey);
        
        
        String jsonString2 = "{\"name\":\"simple test json2\", \"state\":2}";
        autoIncKey = primaryMapKeyInc.incrementAndGet();        
        primary.put(autoIncKey, jsonString2);
        System.out.println("Saved with key2: "+autoIncKey);
        
        Iterable<Long> ids = Fun.filter(indexedMap, 2);
        System.out.println(ids.iterator().next());
        
    }
 
    
}
