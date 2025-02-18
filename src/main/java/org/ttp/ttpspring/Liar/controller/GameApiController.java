package org.ttp.ttpspring.Liar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;
import org.ttp.ttpspring.dto.CreateRoomDTO;
import org.ttp.ttpspring.dto.NicknameDTO;
import org.ttp.ttpspring.dto.PasswordDTO;
import org.ttp.ttpspring.Liar.model.GameRoom;
import org.ttp.ttpspring.Liar.model.Player;
import org.ttp.ttpspring.Liar.service.GameService;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GameApiController {

    private final GameService gameService;
    private final SimpMessageSendingOperations messagingTemplate;


    @PostMapping("/create/room")
    public ResponseEntity<String> createRoom(@RequestBody CreateRoomDTO createRoomDTO) {

        String nickname = createRoomDTO.getNickname();
        int maxPlayer = createRoomDTO.getMaxPlayer();
        String password = createRoomDTO.getPassword();

        System.out.println("닉네임, 플레이어수, 비밀번호 : " + nickname+ "   " +  maxPlayer+ "   " +  password);

        GameRoom room = gameService.createRoom(nickname, maxPlayer, password);
        String roomId = room.getRoomId();

        return ResponseEntity.ok(roomId);

    }

    @PostMapping("/password/check")
    public ResponseEntity<String> checkPassword(@RequestBody PasswordDTO passwordDTO) {

        String roomId = passwordDTO.getRoomId();
        String password = passwordDTO.getPassword();




        GameRoom room = gameService.getRoom(roomId);
        if(room.getPassword().equals(password)){
            return new ResponseEntity<>("OK", HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Wrong password", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/nickname")
    public ResponseEntity<GameRoom> joinToNickname(@RequestBody NicknameDTO nicknameDTO) {

        String roomId = nicknameDTO.getRoomId();
        String nickname = nicknameDTO.getNickname();

        GameRoom room = gameService.getRoom(roomId);
        Set<Player> players = room.getPlayers();
        for (Player player : players) {
            if(player.getNickname().equals(nickname)){
                return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
                //HttpStatus.ACCEPTED = 202
            }
        }

        boolean result = gameService.joinRoom(roomId, nickname);
        GameRoom findRoom = gameService.getRoom(roomId);

        if(result){
            return new ResponseEntity<>(findRoom, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

    }


}
