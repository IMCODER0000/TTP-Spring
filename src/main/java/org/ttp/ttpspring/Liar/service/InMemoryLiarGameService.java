package org.ttp.ttpspring.Liar.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.ttp.ttpspring.Liar.model.CategoryContent;
import org.ttp.ttpspring.Liar.model.LiarGame;
import org.ttp.ttpspring.Liar.model.Player;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service("inMemoryGameService")
public class InMemoryLiarGameService implements GameStorageService {
    private final Cache<String, LiarGame> gameCache;
    private final ExecutorService gameExecutor;

    public InMemoryLiarGameService() {
        this.gameCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(1000)
                .build();
        this.gameExecutor = Executors.newWorkStealingPool();
    }

    @Async("gameTaskExecutor")
    public CompletableFuture<List<String>> getRandomItemsAsync(List<String> list, int rounds) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> copyList = new ArrayList<>(list);
            Collections.shuffle(copyList);
            return copyList.subList(0, Math.min(rounds, copyList.size()));
        }, gameExecutor);
    }

    @Override
    @Async("gameTaskExecutor")
    public CompletableFuture<LiarGame> createLiarGameAsync(Set<Player> players, String gameId) {
        return CompletableFuture.supplyAsync(() -> {
            LiarGame liarGame = new LiarGame();
            liarGame.setGameId(gameId);
            liarGame.setPlayers(players);
            liarGame.setStatus("WAITING");
            liarGame.setKeywords(Arrays.asList("사과", "바나나", "딸기", "포도", "수박"));
            gameCache.put(gameId, liarGame);
            return liarGame;
        }, gameExecutor);
    }

    @Override
    @Async("gameTaskExecutor")
    public CompletableFuture<LiarGame> makeLiarAsync(LiarGame liarGame) {
        return CompletableFuture.supplyAsync(() -> {
            Random random = new Random();
            List<Player> players = new ArrayList<>(liarGame.getPlayers());
            int liarIndex = random.nextInt(players.size());
            liarGame.setLiar(players.get(liarIndex));
            
            List<String> keywords = liarGame.getKeywords();
            liarGame.setKeyword(keywords.get(random.nextInt(keywords.size())));
            
            gameCache.put(liarGame.getGameId(), liarGame);
            return liarGame;
        }, gameExecutor);
    }

    @Override
    @Async("gameTaskExecutor")
    public CompletableFuture<LiarGame> setLiarGameAsync(LiarGame liarGame, Long turnTime, Long rounds, String category) {
        return CompletableFuture.supplyAsync(() -> {
            CategoryContent categoryContent = new CategoryContent();
            CompletableFuture<List<String>> keywordsFuture;

            switch (category) {
                case "음식":
                    keywordsFuture = getRandomItemsAsync(categoryContent.getFood(), Math.toIntExact(rounds));
                    break;
                case "직업":
                    keywordsFuture = getRandomItemsAsync(categoryContent.getJob(), Math.toIntExact(rounds));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid category: " + category);
            }

            liarGame.setKeywords(keywordsFuture.join());
            liarGame.setCategory(category);
            liarGame.setTurnTime(turnTime);
            liarGame.setRound(rounds);
            
            gameCache.put(liarGame.getGameId(), liarGame);
            return liarGame;
        }, gameExecutor);
    }

    @Override
    public Mono<LiarGame> getLiarGameById(String liarGameId) {
        return Mono.fromCallable(() -> 
            Optional.ofNullable(gameCache.getIfPresent(liarGameId)).orElse(null)
        ).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<LiarGame> getLiarGameByRoomId(String roomId) {
        return Mono.fromCallable(() -> 
            gameCache.asMap().values().stream()
                .filter(game -> game.getGameId().equals(roomId))
                .findFirst()
                .orElse(null)
        ).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @Async("gameTaskExecutor")
    public CompletableFuture<Void> setPlayerOrderAsync(String gameId, List<Player> playerOrder) {
        return CompletableFuture.runAsync(() -> {
            LiarGame game = gameCache.getIfPresent(gameId);
            if (game != null) {
                game.setPlayerOrder(playerOrder);
                gameCache.put(gameId, game);
            }
        }, gameExecutor);
    }

    @Override
    @Async("gameTaskExecutor")
    public CompletableFuture<Void> setDescriptionAsync(String gameId, String player, Integer score) {
        return CompletableFuture.runAsync(() -> {
            LiarGame game = gameCache.getIfPresent(gameId);
            if (game != null) {
                game.getDescriptions().put(player, score);
                gameCache.put(gameId, game);
            }
        }, gameExecutor);
    }

    @Override
    @Async("gameTaskExecutor")
    public CompletableFuture<Void> updateCurrentPlayerAsync(String gameId, String nextPlayer) {
        return CompletableFuture.runAsync(() -> {
            LiarGame game = gameCache.getIfPresent(gameId);
            if (game != null) {
                game.setCurrentPlayerIndex(nextPlayer);
                gameCache.put(gameId, game);
            }
        }, gameExecutor);
    }

    @Override
    @Async("gameTaskExecutor")
    public CompletableFuture<Boolean> removePlayerAsync(String gameId, String nickname) {
        return CompletableFuture.supplyAsync(() -> {
            LiarGame game = gameCache.getIfPresent(gameId);
            if (game != null) {
                boolean removed = game.getPlayers().removeIf(player -> 
                    player.getNickname().equals(nickname));
                if (removed) {
                    if (game.getPlayers().isEmpty()) {
                        gameCache.invalidate(gameId);
                    } else {
                        gameCache.put(gameId, game);
                    }
                }
                return removed;
            }
            return false;
        }, gameExecutor);
    }

    @Override
    @Async("gameTaskExecutor")
    public CompletableFuture<Boolean> removeGameAsync(String gameId) {
        return CompletableFuture.supplyAsync(() -> {
            if (gameCache.getIfPresent(gameId) != null) {
                gameCache.invalidate(gameId);
                return true;
            }
            return false;
        }, gameExecutor);
    }

    @Override
    @Async("gameTaskExecutor")
    public CompletableFuture<Void> addVoteAsync(Player player, String gameId) {
        return CompletableFuture.runAsync(() -> {
            LiarGame game = gameCache.getIfPresent(gameId);
            if (game != null) {
                game.getVote().add(player);
                gameCache.put(gameId, game);
            }
        }, gameExecutor);
    }

    @Override
    @Async("gameTaskExecutor")
    public CompletableFuture<Void> changeLiarAsync(String gameId) {
        return CompletableFuture.runAsync(() -> {
            LiarGame game = gameCache.getIfPresent(gameId);
            if (game != null) {
                Set<Player> players = game.getPlayers();
                List<Player> playerList = new ArrayList<>(players);
                Collections.shuffle(playerList);
                game.setLiar(playerList.get(0));
                gameCache.put(gameId, game);
            }
        }, gameExecutor);
    }

    public LiarGame findGameById(String gameId) {
        return gameCache.getIfPresent(gameId);
    }

    @Async("gameTaskExecutor")
    public CompletableFuture<LiarGame> updateGame(LiarGame game) {
        return CompletableFuture.supplyAsync(() -> {
            gameCache.put(game.getGameId(), game);
            return game;
        }, gameExecutor);
    }

    @Override
    public int getActiveGamesCount() {
        return (int) gameCache.asMap().values().stream()
            .filter(game -> !game.getStatus().equals("FINISHED"))
            .count();
    }

    @Override
    public int getTotalPlayersCount() {
        return gameCache.asMap().values().stream()
            .mapToInt(game -> game.getPlayers().size())
            .sum();
    }
}
