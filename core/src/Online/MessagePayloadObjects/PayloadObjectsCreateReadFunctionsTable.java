package Online.MessagePayloadObjects;

import Online.Base.MessagePayload;
import Online.MessagePayloadObjects.CommonPayloadObjects.PayloadInvalid;
import Online.MessagePayloadObjects.CommonPayloadObjects.PayloadStringData;
import Online.MessagePayloadObjects.PlayerPayloadObjects.PayloadLoginData;
import Online.MessagePayloadObjects.PlayerPayloadObjects.PayloadSpeedXY;
import Online.MessagePayloadObjects.ServerPayloadObjects.PayloadGameFullData;
import Online.MessagePayloadObjects.ServerPayloadObjects.PayloadGameTickData;
import Online.Base.ReadFunctions;

import java.util.HashMap;

import static Online.Base.ReadFunctions.fromClass;

public class PayloadObjectsCreateReadFunctionsTable {
    public static final HashMap<Class<? extends MessagePayload>, ReadFunctions> payloadFunctionsMap = new HashMap<>();

    static {
        try {
            payloadFunctionsMap.put(PayloadInvalid.class, fromClass(PayloadInvalid.class));

            payloadFunctionsMap.put(PayloadStringData.class, fromClass(PayloadStringData.class));

            payloadFunctionsMap.put(PayloadLoginData.class, fromClass(PayloadLoginData.class));
            payloadFunctionsMap.put(PayloadSpeedXY.class, fromClass(PayloadSpeedXY.class));

            payloadFunctionsMap.put(PayloadGameTickData.class, fromClass(PayloadGameTickData.class));
            payloadFunctionsMap.put(PayloadGameFullData.class, fromClass(PayloadGameFullData.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
