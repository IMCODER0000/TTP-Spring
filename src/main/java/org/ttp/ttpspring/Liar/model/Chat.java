package org.ttp.ttpspring.Liar.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    private String gameId;
    private String sender;
    private String content;
    private LocalDateTime timestamp;

    public Chat(String content, String gameId, String sender) {
        this.content = content;
        this.gameId = gameId;
        this.sender = sender;
        this.timestamp = LocalDateTime.now();
    }
}
