package com.booking.processing;

//import com.booking.room.model.Room;
import com.booking.room.model.Room;
import lombok.Data;

@Data
public class AuthorizedReservation {
   private Room room;
   private String authorizedBy;
}
