import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class elevator {
    private int floorNum = 0;
    private boolean movingDown = false;
    private boolean movingUp = false;
    private boolean isMoving = false;

    synchronized int getfloornum() {
        return floorNum;
    }

    synchronized boolean getmovingdown() {
        return movingDown;
    }

    synchronized boolean getmovingup() {
        return movingUp;
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
    private int currFloor = 0;
    private int destFloor = 0;

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

        // This thread creates a random request every n seconds. (Set to 200 requests)
        Runnable requestcreator = new Runnable() {
            @Override
            public void run() {
                int requestnum = 200;
                while (true) {
                    elevatorRequest request = new elevatorRequest();
                    Random rand = new Random();
                    // It randomly chooses to make a request to either go up to a higher floor or
                    // from a higher floor down to ground floor.
                    if (rand.nextBoolean()) {
                        // Get a random number from 1 to 55.
                        int randomfloor = rand.nextInt(55) + 1;
                        request.setcurrentfloor(0);
                        request.setdestfloor(randomfloor);
                        request.setcurrentfloor(0);
                    } else {
                        int randomfloor = rand.nextInt(55) + 1;
                        request.setcurrentfloor(randomfloor);
                        request.setdestfloor(0);
                    }
                    System.out.println("Adding a new request to the list.");
                    listedit.addtolist(listelevatorrequests, request);
                    if (requestnum == 0)
                        break;
                    requestnum -= 1;
                    try {
                        Thread.sleep(8000);
                    } catch (InterruptedException e) {
                    }
                }

            }
        };
        // Start the request creating thread.
        Thread thread1 = new Thread(requestcreator);
        thread1.start();
        // Start UI.
        MyFrame frame = new MyFrame();
        MyPanel panel = frame.getpanel();
        // Create boolean which will be used to delete the requests as they are taken
        // care of after a certain delay to ensure no thread will try to access them
        // after they are deleted.
        boolean processed = false;
        // Main loop of the program. It checks what requests are available and it
        // determines what elevator would be best for it.
        while (true) {
            if (!listelevatorrequests.isEmpty()) {
                System.out.println("Processing request. Current floor: " + listelevatorrequests.get(0).getcurrentfloor()
                        + ". Destination floor: " + listelevatorrequests.get(0).getdestfloor());
                // Create variable to keep track of the index of the elevator chosen to be the
                // one that takes care of the request.
                int assignedelevatorindex = -1;
                // Create variable to keep track of the shortest distance between the elevator
                // and the request.
                int shortestdistance = 56;
                // Loop through elevators to determine the best one for the given request.
                for (int i = 0; i < arrayelevators.length; i++) {
                    // Check if it's not moving up (We want to ignore it otherwise as we only want
                    // idle or moving down).
                    if (!arrayelevators[i].getmovingup()) {
                        // Check if the elevator is moving down and if its floor is lower than the
                        // request floor. If it is, we skip this one as it's already passed the request.
                        if (arrayelevators[i].getmovingdown()
                                && arrayelevators[i].getfloornum() < listelevatorrequests.get(0).getcurrentfloor())
                            continue;
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
                    // Check if the elevator that was picked as the best one is going down. If it
                    // is, we can consider the request to have been taken care of.
                    if (arrayelevators[assignedelevatorindex].getmovingdown()) {
                        System.out.println("Elevator at index " + arrayelevators[assignedelevatorindex]
                                + " was going down and it was on the same floor as the request.");
                        listedit.removefromlist(listelevatorrequests, listelevatorrequests.get(0));
                        // If the elevator is idling on the same floor as the request, we need to
                        // schedule it to only move toward the destination floor. (checked for in the
                        // moveelevator method)
                    } else if (!arrayelevators[assignedelevatorindex].getismoving()) {
                        final int index = assignedelevatorindex;
                        System.out.println("Launching elevator moving thread.");
                        Thread threadelevator = new Thread(() -> moveelevator(arrayelevators, index,
                                listelevatorrequests.get(0).getcurrentfloor(),
                                listelevatorrequests.get(0).getdestfloor()));
                        threadelevator.start();
                        processed = true;
                    }
                }
                // Check if an elevator was assigned to the request and if it isn't moving down.
                // If there is no best idle elevator (else statement), it'll skip the request
                // for later as it will either choose a new idle elevator or line up with one
                // coming down.
                else if (assignedelevatorindex != -1 && !arrayelevators[assignedelevatorindex].getmovingdown()) {
                    final int index = assignedelevatorindex;
                    System.out.println("Launching elevator moving thread.");
                    Thread threadelevator = new Thread(() -> moveelevator(arrayelevators, index,
                            listelevatorrequests.get(0).getcurrentfloor(), listelevatorrequests.get(0).getdestfloor()));
                    threadelevator.start();
                    processed = true;
                } else {
                    elevatorRequest temp = listelevatorrequests.get(0);
                    listelevatorrequests.remove(listelevatorrequests.get(0));
                    listelevatorrequests.add(temp);
                }
            }
            // Pass elevator data to a method that calls the repaint method for the panel.
            panel.drawelevators(arrayelevators);

            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
            }
            // Remove the request if it was dealt with.
            if (processed) {
                listelevatorrequests.remove(listelevatorrequests.get(0));
                System.out.println("Request has been taken care of.");
                processed = false;
            }
        }
    }

    // This method runs on a separate thread which moves the elevator to the floor
    // the request was made from (if needed) and then towards the destination.
    public static void moveelevator(elevator[] _elevatorarray, int _arrayindex, int _requestfloor,
            int _destinationfloor) {
        int moveamount = 1;
        // Calculate the absolute distance between the elevator and the request floor.
        int floordifference = Math.abs(_elevatorarray[_arrayindex].getfloornum() - _requestfloor);
        // If floordifference is 0, it means both the elevator and the request are on
        // the same floor. The elevator will skip moving toward it and instead it will
        // just move toward the destination floor.
        if (floordifference != 0) {
            _elevatorarray[_arrayindex].setismoving(true);
            // If the elevator is above the request floor, we make it move -1 floors at a
            // time so it goes down. If it is below it, it'll go up.
            if (_elevatorarray[_arrayindex].getfloornum() > _requestfloor) {
                moveamount *= -1;
                _elevatorarray[_arrayindex].setmovingdown(true);
                _elevatorarray[_arrayindex].setmovingup(false);
            } else if (_elevatorarray[_arrayindex].getfloornum() < _requestfloor) {
                _elevatorarray[_arrayindex].setmovingdown(false);
                _elevatorarray[_arrayindex].setmovingup(true);
            }
            System.out.println("Moving elevator at index: " + _arrayindex + " by " + floordifference + " floors.");
            // For loop that handles moving the elevator as needed.
            for (int i = 0; i < floordifference; i++) {
                try {
                    Thread.sleep(700);
                } catch (InterruptedException e) {
                }
                //Move elevator one floor at a time.
                _elevatorarray[_arrayindex].setfloornum(_elevatorarray[_arrayindex].getfloornum() + moveamount);
            }
            System.out.println("Elevator at index: " + _arrayindex + " has reached the request floor.");
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        // This part is the same as before except it uses the destinationfloor variable instead of the requestfloor one.
        floordifference = Math.abs(_elevatorarray[_arrayindex].getfloornum() - _destinationfloor);
        moveamount = 1;
        if (_elevatorarray[_arrayindex].getfloornum() > _destinationfloor) {
            moveamount *= -1;
            _elevatorarray[_arrayindex].setmovingdown(true);
            _elevatorarray[_arrayindex].setmovingup(false);
        } else if (_elevatorarray[_arrayindex].getfloornum() < _destinationfloor) {
            _elevatorarray[_arrayindex].setmovingdown(false);
            _elevatorarray[_arrayindex].setmovingup(true);
        }
        System.out.println("Moving elevator at index: " + _arrayindex + " by " + floordifference + " floors.");
        for (int i = 0; i < floordifference; i++) {
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
            }
            _elevatorarray[_arrayindex].setfloornum(_elevatorarray[_arrayindex].getfloornum() + moveamount);
        }
        System.out.println("Elevator at index: " + _arrayindex + " has reached its destination.");
        // The elevator has arrived at its destination.
        // Reset moving variables making the elevator idle.
        _elevatorarray[_arrayindex].setmovingdown(false);
        _elevatorarray[_arrayindex].setmovingup(false);
        _elevatorarray[_arrayindex].setismoving(false);
    }

}
