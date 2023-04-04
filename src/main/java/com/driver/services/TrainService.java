package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train train = new Train();

        String route = "";
        List<Station> stations = trainEntryDto.getStationRoute();
        for(int i=0;i<stations.size();i++){
            if(i == stations.size()-1){
                route = route + stations.get(i);
            }
            else{
                route = route + stations.get(i) + ",";
            }
        }
        //set all parameter of train
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setRoute(route);
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        trainRepository.save(train);

        return train.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        int total = train.getNoOfSeats();

        List<Ticket> tickets = train.getBookedTickets();

        String[] stations = train.getRoute().split(",");

        Station source = seatAvailabilityEntryDto.getFromStation();
        Station destination = seatAvailabilityEntryDto.getToStation();

        boolean isSource = false;
        boolean isDestination = false;

        for(String station:stations){
            if(String.valueOf(source).equals(station)){
                isSource = true;
            }
            if(String.valueOf(destination).equals(station)){
                isDestination = true;
            }

            if(isSource){
                for(Ticket ticket:tickets){
                    if(String.valueOf(ticket.getFromStation()).equals(station)){
                        total -= ticket.getPassengersList().size();
                    }
                    if(String.valueOf(ticket.getToStation()).equals(station)){
                        total += ticket.getPassengersList().size();
                    }
                }
            }
            if(isDestination){
                break;
            }
        }
       return total;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Train train = trainRepository.findById(trainId).get();
        String[] stations = train.getRoute().split(",");

        //see if station is valid
        boolean isValid = false;
        for(String curStation:stations){
            if(String.valueOf(station).equals(curStation))
                isValid = true;
        }
        if(!isValid){
            throw new Exception("Train is not passing from this station");
        }

        //no of boarding passengers
        List<Ticket> tickets = train.getBookedTickets();
        int count = 0;
        for(Ticket ticket:tickets){
            if(ticket.getFromStation().equals(station)){
                count += ticket.getPassengersList().size();
            }
        }
        return count;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        int age = 0;
        Train train = trainRepository.findById(trainId).get();
        List<Ticket> tickets = train.getBookedTickets();
        for(Ticket ticket:tickets){
            for(Passenger passenger: ticket.getPassengersList()){
                if(passenger.getAge() > age){
                    age = passenger.getAge();
                }
            }
        }
        return age;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains
        // that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Integer> allTrains = new ArrayList<>();

        List<Train> trains = trainRepository.findAll();

        for(int i=0; i<trains.size(); i++){
            Train train = trains.get(i);
            String[] route = train.getRoute().split(",");
            for(int k=0; k<route.length; k++){
                if(route[k].equals(station)){
                    if(startTime.compareTo(train.getDepartureTime()) <= 0 && endTime.compareTo(train.getDepartureTime()) >= 0)
                        allTrains.add(train.getTrainId());
                }
            }
        }

        return allTrains;
    }

}
