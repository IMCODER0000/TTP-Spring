package org.ttp.ttpspring.Liar.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class GameRoom {
    private String roomId;
    private String hostName;
    private int maxPlayers;
    private Set<Player> players;
    private GameStatus status;
    private String password;

    public GameRoom(String hostName, int maxPlayers, String password) {
        this.roomId = UUID.randomUUID().toString();
        this.players = new HashSet<>();
        this.players.add(Player.builder()
                .nickname(hostName)
                .score(0)
                .build());
        this.status = GameStatus.WAITING;
        this.maxPlayers = maxPlayers;
        this.password = password;

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
}
