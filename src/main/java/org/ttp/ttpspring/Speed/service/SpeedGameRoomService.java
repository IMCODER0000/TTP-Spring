package org.ttp.ttpspring.Speed.service;

import org.springframework.stereotype.Service;
import org.ttp.ttpspring.Liar.model.Player;
import org.ttp.ttpspring.Speed.model.SpeedGameRoom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SpeedGameRoomService {
    private final Map<String, SpeedGameRoom> gameRooms = new ConcurrentHashMap<>();

    public SpeedGameRoom createRoom(String hostName, int maxPlayers, String password, String browserId) {
        SpeedGameRoom room = new SpeedGameRoom(hostName, maxPlayers, password);
        Map<String, String> playerInfo = room.getPlayerInfo();
        playerInfo.put(hostName, browserId);
        System.out.println("룸 메이크 정보 : " + playerInfo.toString());
        System.out.println("브라우저 아이디 : " + browserId);
        gameRooms.put(room.getRoomId(), room);
        return room;
    }

    public SpeedGameRoom getRoom(String roomId) {
        return gameRooms.get(roomId);
    }

    public List<SpeedGameRoom> getAllRooms() {
        return new ArrayList<>(gameRooms.values());
    }

    public boolean joinRoom(String roomId, String nickname, String browserId) {
        SpeedGameRoom room = gameRooms.get(roomId);
        if (room == null || room.isFull()) {
            return false;
        }
        Map<String, String> playerInfo = room.getPlayerInfo();
        playerInfo.put(nickname, browserId);
        return room.addPlayer(Player.builder()
                .nickname(nickname)
                .score(0)
                .build());
    }

    public boolean addPlayerToRoom(String roomId, String nickname) {
        SpeedGameRoom room = gameRooms.get(roomId);
        if (room != null) {
            return room.addPlayer(Player.builder()
                .nickname(nickname)
                .score(0)
                .build());
        }
        return false;
    }

    public String leaveRoom(String roomId, String nickname) {
        SpeedGameRoom room = gameRooms.get(roomId);
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
            room.removePlayerInfo(nickname);
        }


        // 방장이 나가면 다음 사람에게 방장 위임
        if (nickname.equals(room.getHostName()) && !room.getPlayers().isEmpty()) {
            room.setHostName(room.getPlayers().iterator().next().getNickname());
        }

        return "Leave Player";
    }
}
