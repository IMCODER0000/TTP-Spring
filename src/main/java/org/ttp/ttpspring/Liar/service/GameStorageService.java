package org.ttp.ttpspring.Liar.service;

import org.ttp.ttpspring.Liar.model.LiarGame;
import org.ttp.ttpspring.Liar.model.Player;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface GameStorageService {
    CompletableFuture<LiarGame> createLiarGameAsync(Set<Player> players, String gameId);
    CompletableFuture<LiarGame> makeLiarAsync(LiarGame liarGame);
    CompletableFuture<LiarGame> setLiarGameAsync(LiarGame liarGame, Long turnTime, Long rounds, String category);
    CompletableFuture<Void> setPlayerOrderAsync(String gameId, List<Player> playerOrder);
    CompletableFuture<Void> setDescriptionAsync(String gameId, String player, Integer score);
    CompletableFuture<Void> updateCurrentPlayerAsync(String gameId, String nextPlayer);
    CompletableFuture<Boolean> removePlayerAsync(String gameId, String nickname);
    CompletableFuture<Boolean> removeGameAsync(String gameId);
    CompletableFuture<Void> addVoteAsync(Player player, String gameId);
    CompletableFuture<Void> changeLiarAsync(String gameId);
    Mono<LiarGame> getLiarGameById(String liarGameId);
    Mono<LiarGame> getLiarGameByRoomId(String roomId);
    int getActiveGamesCount();
    int getTotalPlayersCount();
}
