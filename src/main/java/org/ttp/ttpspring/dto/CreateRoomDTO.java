package org.ttp.ttpspring.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoomDTO {

    private String nickname;
    private String password;
    private int maxPlayer;
    private String browserId;

    public CreateRoomDTO(int maxPlayer, String nickname, String password, String browserId) {
        this.maxPlayer = maxPlayer;
        this.nickname = nickname;
        this.password = password;
        this.browserId = browserId;
    }
}
