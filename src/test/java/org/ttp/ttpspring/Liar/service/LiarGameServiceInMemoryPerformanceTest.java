package org.ttp.ttpspring.Liar.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.ttp.ttpspring.Liar.model.LiarGame;
import org.ttp.ttpspring.Liar.model.Player;
import org.ttp.ttpspring.TtpSpringApplication;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@SpringBootTest(classes = TtpSpringApplication.class)
@ActiveProfiles("test")
public class LiarGameServiceInMemoryPerformanceTest {

    @Autowired
    private LiarGameService gameService;

    private static final int TOTAL_GAMES = 100;
    private static final int PLAYERS_PER_GAME = 6;
    private static final int WARMUP_ITERATIONS = 5;
    private static final int TIMEOUT_SECONDS = 10;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== InMemory Performance Test Configuration ===");
        System.out.println(String.format("Total Games: %d", TOTAL_GAMES));
        System.out.println(String.format("Players per Game: %d", PLAYERS_PER_GAME));
        System.out.println(String.format("Total Players: %d", TOTAL_GAMES * PLAYERS_PER_GAME));
        System.out.println(String.format("Warmup Iterations: %d", WARMUP_ITERATIONS));
        System.out.println("");

        try {
            // JVM 웜업
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                String gameId = createTestGame("warmup" + i);
                if (gameId != null) {
                    System.out.println(String.format("Warmup game %d created successfully", i));
                }
            }
            
