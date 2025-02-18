package org.ttp.ttpspring.Liar.repository;

import org.springframework.stereotype.Repository;
import org.ttp.ttpspring.Liar.model.LiarGame;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class LiarGameRepository {
    private final Map<String, LiarGame> games = new ConcurrentHashMap<>();

    public void save(LiarGame game) {
        games.put(game.getGameId(), game);
    }

    public LiarGame findById(String gameId) {
        return games.get(gameId);
    }

    public void deleteById(String gameId) {
        games.remove(gameId);
    }

    public void clear() {
        games.clear();
    }

    public boolean exists(String gameId) {
        return games.containsKey(gameId);
    }
}
