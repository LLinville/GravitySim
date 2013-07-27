//Gravity simulation applet
//Levi Linville

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Action;
import javax.swing.JOptionPane;
import java.util.*;
import java.lang.*;
class Planet{
	public static double G=v.G;
	public boolean moveable=true;
	public double xpos=0,ypos=0;//position of planet on screen
	public double xvel=0,yvel=0;//velocity of planet (pixels/frame)
	public double xaccel=0,yaccel=0;//acceleration of planet(pixels/frame^2)
	public double mass=10;//mass of the planet

	Planet(boolean rndVel){//creates a new planet, with random velocity if rndVel is true, or zero velocity if rndVel is false
		Random rnd=new Random();
		xpos=rnd.nextDouble()*500+250;
		ypos=rnd.nextDouble()*500+250;
		if(rndVel){
			xvel=rnd.nextDouble()*10-5;
			yvel=rnd.nextDouble()*10-5;
		}
	}

	Planet(double xp,double yp){
		xpos=xp;
		ypos=yp;
	}

	Planet(double xp,double yp,boolean rndVel){
		Random rnd=new Random();
		xpos=xp;
		ypos=yp;
		if(rndVel){
			xvel=rnd.nextDouble()*10-5;
			yvel=rnd.nextDouble()*10-5;
		}
	}

	Planet(double xp,double yp,double m,boolean move){
		xpos=xp;
		ypos=yp;
		mass=m;
		moveable=move;
	}

	Planet(double xp,double yp,double xv,double yv){
		xpos=xp;
		ypos=yp;
		xvel=xv;
		yvel=yv;
	}

	Planet(double xp,double yp,double xv,double yv,double xa,double ya,double m){
		xpos=xp;
		ypos=yp;
		xvel=xv;
		yvel=yv;
		xaccel=xa;
		yaccel=ya;
		mass=m;
	}

	public void setVel(double x,double y){
		xvel=x;
		yvel=y;
	}

	public String toString(){
		String result="Planet. X: "+xpos+" Y: "+ypos;
		return result;
	}

	public void calcPull(Planet[] planets,boolean[] exists){//calculate the acceleration of the planet from an array of planets and an array of if an individual planet exists
		xaccel=0;
		yaccel=0;
		double xa=0,ya=0;
		double mag=0;
		double factor=0;
		double dx=0,dy=0;
		for(int a=0;a<planets.length;a++){
			if(exists[a]&&planets[a]!=null){
				if(planets[a].xpos!=xpos&&planets[a].ypos!=ypos){
					dx=planets[a].xpos-xpos;
					dy=planets[a].ypos-ypos;
					mag=Math.sqrt(dx*dx+dy*dy);//dist between planets
					factor = G*(planets[a].mass*mass)/(mag*mag)/mass;//pull of gravity between planets
					xa+=factor*dx/mag;
					ya+=factor*dy/mag;
					//System.out.println("xpos: "+planets[a].xpos+" "+xpos);
					//System.out.println("ypos: "+planets[a].ypos+" "+ypos);
					//System.out.printf("\ndx:%f\ndy:%f\nmag:%f\nfactor:%f\nxa:%f\nya:%f\n",dx,dy,mag,factor,xa,ya);
				}
			}
		}
		xaccel+=xa;
		yaccel+=ya;
	}

	public void move(){
		if(moveable){
			xvel+=xaccel;
			yvel+=yaccel;
			double speed=Math.sqrt(xvel*xvel+yvel*yvel);
			if(speed>v.maxPlanetSpeed){//clamp velocity to maximum of v.maxPlanetSpeed
				xvel=xvel/speed*v.maxPlanetSpeed;
				yvel=yvel/speed*v.maxPlanetSpeed;
			}
			xpos+=xvel*v.step;
			ypos+=yvel*v.step;
		}
	}


}
class v{
	static int pathLineLength=100;//length of red path line. increase for a longer line
	static int numPlanets=1;//number of moving planets
	static int numFixedPlanets=2;//number of non-moveable planets. These planets must be initialized separately, or will default to moving planets
	static boolean rndVel=true;//new planets will have a random velocity if rndVel is true, or zero velocity if rndVel is false
	static int radius=7;//default planet radius
	static double G=100;//power of gravity. increase this for more pull between objects
	static double step=1;//time step of simulation. lower this to increase accuracy
	static int fps=10;//times to update simulation per second. lower this to increase performance
	static double maxPlanetSpeed=20;//fastest a planet can travel

	static int delay=(int)Math.floor(1000/fps);//calculates the delay in ms between updates
}

public class GravitySim extends Applet implements MouseListener,MouseMotionListener,KeyListener{
	Planet[] planets=new Planet[v.numPlanets+v.numFixedPlanets+100];
	boolean[] exists=new boolean[v.numPlanets+v.numFixedPlanets+100];
	int mx=0,my=0;
	Random rnd=new Random();
	int delay=v.delay;//time to wait between updates

