package com.example.application.views;

import com.example.application.client.AssistantService;
import com.example.application.client.BookingService;
import com.example.application.services.BookingDetails;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.firitin.components.messagelist.MarkdownMessage;
import org.vaadin.firitin.components.messagelist.MarkdownMessage.Color;
import org.vaadin.firitin.components.orderedlayout.VScroller;
import org.vaadin.firitin.components.orderedlayout.VVerticalLayout;

import java.util.UUID;

@PageTitle("Bookings")
@Route("")
public class MainView extends SplitLayout {

    private final AssistantService assistantService;
    private final BookingService bookingService;
    VerticalLayout messageList = new VVerticalLayout()
            .withSpacing(false);
    MessageInput messageInput = new MessageInput();
    VerticalLayout chat = new VVerticalLayout()
            .withExpanded(new VScroller(messageList))
            .withComponent(messageInput);
    Grid<BookingDetails> grid = new Grid(BookingDetails.class);
    private String chatId = UUID.randomUUID().toString();
    private MarkdownMessage assistantMsg;

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
        String question = submitEvent.getValue();
        addQuestionToList(question);
        streamResponse(question);
        refreshGrid();
    }

    private void streamResponse(String question) {
        assistantService.chat(this.chatId, question)
                .subscribe(token -> assistantMsg.appendMarkdownAsync(token));
    }

    private void addQuestionToList(String question) {
        var customerMsg = new MarkdownMessage(question, "Customer", Color.AVATAR_PRESETS[1]);
        assistantMsg = new MarkdownMessage("Assistant", Color.AVATAR_PRESETS[1]);
        messageList.add(
                customerMsg,
                assistantMsg
        );
        assistantMsg.scrollIntoView();
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
