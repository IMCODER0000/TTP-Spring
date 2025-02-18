package org.ttp.ttpspring.Liar.model;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.*;

@Getter
@Setter
public class LiarGame implements Serializable {
    private String gameId;
    private Set<Player> players;
    private Long round;
    private List<Player> vote;
    private Player liar;
    private String liarNickname;
    private int currentTurn;
    private int currentRound;
    private Long turnTime;
    private Long rounds;
    private String category;
    private List<String> keywords;
    private String roomId;
    private Set<String> readyPlayers;
    private List<Player> playerOrder;
    private String currentPlayerIndex;
    private Map<String, Integer> descriptions;
    private int readyStatus;
    private String status;
    private String keyword;
    private Map<String, Integer> voteResults;
    private Set<String> votedPlayers;

    public LiarGame() {
        this.gameId = UUID.randomUUID().toString();
        this.players = new HashSet<>();
        this.round = 0L;
        this.vote = new ArrayList<>();
        this.keywords = new ArrayList<>(Arrays.asList("가","나","다"));
        this.playerOrder = new ArrayList<>();
        this.currentPlayerIndex = "";
        this.descriptions = new HashMap<>();
        this.readyPlayers = new HashSet<>();
        this.currentRound = 1;
        this.currentTurn = 1;
        this.readyStatus = 0;
        this.voteResults = new HashMap<>();
        this.votedPlayers = new HashSet<>();
        this.status = "WAITING";
    }

    public void addReadyPlayer(String nickname) {
        if (readyPlayers == null) {
            readyPlayers = new HashSet<>();
        }
        readyPlayers.add(nickname);
    }

    public Set<String> getReadyPlayers() {
        return readyPlayers != null ? readyPlayers : new HashSet<>();
    }

    public void clearReadyPlayers() {
        if (readyPlayers != null) {
            readyPlayers.clear();
        }
    }

    public void setLiar(Player liar) {
        this.liar = liar;
        this.liarNickname = liar != null ? liar.getNickname() : null;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
        this.roomId = gameId; // roomId도 gameId와 동일하게 설정
    }

    public String getGameId() {
        return this.gameId;
    }
}
