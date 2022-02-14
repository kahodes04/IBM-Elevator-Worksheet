import java.awt.*;
import javax.swing.*;

import java.util.Collections;

public class MyPanel extends JPanel {

elevator[] elevatorarray;
java.util.List<elevatorRequest> requestlist;

    MyPanel() {
        this.setPreferredSize(new Dimension(500, 600));
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;

        super.paintComponent(g);

        if(elevatorarray != null){
            java.util.List<elevatorRequest> temprequestlist;
            temprequestlist = Collections.synchronizedList(requestlist);

            g2D.drawString("^ elevator array ^", 150, 700);

            g2D.setPaint(Color.blue);
            g2D.drawRect(20, 600 - (elevatorarray[0].getfloornum() * 10), 40, 70);

            g2D.drawString(String.valueOf(elevatorarray[0].getfloornum()) , 35, 640 - (elevatorarray[0].getfloornum() * 10) );

            g2D.drawRect(70, 600 - (elevatorarray[1].getfloornum() * 10), 40, 70);

            g2D.drawString(String.valueOf(elevatorarray[1].getfloornum()) , 85, 640 - (elevatorarray[1].getfloornum() * 10) );
    
            g2D.drawRect(120, 600 - (elevatorarray[2].getfloornum() * 10), 40, 70);

            g2D.drawString(String.valueOf(elevatorarray[2].getfloornum()) , 135, 640 - (elevatorarray[2].getfloornum() * 10) );
    
            g2D.drawRect(170, 600 - (elevatorarray[3].getfloornum() * 10), 40, 70);

            g2D.drawString(String.valueOf(elevatorarray[3].getfloornum()) , 185, 640 - (elevatorarray[3].getfloornum() * 10) );
    
            g2D.drawRect(220, 600 - (elevatorarray[4].getfloornum() * 10), 40, 70);

            g2D.drawString(String.valueOf(elevatorarray[4].getfloornum()) , 235, 640 - (elevatorarray[4].getfloornum() * 10) );
    
            g2D.drawRect(270, 600 - (elevatorarray[5].getfloornum() * 10), 40, 70);

            g2D.drawString(String.valueOf(elevatorarray[5].getfloornum()) , 285, 640 - (elevatorarray[5].getfloornum() * 10) );
    
            g2D.drawRect(320, 600 - (elevatorarray[6].getfloornum() * 10), 40, 70);

            g2D.drawString(String.valueOf(elevatorarray[6].getfloornum()) , 335, 640 - (elevatorarray[6].getfloornum() * 10) );
        }
    }

    protected void drawelevators(elevator[] _elevatorarray, java.util.List<elevatorRequest> _requestlist) {
        elevatorarray = _elevatorarray;
        requestlist = _requestlist;
        repaint();
    }
}