	boolean aline=true,vline=true,gline=true,randnew=false,paused=false;
	public void init(){
		addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setFocusable(true);

		for(int a=0;a<planets.length;a++){exists[a]=false;}

		for(int a=0;a<v.numPlanets;a++){//create moveable planets
			System.out.println("Creating planet number "+a);
			planets[a]=new Planet(v.rndVel);
			exists[a]=true;

		}

		//creating fixed planets
		//planets[0]=new Planet(476,622);
		setSize(new Dimension(600,600));
		planets[v.numPlanets]=new Planet(250,250,10,false);
		exists[v.numPlanets]=true;
		planets[v.numPlanets+1]=new Planet(350,250,10,false);
		exists[v.numPlanets+1]=true;

		//timer to update planets
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){
			public void run(){
				if(!paused){
					updatePlanets();
				}
				repaint();
			}
		}, 0, delay);
	}

    public void keyPressed(KeyEvent e){
		char c=e.getKeyChar();
		if(c=='h'){
			System.out.println("Help menu opened");
			JOptionPane.showMessageDialog(null, "Click mouse to make a new planet\nRed line is the acceleration vector of that planet\nBlue line is the velocity vector of that planet\nGreen line shows the flow of the gravitational field at a point\n\n"
			+"Key Shortcuts:\nH - Help and Commands\nA - Toggle red acceleration lines\nV - Toggle blue velocity lines\nG - Toggle green gravity field line\nR - Toggle random direction of new planets\nP - Pause/Unpause");
		}
		if(c=='a'){
			if(aline==true){aline=false;}else{aline=true;}
			System.out.println("Acceleration line toggled");
		}
		if(c=='v'){
			if(vline==true){vline=false;}else{vline=true;}
			System.out.println("Velocity line toggled");
		}
		if(c=='g'){
			if(gline==true){gline=false;}else{gline=true;}
			System.out.println("Gravity field line toggled");
		}
		if(c=='r'){
			if(randnew==true){randnew=false;}else{randnew=true;}
			System.out.println("New planet random direction toggled");
		}
		if(c=='p'){
			if(paused==true){
				paused=false;
				System.out.println("Paused");
			}else{
				paused=true;
				System.out.println("Unpaused");
			}
		}
		e.consume();
    }
    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}

    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseClicked(MouseEvent e){}
    public void mousePressed(MouseEvent e){
		mx=e.getX();
		my=e.getY();
		for(int a=0;a<planets.length;a++){
			if(!exists[a]){
				exists[a]=true;
				planets[a]=new Planet(mx,my,randnew);
				break;
			}

		}
		System.out.println("created planet at "+mx+","+my);
	}
    public void mouseReleased(MouseEvent e){}
    public void mouseMoved(MouseEvent e){
		mx=e.getX();
		my=e.getY();
	}
    public void mouseDragged(MouseEvent e){}

	void updatePlanets(){
		for(int a=0;a<planets.length;a++){
			if(exists[a]){
				if(planets[a].xpos<0||planets[a].xpos>600||planets[a].ypos<0||planets[a].ypos>600){exists[a]=false;}
				planets[a].calcPull(planets,exists);//calculate acceleration of each planet
				planets[a].move();//move the planet
			}
			//System.out.println("Calculating pull for planet "+a);
		}
		repaint();
	}

	public static Planet collide(Planet p1,Planet p2){
		Planet p3=new Planet((p1.xpos+p2.xpos)/2,(p1.ypos+p2.ypos)/2);
		p3.setVel(p1.xvel+p2.xvel,p1.yvel+p2.yvel);
		return p3;
	}

	//this calculates the line showing the gravitational field
	public int[][] calcPathLine(int x, int y,Planet[] planets,boolean[] exists){//return a 2d array of future path of planet at (x,y) in the form of {{x coords},{y coords}}
		int[][] linePoints=new int[2][v.pathLineLength];
		Planet testPlanet=new Planet(mx,my);
		for(int a=0;a<v.pathLineLength;a++){
			linePoints[0][a]=(int)testPlanet.xpos;
			linePoints[1][a]=(int)testPlanet.ypos;
			testPlanet.calcPull(planets,exists);
			testPlanet.move();
		}
		return linePoints;
	}

	public void paint(Graphics g){
		int xpos,ypos,r=v.radius;
		double xvel,yvel,xaccel,yaccel;
		Font f = new Font("Dialog", Font.BOLD, 15);
		g.setFont(f);
		g.drawString ("Press H for help and commands.", 20,20);
		if(paused){g.drawString ("Simulation Paused", 200,550);}
		for(int a=0;a<planets.length;a++){
			if(exists[a]){
				xpos=(int)planets[a].xpos;
				ypos=(int)planets[a].ypos;
				xvel=planets[a].xvel;
				yvel=planets[a].yvel;
				xaccel=planets[a].xaccel;
				yaccel=planets[a].yaccel;
				//System.out.println("Drawing");

				//draw planet
				g.setColor(Color.black);
				g.drawOval(xpos-r,ypos-r,r*2,r*2);

				//draw velocity line
				if(vline){
					g.setColor(Color.blue);
					g.drawLine(xpos,ypos,(int)Math.floor((xpos+xvel/v.step*10)),(int)Math.floor((ypos+yvel/v.step*10)));
				}

				if(aline){
					//draw acceleration line
					g.setColor(Color.red);
					g.drawLine(xpos,ypos,(int)Math.floor((xpos+xaccel/v.step*100)),(int)Math.floor((ypos+yaccel/v.step*200)));
				}

				if(gline){
					//draw field line
					g.setColor(Color.green);
					g.drawPolyline(calcPathLine(mx,my,planets,exists)[0],calcPathLine(mx,my,planets,exists)[1],v.pathLineLength);
				}
			}
		}
	}
}
