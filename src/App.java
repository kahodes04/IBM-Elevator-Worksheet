import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.*;
import javax.swing.*;

class elevator {
    int floorNum = 0;
    boolean movingDown = false;
    boolean movingUp = false;
    boolean isMoving = false;

    synchronized int getfloornum() {
        return floorNum;
    }

    synchronized boolean getmovingdown() {
        return movingDown;
    }

    synchronized boolean getmovingup() {
        return movingDown;
    }

    synchronized boolean getismoving() {
        return isMoving;
    }

    synchronized void setfloornum(int _floornum) {
        floorNum = _floornum;
    }

    synchronized void setmovingdown(boolean _movingdown) {
        movingDown = _movingdown;
    }

    synchronized void setmovingup(boolean _movingup) {
        movingUp = _movingup;
    }

    synchronized void setismoving(boolean _ismoving) {
        isMoving = _ismoving;
    }
}

class elevatorRequest {
    int currFloor = 0;
    int destFloor = 0;

    synchronized int getcurrentfloor() {
        return currFloor;
    }

    synchronized int getdestfloor() {
        return destFloor;
    }

    synchronized void setcurrentfloor(int _currfloor) {
        currFloor = _currfloor;
    }

    synchronized void setdestfloor(int _destfloor) {
        destFloor = _destfloor;
    }
}

class listedit {
    synchronized static void addtolist(List<elevatorRequest> _listelevatorrequests, elevatorRequest _elevatorrequest) {
        _listelevatorrequests.add(_elevatorrequest);
    }

    synchronized static void removefromlist(List<elevatorRequest> _listelevatorrequests,
            elevatorRequest _elevatorrequest) {
        _listelevatorrequests.remove(_elevatorrequest);
    }

}

public class App {
    public static void main(String[] args) throws Exception {
        // Create the array and list that will contain the elevators and the requests.
        List<elevatorRequest> listelevatorrequests = new ArrayList<elevatorRequest>();
        elevator arrayelevators[] = new elevator[7];

        // Instantiate elevators and populate the array.
        for (int i = 0; i < arrayelevators.length; i++) {
            elevator temp = new elevator();
            arrayelevators[i] = temp;
        }
        ;

        // Thread that'll create a total of 50 requests, one every 8 seconds.
        Runnable requestcreator = new Runnable() {
            @Override
            public void run() {
                int requestnum = 200;
                while (true) {
                    elevatorRequest request = new elevatorRequest();
                    Random rand = new Random();
                    // It randomly chooses to make a request to either go up to a higher floor or
                    // down
                    // to ground floor.
                    if (rand.nextBoolean()) {
                        int randomfloor = rand.nextInt(54) + 1;
                        request.setcurrentfloor(0);
                        request.setdestfloor(randomfloor);
                    } else {
                        int randomfloor = rand.nextInt(54) + 1;
                        request.setcurrentfloor(randomfloor);
                        request.setdestfloor(0);
                    }
                    System.out.println("Adding new request to the list.");
                    listedit.addtolist(listelevatorrequests, request);
                    if (requestnum == 0)
                        break;
                    requestnum -= 1;
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }

            }
        };
        Thread thread1 = new Thread(requestcreator);
        thread1.start();
        // Check the requests and assign elevators to go pick up the guests:
        MyFrame frame = new MyFrame();
        MyPanel panel = frame.getpanel();
        boolean processed = false;
        while (true) {
            if (!listelevatorrequests.isEmpty()) {
                // Console commands
                System.out.println("Processing request. Current floor: " + listelevatorrequests.get(0).currFloor
                        + ". Destination floor: " + listelevatorrequests.get(0).destFloor);
                // Create variable to keep track of the index of the elevator chosen to be the
                // one that takes care of the request.
                int assignedelevatorindex = -1;
                // Create variable to keep the shortest distance from elevator
                // going down to the floor of the request being processed.
                int shortestdistance = 56;
                // Loop through elevators to determine the best one for the given request.
                for (int i = 0; i < arrayelevators.length; i++) {
                    // Check if it's not moving up (We want to ignore it otherwise as we only want
                    // idle or moving down)
                    if (!arrayelevators[i].getmovingdown()) {
                        // Get the absolute distance between request floor and elevator floor.
                        int distance = Math
                                .abs(arrayelevators[i].getfloornum() - listelevatorrequests.get(0).getcurrentfloor());
                        // If the distance in this instance is the best one yet, we'll save it as the
                        // current best one.
                        if (distance < shortestdistance) {
                            // Save the distance of this elevator at this index as the new shortest one.
                            assignedelevatorindex = i;
                            shortestdistance = distance;
                        }
                    }
                }
                // If this variable is 0, it means an elevator was chosen and that it is on the
                // same floor as the request.
                if (shortestdistance == 0) {
                    // Check if the elevator is moving down. If it is, we delete the request as we
                    // can consider it to have been taken care of because we don't need to launch an
                    // elevator thread.
                    if (arrayelevators[assignedelevatorindex].movingDown) {
                        System.out.println("Elevator was on the same floor as the request and it was going down.");
                        listedit.removefromlist(listelevatorrequests, listelevatorrequests.get(0));
                        System.out.println("Request has been taken care of.");
                    } else if (!arrayelevators[assignedelevatorindex].getismoving()) {
                        // If the elevator is idling on the same floor as the request, we need to
                        // schedule it to only move toward the destination floor.
                        final int index = assignedelevatorindex;
                        
                        System.out.println("request list size: " + listelevatorrequests.get(0).getcurrentfloor());
                        Thread threadelevator = new Thread(() -> moveelevator(arrayelevators, index,
                                listelevatorrequests.get(0).getcurrentfloor(),
                                listelevatorrequests.get(0).getdestfloor()));
                        threadelevator.start();
                        System.out.println("request list size: " + listelevatorrequests.size());
                        System.out.println("Launching elevator moving thread.");
                        //listedit.removefromlist(listelevatorrequests, listelevatorrequests.get(0));
                        System.out.println("Request has been taken care of. Got past shortest distance == 0");
                        processed = true;
                    }

                }
                // Check if an elevator was assigned to the request and if it isn't moving down.
                // If there is no best idle elevator (else), it'll skip the request for later as it
                // will either choosea new idle elevator or line up with one coming down.
                else if (assignedelevatorindex != -1 && !arrayelevators[assignedelevatorindex].getmovingdown()) {
                    final int index = assignedelevatorindex;
                    System.out.println("request list size: " + listelevatorrequests.size());
                    Thread threadelevator = new Thread(() -> moveelevator(arrayelevators, index,
                            listelevatorrequests.get(0).getcurrentfloor(), listelevatorrequests.get(0).getdestfloor()));
                    threadelevator.start();
                    System.out.println("request list size: " + listelevatorrequests.size());
                    System.out.println("Launching elevator moving thread.");
                    // We can consider the request as to have been taken care of, so we delete it.
                    System.out.println("Request has been taken care of.");
                    //listedit.removefromlist(listelevatorrequests, listelevatorrequests.get(0));
                    processed = true;
                } else {
                    elevatorRequest temp = listelevatorrequests.get(0);
                    listelevatorrequests.remove(listelevatorrequests.get(0));
                    listelevatorrequests.add(temp);
                }
            }
            panel.drawelevators(arrayelevators, listelevatorrequests);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
            if(processed){
                listelevatorrequests.remove(listelevatorrequests.get(0));
                processed = false;
            }

        }

    }

