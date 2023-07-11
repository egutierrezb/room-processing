package com.booking.processing;

import com.booking.room.controller.RoomReservationController;
import com.booking.room.model.Room;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
//This controller is just a wrapper of Room Reservation controller
//This is v1 for room reservation wrapper
public class ProcessingRoomController {

    //Connection to Room Reservation web controller
    @Autowired
    RestTemplate restTemplate;

    //EC2 url for aws container
    @Value("${ec2.url}")
    private String url;

    //EC2 path for aws container
    @Value("${ec2.path}")
    private String path;

    //Logger which would be injected into AWS CloudWatch when ECS gets deployed
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingRoomController.class);

    @GetMapping("/wrapper-room")
    @HystrixCommand(fallbackMethod = "defaultRoomsAuthorized")
    public ResponseEntity<List<AuthorizedReservation>> getRoomsAuthorized() throws Exception {
        List<AuthorizedReservation> authorizedReservationList = new ArrayList<>();
        ResponseEntity<List<Room>> response = null;
        LOG.info("Wrapper endpoint called "+url+path);
        try {
            response = restTemplate.exchange(url+path, HttpMethod.GET, null, new ParameterizedTypeReference<List<Room>>() {
            });
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError())
                throw new Exception("No Rooms have been feeded into DB!");
            else
                throw new Exception("Can not connect to EC2!");
        }
        List<Room> responseList = response.getBody();
        if(responseList!=null) {
            responseList.stream().forEach(s-> {
                AuthorizedReservation auth = new AuthorizedReservation();
                auth.setRoom(s);
                auth.setAuthorizedBy("Alex Gutierrez");
                authorizedReservationList.add(auth);
            });
        } else {
            return new ResponseEntity<>(authorizedReservationList, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(authorizedReservationList, HttpStatus.OK);
    }

    public ResponseEntity<List<AuthorizedReservation>> defaultRoomsAuthorized() {
        List<AuthorizedReservation> authorizedReservationList = new ArrayList<>();
        LOG.info("Entering to fallback method");
        return new ResponseEntity<>(authorizedReservationList, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
