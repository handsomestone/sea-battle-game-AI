package MSTCOSSDK;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class AI extends AbstractAI {
	public AI(OSInterface Interface) {
		super(Interface);
	}
	
	List<ShipInfo> ships,a,b;
	List<Task> avoidCollTask = new ArrayList<Task>();
	List<ShipInfo> lastSeenEnemy = new ArrayList<ShipInfo>();
	List<ShipInfo> lastSeenMine = new ArrayList<ShipInfo>();
	int signalToAssemple = 0;
	
	int[] blockTimes = {0,0,0,0,0,0,0,0,0,0,0,0,0};
	int[] avoidTimes = {0,0,0,0,0,0,0,0,0,0,0,0,0};
	int lastTime = 0;
	int interverl =0;
	Random random = new Random();
	float d;
	int k;
	boolean[] avoidCol = {false,false,false,false,false,false,false,false,false,false,false};
	boolean[] repair = {false,false,false,false,false,false,false,false,false,false,false};
	private int isCollision(ShipInfo a,List<ResourceInfo> resource)
	{
		float predictRadius = 90;
		float predictRadius_B =100;
		float collisionRadius = 60;
		float a_xais = a.PositionX;
		float a_yais = a.PositionY;

		if((Math.abs(a_xais+a.DirectionX*predictRadius_B))>=1024)
		{	
			return 1;
		}
		else if((Math.abs(a_yais+a.DirectionY*predictRadius_B))>=1024)
		{	
			return 1;
		}
		for(int i=0;i<resource.size();i++)
		{
			//System.out.println("(a_xais+a.DirectionX*predictRadius) "+(a_xais+a.DirectionX*predictRadius));
		//	System.out.println("(a_yais+a.DirectionY*predictRadius) "+(a_yais+a.DirectionY*predictRadius));
			//System.out.println("(resource.get(i).PositionX-32) "+(resource.get(i).PositionX-collisionRadius)+" (resource.get(i).PositionX+32)"+(resource.get(i).PositionX+collisionRadius));
			//System.out.println("(resource.get(i).PositionY-32) "+(resource.get(i).PositionY-collisionRadius)+" (resource.get(i).PositionY+32)"+(resource.get(i).PositionY+collisionRadius));
			if((a_xais+a.DirectionX*predictRadius)>(resource.get(i).PositionX-collisionRadius)&&(a_xais+a.DirectionX*predictRadius)<(resource.get(i).PositionX+collisionRadius))
			{	
				if((a_yais+a.DirectionY*predictRadius)>(resource.get(i).PositionY-collisionRadius)&&(a_yais+a.DirectionY*predictRadius)<(resource.get(i).PositionY+collisionRadius))
				{
					
					return 2;
				}
			}	
		}
		return 0;
	}
	private int isWillColWithTeam(ShipInfo myShip,List<ShipInfo> listship,int delta)
	{
		float distance = 4096;
		float Mindist = 4096;
		int shipP = 10;
		float predictX = 0;
		float predictY = 0;
		float predictXh =0;
		float predictYh =0;
		int isCol = -1;
		for(int i=0;i<listship.size();i++)
		{
			distance=distBetween2Ship(myShip,listship.get(i));
			if(distance<Mindist)
			{
				Mindist = distance;
				shipP=i;
			}
		}
		if(shipP != 10)
		{
			predictX = myShip.PositionX+myShip.VelocityX*delta;
			predictY = myShip.PositionY+myShip.VelocityY*delta;
			predictXh = listship.get(shipP).PositionX+listship.get(shipP).VelocityX*delta;
			predictYh = listship.get(shipP).PositionY+listship.get(shipP).VelocityY*delta;
			distance = disP2P(predictX,predictY,predictXh,predictYh);
			if(distance<=30)
			{
				isCol=shipP;
			}
		}
		return isCol;	
	}
	private float getLeastDegree(ShipInfo my,ShipInfo he)
	{
		float e_vectorX = 0;
		float e_vectorY = 0;
		float degree = 0;
		float rotation = 0;
		e_vectorX = he.PositionX - my.PositionX;
		e_vectorY = he.PositionY - my.PositionY;
		degree = getRotateAngle(my.DirectionX,my.DirectionY,e_vectorX,e_vectorY);
		if(degree>=45&&degree<=135)
		{
			rotation = degree-45+90;
		}
		else if(degree<=-45&&degree>=-135)
		{
			rotation = Math.abs(degree)-45+90;
			rotation = -rotation;
		}
		return rotation;
	}
	/*x1,y1 is ship's DirectionX and Y: x2,y2 is point to point vector*/
	private float getRotateAngle(float x1, float y1, float x2, float y2)
	{
	 double epsilon = 1.0e-6;
	// const double nyPI = acos(-1.0);
	 float dist, dot, degree, angle;
	 
	 // normalize
	 
	 dist = (float)Math.sqrt( x1 * x1 + y1 * y1 );
	 x1 /= dist;
	 y1 /= dist;
	 dist = (float)Math.sqrt( x2 * x2 + y2 * y2 );
	 x2 /= dist;
	 y2 /= dist;
	 // dot product
	 dot = x1 * x2 + y1 * y2;
	 if ( Math.abs(dot-1.0) <= epsilon ) 
	  degree = 0;
	 else if ( Math.abs(dot+1.0) <= epsilon ) 
	  degree = -180;
	 else {
	  float cross;
	  
	  angle =(float) Math.acos(dot);/*I am sure it is in 0-1*/
	  degree = (float)Math.toDegrees(angle);
	  //cross product
	  cross = x1 * y2 - x2 * y1;
	  // vector p2 is clockwise from vector p1 
	  // with respect to the origin (0.0)
	  if (cross < 0 ) { 
	   degree =-degree;
	  } 
	   
	 }
	 //degree = angle *  180.0 / nyPI;
	 return degree;
	}
	private float disP2P(float x1,float y1,float x2,float y2)
	{
		return (float)Math.ceil(Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2)));
	}
	private float rotationDegree(ShipInfo a,List<ResourceInfo> resource)
	{
		float a_xais = a.PositionX;
		float a_yais = a.PositionY;
		float detecRadius = 100;
		float collisionRadius = 80;
		float degree = 0;
		float delta = 0;
		float Maxdegree = 0;
		float angle = 0;
		float sin = 0;
		int colResource = 0;
		for(int i=0;i<resource.size();i++)
		{
			if((a_xais+a.DirectionX*detecRadius)>(resource.get(i).PositionX-collisionRadius)&&(a_xais+a.DirectionX*detecRadius)<(resource.get(i).PositionX+collisionRadius))
			{	if((a_yais+a.DirectionY*detecRadius)>(resource.get(i).PositionY-collisionRadius)&&(a_yais+a.DirectionY*detecRadius)<(resource.get(i).PositionY+collisionRadius))
				{
					colResource =  i;
					break;
				}
			}
		}
		degree = getRotateAngle(a.DirectionX,a.DirectionY,resource.get(colResource).PositionX -a_xais,resource.get(colResource).PositionY -a_yais );
		sin = 32/disP2P(a_xais,a_yais,resource.get(colResource).PositionX,resource.get(colResource).PositionY);
		angle = (float)Math.asin(sin);
		Maxdegree = (float)Math.toDegrees(angle);
		if(degree>=0)
		{
			degree = -(Maxdegree-degree+delta);
		}
		else
		{
			degree = Maxdegree+degree+delta;
		}
		return degree;
	}
	private float distBetween2Ship(ShipInfo a,ShipInfo b)
	{
		return (float)Math.ceil(Math.sqrt((a.PositionX - b.PositionX) * (a.PositionX - b.PositionX) + (a.PositionY - b.PositionY) * (a.PositionY - b.PositionY)));
	}
	private int isEnemyDo(ShipInfo myShip,ShipInfo enemyShip)
	{
		float future_x =0;
		float future_y =0;
		boolean isEscape = false;
		float distanceNow = 0;
		float distanceFuture = 0;
		float time = 1;
		int enemyDo = 0; /*0 stay 1 fight 2 escape 3 align Move*/
		distanceNow = distBetween2Ship(myShip,enemyShip);
		future_x = enemyShip.PositionX+enemyShip.VelocityX*time;
		future_y = enemyShip.PositionY+enemyShip.VelocityY*time;
		distanceFuture = disP2P(myShip.PositionX,myShip.PositionY,future_x,future_y);
		if(distanceFuture > distanceNow)
		{
			enemyDo = 2;
		}
		else if(distanceFuture < distanceNow)
		{
			enemyDo = 1;
		}
		else
		{
			enemyDo = 0;
		}
		return enemyDo;
	}
	
	private int isEnemyDoVector(ShipInfo myShip,ShipInfo enemyShip)
	{
		float degree = 0;
		int enemyDo = 1; /*0 stay 1 fight 2 escape 3 align Move*/
		float dot =0;
		float angle = 0;
		double epsilon = 1.0e-6;
		 dot = myShip.DirectionX * enemyShip.DirectionX + myShip.DirectionY * enemyShip.DirectionY;
		 if ( Math.abs(dot-1.0) <= epsilon ) 
		  degree = 0;
		 else if ( Math.abs(dot+1.0) <= epsilon ) 
		  degree = -180;
		 else {		  		  
		  angle =(float) Math.acos(dot);/*I am sure it is in 0-1*/
		  degree = (float)Math.toDegrees(angle);
		 }
		 if(degree==-180||degree>90)
		 {
			 enemyDo = 1; /*head towards me*/
		 }
		 else if(degree<=90&&degree>0)
		 {
			 enemyDo = 2; /*escape*/
		 }
		 else if(degree == 0)
		 {
			 enemyDo = 3; /*align move*/
		 }
		 else
		 {
			 if(enemyShip.CurrentSpeed == 0)
				 enemyDo =0;
		 }
		return enemyDo;
	}
	
	private float getMinDegree(ShipInfo myShip,List<ShipInfo> Enemyship)
	{
		if(Enemyship.size() == 0)
			return 0;
		float e_vectorX = 0;
		float e_vectorY = 0;
		float degreeR = 0;
		float degreeL = 0;
		float degree =0;
		float MindegreeL = 180;
		float MindegreeR = 180;
		for(int i=0;i<Enemyship.size();i++)
		{
			e_vectorX = Enemyship.get(i).PositionX - myShip.PositionX;
			e_vectorY = Enemyship.get(i).PositionY - myShip.PositionY;
			degree = getRotateAngle(myShip.DirectionX,myShip.DirectionY,e_vectorX,e_vectorY);
			if(degree<45&&degree>=0)
			{
				degreeR = 45-degree;
				degreeR = -degreeR;
				if(Math.abs(degreeR)<Math.abs(MindegreeR))
				{
					MindegreeR = degreeR;
				}
			}
			else if(degree>135&&degree<=180)
			{
				degreeR = degree-135;
				if(Math.abs(degreeR)<Math.abs(MindegreeR))
				{
					MindegreeR = degreeR;
				}
			}
			else if(degree>-45&&degree<0)
			{
				degreeL= 45-Math.abs(degree);
				if(Math.abs(degreeL)<Math.abs(MindegreeL))
				{
					MindegreeL = degreeL;
				}
			}
			else if(degree<-135&&degree>-180)
			{
				degreeL=Math.abs(degree)-135;
				degreeL=-degreeL;
				if(Math.abs(degreeL)<Math.abs(MindegreeL))
				{
					MindegreeL = degreeL;
				}
			}
		}
		if(Math.abs(MindegreeL)<Math.abs(MindegreeR))
		{
			return MindegreeL;
		}
		else if(Math.abs(MindegreeL)>=Math.abs(MindegreeR))
		{
			return MindegreeR;
		}
		return 0;
	}
	private int[] getInShootRangeShip(ShipInfo myShip,List<ShipInfo> Enemyship)
	{
		int[] canShootShip = {0,0}; /*0 Right 1 Left*/
	
		if(Enemyship.size() == 0)
			return canShootShip;
		float e_vectorX = 0;
		float e_vectorY = 0;
		float degree = 0;
		float dist = 0;
		float Min = 4096,MinR=4096;
		//System.out.println("!!!!!!!!!!!!!"+Enemyship.size());
		for(int i=0;i<Enemyship.size();i++)
		{
			e_vectorX = Enemyship.get(i).PositionX - myShip.PositionX;
			e_vectorY = Enemyship.get(i).PositionY - myShip.PositionY;
			degree = getRotateAngle(myShip.DirectionX,myShip.DirectionY,e_vectorX,e_vectorY);
			if(degree<=-45&&degree>=-135)
			{
				if(Enemyship.get(i).Armor<150)
				{
					canShootShip[1] = Enemyship.get(i).ID;
					//System.out.println("L armor "+canShootShip[1]);
				}
				else
				{
					dist = disP2P(myShip.PositionX,myShip.PositionY,Enemyship.get(i).PositionX,Enemyship.get(i).PositionY);
					if(dist<Min)
					{
						Min = dist;
						canShootShip[1] = Enemyship.get(i).ID;
					}
					//System.out.println("!!!!!L armor "+Enemyship.get(i).ID);
				}
			}
			else if(degree>=45&&degree<=135)
			{
				if(Enemyship.get(i).Armor<150)
				{
					canShootShip[0] = Enemyship.get(i).ID;
					//System.out.println("R armor "+canShootShip[0]);
				}
				else
				{
					dist = disP2P(myShip.PositionX,myShip.PositionY,Enemyship.get(i).PositionX,Enemyship.get(i).PositionY);
					if(dist<MinR)
					{
						MinR = dist;
						canShootShip[0] = Enemyship.get(i).ID;
					}
					//System.out.println("!!!!!R armor "+Enemyship.get(i).ID);
				}
			}
		}
		
	
		return canShootShip;
	}
	private ShipInfo getNearestShip(ShipInfo ship,List<ShipInfo> shipList)
	{
		ShipInfo shipN = null;
		float minDist =4096;
		float distance = 0;
		if(shipList.size() == 0)
			return shipN;
		for(int i=0;i<shipList.size();i++)
		{
			distance = distBetween2Ship(ship,shipList.get(i));
			if(distance<minDist)
			{
				minDist = distance;
				shipN = shipList.get(i);
			}
		}
		return shipN;
	}
	private ShipInfo getWeakestShip(List<ShipInfo> shipList)
	{
		ShipInfo ship = null;
		float minArmor =1000;
		if(shipList.size() == 0)
			return ship;
		for(int i=0;i<shipList.size();i++)
		{
			if(shipList.get(i).Armor<minArmor)
			{
				minArmor = shipList.get(i).Armor;
				ship = shipList.get(i);
			}
		}
		return ship;
	}
	private float distBetweenShipIsland(ShipInfo ship,ResourceInfo resource)
	{
		float distanceToKernel = 0;
		float deltaDistance = 100;
		distanceToKernel = (float)Math.ceil(Math.sqrt((ship.PositionX -resource.PositionX )*(ship.PositionX -resource.PositionX )+(ship.PositionY -resource.PositionY )*(ship.PositionY -resource.PositionY )));
		if(distanceToKernel<196)
			return 0;
		return (distanceToKernel - deltaDistance);
	}
	private float dist(ShipInfo a, ShipInfo b)
	{
		return (a.PositionX - b.PositionX) * (a.PositionX - b.PositionX) + (a.PositionY - b.PositionY) * (a.PositionY - b.PositionY);
	}
	private float Score(float distance,int priority,float modifier)
	{
		return (6-priority+modifier)/distance;
	}
	private int getMyNearestEnemyIsland(ShipInfo myShip,List<ResourceInfo> resources)
	{
		int positionInResource = 0;
		float Mindist = 4096;
		for(int i=0;i<resources.size();i++)
		{
			float dist = distBetweenShipIsland(myShip,resources.get(i));
			if(dist < Mindist)
			{
				Mindist = dist;
				positionInResource = i;
			}
		}
		return positionInResource;
	}
	private int shipID2pInList(int shipID,List<ShipInfo> myShips)
	{
		int positionInList = 0;
		for(int i=0;i<myShips.size();i++)
		{
			if(myShips.get(i).ID==shipID)
			{
				positionInList = i;
				return positionInList;
			}
		}
		return positionInList;
	}
	private Task getMyLastAssignTask(int shipID,List<Task> lastAssignTask)
	{
		Task myTask = null;
		if(lastAssignTask.size()==0)
			return null;
		for(int i=0;i<lastAssignTask.size();i++)
		{
			myTask = lastAssignTask.get(i);
			if(shipID == myTask.ShipID)
				return myTask;
		}
		return myTask;
	}
	/*modifie new centerShip*/
	private int assempleCenterShip(List<ShipInfo> myShips)
	{
		int centerShipID = 0;
		float averageX = 0;
		float averageY = 0;
		float totalX = 0;
		float totalY = 0;
		float minDist =4096;
		
		for(int i=0;i<myShips.size();i++)
		{
			totalX += myShips.get(i).PositionX;
			totalY += myShips.get(i).PositionY;
		}
		averageX = totalX/myShips.size();
		averageY = totalY/myShips.size();
		for(int j=0;j<myShips.size();j++)
		{
			float distance = 4098;
			distance = disP2P(averageX,averageY,myShips.get(j).PositionX,myShips.get(j).PositionY);
			if(distance < minDist)
			{
				minDist = distance;
				centerShipID = j;
			}
		}
		return centerShipID;
	}
	
	private float getAverageX(List<ShipInfo> myShips)
	{
		float averageX = 0;		
		float totalX = 0;
		for(int i=0;i<myShips.size();i++)
		{
			totalX += myShips.get(i).PositionX;
			
		}
		averageX = totalX/myShips.size();
		return averageX;
	}
	private float getAverageY(List<ShipInfo> myShips)
	{
		float averageX = 0;		
		float totalX = 0;
		for(int i=0;i<myShips.size();i++)
		{
			totalX += myShips.get(i).PositionX;
			
		}
		averageX = totalX/myShips.size();
		return averageX;
	}
	private boolean isNeedAssemple(ShipInfo ship,List<ShipInfo> myShips)
	{
		boolean needAssemple = false;
		float dist2CenterShip = 4096;
		float assempleRadius = 300;
		int centerShipID =0;
		centerShipID = assempleCenterShip(myShips);
		dist2CenterShip = distBetween2Ship(ship,myShips.get(centerShipID));
		if(dist2CenterShip > assempleRadius)
		{	
			needAssemple = true;
		}
		return needAssemple;
	}
	private boolean isNeedRotate(ShipInfo ship,ShipInfo centerShip)
	{
		boolean needRotate = false;
		float dist2CenterShip = 4096;
		float rotateRadius = 50;
		dist2CenterShip = distBetween2Ship(ship,centerShip);
		if(ship.IsMoving == false)
		{
			needRotate = false;
		}
		if(dist2CenterShip<rotateRadius)
		{
			if(ship.Rotation != centerShip.Rotation)
			{
				needRotate = true;
			}
		}
		return needRotate;
	}
	/*return position in list*/
	/*
	private int assempleCenterShip(List<ShipInfo> myShips)
	{
		int centerShipID = 0;
		int maxNearShip = 0; 
		float nearRadius = 50;
		for(int i=0;i<myShips.size();i++)
		{
			int i_nearShips = 0;
			for(int j=0;j<myShips.size();j++)
			{
					float i_xais = myShips.get(i).PositionX;
					float i_yais = myShips.get(i).PositionY;
					float j_xais = myShips.get(j).PositionX;
					float j_yais = myShips.get(j).PositionY;
					System.out.println("i="+myShips.get(i).ID+" j="+myShips.get(j).ID);
					System.out.println("boundX (i_xais-nearRadius)"+(i_xais-nearRadius)+"(i_xais+nearRadius)"+(i_xais+nearRadius));
					System.out.println("boundY (i_yais-nearRadius)"+(i_yais-nearRadius)+"(i_Yais+nearRadius)"+(i_yais+nearRadius));
					System.out.println(" j_xais "+j_xais+" j_yais "+j_yais);
					if(j_xais>(i_xais-nearRadius)&&j_xais<(i_xais+nearRadius))
					{
						if(j_yais>(i_yais-nearRadius)&&j_yais<(i_yais+nearRadius))
						{
							
							i_nearShips++;
							System.out.println("i----"+i_nearShips);
						}
					}
			}
			if(i_nearShips >= maxNearShip)
			{
				System.out.println("inearShips . maxNearShip"+i_nearShips+" "+maxNearShip);
				System.out.println("choose ID "+myShips.get(i).ID);
				centerShipID = i;
				maxNearShip = i_nearShips;
			}
		}
		return centerShipID;
	}*/
	
	private  int turn =0;/*test */
	private  int Me ;
	private  int He ;
	private int StartTime = 0;
    private boolean stateChange =false;
	@Override
	public void Iteration()
	{

		/*
		 * in order to make Ai more intelegent,we must have a strategy for
		 * what to do,each decision is juged by the realtime situation
		 * each ship must choose the high score task,no matter wetheer it
		 * is assigned a task before or not. 
		 * now we just implement several simple strategy
		 * conquer island priority 0: modifier enemyIslandNum
		 * assemple myTeam  priority 1(togetther is beter):modifier enemyShip 
		 * attack the enmeny priority 2: modifier myNum-enemyNum
		 * escape the enmeny priority 3: modifier myNum-enemyNum
		 * repair the ship priority 4 :modifier 5*(shipHealth/1000)
		 */
		List<ShipInfo> Myship = new ArrayList<ShipInfo>();
		List<ShipInfo> Enemyship = new ArrayList<ShipInfo>();
		List<ShipInfo> Allship = new ArrayList<ShipInfo>();
		List<ResourceInfo> Myresource = new ArrayList<ResourceInfo>();
		List<ResourceInfo> Enemyresource = new ArrayList<ResourceInfo>();
		List<ResourceInfo> Neutralresource = new ArrayList<ResourceInfo>();
		List<ResourceInfo> Allresource = new ArrayList<ResourceInfo>();

		List<Task> currentTask = new ArrayList<Task>();
		//Map<int,ShipInfo> getMyshipByID= new HashMap<int,ShipInfo>();
		//Map<int,ShipInfo> getHeshipByID= new HashMap<int,ShipInfo>();
		//ShipInfo[] getMyshipByID = new ShipInfo[5];
		//ShipInfo[] getHeshipByID = new ShipInfo[5];

		int currentTime = Interface.TimeLeft;
		//System.out.println("currentTime "+currentTime);
		boolean[] freeShip = {false,false,false,false,false};
			
		int command = 0;
		StartTime = Interface.TimeLeft;
		Interface.Data();
		command++;
		PossibleAssignment possibleAssign = new PossibleAssignment();
		
		Allship = Interface.Ship;
		Allresource = Interface.Resource;
		 /* case 0: conquer enemybase
		  * case 1: attack enemy
		  * case 2: conquer Neutralbase
		  * case 3: move to mybase
		  * case 4: go to find enemy
		  * special case like avoid collision we should repair our ship if armor less than 200
		  * */
		int state = 10;
		//System.out.println("Allship"+Allship.size());
		if(turn == 0)
		{
			Me = Interface.Faction;
			if(Me == 1)
				He = 2;
			else
				He = 1;
		}
		

		for(int i=0;i<Allship.size();i++)
		{
			
			if(Allship.get(i).Faction == Interface.Faction)
			{
				Myship.add(Allship.get(i));
			
			
			}	
			if(Allship.get(i).Faction == He)
			{
				Enemyship.add(Allship.get(i));
			
			}
		}
	
		for(int k=0;k<Allresource.size();k++)
		{
			//System.out.println("Allresource "+Allresource.get(k).PositionX+" "+Allresource.get(k).PositionY);
			if((Allresource.get(k).Faction) == Me)
				Myresource.add(Allresource.get(k));
			else if(Allresource.get(k).Faction == He)
				Enemyresource.add(Allresource.get(k));
			else
				Neutralresource.add(Allresource.get(k));
		}
		//System.out.println("currentTask.size()for(int g_i=0;g_i<Myship.size();g_i++)!!!!!!!!!!!!!!!!"+currentTask.size());
		for(int g_i=0;g_i<Myship.size();g_i++)
		{
			//System.out.println("currentTask.size()    "+currentTask.size());
			ShipInfo myShip = Myship.get(g_i);
			int m_state = 10;
			RadiusDetective shipDetec = null;
			shipDetec = new RadiusDetective(Myship.get(g_i),lastTime-StartTime);
			if(Enemyresource.size()>0)
			{
				if(shipDetec.isInWar(myShip, Enemyship))
				{
					int nearestEnemyPosition = shipDetec.getMyNearestEnemy(Enemyship);
					int nearestBasePosition = shipDetec.getMyNearestEnemyBase(Enemyresource);
					float e_distance = distBetween2Ship(Myship.get(g_i),Enemyship.get(nearestEnemyPosition));
					float r_distance = distBetweenShipIsland(Myship.get(g_i),Enemyresource.get(nearestBasePosition));
					if((e_distance-(r_distance-100))>=350)
					{
						state = 0;
					}
			
					else 
					{
						state = 1;
					}
				}
				else
				{
					state = 0;
				}
			}
			else if(shipDetec.isInWar(myShip, Enemyship))
			{
				state =1;
			}
			else if(Enemyresource.size()==0&&Neutralresource.size()>0)
			{
				state = 2;
			}
			else if((Enemyresource.size()==0)&&(Enemyship.size()>0))
			{
				state =1 ;
			}
			else if((Enemyship.size()==0)&&(lastSeenEnemy.size()>0))
			{
				state = 4;
			}
			else if((Enemyship.size()==0)&&(lastSeenEnemy.size()==0))
			{
				state = 3;
			}
			switch(state)
			{
				case 0: int i_eBase = shipDetec.getMyNearestEnemyBase(Enemyresource);
						ResourceInfo enemyBase = Enemyresource.get(i_eBase);
						if(enemyBase != null)
						{																	
								Task newTask = new Task(myShip.ID);
								newTask.priority = 0;
								newTask.target_x = enemyBase.PositionX;
								newTask.target_y = enemyBase.PositionY;
								newTask.isStartRotatingTo = true;
								newTask.isStartMoving = true;
								currentTask.add(newTask);															
							//	System.out.println("I want to Attack EnemyBase"+myShip.ID);	
							//	System.out.println("currentTask.size()--- "+currentTask.size());
						}
						
					    break;
				case 1: int i_enemy = shipDetec.getMyNearestEnemy(Enemyship);
						ShipInfo enemyShip = Enemyship.get(i_enemy);
						float distance = 0;
						int todo = 0;
						if(enemyShip!=null)
						{
							distance = distBetween2Ship(myShip,enemyShip);
							if(distance>350)
							{
								todo = 1;
							}
							else if(distance == 350)
							{
								todo = 5;
							}
							else if(distance<350&&distance>=250)
							{
								todo = 2; /*can battle*/
							}
							else if(distance<250&&distance>=60)
							{
								todo = 2;
							}
							else if(distance<60)
							{
								todo = 4;
							}
							
						}
				
						if(todo == 1)
						{
							Task newTask = new Task(myShip.ID);
							newTask.isStartRotatingTo = true;
							if(myShip.IsMoving == false)
							{
								newTask.isStartMoving = true;
							}
							newTask.target_x = enemyShip.PositionX;
							newTask.target_y = enemyShip.PositionY;
							currentTask.add(newTask);
							//System.out.println("Move Clear to Enemy "+myShip.ID);
						}
						else if(todo == 2)
						{
							int guessEnemyAction = -1;
							guessEnemyAction = isEnemyDoVector(myShip,enemyShip);
							if(guessEnemyAction == 1)
							{
								if(Enemyship.size()!=0)
								{
									int[] canShootShip = getInShootRangeShip(myShip,Enemyship);
									float[] cool = myShip.CooldownRemain;
									boolean leftWillShoot = false;
									boolean rightWillShoot = false;
									if(cool[0] == 0&&canShootShip[0]!=0)
									{
										
											Task rshoot = new Task(myShip.ID);
											rshoot.TargetID = canShootShip[0];
											rshoot.isAttack = true;
											currentTask.add(rshoot);
											//System.out.println("R is shooting!!!!!");
											rightWillShoot = true;
									}
									if(cool[1]==0&&canShootShip[1]!=0)
									{
										
											Task lshoot = new Task(myShip.ID);
											lshoot.TargetID = canShootShip[1];
											lshoot.isAttack = true;
											currentTask.add(lshoot);
											leftWillShoot = true;
											//System.out.println("L is shooting!!!!!!!");
									}
									if(cool[0]==0&&canShootShip[0]==0)
									{
										/*Right can not shoot buf */
										if(cool[1]!=0&&canShootShip[1]!=0)
										{
											int pos = 0;
											float needTime = 4;
											for(int l=0;l<Enemyship.size();l++)
											{
												if(Enemyship.get(l).ID==canShootShip[1])
												{
													pos = l;
												}
											}
											float Maxdegree = getLeastDegree(myShip,Enemyship.get(pos));
											needTime =Math.abs(Maxdegree)/45;
											if((cool[1]-needTime)>=0.5)
											{
												Task newTask = new Task(myShip.ID);
												newTask.isStartRotating = true;
												newTask.rotation = Maxdegree;
												currentTask.add(newTask);
											}
										}
									}
									if(cool[1]==0&&canShootShip[1]==0)
									{

										/*left can  shoot but no target while right have */
										if(cool[0]!=0&&canShootShip[0]!=0)
										{
											int pos = 0;
											float needTime = 4;
											for(int l=0;l<Enemyship.size();l++)
											{
												if(Enemyship.get(l).ID==canShootShip[0])
												{
													pos = l;
												}
											}
											float Maxdegree = getLeastDegree(myShip,Enemyship.get(pos));
											needTime =Math.abs(Maxdegree)/45;
											if((cool[0]-needTime)>=0.5)
											{
												Task newTask = new Task(myShip.ID);
												newTask.isStartRotating = true;
												newTask.rotation = Maxdegree;
												currentTask.add(newTask);
											}
										}
									
									}
									if(canShootShip[1]==0&&canShootShip[0]==0)
									{
										/*left and right lost target*/
										float rotateDegree= getMinDegree(myShip,Enemyship);
										if(rotateDegree!=0)
										{
											Task newTask = new Task(myShip.ID);
											newTask.isStartRotating = true;
											newTask.rotation = rotateDegree;
											currentTask.add(newTask);
										}
									}
									
								}
								//System.out.println("Enemy is fighting "+enemyShip.ID);
							}
							else if(guessEnemyAction == 2)
							{
								//System.out.println("Enemy is Escaping"+enemyShip.ID);
								if(Enemyship.size()!= 0)
								{
									int[] canShootShip = getInShootRangeShip(myShip,Enemyship);
									float[] cool = myShip.CooldownRemain;
									if(cool[0] == 0&&canShootShip[0]!=0)
									{
										
											Task rshoot = new Task(myShip.ID);
											rshoot.TargetID = canShootShip[0];
											rshoot.isAttack = true;
											currentTask.add(rshoot);
											//System.out.println("R is shooting!!!!!");
										
									}
									if(cool[1]==0&&canShootShip[1]!=0)
									{
										
											Task lshoot = new Task(myShip.ID);
											lshoot.TargetID = canShootShip[1];
											lshoot.isAttack = true;
											currentTask.add(lshoot);
											//System.out.println("L is shooting!!!!!!!");
									}
									if((canShootShip[0]==0)&&(canShootShip[1]==0))
									{
										Task chase = new Task(myShip.ID);
										chase.isStartRotatingTo = true;
										chase.target_x = enemyShip.PositionX+enemyShip.VelocityX/2;
										chase.target_y = enemyShip.PositionX+enemyShip.VelocityX/2;
										currentTask.add(chase);
									}
									
								}
							}
							else if(guessEnemyAction == 3)
							{
								if(Enemyship.size()!=0)
								{
									int[] canShootShip = getInShootRangeShip(myShip,Enemyship);
									float[] cool = myShip.CooldownRemain;	
									boolean leftWillShoot = false;
									boolean rightWillShoot = false;
									if(cool[0] == 0&&canShootShip[0]!=0)
									{
										
											Task rshoot = new Task(myShip.ID);
											rshoot.TargetID = canShootShip[0];
											rshoot.isAttack = true;
											currentTask.add(rshoot);
											//System.out.println("R is shooting!!!!!");
											rightWillShoot = true;
									}
									if(cool[1]==0&&canShootShip[1]!=0)
									{
										
											Task lshoot = new Task(myShip.ID);
											lshoot.TargetID = canShootShip[1];
											lshoot.isAttack = true;
											currentTask.add(lshoot);
											leftWillShoot = true;
											//System.out.println("L is shooting!!!!!!!");
									}
									if(canShootShip[1]==0&&canShootShip[0]==0)
									{
										/*left and right lost target*/
										float rotateDegree= getMinDegree(myShip,Enemyship);
										if(rotateDegree!=0)
										{
											Task newTask = new Task(myShip.ID);
											newTask.isStartRotating = true;
											newTask.rotation = rotateDegree;
											currentTask.add(newTask);
										}
									}
									
								}
							//	System.out.println("Enemy is allign moving"+enemyShip.ID);
							}
							else if(guessEnemyAction ==0)
							{
								if(Enemyship.size()!=0)
								{
									int[] canShootShip = getInShootRangeShip(myShip,Enemyship);
									float[] cool = myShip.CooldownRemain;	
									boolean leftWillShoot = false;
									boolean rightWillShoot = false;
									if(cool[0] == 0&&canShootShip[0]!=0)
									{
										
											Task rshoot = new Task(myShip.ID);
											rshoot.TargetID = canShootShip[0];
											rshoot.isAttack = true;
											currentTask.add(rshoot);
										//	System.out.println("R is shooting!!!!!");
											rightWillShoot = true;
									}
									if(cool[1]==0&&canShootShip[1]!=0)
									{
										
											Task lshoot = new Task(myShip.ID);
											lshoot.TargetID = canShootShip[1];
											lshoot.isAttack = true;
											currentTask.add(lshoot);
											leftWillShoot = true;
											//System.out.println("L is shooting!!!!!!!");
									}
									if(canShootShip[1]==0&&canShootShip[0]==0)
									{
										/*left and right lost target*/
										float rotateDegree= getMinDegree(myShip,Enemyship);
										if(rotateDegree!=0)
										{
											Task newTask = new Task(myShip.ID);
											newTask.isStartRotating = true;
											newTask.rotation = rotateDegree;
											currentTask.add(newTask);
										}
									}
									
								}
							//	System.out.println("Enemy is allign moving"+enemyShip.ID);
								//System.out.println("Enemy is Stay"+enemyShip.ID);
							}
							else
							{
								//System.out.println("I do not know what he do");
							}
							
						}
						else if(todo == 4)
						{
							int[] canShootShip = getInShootRangeShip(myShip,Enemyship);
							float[] cool = myShip.CooldownRemain;	
							boolean leftWillShoot = false;
							boolean rightWillShoot = false;
							float MindegreeToRotate = 0;
							float degree = 180;
							int whatTodo = 10;
							if(cool[0] == 0&&canShootShip[0]!=0)
							{
								
									Task rshoot = new Task(myShip.ID);
									rshoot.TargetID = canShootShip[0];
									rshoot.isAttack = true;
									currentTask.add(rshoot);
									//System.out.println("R is shooting!!!!!");
									rightWillShoot = true;
							}
							if(cool[1]==0&&canShootShip[1]!=0)
							{
								
									Task lshoot = new Task(myShip.ID);
									lshoot.TargetID = canShootShip[1];
									lshoot.isAttack = true;
									currentTask.add(lshoot);
									leftWillShoot = true;
									//System.out.println("L is shooting!!!!!!!");
							}
							/*avoid collision*/ /*0 stay 1 fight 2 escape 3 align Move*/
							whatTodo = isEnemyDoVector(myShip,enemyShip);
							if(whatTodo == 1)
							{
								MindegreeToRotate=getRotateAngle(myShip.DirectionX,myShip.DirectionY,enemyShip.DirectionX,enemyShip.DirectionY);
								if(MindegreeToRotate != 0 )
								{
									if(MindegreeToRotate ==-180)
									{
										degree = getRotateAngle(myShip.PositionX-enemyShip.PositionX,myShip.PositionY-enemyShip.PositionY,enemyShip.DirectionX,enemyShip.DirectionY);
										if(degree == 0)
										{
											/*head to head hit*/
											Task newTask = new Task(myShip.ID);
											newTask.isStartRotating = true;
											newTask.rotation = 90;
											currentTask.add(newTask);
										}
									}
									else if(Math.abs(MindegreeToRotate)<=90)
									{
										Task newTask = new Task(myShip.ID);
										newTask.isStartRotating = true;
										newTask.rotation = enemyShip.Rotation;
										currentTask.add(newTask);
									}
									else if(Math.abs(MindegreeToRotate)>90&&Math.abs(MindegreeToRotate)<180)
									{
										Task newTask = new Task(myShip.ID);
										newTask.isStartRotating = true;
										if(MindegreeToRotate<0)
										{
											newTask.rotation = MindegreeToRotate+90;
										}
										else
										{
											newTask.rotation = MindegreeToRotate-90;
										}
										currentTask.add(newTask);
									}
								}
							}
							else if(whatTodo==0)
							{
								Task newTask = new Task(myShip.ID);
								newTask.isStartRotating = true;
								newTask.rotation = 90;
								currentTask.add(newTask);
							}
							
						}
						else if(todo == 5)
						{
							Task newTask = new Task(myShip.ID);
							newTask.TargetID = 0;
							newTask.isAttack = true;
							currentTask.add(newTask);
						//	System.out.println("Not Attack now, It is too far "+myShip.ID);
						}
					break;
				case 2: int i_nBase = shipDetec.getMyNearestNeutralBase(Neutralresource);
						ResourceInfo neutralBase = Neutralresource.get(i_nBase);
						if(neutralBase != null)
						{
							Task newTask = new Task(myShip.ID);
							newTask.priority = 0;
							newTask.target_x = neutralBase.PositionX;
							newTask.target_y = neutralBase.PositionY;
							newTask.isStartMoving = true;
							newTask.isStartRotatingTo =true;
							currentTask.add(newTask);
						//	System.out.println("I want to neutral Base "+myShip.ID);	
						//	System.out.println("currentTask.size()--- "+currentTask.size());
						}
					break;
				case 4: if(lastSeenEnemy.size()!=0&&Enemyship.size()==0)
						{
					        int pos = shipDetec.getMyNearestEnemy(lastSeenEnemy);
					        ShipInfo lastE_ship = lastSeenEnemy.get(pos);
					        if(lastE_ship != null)
							{
					        	Task newTask = new Task(myShip.ID);
					        	newTask.priority=0;
								newTask.target_x = lastE_ship.PositionX;
								newTask.target_y = lastE_ship.PositionY;
								newTask.isStartRotatingTo = true;
								currentTask.add(newTask);
							}
							
						}
					break;
			}
			//System.out.println("afer currentTask.size()for(int g_i=0;g_i<Myship.size();g_i++)!!!!!!!!!!!!!!!!"+currentTask.size());
		}
		
	//System.out.println("currentTask.size() before col!!!!!!!!!!!!!!!!!"+currentTask.size());
		for(int m=0;m<Myship.size();m++)
		{
			int cases = 0;
			if((cases = isCollision(Myship.get(m),Allresource))!=0)
			{
				
				if(cases == 1)
				{
					
					Task lastTask = getMyLastAssignTask(Myship.get(m).ID,currentTask);
					boolean isAttack = false;
					int TargetID = 1;
					if(lastTask != null)
					{	
						if(lastTask.isAttack)
						{
							isAttack = true;
							TargetID = lastTask.TargetID;
						}
						currentTask.remove(lastTask);
					}	
					avoidCol[Myship.get(m).ID] = true;
					Task myTask = new Task(Myship.get(m).ID);
					myTask.priority = 100;
					myTask.isAttack = isAttack;
					if(isAttack)
					{
						myTask.TargetID = TargetID;
					}
					myTask.isStartRotating = true;
					myTask.rotation = 180;
					currentTask.add(myTask);
					
				
				}
				else 
				{
					//System.out.println("avoid collsion "+Myship.get(m).ID);
					Task lastTask = getMyLastAssignTask(Myship.get(m).ID,currentTask);
					boolean isAttack =false;
					int TargetID =1;
					int[] rotateArray = {-2,2,-2,2,2,3,3};
					int[] rotateArray2 = {-2,2,-2,2,-2,4,4};
					int rand=0;
					if(lastTask != null)
					{
						if(lastTask.isAttack)
						{
							isAttack = true;
							TargetID = lastTask.TargetID;
						}
						currentTask.remove(lastTask);
					}
					avoidTimes[Myship.get(m).ID]++;
					avoidCol[Myship.get(m).ID] = true;
					Task myTask = new Task(Myship.get(m).ID);
					myTask.priority = 100;
					myTask.isStartRotating = true;
					myTask.isAttack = isAttack;
					if(isAttack)
					{
						myTask.TargetID = TargetID;
					}
					if(avoidTimes[Myship.get(m).ID]==1)
					{
						myTask.rotation = rotationDegree(Myship.get(m),Allresource);
					}
					
					if(avoidTimes[Myship.get(m).ID]>=2&&avoidTimes[Myship.get(m).ID]<6)
					{
						rand = random.nextInt()%7;
						if(rand>6||rand<0)
						{
							rand=3;
						}
						myTask.rotation = 45*rotateArray[rand];
					}
					else
					{
						rand = random.nextInt()%7;
						if(rand>6||rand<0)
						{
							rand=3;
						}
						myTask.rotation = -45*rotateArray2[rand];
					}
					currentTask.add(myTask);
						//System.out.println("cases IN 2 --------");
					
					
				}
			}
			else
			{
				avoidCol[Myship.get(m).ID] = false;
				avoidTimes[Myship.get(m).ID] = 0;
			}
			
		}
		
		for(int i=0;i<Myship.size();i++)
		{
			if(Myship.get(i).IsBlocked)
			{
				Task myTask = getMyLastAssignTask(Myship.get(i).ID,currentTask);
				boolean isAttack = false;
				int TargetID = 1;
				if(myTask != null)
				{
					if(myTask.isAttack)
					{
						isAttack = true;
						TargetID = myTask.TargetID;
					}
					currentTask.remove(myTask);
				}
				//System.out.println("avoid block "+Myship.get(i).ID);
				int[] rotateArray = {-1,1,-2,2,-2,3,3};
				int[] rotateArray2 = {-1,1,-2,2,2,-1,2};
				int l=0;
				blockTimes[Myship.get(i).ID]++;
				//System.out.println("shipID----"+Myship.get(i).ID);
				Task newTask = new Task(Myship.get(i).ID);
				newTask.isStartRotating = true;
				
				newTask.isAttack = isAttack;
				if(isAttack)
				{
					newTask.TargetID = TargetID;
				}
				if(blockTimes[Myship.get(i).ID]<=4)
				{
					l = random.nextInt()%7;
					if(l>6||l<0)
					{
						l=3;
					}
					newTask.rotation = 45*rotateArray[l];
				}
				else
				{
					l = random.nextInt()%7;
					if(l>6||l<0)
					{
						l=3;
					}
					newTask.rotation = 90*rotateArray2[l];
				}
				
				currentTask.add(newTask);
			//	System.out.println("is blocked!!!!!!!!!!!!!");
			}
			else
			{
				blockTimes[Myship.get(i).ID]=0;
			}
		}
		//System.out.println("currentTask.size()!!!!!!!!!!!!!!!!!"+currentTask.size());
		if(currentTask.size()>0)
		{
			/* it is used to check the Task and change it
			 * this has been a mistake ,cas if lastAssignTask.is not finished we should continue
			 * do it,but if lastAssignTask is finished we have already remove it,can make new task
			if(lastAssignTask.size()>0)
			{
				lastAssignTask.clear();
			}*/
			for(Task l:currentTask)
			{
				
				if(command%10==0)
				{
					currentTime = Interface.TimeLeft;
					while((StartTime-currentTime)<1000)
					{
						currentTime = Interface.TimeLeft;
					}
					StartTime = Interface.TimeLeft;
				//	System.out.println("!!!!!!!!!!!!command for this turn "+command);
				}
				if(l.isStartMoving)
				{
					command++;
					Interface.StartMoving(l.ShipID);
				}
				if(l.isMoveTo)
				{
					command++;
					Interface.MoveTo(l.ShipID,l.target_x,l.target_y);
				}
				if(l.isStartRotating)
				{
					command++;
					Interface.StartRotating(l.ShipID,l.rotation);
				}
				if(l.isStartRotatingTo)
				{
					command++;
					Interface.StartRotatingTo(l.ShipID,l.target_x,l.target_y);
				}
				if(l.isStopMoving)
				{
					command++;
					Interface.StopMoving(l.ShipID);
				}
				if(l.isAttack)
				{
					command++;
					Interface.Attack(l.ShipID, l.TargetID);
				}
				if(avoidCol[l.ShipID])
				{	
					avoidCollTask.add(l);
					continue;
				}
				if(repair[l.ShipID])
				{
					continue;
				}
				//lastAssignTask.add(l);
			}
			//turn = 1;
			currentTask.clear();
		}
		if(lastSeenMine.size()>0)
			lastSeenMine.clear();
		for(int i=0;i<Myship.size();i++)
		{
			lastSeenMine.add(Myship.get(i));
		}
		/*it is used to rem Enemyship to prediction*/
		if(lastSeenEnemy.size()>0)
				lastSeenEnemy.clear();
		for(int i=0;i<Enemyship.size();i++)
		{
			lastSeenEnemy.add(Enemyship.get(i));
			
		}
		lastTime = currentTime;
		
		turn++;
	
	
	}
	
	public static void main(String args[])
	{
		String[] temp =new String[2];
		temp[0] = "buaajava";
		temp[1] = "111.22.33";
		SDK.StartGame(temp);
		
	}
}
class RadiusDetective
{
	private ShipInfo ship;
	private int delta=0;
	private int myNearEnemyNum =0;
	private int myNearMyshipNum =0;
	private final float rangeView = 400;
	private final float enemyRangeView = 400;
	private final float myShipRangeView =350;
	private ResourceInfo myNearestBaseInView ;
	private ResourceInfo myNearestEnemyBaseInView;
	private ResourceInfo myNearestNeutralBaseInView;
	public RadiusDetective(ShipInfo ship,int delta)
	{
		this.ship = ship;
		this.delta = delta;
	}
	public boolean isInWar(ShipInfo myShip,List<ShipInfo> enemyShip)
	{
		
		float xais = ship.PositionX;
		float yais = ship.PositionY;
		for(int i=0;i<enemyShip.size();i++)
		{
			float e_xais = enemyShip.get(i).PositionX;
			float e_yais = enemyShip.get(i).PositionY;
			if(e_xais>(xais-rangeView)&&e_xais<(xais+rangeView))
			{	
				if(e_yais>(yais-rangeView)&&e_yais<(yais+rangeView))
				{
					return true;
				}
			}
		}
		return false;
		
	}
	/*it is used to predict wether to start a war*/
	public int getMyNearEnemyNum(List<ShipInfo> enemyShip)
	{
		float xais = ship.PositionX;
		float yais = ship.PositionY;
		for(int i=0;i<enemyShip.size();i++)
		{
			float predictionX = delta*enemyShip.get(i).VelocityX + enemyShip.get(i).PositionX;
			float predictionY = delta*enemyShip.get(i).VelocityY + enemyShip.get(i).PositionY;
			if(predictionX>(xais-enemyRangeView)&&predictionX<(xais+enemyRangeView))
			{	
				if(predictionY>(yais-enemyRangeView)&&predictionY<(yais+enemyRangeView))
				{
					myNearEnemyNum++;
				}
			}
		}
		return myNearEnemyNum;
	}
	public int getMyNearMyshipNum(List<ShipInfo> myShip)
	{
		float xais = ship.PositionX;
		float yais = ship.PositionY;
		for(int i=0;i<myShip.size();i++)
		{
			if(myShip.get(i).ID == ship.ID)
			{
				myNearMyshipNum++;
				continue;
			}
			float predictionX = delta*myShip.get(i).VelocityX + myShip.get(i).PositionX;
			float predictionY = delta*myShip.get(i).VelocityY + myShip.get(i).PositionY;
			if(predictionX>(xais-myShipRangeView)&&predictionX<(xais+myShipRangeView))
			{	
				if(predictionY>(yais-myShipRangeView)&&predictionY<(yais+myShipRangeView))
				{
					myNearMyshipNum++;
				}
			}
		}
		return myNearMyshipNum;
	}
	/*it is used to decide what to do now ,escape,move near,or Attack
	* this is used for me to make a desition now ,not prediction
	*/
	public int getMyNearestEnemy(List<ShipInfo> enemyShip)
	{
		int positionInList = 0; 
		float Mindist = 4096;
		float distance =0;
		for(int i=0;i<enemyShip.size();i++)
		{
			distance = (float)Math.ceil(Math.sqrt((ship.PositionX-enemyShip.get(i).PositionX)*(ship.PositionX-enemyShip.get(i).PositionX)+(ship.PositionY-enemyShip.get(i).PositionY)*(ship.PositionX-enemyShip.get(i).PositionX)));
			if(distance<Mindist)
			{
				Mindist = distance;
				positionInList = i;
			}
		}
		return positionInList;
	}
	public int getMyNearestEnemyBase(List<ResourceInfo> enemyBase)
	{
		int positionInList = 0;
		float Mindist = 4096;
		float distance = 0;
		for(int i=0;i<enemyBase.size();i++)
		{
			distance = (float)Math.ceil(Math.sqrt((ship.PositionX-enemyBase.get(i).PositionX)*(ship.PositionX-enemyBase.get(i).PositionX)+(ship.PositionY-enemyBase.get(i).PositionY)*(ship.PositionX-enemyBase.get(i).PositionX)));
			if(distance<Mindist)
			{
				Mindist = distance;
				positionInList = i;
			}
		}
		return positionInList;
	}
	
