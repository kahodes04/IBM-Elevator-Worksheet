import javax.swing.*;
import java.awt.*;
public class MyFrame extends JFrame{
 
 MyPanel panel;
 
  MyFrame(){
  
  panel = new MyPanel();
  
  this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  this.setPreferredSize(new Dimension(500, 750));
  this.add(panel);
  this.pack();
  this.setLocationRelativeTo(null);
  this.setVisible(true);
 }  
 MyPanel getpanel(){
    return panel;
 }
}