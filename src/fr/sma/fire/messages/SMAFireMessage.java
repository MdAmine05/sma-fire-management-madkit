package fr.sma.fire.messages;

import madkit.kernel.Message;

public class SMAFireMessage extends Message {

    private final Object content;

    public SMAFireMessage(Object content) {
        this.content = content;
    }

    public Object getContent() {
        return content;
    }
}