	public int getMyNearestNeutralBase(List<ResourceInfo> neutral)
	{
		int positionInList = 0; 
		float Mindist = 4096;
		float distance =0;
		for(int i=0;i<neutral.size();i++)
		{
			distance = (float)Math.ceil(Math.sqrt((ship.PositionX-neutral.get(i).PositionX)*(ship.PositionX-neutral.get(i).PositionX)+(ship.PositionY-neutral.get(i).PositionY)*(ship.PositionX-neutral.get(i).PositionX)));
			if(distance<Mindist)
			{
				Mindist = distance;
				positionInList = i;
			}
		}
		return positionInList;
	}
	
	
}
class PossibleAssignment 
{
	private int ShipID;
 
	private float score;
	private Task task;
	private TreeSet<Task> shipTaskList_1;
	private TreeSet<Task> shipTaskList_2;
	private TreeSet<Task> shipTaskList_3;
	private TreeSet<Task> shipTaskList_4;
	private TreeSet<Task> shipTaskList_5;
	public PossibleAssignment()
	{
		
		shipTaskList_1 = new TreeSet<Task>();
		shipTaskList_2 = new TreeSet<Task>();
		shipTaskList_3 = new TreeSet<Task>();
		shipTaskList_4 = new TreeSet<Task>();
		shipTaskList_5 = new TreeSet<Task>();
	}
	public void addPossibleTask(int ShipID,Task myTask)
	{
		if(ShipID == 1)
		{
			shipTaskList_1.add(myTask);
		}
		else if(ShipID == 2)
		{
			shipTaskList_2.add(myTask);
		}
		else if(ShipID == 3)
		{
			shipTaskList_3.add(myTask);
		}
		else if(ShipID == 4)
		{
			shipTaskList_4.add(myTask);
		}
		else if(ShipID == 5)
		{
			shipTaskList_5.add(myTask);
		}
	}
	public Task getFirstTask(int ShipID)
	{
		if(ShipID == 1)
		{
			return shipTaskList_1.first();
		}
		else if(ShipID == 2)
		{
			return shipTaskList_2.first();
		}
		else if(ShipID == 3)
		{
			return shipTaskList_3.first();
		}
		else if(ShipID == 4)
		{
			return shipTaskList_4.first();
		}
		else if(ShipID == 5)
		{
			return shipTaskList_5.first();
		}
		return null;
	}
}
class Task implements Comparable<Task>
{
	
