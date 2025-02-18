package org.ttp.ttpspring.Speed.model;


import lombok.Getter;
import lombok.Setter;

import java.security.Timestamp;
import java.util.UUID;

@Getter
@Setter
public class SpeedChat {

    private String id;

    private String roomId;

    private String sender;

    private String content;

    private Timestamp sendTime;

    public SpeedChat(String content, String roomId, String sender) {
        this.id = UUID.randomUUID().toString();
        this.content = content;
        this.roomId = roomId;
        this.sender = sender;
    }
}
