package org.ttp.ttpspring.Speed.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.ttp.ttpspring.Liar.model.Chat;
import org.ttp.ttpspring.Speed.model.SpeedGameRoom;
import org.ttp.ttpspring.Speed.service.SpeedGameRoomService;

import java.util.HashMap;
import java.util.Map;

@Controller("SpeedGameWebsoketController")
@RequiredArgsConstructor
public class SpeedGameWebsoketController {

    private final SpeedGameRoomService speedGameRoomService;
    private final SimpMessageSendingOperations messagingTemplate;




    @MessageMapping("/speed_game.getRoom")
    public void getRoom(Map<String, Object> payload) {
        String hostName = (String) payload.get("hostName");
        String roomId = (String) payload.get("roomId");

        // hostName이 null인 경우 기본값 설정
        if (hostName == null) {
            hostName = "defaultHostName"; // null일 경우 기본값 설정
        }



        SpeedGameRoom findRoom = speedGameRoomService.getRoom(roomId);

        // null을 방지하기 위해 비교하기 전에 null 체크
        boolean isHost = false;

        if (findRoom.getHostName() != null) {
            isHost = findRoom.getHostName().equals(hostName);
        }


        Map<String, Object> response = new HashMap<>();

        response.put("type", isHost ? "IS_HOST" : "GET_ROOM");
        response.put("data", findRoom);



        messagingTemplate.convertAndSend("/topic/speed_game/" + roomId, response);
    }


    @MessageMapping("/speed_game.leaveRoom")
    public void leaveRoom(Map<String, Object> payload) {
        String roomId = (String) payload.get("roomId");
        String nickname = (String) payload.get("nickname");

        String result = speedGameRoomService.leaveRoom(roomId, nickname);
        SpeedGameRoom room = speedGameRoomService.getRoom(roomId);
        Map<String, Object> response = new HashMap<>();
        if (result.equals("remove Room")) {
            response.put("type", "PLAYER_LEFT");
            response.put("data", "remove Room");


        } else if(result.equals("Leave Player")){

            response.put("type", "PLAYER_LEFT");
            response.put("data", room);
            response.put("user", nickname);

        }
        else {
            response.put("type", "ERROR");
            Map<String, String> errorData = new HashMap<>();
            errorData.put("message", "방을 나가는데 실패했습니다.");
            response.put("data", errorData);
        }

        messagingTemplate.convertAndSend("/topic/speed_game/" + roomId, response);
    }

    @MessageMapping("/speed_game.chat")
    public void chat(Map<String, Object> payload) {
        String roomId = (String) payload.get("roomId");
        String sender = (String) payload.get("sender");
        String content = (String) payload.get("content");


        Chat chat = new Chat(content,roomId,sender);

        Map<String, Object> response = new HashMap<>();

        response.put("type", "CHAT");
        response.put("data", chat);



        messagingTemplate.convertAndSend("/topic/speed_game/" + roomId, response);
    }





}
