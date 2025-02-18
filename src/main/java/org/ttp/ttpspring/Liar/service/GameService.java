package org.ttp.ttpspring.Liar.service;

import org.springframework.stereotype.Service;
import org.ttp.ttpspring.Liar.model.GameRoom;
import org.ttp.ttpspring.Liar.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private final Map<String, GameRoom> gameRooms = new ConcurrentHashMap<>();

    public GameRoom createRoom(String hostName, int maxPlayers, String password) {
        GameRoom room = new GameRoom(hostName, maxPlayers, password);
        gameRooms.put(room.getRoomId(), room);
        return room;
    }

    public GameRoom getRoom(String roomId) {
        return gameRooms.get(roomId);
    }

    public List<GameRoom> getAllRooms() {
        return new ArrayList<>(gameRooms.values());
    }

    public boolean joinRoom(String roomId, String nickname) {
        GameRoom room = gameRooms.get(roomId);
        if (room == null || room.isFull()) {
            return false;
        }
        return room.addPlayer(Player.builder()
                .nickname(nickname)
                .score(0)
                .build()
        );
    }

    public boolean addPlayerToRoom(String roomId, String nickname) {
        GameRoom room = gameRooms.get(roomId);
        if (room != null) {
            return room.addPlayer(Player.builder()
                .nickname(nickname)
                .score(0)
                .build());
        }
        return false;
    }

    public String leaveRoom(String roomId, String nickname) {
        GameRoom room = gameRooms.get(roomId);
        if (room == null) {
            return "Not Room";
        }
        System.out.println("방장 이름 : " + room.getHostName());
        System.out.println("닉네임 : " + nickname);
        if(room.getHostName().equals(nickname)){
            gameRooms.remove(roomId);
            return "remove Room";
        }
        else{
            room.removePlayer(nickname);
        }


        // 방장이 나가면 다음 사람에게 방장 위임
        if (nickname.equals(room.getHostName()) && !room.getPlayers().isEmpty()) {
            room.setHostName(room.getPlayers().iterator().next().getNickname());
        }

        return "Leave Player";
    }
}
