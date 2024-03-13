package com.example.application.views;

import com.example.application.client.AssistantService;
import com.example.application.client.BookingService;
import com.example.application.services.BookingDetails;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static com.example.application.MessageListUtil.addItem;
import static com.example.application.MessageListUtil.createMessage;
import static com.example.application.MessageListUtil.appendToLastMessage;

@PageTitle("Bookings")
@Route("")
public class MainView extends SplitLayout {

    private final AssistantService assistantService;
    private final BookingService bookingService;
    MessageList messageList = new MessageList();
    MessageInput messageInput = new MessageInput();
    VerticalLayout chat = new VerticalLayout(messageList,messageInput);
    Grid<BookingDetails> grid = new Grid(BookingDetails.class);
    private String chatId = UUID.randomUUID().toString();

    public MainView(
            @Autowired BookingService bookingService,
            @Autowired AssistantService assistantService) {

        this.assistantService = assistantService;
        this.bookingService = bookingService;
        
        addToPrimary(chat);
        addToSecondary(grid);
        setSplitterPosition(30);
        setHeightFull();
        setWidthFull();

        messageList.setSizeFull();
        messageInput.setWidthFull();
        messageInput.addSubmitListener(this::chat);

        grid.setSizeFull();
        configureGrid(this.grid);

    }

    private void chat(MessageInput.SubmitEvent submitEvent) {
        MessageListItem userInput = createMessage(submitEvent.getValue(), "Customer", 1);
        MessageListItem assistantReply = createMessage(" ", "Assistant", 2);
        addItem(this.messageList, userInput,assistantReply);
        assistantService.chat(this.chatId, submitEvent.getValue()).subscribe(token -> appendToLastMessage(messageList, token));
        refreshGrid();
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
        //TODO: Use more granular refresh for big grids grid.getDataProvider().refreshItem(item);
    }

    private void configureGrid(Grid<BookingDetails> grid) {
        grid.addColumn(BookingDetails::bookingNumber);
        grid.addColumn(BookingDetails::firstName);
        grid.addColumn(BookingDetails::lastName);
        grid.addColumn(BookingDetails::bookingFrom);
        grid.addColumn(BookingDetails::bookingTo);
        grid.addColumn(BookingDetails::bookingStatus);
        grid.setItems(bookingService.getBookings());
    }

}
