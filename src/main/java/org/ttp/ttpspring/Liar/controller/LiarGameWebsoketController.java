package org.ttp.ttpspring.Liar.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.ttp.ttpspring.Liar.model.Chat;
import org.ttp.ttpspring.Liar.model.LiarGame;
import org.ttp.ttpspring.Liar.model.Player;
import org.ttp.ttpspring.Liar.service.GameService;
import org.ttp.ttpspring.Liar.service.LiarGameService;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LiarGameWebsoketController {
    private final GameService gameService;
    private final LiarGameService liarGameService;
    private final SimpMessageSendingOperations messageSendingOperations;

    @MessageMapping("/game.createLiarGame")
    public void createLiarGame(Map<String, Object> payload) {
        String gameId = (String) payload.get("roomId");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> playersData = (List<Map<String, Object>>) payload.get("players");

        log.info("Received create game request - GameID: {}, Players: {}", gameId, playersData);

        if (gameId == null || playersData == null || playersData.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("message", "Invalid request: roomId and players are required");
            messageSendingOperations.convertAndSend("/topic/error", errorResponse);
            return;
        }

        Set<Player> players = playersData.stream()
            .map(playerMap -> Player.builder()
                .nickname((String) playerMap.get("nickname"))
                .score((Integer) playerMap.getOrDefault("score", 0))
                .build())
            .collect(Collectors.toSet());

        liarGameService.createLiarGameAsync(players, gameId)
            .thenAccept(liarGame -> {
                Map<String, Object> response = new HashMap<>();
                response.put("type", "CREATE_LIARGAME");
                response.put("data", liarGame);
                messageSendingOperations.convertAndSend("/topic/game/" + gameId, response);
            })
            .exceptionally(throwable -> {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("type", "ERROR");
                errorResponse.put("message", "Failed to create game: " + throwable.getMessage());
                log.error("Failed to create game: {}", throwable.getMessage(), throwable);
                messageSendingOperations.convertAndSend("/topic/game/" + gameId, errorResponse);
                return null;
            });
    }

    @MessageMapping("/game.setLiarGame")
    public void setLiarGame(Map<String, Object> payload) {
        String gameId = (String) payload.get("gameId");
        String nickname = (String) payload.get("nickname");
        Long turnTime = Long.parseLong(payload.get("turnTime").toString());
        Long rounds = Long.parseLong(payload.get("rounds").toString());
        String category = (String) payload.get("category");

        liarGameService.getLiarGameById(gameId)
            .flatMap(liarGame -> Mono.fromFuture(liarGameService.setLiarGameAsync(liarGame, turnTime, rounds, category)))
            .subscribe(liarGame -> {
                Map<String, Object> response = new HashMap<>();
                response.put("type", "SET_GAME");
                response.put("data", liarGame);
                messageSendingOperations.convertAndSend("/topic/game/" + gameId, response);
            }, throwable -> {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("type", "ERROR");
                errorResponse.put("message", "Failed to set game: " + throwable.getMessage());
                messageSendingOperations.convertAndSend("/topic/game/" + gameId, errorResponse);
            });
    }

    @MessageMapping("/game.chatGame")
    public void chatGame(Map<String, Object> payload) {
        String gameId = (String) payload.get("gameId");
        String sender = (String) payload.get("sender");
        String content = (String) payload.get("content");

        Chat chat = new Chat(content, gameId, sender);

        Map<String, Object> response = new HashMap<>();
        response.put("type", "CHAT");
        response.put("data", chat);
        messageSendingOperations.convertAndSend("/topic/game/" + gameId, response);
    }

    @MessageMapping("/game.getLiarGame")
    public void getLiarGame(Map<String, Object> payload) {
        String gameId = (String) payload.get("gameId");

        liarGameService.getLiarGameById(gameId)
            .flatMap(liarGame -> Mono.fromFuture(liarGameService.makeLiarAsync(liarGame)))
            .subscribe(liarGame -> {
                Map<String, Object> response = new HashMap<>();
                response.put("type", "GET_GAME");
                response.put("data", liarGame);
                messageSendingOperations.convertAndSend("/topic/game/" + gameId, response);
            }, throwable -> {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("type", "ERROR");
                errorResponse.put("message", "Failed to get game: " + throwable.getMessage());
                messageSendingOperations.convertAndSend("/topic/game/" + gameId, errorResponse);
            });
    }

    @MessageMapping("/game.startGame")
    public void startGame(Map<String, Object> payload) {
        String gameId = (String) payload.get("gameId");

        liarGameService.getLiarGameById(gameId)
            .flatMap(liarGame -> {
                List<Player> playerOrder = new ArrayList<>(liarGame.getPlayers());
                Collections.shuffle(playerOrder);
                return Mono.fromFuture(liarGameService.setPlayerOrderAsync(gameId, playerOrder))
                    .thenReturn(liarGame);
            })
            .subscribe(liarGame -> {
                List<Player> playerOrder = liarGame.getPlayerOrder();
                String currentPlayer = playerOrder.get(0).getNickname();
                Player currentPlayerObj = Player.builder()
                    .nickname(currentPlayer)
                    .build();
                int currentIndex = playerOrder.indexOf(currentPlayerObj);

                Map<String, Object> response = new HashMap<>();
                response.put("type", "GAME_START");
                response.put("data", new HashMap<String, Object>() {{
                    put("currentPlayer", currentPlayer);
                }});
                messageSendingOperations.convertAndSend("/topic/game/" + gameId, response);

                response = new HashMap<>();
                response.put("type", "TURN_UPDATE");
                response.put("data", new HashMap<String, Object>() {{
                    put("currentPlayer", currentPlayer);
                }});
                messageSendingOperations.convertAndSend("/topic/game/" + gameId, response);
            }, throwable -> {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("type", "ERROR");
                errorResponse.put("message", "Failed to start game: " + throwable.getMessage());
                messageSendingOperations.convertAndSend("/topic/game/" + gameId, errorResponse);
            });
    }

    @MessageMapping("/game.submitWord")
    public void submitWord(Map<String, Object> payload) {
        String gameId = (String) payload.get("gameId");
        String word = (String) payload.get("word");
        String currentPlayer = (String) payload.get("currentPlayer");

        liarGameService.getLiarGameById(gameId)
            .flatMap(liarGame -> {
                List<Player> playerOrder = liarGame.getPlayerOrder();
                int currentIndex = playerOrder.indexOf(Player.builder().nickname(currentPlayer).build());;
                String nextPlayer = null;

                if (currentIndex < playerOrder.size() - 1) {
                    nextPlayer = playerOrder.get(currentIndex + 1).getNickname();
                }

                final String finalNextPlayer = nextPlayer;
                return Mono.fromFuture(liarGameService.updateCurrentPlayerAsync(gameId, nextPlayer))
                    .thenReturn(new AbstractMap.SimpleEntry<>(liarGame, finalNextPlayer));
            })
            .subscribe(result -> {
                LiarGame liarGame = result.getKey();
                String nextPlayer = result.getValue();

                Map<String, Object> response = new HashMap<>();
                response.put("type", "SUBMIT_WORD");
                response.put("data", new HashMap<String, Object>() {{
                    put("word", word);
                    put("currentPlayer", currentPlayer);
                    put("nextPlayer", nextPlayer);
                }});
                messageSendingOperations.convertAndSend("/topic/game/" + gameId, response);

                if (nextPlayer == null) {
                    response = new HashMap<>();
                    response.put("type", "END_TURN");
                    messageSendingOperations.convertAndSend("/topic/game/" + gameId, response);
                }
            }, throwable -> {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("type", "ERROR");
                errorResponse.put("message", "Failed to submit word: " + throwable.getMessage());
                messageSendingOperations.convertAndSend("/topic/game/" + gameId, errorResponse);
            });
    }

    @MessageMapping("/game.vote")
    public void vote(Map<String, Object> payload) {
        String gameId = (String) payload.get("gameId");
        String nickname = (String) payload.get("nickname");
        Player votedPlayer = Player.builder()
            .nickname(nickname)
            .build();

        liarGameService.getLiarGameById(gameId)
            .flatMap(liarGame -> Mono.fromFuture(liarGameService.addVoteAsync(votedPlayer, gameId))
                .thenReturn(liarGame))
            .subscribe(liarGame -> {
                List<Player> votes = liarGame.getVote();
                if (votes.size() == liarGame.getPlayers().size()) {
                    Map<Player, Long> voteCount = votes.stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                            player -> player,
                            java.util.stream.Collectors.counting()
                        ));

                    Player mostVotedPlayer = Collections.max(
                        voteCount.entrySet(),
                        Map.Entry.comparingByValue()
                    ).getKey();

                    Map<String, Object> response = new HashMap<>();
                    response.put("type", "VOTE_RESULT");
                    response.put("data", new HashMap<String, Object>() {{
                        put("votedPlayer", mostVotedPlayer);
                        put("isLiar", mostVotedPlayer.equals(liarGame.getLiar()));
                        put("liar", liarGame.getLiar());
                    }});
                    messageSendingOperations.convertAndSend("/topic/game/" + gameId, response);
                } else {
                    Map<String, Object> response = new HashMap<>();
                    response.put("type", "VOTE_UPDATE");
                    response.put("data", new HashMap<String, Object>() {{
                        put("voteCount", votes.size());
                        put("totalCount", liarGame.getPlayers().size());
                    }});
                    messageSendingOperations.convertAndSend("/topic/game/" + gameId, response);
                }
            }, throwable -> {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("type", "ERROR");
                errorResponse.put("message", "Failed to process vote: " + throwable.getMessage());
                messageSendingOperations.convertAndSend("/topic/game/" + gameId, errorResponse);
            });
    }

    @MessageMapping("/game.nextRound")
    public void nextRound(Map<String, Object> payload) {
        String gameId = (String) payload.get("gameId");
        String winner = (String) payload.get("winner");

        liarGameService.getLiarGameById(gameId)
            .flatMap(liarGame -> {
                Set<Player> players = liarGame.getPlayers();
                return Mono.fromFuture(CompletableFuture.allOf(
                    players.stream()
                        .map(player -> {
                            if (winner.equals("Player") && !player.equals(liarGame.getLiar())) {
                                Integer score = liarGame.getDescriptions().getOrDefault(player.getNickname(), 0);
                                return liarGameService.setDescriptionAsync(gameId, player.getNickname(), score + 10);
                            } else if (winner.equals("Liar") && player.equals(liarGame.getLiar())) {
                                Integer score = liarGame.getDescriptions().getOrDefault(player.getNickname(), 0);
                                return liarGameService.setDescriptionAsync(gameId, player.getNickname(), score + 10);
                            }
                            return CompletableFuture.completedFuture(null);
                        })
                        .toArray(CompletableFuture[]::new)
                ))
                .then(Mono.fromFuture(liarGameService.changeLiarAsync(gameId)))
                .thenReturn(liarGame);
            })
            .subscribe(liarGame -> {
                Map<String, Object> response = new HashMap<>();
                response.put("type", "NEXT_ROUND");
                response.put("data", new HashMap<String, Object>() {{
                    put("scores", liarGame.getDescriptions());
                    put("currentRound", liarGame.getCurrentRound() + 1);
                }});
                messageSendingOperations.convertAndSend("/topic/game/" + gameId, response);
            }, throwable -> {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("type", "ERROR");
                errorResponse.put("message", "Failed to start next round: " + throwable.getMessage());
                messageSendingOperations.convertAndSend("/topic/game/" + gameId, errorResponse);
            });
    }

    @MessageMapping("/game.end")
    public void gameEnd(Map<String, Object> payload) {
        String gameId = (String) payload.get("gameId");

        liarGameService.getLiarGameById(gameId)
            .subscribe(liarGame -> {
                Map<String, Integer> scores = liarGame.getDescriptions();
                String winner = Collections.max(
                    scores.entrySet(),
                    Map.Entry.comparingByValue()
                ).getKey();

                Map<String, Object> response = new HashMap<>();
                response.put("type", "GAME_END");
                response.put("data", new HashMap<String, Object>() {{
                    put("scores", scores);
                    put("winner", winner);
                }});
                messageSendingOperations.convertAndSend("/topic/game/" + gameId, response);
            }, throwable -> {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("type", "ERROR");
                errorResponse.put("message", "Failed to end game: " + throwable.getMessage());
                messageSendingOperations.convertAndSend("/topic/game/" + gameId, errorResponse);
            });
    }
}
