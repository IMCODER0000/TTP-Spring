package org.ttp.ttpspring.Liar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.ttp.ttpspring.Liar.model.Chat;
import org.ttp.ttpspring.Liar.model.GameRoom;
import org.ttp.ttpspring.Liar.service.GameService;

import java.util.HashMap;
import java.util.Map;

@Controller("GameWebsoketController")
@RequiredArgsConstructor
public class GameWebsoketController {

    private final GameService gameService;
    private final SimpMessageSendingOperations messagingTemplate;




    @MessageMapping("/game.getRoom")
    public void getRoom(Map<String, Object> payload) {
        String hostName = (String) payload.get("hostName");
        String roomId = (String) payload.get("roomId");

        // hostName이 null인 경우 기본값 설정
        if (hostName == null) {
            hostName = "defaultHostName"; // null일 경우 기본값 설정
        }



        GameRoom findRoom = gameService.getRoom(roomId);

        // null을 방지하기 위해 비교하기 전에 null 체크
        boolean isHost = false;

        if (findRoom.getHostName() != null) {
            isHost = findRoom.getHostName().equals(hostName);
        }


        Map<String, Object> response = new HashMap<>();

        response.put("type", isHost ? "IS_HOST" : "GET_ROOM");
        response.put("data", findRoom);



        messagingTemplate.convertAndSend("/topic/game/" + roomId, response);
    }


    @MessageMapping("/game.login")
    public void login(Map<String, Object> payload) {
        String password = (String)  payload.get("password");
        String roomId = (String) payload.get("roomId");


        GameRoom findRoom = gameService.getRoom(roomId);
        boolean isLogin = findRoom.getPassword().equals(password);



        Map<String, Object> response = new HashMap<>();
        if (isLogin) {
            response.put("type", "IS_LOGIN");
            response.put("data", isLogin);
        }
        else{
            response.put("type", "IS_LOGIN");
            response.put("data", "not");
        }



        messagingTemplate.convertAndSend("/topic/game/" + roomId, response);
    }

    @MessageMapping("/game.join")
    public void joinRoom(Map<String, Object> payload) {
        String roomId = (String)  payload.get("roomId");
        String nickname = (String) payload.get("nickname");



        boolean joined = gameService.joinRoom(roomId, nickname);



        Map<String, Object> response = new HashMap<>();
        if (joined) {
            response.put("type", "PLAYER_JOINED");
            response.put("data", gameService.getRoom(roomId));
        } else {
            response.put("type", "ERROR");
            Map<String, String> errorData = new HashMap<>();
            errorData.put("message", "방 입장에 실패했습니다.");
            response.put("data", errorData);
        }

        messagingTemplate.convertAndSend("/topic/game/" + roomId, response);
    }

    @MessageMapping("/game.leaveRoom")
    public void leaveRoom(Map<String, Object> payload) {
        String roomId = (String) payload.get("roomId");
        String nickname = (String) payload.get("nickname");

        String result = gameService.leaveRoom(roomId, nickname);
        GameRoom room = gameService.getRoom(roomId);
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

        messagingTemplate.convertAndSend("/topic/game/" + roomId, response);
    }

    @MessageMapping("/game.chat")
    public void chat(Map<String, Object> payload) {
        String roomId = (String) payload.get("roomId");
        String sender = (String) payload.get("sender");
        String content = (String) payload.get("content");


        Chat chat = new Chat(content,roomId,sender);

        Map<String, Object> response = new HashMap<>();

        response.put("type", "CHAT");
        response.put("data", chat);



        messagingTemplate.convertAndSend("/topic/game/" + roomId, response);
    }

}
