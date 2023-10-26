package com.example.application.client;

import com.example.application.services.BookingDetails;
import com.example.application.services.CarRentalService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AnonymousAllowed
public class BookingService {
    private final CarRentalService carRentalService;

    public BookingService(CarRentalService carRentalService) {
        this.carRentalService = carRentalService;
    }

    public List<BookingDetails> getBookings() {
        return carRentalService.getBookings();
    }
}
