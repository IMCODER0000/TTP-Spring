package org.ttp.ttpspring.Speed.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ttp.ttpspring.dto.CreateRoomDTO;
import org.ttp.ttpspring.dto.NicknameDTO;
import org.ttp.ttpspring.dto.PasswordDTO;
import org.ttp.ttpspring.Liar.model.Player;
import org.ttp.ttpspring.Speed.model.SpeedGameRoom;
import org.ttp.ttpspring.Speed.service.SpeedGameRoomService;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/speed")
public class SpeedGameApiController {

    private final SpeedGameRoomService speedGameRoomService;
    private final SimpMessageSendingOperations messagingTemplate;


    @PostMapping("/create/room")
    public ResponseEntity<String> createRoom(@RequestBody CreateRoomDTO createRoomDTO) {

        String nickname = createRoomDTO.getNickname();
        int maxPlayer = createRoomDTO.getMaxPlayer();
        String password = createRoomDTO.getPassword();
        String browserID = createRoomDTO.getBrowserId();

        System.out.println("닉네임33, 플레이어수, 비밀번호 : " + nickname+ "   " +  maxPlayer+ "   " +  password);

        SpeedGameRoom room = speedGameRoomService.createRoom(nickname, maxPlayer, password, browserID);
        String roomId = room.getRoomId();

        return ResponseEntity.ok(roomId);

    }

    @PostMapping("/password/check")
    public ResponseEntity<String> checkPassword(@RequestBody PasswordDTO passwordDTO) {

        String roomId = passwordDTO.getRoomId();
        String password = passwordDTO.getPassword();




        SpeedGameRoom room = speedGameRoomService.getRoom(roomId);
        if(room.getPassword().equals(password)){
            return new ResponseEntity<>("OK", HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Wrong password", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/nickname")
    public ResponseEntity<SpeedGameRoom> joinToNickname(@RequestBody NicknameDTO nicknameDTO) {

        String roomId = nicknameDTO.getRoomId();
        String nickname = nicknameDTO.getNickname();
        String browserId = nicknameDTO.getBrowserId();

        SpeedGameRoom room = speedGameRoomService.getRoom(roomId);
        Set<Player> players = room.getPlayers();
        for (Player player : players) {
            if(player.getNickname().equals(nickname)){
                return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
                //HttpStatus.ACCEPTED = 202
            }
        }

        boolean result = speedGameRoomService.joinRoom(roomId, nickname, browserId);
        SpeedGameRoom findRoom = speedGameRoomService.getRoom(roomId);

        if(result){
            return new ResponseEntity<>(findRoom, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

    }


}
