package com.example.application;

import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *  Some utility methods to make the usage of Vaadin MessageList and MessageItem a bit easier.
 */
public class MessageListUtil {


    public static void appendToLastMessage(MessageList messageList, String token) {
        messageList.getUI().ifPresent(ui -> ui.access(()-> messageList.getElement().executeJs("this.querySelector('vaadin-message:last-child').childNodes[1].textContent += $0", token)));
    }

    public static void addItem(MessageList messageList, MessageListItem... message) {
        ArrayList<MessageListItem> list = new ArrayList<>(messageList.getItems());
        list.addAll(Arrays.stream(message).toList());
        messageList.setItems(list);
    }

    public static MessageListItem createMessage(String text, String user, int id) {
        MessageListItem msg = new MessageListItem(text,
                LocalDateTime.now().toInstant(ZoneOffset.UTC), user);
        msg.setUserColorIndex(id);
        return msg;
    }
}
