package org.ttp.ttpspring.Speed.model;

import lombok.Getter;
import lombok.Setter;
import org.ttp.ttpspring.Liar.model.Player;
import org.ttp.ttpspring.Speed.Entity.SpeedQuiz;

import java.util.*;

@Getter
@Setter
public class SpeedQuizGame {


    private String speedGameId;

    private Set<Player> players;

    private Map<String, String> playerInfo;

    private Map<String, Integer> score;

    private String category;

    private int readyStatus;

    private Integer maxScore;

    private String roomId;

    private Integer quizNum;

    private List<SpeedQuiz> quizzes;

    private List<String> passedPlayer;

    public SpeedQuizGame(String roomId) {
        this.speedGameId = UUID.randomUUID().toString();
        this.roomId = roomId;
        this.players = new HashSet<>();
        this.readyStatus = 0;
        this.quizzes = new ArrayList<>();
        this.passedPlayer = new ArrayList<>();
        this.playerInfo = new HashMap<>();
    }
}
