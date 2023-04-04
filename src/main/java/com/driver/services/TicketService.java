package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();

        int total = 0;
        for(Ticket ticket:train.getBookedTickets()){
            total += ticket.getPassengersList().size();
        }
        if(total + bookTicketEntryDto.getNoOfSeats() > train.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }


        Ticket ticket = new Ticket();

        List<Passenger> passengers = new ArrayList<>();

        for(int id:bookTicketEntryDto.getPassengerIds()){
            Passenger passenger = passengerRepository.findById(id).get();
            passengers.add(passenger);
        }

        int start = -1;
        int end = -1;
        int i=0;
        for(Station station:Station.values()){

            if(station.equals(bookTicketEntryDto.getFromStation())){
                start = i;
            }
            if(station.equals(bookTicketEntryDto.getToStation())){
                end = i;
            }
            i++;
        }

        if(start == -1 || end == -1){
            throw new Exception("Invalid stations");
        }

        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());

        ticket.setTotalFare((end-start)*300);

        System.out.println((end-start)*300);

        //set parent and child objects

        ticket.setTrain(train);
        train.getBookedTickets().add(ticket);
        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();

        passenger.getBookedTickets().add(ticket);
        ticket.setPassengersList(passengers);

        trainRepository.save(train);

        return ticket.getTicketId();

    }
}
