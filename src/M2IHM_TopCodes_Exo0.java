/*
 * @(#) WebCamSample.java
 * 
 * Tangible Object Placement Codes (TopCodes)
 * Copyright (c) 2007 Michael S. Horn
 * 
 *           Michael S. Horn (michael.horn@tufts.edu)
 *           Tufts University Computer Science
 *           161 College Ave.
 *           Medford, MA 02155
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2) as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyException;
import java.awt.AWTException;
import webcam.*;
import topcodes.*;
import java.util.List;
import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.RenderingHints;import java.awt.Robot;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;


/**
 * This is a sample application that integrates the webcam library
 * with the TopCode scanner.  This code will only work on windows
 * machines--I tested with XP, but it should work fine with 
 * Vista as well.
 * 
 * To run this sample, you will need a webcamera with VGA (640x480)
 * resolution.  A Logitech QuickCam is a good choice.  Plug in your
 * camera, and then use this command to launch the demo:
 * <blockquote>
 *   $ java -cp lib/topcodes.jar WebCamSample
 * </blockquote>
 *
 * @author Michael Horn
 * @version $Revision: 1.1 $, $Date: 2008/02/04 15:00:59 $
 */
public class M2IHM_TopCodes_Exo0 extends JPanel
   implements ActionListener, WindowListener {

   
   /** The main app window */
   protected JFrame frame;

   /** Camera Manager dialog */
   protected WebCam webcam;

   /** TopCode scanner */
   protected Scanner scanner;

   /** Animates display */
   protected Timer animator;

    private boolean flag115 = false;
    private int pieMenuItemsNb = 5;
    private List<Color> pieMenuColors = new ArrayList<Color>();
    private Robot robot;
    private Ivy bus;
    private boolean created;
    private List<String> selectedShapes = new ArrayList<>();
    private String selectedShape;


   public M2IHM_TopCodes_Exo0() {
      super(true);
      this.frame    = new JFrame("M2Pro IHM _ Université de Toulouse III / ENAC _ TopCodes Exo 0");
      this.webcam   = new WebCam();
      this.scanner  = new Scanner();
      this.animator = new Timer(100, this);  // 10 frames / second

      
        //Generate colors for the pie menu
        Random rand = new Random();
        for(int i=0; i<pieMenuItemsNb; i++){
            pieMenuColors.add(new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat()));
        }

        //create Robot
        try {
           robot = new Robot();
        } catch (AWTException ex) {
           Logger.getLogger(M2IHM_TopCodes_Exo0.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //setup Ivy
        bus = new Ivy("TopCodes", "TopCodes ready", null);
        try {
            bus.start("127.0.0.1:1234");
            Thread.sleep(1000);
            bus.sendMsg("Palette:CreerRectangle x=0 y=0 longueur=5 hauteur=5");
        } catch (IvyException ex) {
            Logger.getLogger(M2IHM_TopCodes_Exo0.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(M2IHM_TopCodes_Exo0.class.getName()).log(Level.SEVERE, null, ex);
        }
      
      //--------------------------------------------------
      // Set up the application frame
      //--------------------------------------------------
      setOpaque(true);
      setPreferredSize(new Dimension(640, 480));
      frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      frame.setContentPane(this);
      frame.addWindowListener(this);
      frame.pack();
      frame.setVisible(true);

      
      //--------------------------------------------------
      // Connect to the webcam (this might fail if the
      // camera isn't connected yet).
      //--------------------------------------------------
      try {
         this.webcam.initialize();

         //---------------------------------------------
         // This can be set to other resolutions like
         // (320x240) or (1600x1200) depending on what
         // your camera supports
         //---------------------------------------------
         this.webcam.openCamera(640, 480);
      } catch (Exception x) {
         x.printStackTrace();
      }

      requestFocusInWindow();
      animator.start();
   }


   protected void paintComponent(Graphics graphics) {
      Graphics2D g = (Graphics2D)graphics;
      List<TopCode> codes = null;

      //----------------------------------------------------------
      // Capture a frame from the video stream and scan it for
      // TopCodes. 
      //----------------------------------------------------------
      try {
         if (webcam.isCameraOpen()) {
            webcam.captureFrame();
            codes = scanner.scan(
               webcam.getFrameData(),
               webcam.getFrameWidth(),
               webcam.getFrameHeight());
         }
      } catch (WebCamException wcx) {
         System.err.println(wcx);
      }

      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
      g.setFont(new Font(null, 0, 12));

      BufferedImage image = scanner.getImage();
      if (image != null && !flag115) {
         g.drawImage(image, 0, 0, null);
      }else {
    	  g.fillRect(0, 0, getWidth(), getHeight());
      }

      if (codes != null) {
         for (TopCode top : codes) {

	    	 if(top.getCode() == 115) {
	    		 flag115 = true;
	    	 }else if(top.getCode() == 181) {
	    		 flag115 = false;
	    	 }
	    	 
	    	 if(!flag115 && top.getCode() != 339 && top.getCode() != 217 && top.getCode() != 341 && top.getCode() != 31) {
	            // Draw the topcode in place
	            top.draw(g);
	    	 }

            //--------------------------------------------
            // Draw the topcode ID number below the symbol
            //--------------------------------------------
            String code = String.valueOf(top.getCode());
            int d = (int)top.getDiameter();
            int x = (int)top.getCenterX();
            int y = (int)top.getCenterY();
            int fw = g.getFontMetrics().stringWidth(code);
            
            g.setColor(Color.WHITE);
            g.fillRect((int)(x - fw/2 - 3),
                       (int)(y + d/2 + 6),
                       fw + 6, 12);
            g.setColor(Color.BLACK);
            g.drawString(code, x - fw/2, y + d/2 + 16);
            if(top.getCode() == 339) {
            	//Draw angle next to topcode code (
            	g.drawString(""+Math.toDegrees(top.getOrientation()), x - fw/2 + fw + 6, y + d/2 + 16);
            	
            	//Draw circle next to the code (on the paper)
            	g.setColor(Color.RED);
            	g.fillOval(
            			(int)(top.getCenterX()-(top.getDiameter()/2)*Math.sin(top.getOrientation())),
            			(int)(top.getCenterY()+(top.getDiameter()/2)*Math.cos(top.getOrientation())),
            			20,
            			20);
            }
            if(top.getCode() == 217) {
            	//Draw a disc over the topcode
            	g.setColor(Color.WHITE);
            	int xOval = (int) (top.getCenterX()-(top.getDiameter()/2));
            	int yOval = (int) (top.getCenterY()-(top.getDiameter()/2));
            	g.fillOval(xOval, yOval, (int)top.getDiameter(), (int)top.getDiameter());
            	
            	//Draw arc around the disc
            	g.setColor(Color.RED);
            	g.setStroke(new BasicStroke(10));
            	g.drawArc(xOval, yOval, (int)top.getDiameter(), (int)top.getDiameter(),0,(int)Math.toDegrees(top.getOrientation()));
            	
            	//Draw angle in the center
            	g.setColor(Color.BLACK);
            	g.drawString(""+Math.floor(Math.toDegrees(top.getOrientation())), x - fw/2, y);
            }
            
            if(top.getCode() == 341){
                float angle = 360/pieMenuItemsNb;
                for(int i=0; i<pieMenuItemsNb; i++){
                    double currentAngle = Math.toDegrees(-top.getOrientation());
                    float diameter = top.getDiameter();
                    System.out.println(currentAngle);
                    if(currentAngle < (i+1)*angle && currentAngle > i*angle){
                        diameter = 1.2f*top.getDiameter();
                    }
                    
                    g.setColor(pieMenuColors.get(i));
                    int xOval = (int) (top.getCenterX()-(diameter/2));
                    int yOval = (int) (top.getCenterY()-(diameter/2));
                    g.fillArc(xOval, yOval, (int)diameter, (int)diameter,(int)(i*angle),(int)(angle));
                    
                    g.setColor(Color.BLACK);
                    double angleInRadian = Math.toRadians(i*angle + angle/2);
                    g.drawString(""+i,(int)(top.getCenterX() + ((top.getDiameter()-15)/2)*Math.cos(angleInRadian)), (int)(top.getCenterY() - ((top.getDiameter()-15)/2)*Math.sin(angleInRadian)));
                }
            }
            
            for(String shape : selectedShapes){
                try {
                    bus.sendMsg("Palette:ModifierEpaisseur nom="+shape+" epaisseur="+1);
                    bus.sendMsg("Palette:ModifierCouleur nom="+shape+" couleurContour=black");
                } catch (IvyException ex) {
                    Logger.getLogger(M2IHM_TopCodes_Exo0.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            selectedShapes.clear();
            if(top.getCode() == 31){
                robot.mouseMove((int)top.getCenterX(), (int)top.getCenterY());
                double currentAngle = Math.toDegrees(top.getOrientation()+2*Math.PI);
                try {
                    bus.sendMsg("Palette:DeplacerObjetAbsolu nom=R1 x="+(int)top.getCenterX()+" y="+(int)top.getCenterY());
                    
                    bus.bindMsg("Palette:ResultatTesterPoint x="+(int)top.getCenterX()+" y="+(int)top.getCenterY()+" nom=(.*)", (client, args) -> {
                        if(!args[0].equals("R1")){
                            selectedShapes.add(args[0]);
                            for(String shape : selectedShapes){
                                try {
                                    bus.sendMsg("Palette:ModifierEpaisseur nom="+shape+" epaisseur="+5);
                                } catch (IvyException ex) {
                                    Logger.getLogger(M2IHM_TopCodes_Exo0.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    });
                    bus.sendMsg("Palette:TesterPoint x="+(int)top.getCenterX()+" y="+(int)top.getCenterY());
                } catch (IvyException ex) {
                    Logger.getLogger(M2IHM_TopCodes_Exo0.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(currentAngle < 135 && currentAngle > 45 && selectedShapes.size()>0){
                    selectedShape = selectedShapes.get(0);
                    System.out.println("selectedShape "+selectedShape);
                }else if(currentAngle < 360-45 && currentAngle > 360-135){
                    selectedShape = null;
                }
            }
            if(selectedShape != null){
                try {
                    bus.sendMsg("Palette:ModifierCouleur nom="+selectedShape+" couleurContour=255:0:0");
                } catch (IvyException ex) {
                    Logger.getLogger(M2IHM_TopCodes_Exo0.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(top.getCode() == 241){
                double currentAngle = Math.toDegrees(top.getOrientation()+2*Math.PI);
                if(currentAngle < 135 && currentAngle > 45){
                    if(!created){
                        try {
                            bus.sendMsg("Palette:CreerRectangle");
                            created = true;
                        } catch (IvyException ex) {
                            Logger.getLogger(M2IHM_TopCodes_Exo0.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }else if(currentAngle < 360-45 && currentAngle > 360-135){
                    if(!created){
                        try {
                            bus.sendMsg("Palette:CreerEllipse");
                            created = true;
                        } catch (IvyException ex) {
                            Logger.getLogger(M2IHM_TopCodes_Exo0.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }else{
                    created = false;
                }
                
            }
            
            // textual feedback
            System.out.println("TopCode détecté : " + code + 
            					" pos.  X  " + x + 
            					" pos.  y  " + y +
            					" diametre " + d);
         }
      }
   }


   
   public void actionPerformed(ActionEvent e) {
      if (e.getSource() == animator) repaint();
   }

      
/******************************************************************/
/*                        WINDOW EVENTS                           */
/******************************************************************/
   public void windowClosing(WindowEvent e) {
      this.webcam.closeCamera();
      this.webcam.uninitialize();
      frame.setVisible(false);
      frame.dispose();
      System.exit(0);
   }
   
   public void windowActivated(WindowEvent e) { } 
   public void windowClosed(WindowEvent e) { }
   public void windowDeactivated(WindowEvent e) { }
   public void windowDeiconified(WindowEvent e) { } 
   public void windowIconified(WindowEvent e) { } 
   public void windowOpened(WindowEvent e) { }


   public static void main(String[] args) {

      //--------------------------------------------------
      // Fix cursor flicker problem (sort of :( )
      //--------------------------------------------------
      System.setProperty("sun.java2d.noddraw", "");
      
      //--------------------------------------------------
      // Use standard Windows look and feel
      //--------------------------------------------------
      try { 
         UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName());
      } catch (Exception x) { ; }

      //--------------------------------------------------
      // Schedule a job for the event-dispatching thread:
      // creating and showing this application's GUI.
      //--------------------------------------------------
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               new M2IHM_TopCodes_Exo0();
            }
         });
   }
}

