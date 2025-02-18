package org.ttp.ttpspring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AsyncGameService {
    
    private final Map<String, ReentrantLock> gameLocks = new ConcurrentHashMap<>();
    
    @Async("gameTaskExecutor")
    public CompletableFuture<Boolean> processGameAction(String gameId, String playerId, String action) {
        ReentrantLock gameLock = gameLocks.computeIfAbsent(gameId, k -> new ReentrantLock());
        
        try {
            // 데드락 방지를 위한 tryLock 사용
            if (!gameLock.tryLock(5, java.util.concurrent.TimeUnit.SECONDS)) {
                log.warn("Failed to acquire lock for game: {}", gameId);
                return CompletableFuture.completedFuture(false);
            }
            
            try {
                // 게임 로직 처리
                return CompletableFuture.completedFuture(processGameLogic(gameId, playerId, action));
            } finally {
                gameLock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Game processing interrupted: {}", gameId, e);
            return CompletableFuture.completedFuture(false);
        }
    }
    
    private boolean processGameLogic(String gameId, String playerId, String action) {
        // 실제 게임 로직 구현
        return true;
    }
    
    @Async("gameTaskExecutor")
    public CompletableFuture<Void> cleanupGame(String gameId) {
        gameLocks.remove(gameId);
        return CompletableFuture.completedFuture(null);
    }
}