		public int priority=1096;
		public float distance=1;
		public float modifier = 0;
		public int centerID; /*it is used to assemple my ship*/
		public int ShipID;
		
		public int TargetID;
		
		public float score;
		public float rotation=0;
		public float target_x=0;
		public float target_y=0;

		public boolean isAssemple=false;
			
		public boolean isStopMoving = false;
		public boolean isStopRotating = false;
		public boolean isMoveTo = false;
		
		public boolean isStartMoving = false;
		public boolean isStartRotating = false;
		public boolean isStartRotatingTo = false;
		
		public boolean isAttack = false;
		
		public Task(int ShipID)
		{
			this.ShipID = ShipID;
		}	
		
		public boolean equal(Task newTask)
		{
			if(this.priority != newTask.priority)
				return false;
			if(this.target_x!=newTask.target_x||this.target_y!=newTask.target_y)
				return false;
			if(this.rotation!=newTask.rotation)
				return false;
			if(this.isStartMoving != newTask.isStartMoving)
				return false;
			if(this.isStopMoving != newTask.isStopMoving)
				return false;
			if(this.isStopRotating!=newTask.isStopRotating)
				return false;
			if(this.isAssemple !=newTask.isAssemple)
				return false;
			return true;
		}
		public int compareTo(Task arg0) {
			if(arg0.score > this.score)
				return 1;
			else if(arg0.score == this.score)
			{
				if(arg0.priority<this.priority)
				{
					return 1;
				}
				else if(arg0.priority>this.priority)
				{
					return -1;
				}
				else
				{
					return 0;
				}
			}
			else
				return -1;
			
		}

}

