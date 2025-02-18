package org.ttp.ttpspring.config;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SessionTracker {
    private final ConcurrentHashMap<String, Long> activeSessions = new ConcurrentHashMap<>();
    private final AtomicInteger activeSessionCount = new AtomicInteger(0);

    public void trackSession(String sessionId) {
        activeSessions.put(sessionId, System.currentTimeMillis());
        activeSessionCount.incrementAndGet();
    }

    public void removeSession(String sessionId) {
        if (activeSessions.remove(sessionId) != null) {
            activeSessionCount.decrementAndGet();
        }
    }

    public int getActiveSessionCount() {
        return activeSessionCount.get();
    }

    public boolean isSessionActive(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    public void cleanupExpiredSessions(long expirationTimeMillis) {
        long currentTime = System.currentTimeMillis();
        activeSessions.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() > expirationTimeMillis) {
                activeSessionCount.decrementAndGet();
                return true;
            }
            return false;
        });
    }
}
