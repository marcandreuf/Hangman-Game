package org.hmgame.persistence;

import java.util.List;

/**
 * 
 * @author marc
 */
public interface PersistenceService {
    
    public String load(Long keyId);
    public Long save(String objectToStore);
    public void update(long keyId, String content);
    public List<String> queryByState(String state);
    public void resetDB();
    
}
