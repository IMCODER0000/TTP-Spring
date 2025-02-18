package org.ttp.ttpspring.Speed.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.ttp.ttpspring.Liar.model.Chat;
import org.ttp.ttpspring.Liar.model.GameRoom;
import org.ttp.ttpspring.Liar.model.LiarGame;
import org.ttp.ttpspring.Liar.model.Player;
import org.ttp.ttpspring.Speed.model.SpeedGameRoom;
import org.ttp.ttpspring.Speed.model.SpeedQuizGame;
import org.ttp.ttpspring.Speed.service.SpeedGameRoomService;
import org.ttp.ttpspring.Speed.service.SpeedQuizGameService;
import org.ttp.ttpspring.Speed.service.SpeedQuizService;

import java.util.*;

@Controller("SpeedQuizWebsoketController")
@RequiredArgsConstructor
public class SpeedQuizWebsoketController {

    private final SimpMessageSendingOperations messageSendingOperations;
    private final SpeedQuizGameService speedQuizGameService;
    private final SpeedGameRoomService speedGameRoomService;
    private final SpeedQuizService speedQuizService;


    @MessageMapping("/speed_game.createSpeedQuizGame")
    public void createLiarGame(Map<String, Object> payload){
        String roomId = (String) payload.get("roomId");

        String category = (String) payload.get("category");
        Integer maxScore = (Integer) payload.get("maxScore");
        Integer quizNum = (Integer) payload.get("quizNum");

        SpeedGameRoom room = speedGameRoomService.getRoom(roomId);
        Set<Player> players = room.getPlayers();
        Map<String, String> playerInfo = room.getPlayerInfo();
        SpeedQuizGame speedQuizGame;

        Map<String, Integer> playerIntegerMap = new HashMap<>();
        for (Player player : players) {
            playerIntegerMap.put(player.getNickname(),0);
        }

        if(quizNum != 0){
            speedQuizGame = speedQuizGameService.createSpeedQuizGameByQuizNum(players, roomId, quizNum, category);
            speedQuizGame.setScore(playerIntegerMap);
            speedQuizGame.setPlayerInfo(playerInfo);
        } else {
            speedQuizGame = speedQuizGameService.createSpeedQuizGameByMaxScore(players, roomId, maxScore, category);
            speedQuizGame.setScore(playerIntegerMap);
            speedQuizGame.setPlayerInfo(playerInfo);
        }








        Map<String, Object> response = new HashMap<>();
        response.put("type", "CREATE_SPEEDQUIZ");
        response.put("data", speedQuizGame);



        messageSendingOperations.convertAndSend("/topic/speed_game/"+roomId,response);


    }


    @MessageMapping("/speed_game.getSpeedQuizGame")
    public void getLiarGame(Map<String, Object> payload){
        String speedGameId = (String) payload.get("SpeedGameId");



        SpeedQuizGame speedQuizGameById = speedQuizGameService.getSpeedQuizGameById(speedGameId);



        Map<String, Object> response = new HashMap<>();
        response.put("type", "GET_GAME");
        response.put("data", speedQuizGameById);


        messageSendingOperations.convertAndSend("/topic/speed_game/"+speedGameId,response);


    }

    @MessageMapping("/speed_game.submit")
    public void chat(Map<String, Object> payload) {
        String speedGameId = (String) payload.get("speedGameId");
        String sender = (String) payload.get("sender");
        String answer = (String) payload.get("answer");
        int questionIndex = (int) payload.get("questionIndex");

        System.out.println("샌더1 : " + sender);
        System.out.println("정답1 : " + answer);

        if(!speedQuizGameService.submit(questionIndex,answer,speedGameId)){
            Chat chat = new Chat(answer,speedGameId,sender);

            Map<String, Object> response = new HashMap<>();

            System.out.println("샌더1 : " + chat.getSender());
            System.out.println("정답1 : " + chat.getContent());


            response.put("type", "SUBMIT_NO_ANSWER");
            response.put("data", chat);



            messageSendingOperations.convertAndSend("/topic/speed_game/" + speedGameId, response);
        }
        else{
            SpeedQuizGame speedQuizGameById = speedQuizGameService.getSpeedQuizGameById(speedGameId);
            Player winner = speedQuizGameById.getPlayers()
                    .stream()
                    .filter(player -> player.getNickname().equals(sender))
                    .findFirst()
                    .orElse(null);
            assert winner != null;
            winner.setScore(winner.getScore()+10);
            Integer newScore = speedQuizGameById.getScore().get(winner.getNickname()) + 10;
            speedQuizGameById.getScore().put(winner.getNickname(),newScore);


            if(speedQuizGameById.getQuizNum() == null){



                System.out.println("점수 + 10 : " + winner.getScore());
                System.out.println("목표 점수  : " + speedQuizGameById.getMaxScore());

                if(winner.getScore() == speedQuizGameById.getMaxScore()){

                    System.out.println("끝남!!");
                    Map<String, Integer> result = speedQuizGameById.getScore();
                    Map<String, Object> response = new HashMap<>();

                    response.put("type", "GAME_END");
                    response.put("data", new HashMap<String, Object>() {{
                        put("winner", sender);
                        put("result", result);
                    }});
                    messageSendingOperations.convertAndSend("/topic/speed_game/" + speedGameId, response);
                    return;
                }


            }
            else if(speedQuizGameById.getMaxScore()==null){
                System.out.println("현재 문제 번호, 총 문제 개수 : " + questionIndex + "   " + speedQuizGameById.getQuizNum());
                if(questionIndex+1 == speedQuizGameById.getQuizNum()){
                    Set<Player> players = speedQuizGameById.getPlayers();
                    Player player = players.stream()
                            .max(Comparator.comparingInt(Player::getScore))
                            .orElse(null);


                    Map<String, Integer> result = speedQuizGameById.getScore();

                    Map<String, Object> response = new HashMap<>();

                    response.put("type", "GAME_END");
                    response.put("data", new HashMap<String, Object>() {{
                        put("winner", player);
                        put("result", result);
                    }});

                    messageSendingOperations.convertAndSend("/topic/speed_game/" + speedGameId, response);
                    return;
                }




            }




            Map<String, Object> response = new HashMap<>();

            response.put("type", "SUBMIT_ANSWER");
            response.put("data", new HashMap<String, Object>() {{
                put("winner", sender);
                put("game", speedQuizGameById);
            }});







            messageSendingOperations.convertAndSend("/topic/speed_game/" + speedGameId, response);
        }





    }

