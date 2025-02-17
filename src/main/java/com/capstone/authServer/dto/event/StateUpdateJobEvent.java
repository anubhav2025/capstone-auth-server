package com.capstone.authServer.dto.event;

import java.util.UUID;

import com.capstone.authServer.dto.event.payload.StateUpdateJobEventPayload;
import com.capstone.authServer.enums.EventTypes;

public final class StateUpdateJobEvent implements Event<StateUpdateJobEventPayload> {
    private StateUpdateJobEventPayload payload;
    private String eventId;
    private EventTypes type = EventTypes.UPDATE_FINDING;


    public StateUpdateJobEvent(StateUpdateJobEventPayload payload) {
        this.eventId = UUID.randomUUID().toString();
        this.payload = payload;
    }

    
    public StateUpdateJobEvent() {
        this.eventId = UUID.randomUUID().toString();
    }


    @Override
    public EventTypes getType() {
        return type;
    }

    @Override
    public StateUpdateJobEventPayload getPayload() {
        return payload;
    }

    @Override
    public String getEventId() {
        return eventId;
    }
}