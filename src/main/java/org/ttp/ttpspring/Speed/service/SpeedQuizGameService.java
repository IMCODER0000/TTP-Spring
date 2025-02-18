package org.ttp.ttpspring.Speed.service;


import org.springframework.stereotype.Service;
import org.ttp.ttpspring.Liar.model.Player;
import org.ttp.ttpspring.Speed.Entity.SpeedQuiz;
import org.ttp.ttpspring.Speed.model.SpeedQuizGame;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SpeedQuizGameService {

    private final SpeedQuizService speedQuizService;
    private final Map<String, SpeedQuizGame> speedQuizGames = new ConcurrentHashMap<>();

    public SpeedQuizGameService(SpeedQuizService speedQuizService) {
        this.speedQuizService = speedQuizService;
    }
    


    public SpeedQuizGame createSpeedQuizGameByMaxScore(Set<Player> players, String roomId, Integer maxScore, String category) {

        SpeedQuizGame speedQuizGame = new SpeedQuizGame(roomId);
        for (Player player : players) {
            speedQuizGame.getPlayers().add(player);
        }
        speedQuizGame.setCategory(category);
        speedQuizGame.setMaxScore(maxScore);


        List<SpeedQuiz> randomQuizzes = speedQuizService.getRandomQuizzes(30);
        if (maxScore == 100) {
            randomQuizzes = speedQuizService.getRandomQuizzes(60);
        } else if (maxScore == 150) {
            randomQuizzes = speedQuizService.getRandomQuizzes(120);
        }


        speedQuizGame.setQuizzes(randomQuizzes);
        speedQuizGames.put(speedQuizGame.getSpeedGameId(), speedQuizGame);
        return speedQuizGame;


    }


    public SpeedQuizGame createSpeedQuizGameByQuizNum(Set<Player> players, String roomId, Integer quizNum, String category) {

        SpeedQuizGame speedQuizGame = new SpeedQuizGame(roomId);
        for (Player player : players) {
            speedQuizGame.getPlayers().add(player);
        }
        speedQuizGame.setCategory(category);
        speedQuizGame.setQuizNum(quizNum);

        List<SpeedQuiz> randomQuizzes = speedQuizService.getRandomQuizzes(quizNum);
        speedQuizGame.setQuizzes(randomQuizzes);

        speedQuizGames.put(speedQuizGame.getSpeedGameId(), speedQuizGame);
        return speedQuizGame;


    }



    public SpeedQuizGame getSpeedQuizGameById(String speedGameId){



        return speedQuizGames.get(speedGameId);
    }


    public boolean submit(int questionIndex, String answer,String speedGameId) {
        SpeedQuizGame speedQuizGameById = getSpeedQuizGameById(speedGameId);
        SpeedQuiz speedQuiz = speedQuizGameById.getQuizzes().get(questionIndex);
        if(speedQuiz.getAnswer().equals(answer)){
            return true;
        }
        else{
            return false;
        }
    }
    public boolean removePlayer(String gameId, String nickname) {
        SpeedQuizGame speedQuizGameById = getSpeedQuizGameById(gameId);
        Set<Player> players = speedQuizGameById.getPlayers();
        return players.removeIf(player -> player.getNickname().equals(nickname));
    }

    public String removePlayerInfo(String gameId, String nickname) {
        SpeedQuizGame speedQuizGameById = getSpeedQuizGameById(gameId);
        Map<String, String> playerInfo = speedQuizGameById.getPlayerInfo();
        return playerInfo.remove(nickname);
    }

    public boolean remove(String gameId) {
        // Map에서 gameId에 해당하는 항목을 삭제하고 성공 여부 반환
        return speedQuizGames.remove(gameId) != null;
    }

}
