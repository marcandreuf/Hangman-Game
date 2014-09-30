package org.hmgame.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
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
public class ConcurrentPersistenceService implements PersistenceService {
    private static final String MAPDB_FILENAME = "hangmanmapdb.db";
    private static final String PRIMARY_MAP_NAME = "primaryMap";
    private static final String PRIMARY_MAP_KEYINC_NAME = "primaryMap_keyinc";
    private static final Logger logger = 
            Logger.getLogger(ConcurrentPersistenceService.class.getName());
    
    private final DB db;
    private final BTreeMap<Long, String> primaryMap;
    private final Atomic.Long primaryMapKeyInc;
    private final Lock atomicActionLock = new ReentrantLock();
    private final NavigableSet<Fun.Tuple2<Integer,Long>> indexedMap;

    
    private static class DBSingletonHolder {
        private static final PersistenceService INSTANCE = 
                                     new ConcurrentPersistenceService();
    }
    
    public static PersistenceService getDBInstance(){
        return DBSingletonHolder.INSTANCE;
    }  
    
    
    private ConcurrentPersistenceService() {          
        File dbFile = openDbFile();
        this.db = DBMaker.newFileDB(dbFile).closeOnJvmShutdown().make();
        primaryMap = db.getTreeMap(PRIMARY_MAP_NAME);
        primaryMapKeyInc = db.getAtomicLong(PRIMARY_MAP_KEYINC_NAME);        
        indexedMap = new TreeSet<>();
        linkIndexByStateSecondaryMap();
    }
    
    private File openDbFile() {
        logger.log(Level.INFO, "Create DB singleton instance.");
        String dbPath = System.getProperty("java.io.tmpdir");
        File dbFile = new File(dbPath
                +System.getProperty("file.separator")
                +MAPDB_FILENAME);
        logger.log(Level.INFO, "Open db file: {0}", dbFile);
        return dbFile;
    }
    
    private void linkIndexByStateSecondaryMap() {
        Bind.secondaryKey(primaryMap, indexedMap, 
           new Fun.Function2<Integer, Long, String>() {
                @Override
                public Integer run(Long key, String value) {                
                    try {
                        JSONObject jsonObj = new JSONObject(value);
                        String state = jsonObj.getString("state");
                        return getStateValue(state);
                    } catch (JSONException ex) {
                        logger.log(Level.WARNING, 
                                "Object [{0}] does not have state value, "
                                 + "can not be indexed.", value);
                        return -1;
                    }
                }
           });
    }
            
    private int getStateValue(String state){
        switch (state) {
            case "new": return 0;
            case "playing": return 1;
            case "win": return 2;
            case "lost": return 3;
            case "deleted": return 4;
            default: return -1;
        }
    }    
    
    @Override
    public Long save(String objectToStore) {
        Long autoIncKey = 0L;
        autoIncKey = primaryMapKeyInc.incrementAndGet();        
        primaryMap.put(autoIncKey, objectToStore);
        db.commit();
        return autoIncKey;
    }

    @Override
    public String load(Long keyId) {
        return primaryMap.get(keyId);
    }

    @Override
    public List<String> queryByState(String state) {
        List<String> list = new ArrayList<>();
        Iterable<Long> ids;
        atomicActionLock.lock();
        try{
            ids = filterListOfKeyIds(state);
            list = loadObjectsByKeyId(ids);
        }finally{
            atomicActionLock.unlock();
        }
        return list;
    }

    private Iterable<Long> filterListOfKeyIds(String state) {
        Iterable<Long> ids;
        if("all".equals(state)){
            ids = primaryMap.navigableKeySet();
        }else{
            int stateValue = getStateValue(state);
            ids = Fun.filter(indexedMap, stateValue);
        }
        return ids;
    }
    
    private List<String> loadObjectsByKeyId(Iterable<Long> ids) {
        List<String> list = new ArrayList<>();
        for (Long item : ids) {
            list.add(primaryMap.get(item));
        }
        return list;
    }


    @Override
    public void resetDB() {
        atomicActionLock.lock();;
        try{
            clearMaps();
        }finally{
            atomicActionLock.unlock();
        }
    }    

    private void clearMaps() {
        primaryMap.clear();
        indexedMap.clear();
    }
    
    
    @Override
    public void update(long keyId, String content) {
        if(primaryMap.replace(keyId, content) == null){
            throw new IndexOutOfBoundsException("Key id not found in the DB.");
        }
        db.commit();
    }
    
}