    // This method runs on a separate thread which moves the elevator to the floor
    // the request was made from and then towards the destination.
    public static void moveelevator(elevator[] _elevatorarray, int _arrayindex, int _requestfloor,
            int _destinationfloor) {
        _elevatorarray[_arrayindex].setismoving(true);
        int moveamount = 1;

        // Calculate how many floors the elevator has to move.
        int floordifference = Math.abs(_elevatorarray[_arrayindex].getfloornum() - _requestfloor);
        // Set moving up to true and it'll be changed if the elevator is instead going
        // down.
        System.out.println("2");
        // If floordifference is 0, it means both the elevator and the request are on
        // the same floor. The elevator will skip moving toward it.
        System.out.println(floordifference);
        if (floordifference != 0) {
            // If the elevator is above the request floor, we make it move -1 floors at a
            // time (Goes down). If it is below it, it'll go up.
            if (_elevatorarray[_arrayindex].getfloornum() > _requestfloor) {
                moveamount *= -1;
                _elevatorarray[_arrayindex].setmovingdown(true);
                _elevatorarray[_arrayindex].setmovingup(false);
            } else if (_elevatorarray[_arrayindex].getfloornum() < _requestfloor) {
                _elevatorarray[_arrayindex].setmovingdown(false);
                _elevatorarray[_arrayindex].setmovingup(true);
            }
            System.out.println("3");
            for (int i = 0; i < floordifference; i++) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
                _elevatorarray[_arrayindex].setfloornum(_elevatorarray[_arrayindex].getfloornum() + moveamount);
                System.out.println("Moved elevator at index: " + _arrayindex + " by " + moveamount + " floor.");
            }
            System.out.println("Elevator at index: " + _arrayindex + " has reached the request floor.");
        }
        System.out.println("4");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        floordifference = Math.abs(_elevatorarray[_arrayindex].getfloornum() - _destinationfloor);
        System.out.println(floordifference + " floor different between request and destination");
        moveamount = 1;
        if (_elevatorarray[_arrayindex].getfloornum() > _destinationfloor) {
            moveamount *= -1;
            _elevatorarray[_arrayindex].setmovingdown(true);
            _elevatorarray[_arrayindex].setmovingup(false);
        } else if (_elevatorarray[_arrayindex].getfloornum() < _destinationfloor) {
            _elevatorarray[_arrayindex].setmovingdown(false);
            _elevatorarray[_arrayindex].setmovingup(true);
        }
        for (int i = 0; i < floordifference; i++) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }

            _elevatorarray[_arrayindex].setfloornum(_elevatorarray[_arrayindex].getfloornum() + moveamount);
            System.out.println("Moved elevator at index: " + _arrayindex + " by " + moveamount + " floor.");
        }
        System.out.println("Elevator at index: " + _arrayindex + " has reached its destination.");
        // The elevator has arrived at its destination.
        // Reset moving variables making the elevator idle.
        _elevatorarray[_arrayindex].setmovingdown(false);
        _elevatorarray[_arrayindex].setmovingup(false);
        _elevatorarray[_arrayindex].setismoving(false);
    }

}
