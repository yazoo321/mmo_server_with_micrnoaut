package server.socket.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SocketResponseType {
    NEW_PLAYER_MOTION("NEW_PLAYER_MOTION"),
    NEW_PLAYER_APPEARANCE("NEW_PLAYER_APPEARANCE"),
    REMOVE_PLAYERS("REMOVE_PLAYERS"),
    PLAYER_MOTION_UPDATE("PLAYER_MOTION_UPDATE"),
    MOB_MOTION_UPDATE("MOB_MOTION_UPDATE"),
    REMOVE_MOBS("REMOVE_MOBS");

    public final String type;
}