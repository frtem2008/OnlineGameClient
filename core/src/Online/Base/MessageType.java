package Online.Base;

import Online.MessagePayloadObjects.CommonPayloadObjects.PayloadInvalid;
import Online.MessagePayloadObjects.CommonPayloadObjects.PayloadStringData;
import Online.MessagePayloadObjects.PlayerPayloadObjects.PayloadLoginData;
import Online.MessagePayloadObjects.PlayerPayloadObjects.PayloadSpeedXY;
import Online.MessagePayloadObjects.ServerPayloadObjects.PayloadGameFullData;
import Online.MessagePayloadObjects.ServerPayloadObjects.PayloadGameTickData;

public enum MessageType {

    INVALID(PayloadInvalid.class), //for new non-created messages

    ERROR(PayloadStringData.class),       //reaction on invalid message
    INFO(PayloadStringData.class), //some information

    LOGIN_DATA(PayloadLoginData.class),    //player login data (nick, etc)
    SPEED_XY(PayloadSpeedXY.class),        //player xy speed(for now: sx, sy)
    GAME_DATA_TICK(PayloadGameTickData.class), // game data for one tick (only updates and deletions)

    GAME_DATA_FULL(PayloadGameFullData.class), // complete game data (first send for new players and maybe sync send every n seconds)
    ;


    final Class<? extends MessagePayload> payload;

    MessageType(Class<? extends MessagePayload> payload) {
        this.payload = payload;
    }
}