            System.gc();
            Thread.sleep(1000); // GC 완료 대기
            System.out.println("Warmup completed successfully\n");
            
        } catch (Exception e) {
            System.err.println(String.format("Setup failed: %s", e.getMessage()));
            throw new RuntimeException("Failed to setup test environment", e);
        }
    }

    @Test
    @DisplayName("InMemory 게임 서비스 성능 테스트")
    void performanceTest() throws InterruptedException {
        // 1. 메모리 사용량 테스트
        long initialMemory = getUsedMemory();
        List<String> gameIds = new ArrayList<>();
        List<Long> gameCreationTimes = new ArrayList<>();
        
        // 2. 게임 생성 성능 테스트
        System.out.println("Starting game creation performance test...");
        long totalStartTime = System.nanoTime();
        
        for (int i = 0; i < TOTAL_GAMES; i++) {
            long startTime = System.nanoTime();
            String gameId = createTestGame("game" + i);
            long endTime = System.nanoTime();
            
            if (gameId != null) {
                gameIds.add(gameId);
                gameCreationTimes.add((endTime - startTime) / 1_000_000);
            }
            
            if (i > 0 && i % 10 == 0) {
                System.out.println(String.format("Created %d games...", i));
            }
        }
        
        long totalEndTime = System.nanoTime();
        double totalCreationTime = (totalEndTime - totalStartTime) / 1_000_000.0;
        System.out.println(String.format("Total game creation time: %.2f ms", totalCreationTime));

        // 3. 동시 접속 성능 테스트
        System.out.println("\nStarting concurrent player joining test...");
        List<CompletableFuture<Long>> joinFutures = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(PLAYERS_PER_GAME * TOTAL_GAMES);

        for (String gameId : gameIds) {
            for (int j = 0; j < PLAYERS_PER_GAME; j++) {
                final int playerIndex = j;
                CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                    long startTime = System.nanoTime();
                    try {
                        Set<Player> players = new HashSet<>();
                        players.add(createPlayer(gameId, playerIndex));
                        gameService.createLiarGameAsync(players, gameId).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        return (System.nanoTime() - startTime) / 1_000_000;
                    } catch (TimeoutException e) {
                        System.err.println(String.format("Timeout while joining game %s: %s", gameId, e.getMessage()));
                        return -1L;
                    } catch (Exception e) {
                        System.err.println(String.format("Error joining game %s: %s", gameId, e.getMessage()));
                        return -1L;
                    }
                }, executorService);
                joinFutures.add(future);
            }
        }

        List<Long> joinTimes = new ArrayList<>();
        for (CompletableFuture<Long> future : joinFutures) {
            try {
                Long time = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (time > 0) {
                    joinTimes.add(time);
                }
            } catch (TimeoutException e) {
                System.err.println("Timeout while getting join time: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error getting join time: " + e.getMessage());
            }
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // 최종 메모리 측정
        System.gc();
        Thread.sleep(1000); // GC 완료 대기
        long finalMemory = getUsedMemory();

        // 결과 출력
        printResults(initialMemory, finalMemory, gameCreationTimes, joinTimes);
    }

    private String createTestGame(String gameId) {
        try {
            LiarGame game = new LiarGame();
            game.setGameId(gameId);
            Set<Player> players = new HashSet<>();
            players.add(createPlayer(gameId, 0));
            game.setPlayers(players);
            gameService.createLiarGameAsync(players, gameId).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return gameId;
        } catch (Exception e) {
            System.err.println(String.format("Error creating test game %s: %s", gameId, e.getMessage()));
            return null;
        }
    }

    private Player createPlayer(String gameId, int index) {
        return Player.builder()
                .nickname(String.format("player_%s_%d", gameId, index))
                .score(0)
                .userId(String.format("user_%s_%d", gameId, index))
                .build();
    }

    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private void printResults(long initialMemory, long finalMemory, List<Long> gameCreationTimes, List<Long> joinTimes) {
        long memoryUsed = finalMemory - initialMemory;
        double avgGameCreationTime = gameCreationTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double avgJoinTime = joinTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long maxGameCreationTime = gameCreationTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long maxJoinTime = joinTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        
        double gameCreationStdDev = calculateStandardDeviation(gameCreationTimes);
        double joinTimeStdDev = calculateStandardDeviation(joinTimes);

        System.out.println("=== Memory Usage (메모리 사용량) ===");
        System.out.println(String.format("Initial Memory (초기 메모리): %d MB", initialMemory / (1024 * 1024)));
        System.out.println(String.format("Final Memory (최종 메모리): %d MB", finalMemory / (1024 * 1024)));
        System.out.println(String.format("Memory Used (사용된 메모리): %d MB", memoryUsed / (1024 * 1024)));
        System.out.println(String.format("Memory per Game (게임당 메모리): %d KB", (memoryUsed / TOTAL_GAMES) / 1024));
        System.out.println(String.format("Total Games Created (생성된 총 게임 수): %d", gameCreationTimes.size()));
        System.out.println();
        
        System.out.println("=== Game Creation Performance (게임 생성 성능) ===");
        System.out.println(String.format("Average Response Time (평균 응답 시간): %.2f ms", avgGameCreationTime));
        System.out.println(String.format("Max Response Time (최대 응답 시간): %d ms", maxGameCreationTime));
        System.out.println(String.format("Standard Deviation (표준 편차): %.2f ms", gameCreationStdDev));
        System.out.println(String.format("Success Rate (성공률): %.2f%%", (gameCreationTimes.size() * 100.0) / TOTAL_GAMES));
        System.out.println(String.format("Total Games Created (생성된 총 게임 수): %d", gameCreationTimes.size()));
        System.out.println();
        
        System.out.println("=== Concurrent Players Joining Performance (동시 접속 성능) ===");
        System.out.println(String.format("Average Response Time (평균 응답 시간): %.2f ms", avgJoinTime));
        System.out.println(String.format("Max Response Time (최대 응답 시간): %d ms", maxJoinTime));
        System.out.println(String.format("Standard Deviation (표준 편차): %.2f ms", joinTimeStdDev));
        System.out.println(String.format("Success Rate (성공률): %.2f%%", (joinTimes.size() * 100.0) / (TOTAL_GAMES * PLAYERS_PER_GAME)));
        System.out.println(String.format("Total Players Joined (참가한 총 플레이어 수): %d", joinTimes.size()));
    }

    private double calculateStandardDeviation(List<Long> values) {
        double mean = values.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double variance = values.stream()
            .mapToDouble(value -> Math.pow(value - mean, 2))
            .average()
            .orElse(0.0);
        return Math.sqrt(variance);
    }

    @AfterEach
    void tearDown() {
        // 인메모리 테스트는 별도의 정리가 필요 없음
    }
}
