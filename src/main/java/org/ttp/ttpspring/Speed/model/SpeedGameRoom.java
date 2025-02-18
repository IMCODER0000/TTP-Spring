package org.ttp.ttpspring.Speed.model;

import lombok.Getter;
import lombok.Setter;
import org.ttp.ttpspring.Liar.model.GameStatus;
import org.ttp.ttpspring.Liar.model.Player;

import java.util.*;

@Getter
@Setter
public class SpeedGameRoom {
    private String roomId;
    private String hostName;
    private int maxPlayers;
    private Set<Player> players;
    private Map<String, String> playerInfo;
    private GameStatus status;
    private String password;

    public SpeedGameRoom(String hostName, int maxPlayers, String password) {
        this.roomId = UUID.randomUUID().toString();
        this.players = new HashSet<>();
        this.players.add(Player.builder()
            .nickname(hostName)
            .score(0)
            .build());
        this.status = GameStatus.WAITING;
        this.maxPlayers = maxPlayers;
        this.password = password;
        this.playerInfo = new HashMap<>();
    }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    public boolean addPlayer(Player player) {
        if (isFull()) {
            return false;
        }
        return players.add(player);
    }

    public boolean removePlayer(String nickname) {
        return players.removeIf(player -> player.getNickname().equals(nickname));
    }

    public String removePlayerInfo(String nickname) {
        return playerInfo.remove(nickname);
    }
}