    @MessageMapping("/speed_game.start")
    public void start(Map<String, Object> payload) {
        String speedGameId = (String) payload.get("speedGameId");
        String nickname = (String) payload.get("nickname");



        SpeedQuizGame speedQuizGameById = speedQuizGameService.getSpeedQuizGameById(speedGameId);
        speedQuizGameById.setReadyStatus(speedQuizGameById.getReadyStatus()+1);



        if(speedQuizGameById.getReadyStatus() == speedQuizGameById.getPlayers().size()){
            System.out.println("게임 시작");

            Map<String, Object> response = new HashMap<>();

            response.put("type", "START");
            response.put("data", "START");




            messageSendingOperations.convertAndSend("/topic/speed_game/" + speedGameId, response);

            speedQuizGameById.setReadyStatus(0);
        }
        else{
            System.out.println("게임 준비완료");


            Map<String, Object> response = new HashMap<>();

            response.put("type", "START");

            response.put("data", new HashMap<String, Object>() {{
                put("Status", "READY");
                put("nickname", nickname);
            }});





            messageSendingOperations.convertAndSend("/topic/speed_game/" + speedGameId, response);


        }





    }


    @MessageMapping("/speed_game.pass")
    public void pass(Map<String, Object> payload) {
        String speedGameId = (String) payload.get("speedGameId");
        String nickname = (String) payload.get("nickname");

        SpeedQuizGame speedQuizGameById = speedQuizGameService.getSpeedQuizGameById(speedGameId);
        speedQuizGameById.setReadyStatus(speedQuizGameById.getReadyStatus()+1);
        List<String> passedPlayer = speedQuizGameById.getPassedPlayer();
        passedPlayer.add(nickname);


        if(speedQuizGameById.getReadyStatus() == speedQuizGameById.getPlayers().size()){
            System.out.println("패싱 됌 ");

        Map<String, Object> response = new HashMap<>();

            response.put("type", "PASS");
            response.put("data", "END");




            messageSendingOperations.convertAndSend("/topic/speed_game/" + speedGameId, response);
            speedQuizGameById.setPassedPlayer(new ArrayList<>());
            speedQuizGameById.setReadyStatus(0);
        }
        else{
            System.out.println("패스 신청 완료 ");


            Map<String, Object> response = new HashMap<>();

            response.put("type", "PASS");

            response.put("data", new HashMap<String, Object>() {{
                put("result", "READY");
                put("passedPlayer", passedPlayer);
            }});




            messageSendingOperations.convertAndSend("/topic/speed_game/" + speedGameId, response);


        }





    }




    @MessageMapping("/speed_game.leave")
    public void leave(Map<String, Object> payload) {
        String speedGameId = (String) payload.get("speedGameId");
        String nickname = (String) payload.get("nickname");




        SpeedQuizGame speedQuizGameById = speedQuizGameService.getSpeedQuizGameById(speedGameId);

        boolean removePlayer = speedQuizGameService.removePlayer(speedGameId, nickname);
        speedQuizGameService.removePlayerInfo(speedGameId, nickname);


        boolean remove = false;
        if(speedQuizGameById.getPlayers().isEmpty()) {
            remove = speedQuizGameService.remove(speedGameId);



            // 게임 시작 알림 (플레이어 순서 포함)
            Map<String, Object> response = new HashMap<>();
            response.put("type", "REMOVE");


            response.put("data", new HashMap<String, Object>() {{
                put("result", "removeGame");
            }});
            messageSendingOperations.convertAndSend("/topic/speed_game/" + speedGameId, response);


        }
        else{





            // 게임 시작 알림 (플레이어 순서 포함)
            Map<String, Object> response = new HashMap<>();
            response.put("type", "REMOVE");
            response.put("data", new HashMap<String, Object>() {{
                put("result", "removePlayer");
                put("removePlayer", nickname);
                put("game", speedQuizGameById);
            }});
            messageSendingOperations.convertAndSend("/topic/speed_game/" + speedGameId, response);


        }


    }


}
