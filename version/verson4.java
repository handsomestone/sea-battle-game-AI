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
	/*
		lastSeen enemy
		lastAssignTask
	*/
	//List<Task> lastAssignTask = new ArrayList<Task>();
	List<Task> avoidCollTask = new ArrayList<Task>();
	List<ShipInfo> lastSeenEnemy = new ArrayList<ShipInfo>();
	int signalToAssemple = 0;
	
	int[] blockTimes = {0,0,0,0,0,0};
	
	int lastTime = 0;
	
	Random random = new Random();
	float d;
	int k;
	boolean[] avoidCol = {false,false,false,false,false,false};
	boolean[] repair = {false,false,false,false,false,false};
	private int isCollision(ShipInfo a,List<ResourceInfo> resource)
	{
		float predictRadius = 90;
		float predictRadius_B =100;
		float collisionRadius = 60;
		float a_xais = a.PositionX;
		float a_yais = a.PositionY;
	/*	System.out.println("a_xais "+a_xais);
		System.out.println("a_yais "+a_yais);
		System.out.println("a.DirectionX "+a.DirectionX);
		System.out.println("a.DirectionY "+a.DirectionY);
		System.out.println("(Math.abs(a_xais+a.DirectionX*predictRadius)"+Math.abs(a_xais+a.DirectionX*predictRadius));
		System.out.println("Math.abs(a_yais+a.DirectionY*predictRadius) "+Math.abs(a_yais+a.DirectionY*predictRadius));
	*/
		if((Math.abs(a_xais+a.DirectionX*predictRadius_B))>=800)
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
			if(Allship.get(i).Faction == Me)
			{
				Myship.add(Allship.get(i));
			//	System.out.println(Myship.size());
				//getMyshipByID[Allship.get(i).ID-1] = Allship.get(i);
			
			}	
			else
			{
				Enemyship.add(Allship.get(i));
				//getHeshipByID[Allship.get(i).ID-1] = Allship.get(i);
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
		
	
	
		/*full attack*/
		if(Enemyresource.size()>0)
		{
			stateChange = true;
			if(Enemyship.size()>0)
			{	
				int commandshipID = assempleCenterShip(Myship);
				RadiusDetective comShipDetec = new RadiusDetective(Myship.get(commandshipID),lastTime-StartTime);
				int nearestEnemyPosition = comShipDetec.getMyNearestEnemy(Enemyship);
				int nearestBasePosition = comShipDetec.getMyNearestEnemyBase(Enemyresource);
				float e_distance = distBetween2Ship(Myship.get(commandshipID),Enemyship.get(nearestEnemyPosition));
				float r_distance = distBetweenShipIsland(Myship.get(commandshipID),Enemyresource.get(nearestBasePosition));
				/*if we met enemy and resource we should conquer enmey 
				 *  1 distance to enmey much far than ditance to enemybase(about 200)
				 *  2 if distance to enemy base less than r_ditance-196 < 50 we should conquer
				 * */
				if((e_distance-(r_distance-100))>=200)
				{
					state = 0;/*conquer enemybase*/
				}
				else if((r_distance - 196)<50)
				{
					state = 0;/*special case only my fleet is very close to enemybase*/
				}
				else 
				{
					state = 1;/*if met,we fight*/
				}
				
			}
			else
			{
				state = 0;/*this no dout to conquer*/
			}
		}
		else if((Enemyresource.size()==0)&&(Enemyship.size()>0))
		{
			/*in this case we must fight */
			state = 1;
		}
		else if((Enemyship.size()==0)&&(Neutralresource.size()>0))
		{			
			/*we can not find the enemy so we must conquer neutralresource*/
			state = 2;
			
		}
		else if((Enemyship.size()==0)&&(lastSeenEnemy.size()>0))
		{
			/*we lost the enemy position but we can guess by its last position*/
			state = 4;
					
		}
		else if((Enemyship.size()==0)&&(lastSeenEnemy.size()==0))
		{
			/*we can find any enemy so we should move to my base*/
			state = 3;
		}
		System.out.println("this is used to check the state "+state);
		/*now we got command ship's command we must do*/
		if((state == 0))
		{
			currentTask.clear();
			int leaderShip = shipID2pInList(3,Myship);
			//leaderShip = assempleCenterShip(Myship);
			RadiusDetective comShipDetec;		
		    comShipDetec = new RadiusDetective(Myship.get(leaderShip),lastTime-StartTime);
			
			int i_eBase = comShipDetec.getMyNearestEnemyBase(Enemyresource);
			ResourceInfo enemyBase = Enemyresource.get(i_eBase);
			//System.out.println("state == 0) in !!!!!!!!!!!!!!");
			if(enemyBase != null)
			{
				/*test leadership*/
			
				if((avoidCol[leaderShip]==false)&&(repair[leaderShip]==false))
				{
				//	System.out.println("state == 0) in 1!!!!!!!!!!!!!!");				
						Task newTask = new Task(Myship.get(leaderShip).ID);
						newTask.priority = 0;
						newTask.target_x = enemyBase.PositionX;
						newTask.target_y = enemyBase.PositionY;
						newTask.isStartRotatingTo = true;
						newTask.isStartMoving = true;
						currentTask.add(newTask);
						System.out.println("state == 0) in 2!!!!!!!!!!!!!!");
					
					System.out.println("assign task to team");
					for(int k =0 ;k<Myship.size();k++)
					{
						if(k != leaderShip)
						{
							if((avoidCol[Myship.get(k).ID]==false)&&(repair[Myship.get(k).ID]==false))
							{
								/*if(lastAssignTask.size()!=0)
								{
									Task old = getMyLastAssignTask(Myship.get(k).ID,lastAssignTask);
									if(old == null)
									{
										Task newTask = new Task(Myship.get(k).ID);
										newTask.priority = 0;
										newTask.target_x = enemyBase.PositionX;
										newTask.target_y = enemyBase.PositionY;
										newTask.isStartRotatingTo = true;
										newTask.isStartMoving = true;
										currentTask.add(newTask);
									}
									else
									{*/
										Task t_newTask = new Task(Myship.get(k).ID);
										t_newTask.priority = 0;
										t_newTask.target_x = enemyBase.PositionX;
										t_newTask.target_y = enemyBase.PositionY;
										t_newTask.isStartRotatingTo = true;
										t_newTask.isStartMoving = true;
										
										currentTask.add(t_newTask);
										
									
								}
							}
						}
							
					}
				}
				else
				{
					/*if leadership have some problem we choose random ship*/
					 /*int randLeader = Math.abs(random.nextInt())%Myship.size();
					 if(randLeader == leaderShip)
					 {
						randLeader = (randLeader+1)%Myship.size();
					 }*/
					int randLeader = 0;
					for(int k=0;k<Myship.size();k++)
					{
						if(k == leaderShip)
							continue;
						if(Myship.get(k).IsBlocked)
							continue;
						if(avoidCol[Myship.get(k).ID]||repair[Myship.get(k).ID])
							continue;
						randLeader = k;
						break;
					}
					if(Myship.size()==1)
					{
						state = 3;/*i do not have enough ship to battle go home~*/
					}
					else
					{
						Task newTask = new Task(Myship.get(randLeader).ID);
						newTask.priority = 0;
						newTask.target_x = enemyBase.PositionX;
						newTask.target_y = enemyBase.PositionY;
						newTask.isStartRotatingTo = true;
						newTask.isStartMoving = true;
						
						currentTask.add(newTask);
						
					
				
						for(int i=0;i<Myship.size();i++)
						{
							if(i == randLeader)
							{
								continue;
							}
							if(avoidCol[Myship.get(i).ID]||repair[Myship.get(i).ID])
							{
								continue;
							}
							Task i_newTask = new Task(Myship.get(i).ID);
							i_newTask.priority = 0;
							i_newTask.target_x = enemyBase.PositionX;
							i_newTask.target_y = enemyBase.PositionY;
							i_newTask.isStartRotatingTo = true;
							i_newTask.isStartMoving = true;
							
							currentTask.add(i_newTask);
						
							
					
							
						}
					}
					
				}
				
			}
		else if(state == 1)
		{
			/*this is going to fight */
			/*
			RadiusDetective comShipDetec;
			for(int i=0;i<Myship.size();i++)
			{
				if(avoidCol[Myship.get(i).ID]||repair[Myship.get(i).ID])
					continue;
				 ShipInfo ship = Myship.get(i);
				 comShipDetec = new RadiusDetective(ship,lastTime-StartTime);
				 int enmeyP = comShipDetec.getMyNearestEnemy(Enemyship);
				 ShipInfo enemy = Enemyship.get(enmeyP);
				 
			}*/
			System.out.println("fight ~~~~~~~~~~~~~~~");
		}
		else if(state ==2&&stateChange==true)
		{
			System.out.println("2 222this is used to check the state "+state);
			/*we can not find the enemy so we must conquer neutralresource*/
			currentTask.clear();
			int leaderShip   = assempleCenterShip(Myship);      //= shipID2pInList(3,Myship);
			//leaderShip = assempleCenterShip(Myship);
			System.out.println("2 222leaderShip "+leaderShip);
			RadiusDetective comShipDetec;		
		    comShipDetec = new RadiusDetective(Myship.get(leaderShip),lastTime-StartTime);
			
			int i_eBase = comShipDetec.getMyNearestNeutralBase(Neutralresource);
			System.out.println("2 i_eBase "+i_eBase);
			ResourceInfo neutralBase = Neutralresource.get(i_eBase);
			//System.out.println("state == 0) in !!!!!!!!!!!!!!");
			if(neutralBase != null)
			{
				/*test leadership*/
			
				if((avoidCol[leaderShip]==false)&&(repair[leaderShip]==false))
				{
				//	System.out.println("state == 0) in 1!!!!!!!!!!!!!!");				
						Task newTask = new Task(Myship.get(leaderShip).ID);
						newTask.priority = 0;
						newTask.target_x = neutralBase.PositionX;
						newTask.target_y = neutralBase.PositionY;
						newTask.isStartRotatingTo = true;
						newTask.isStartMoving = true;
						currentTask.add(newTask);
						System.out.println("state == 0) in 2!!!!!!!!!!!!!!");
					
					System.out.println("assign task to team");
					for(int k =0 ;k<Myship.size();k++)
					{
						if(k != leaderShip)
						{
							if((avoidCol[Myship.get(k).ID]==false)&&(repair[Myship.get(k).ID]==false))
							{
								/*if(lastAssignTask.size()!=0)
								{
									Task old = getMyLastAssignTask(Myship.get(k).ID,lastAssignTask);
									if(old == null)
									{
										Task newTask = new Task(Myship.get(k).ID);
										newTask.priority = 0;
										newTask.target_x = enemyBase.PositionX;
										newTask.target_y = enemyBase.PositionY;
										newTask.isStartRotatingTo = true;
										newTask.isStartMoving = true;
										currentTask.add(newTask);
									}
									else
									{*/
										Task t_newTask = new Task(Myship.get(k).ID);
										t_newTask.priority = 0;
										t_newTask.target_x = neutralBase.PositionX;
										t_newTask.target_y = neutralBase.PositionY;
										t_newTask.isStartRotatingTo = true;
										t_newTask.isStartMoving = true;
										
										currentTask.add(t_newTask);
										
									
								}
							}
						}
							
					}
				else
				{
					int randLeader = 0;
					for(int k=0;k<Myship.size();k++)
					{
						if(k == leaderShip)
							continue;
						if(Myship.get(k).IsBlocked)
							continue;
						if(avoidCol[Myship.get(k).ID]||repair[Myship.get(k).ID])
							continue;
						randLeader = k;
						break;
					}
					if(Myship.size()==1)
					{
						state = 3;/*i do not have enough ship to battle go home~*/
					}
					else
					{
						Task newTask = new Task(Myship.get(randLeader).ID);
						newTask.priority = 0;
						newTask.target_x = neutralBase.PositionX;
						newTask.target_y = neutralBase.PositionY;
						newTask.isStartRotatingTo = true;
						newTask.isStartMoving = true;
						
						currentTask.add(newTask);
						
					
				
						for(int i=0;i<Myship.size();i++)
						{
							if(i == randLeader)
							{
								continue;
							}
							if(avoidCol[Myship.get(i).ID]||repair[Myship.get(i).ID])
							{
								continue;
							}
							Task i_newTask = new Task(Myship.get(i).ID);
							i_newTask.priority = 0;
							i_newTask.target_x = neutralBase.PositionX;
							i_newTask.target_y = neutralBase.PositionY;
							i_newTask.isStartRotatingTo = true;
							i_newTask.isStartMoving = true;
							
							currentTask.add(i_newTask);
						
							
					
							
						}
					}
					
				}
			}
		}
		else if(state == 3)
		{
			/*we can find any enemy so we should move to my base*/
			System.out.println("move to the base");
		}
		else if(state == 4)
		{
			/*we lost the enemy position but we can guess by its last position*/
			System.out.println("lost enemy position ");
		}
		
		if(turn == 0)
		{
			System.out.println("turn == 0 in");
			int startCase = 0;
			int leastTimes =0;
			int regMid = 0;
			boolean normalMid = true;
			int samePostionX = 0;
			int[] checkCase = new int[2];
			float shipPosionX = Myship.get(1).PositionX;
			float MidDistance = Math.abs(shipPosionX);
			
			for(int k=0;k<Allresource.size();k++)
			{
				if((Allresource.get(k).PositionX == 0)&&(Allresource.get(k).PositionY==0))
				{
					regMid = k;
					normalMid = true;
					System.out.println("turn == 0 in normalMid = true;");
				}
				else if((Allresource.get(k).PositionX == 0)&&(Allresource.get(k).PositionY!=0))
				{
					regMid = k;
					normalMid = false;
					System.out.println("turn == 0 in normalMid = false;");
				}
				if(Math.abs(Allresource.get(k).PositionX - shipPosionX)< MidDistance)
				{
					    leastTimes++;
						if((Allresource.get(k).PositionX)!=0)
						{
							checkCase[0] = k;
						}
						System.out.println("turn == 0 in leastTimes"+leastTimes);
				}
			}
			if(normalMid == true&&(leastTimes == 2))
			{
				if((Allresource.get(checkCase[0]).PositionY)*(Allresource.get(checkCase[1]).PositionY)>0)
				{
					if(Me == 1)
					{
						//System.out.println("turn == 0 in Me == 1 getMyshipByID[0].PositionY"+getMyshipByID[0].PositionY);
						Task myTask = new Task(1);
						myTask.isMoveTo =true;						
						myTask.target_x = 0;
						int shipID1 = shipID2pInList(1,Myship);
						myTask.target_y = Myship.get(shipID1).PositionY;
						currentTask.add(myTask);
						Task myTask1 = new Task(2);
						myTask1.isMoveTo =true;
						myTask1.target_x = 0;
						int shipID2 = shipID2pInList(2,Myship);
						myTask1.target_y = Myship.get(shipID2).PositionY;
						currentTask.add(myTask1);
						Task myTask2 = new Task(3);
						myTask2.isMoveTo =true;
						myTask2.target_x = 0;
						int shipID3 = shipID2pInList(3,Myship);
						myTask2.target_y = Myship.get(shipID3).PositionY;
						currentTask.add(myTask2);
						if(Allresource.get(checkCase[0]).PositionY<Allresource.get(checkCase[1]).PositionY)
						{
							Task myTask3 = new Task(4);
							myTask3.isMoveTo =true;
							myTask3.isAssemple =true;/*task finish to assemple*/
							myTask3.target_x = Allresource.get(checkCase[0]).PositionX;
							myTask3.target_y = Allresource.get(checkCase[0]).PositionY;
							currentTask.add(myTask3);
							Task myTask4 = new Task(5);
							myTask4.isMoveTo =true;
							myTask4.isAssemple = true;/*task finish to assemple*/
							myTask4.target_x = Allresource.get(checkCase[1]).PositionX;
							myTask4.target_y = Allresource.get(checkCase[1]).PositionY;
							currentTask.add(myTask4);
						}
						else
						{
							Task myTask3 = new Task(4);
							myTask3.isMoveTo =true;
							myTask3.isAssemple = true;
							myTask3.target_x = Allresource.get(checkCase[1]).PositionX;
							myTask3.target_y = Allresource.get(checkCase[1]).PositionY;
							currentTask.add(myTask3);
							Task myTask4 = new Task(5);
							myTask4.isMoveTo =true;
							myTask4.isAssemple = true;
							myTask4.target_x = Allresource.get(checkCase[0]).PositionX;
							myTask4.target_y = Allresource.get(checkCase[0]).PositionY;
							currentTask.add(myTask4);
						}
					}
					else
					{
						Task myTask = new Task(3);
						myTask.isMoveTo =true;
						myTask.target_x = 0;
						int shipID3 = shipID2pInList(3,Myship);
						myTask.target_y = Myship.get(shipID3).PositionY;
						currentTask.add(myTask);
						Task myTask1 = new Task(4);
						myTask1.isMoveTo =true;
						myTask1.target_x = 0;
						int shipID4 = shipID2pInList(4,Myship);
						myTask1.target_y = Myship.get(shipID4).PositionY;
						currentTask.add(myTask1);
						Task myTask2 = new Task(5);
						myTask2.isMoveTo =true;
						myTask2.target_x = 0;
						int shipID5 = shipID2pInList(5,Myship);
						myTask2.target_y = Myship.get(shipID5).PositionY;
						currentTask.add(myTask2);
						if(Allresource.get(checkCase[0]).PositionY<Allresource.get(checkCase[1]).PositionY)
						{
							Task myTask3 = new Task(2);
							myTask3.isMoveTo =true;
							myTask3.isAssemple=true;
							myTask3.target_x = Allresource.get(checkCase[0]).PositionX;
							myTask3.target_y = Allresource.get(checkCase[0]).PositionY;
							currentTask.add(myTask3);
							Task myTask4 = new Task(1);
							myTask4.isMoveTo =true;
							myTask4.isAssemple =true;
							myTask4.target_x = Allresource.get(checkCase[1]).PositionX;
							myTask4.target_y = Allresource.get(checkCase[1]).PositionY;
							currentTask.add(myTask4);
						}
						else
						{
							Task myTask3 = new Task(2);
							myTask3.isMoveTo =true;
							myTask3.isAssemple =true;
							myTask3.target_x = Allresource.get(checkCase[1]).PositionX;
							myTask3.target_y = Allresource.get(checkCase[1]).PositionY;
							currentTask.add(myTask3);
							Task myTask4 = new Task(1);
							myTask4.isMoveTo =true;
							myTask4.isAssemple=true;
							myTask4.target_x = Allresource.get(checkCase[0]).PositionX;
							myTask4.target_y = Allresource.get(checkCase[0]).PositionY;
							currentTask.add(myTask4);
						}
					}
				}
				else
				{
					Task myTask = new Task(2);
					myTask.isMoveTo = true;
					myTask.target_x = 0;
					int shipID2 = shipID2pInList(2,Myship);
					myTask.target_y = Myship.get(shipID2).PositionY;
					currentTask.add(myTask);
					Task myTask1 = new Task(3);
					myTask1.isMoveTo = true;
					myTask1.target_x = 0;
					int shipID3 = shipID2pInList(3,Myship);
					myTask1.target_y = Myship.get(shipID3).PositionY;;
					currentTask.add(myTask1);
					Task myTask2 = new Task(4);
					myTask2.isMoveTo = true;
					myTask2.target_x = 0;
					int shipID4 = shipID2pInList(4,Myship);
					myTask2.target_y = Myship.get(shipID4).PositionY;;
					currentTask.add(myTask2);
					if(Allresource.get(checkCase[0]).PositionY<=0)
					{
						Task myTask3 = new Task(1);
						myTask3.isMoveTo = true;
						myTask3.isAssemple=true;
						myTask3.target_x = Allresource.get(checkCase[0]).PositionX;
						myTask3.target_y = Allresource.get(checkCase[0]).PositionY;;
						currentTask.add(myTask3);
						Task myTask4 = new Task(5);
						myTask4.isMoveTo = true;
					    myTask4.isAssemple = true;
						myTask4.target_x = Allresource.get(checkCase[1]).PositionX;
						myTask4.target_y = Allresource.get(checkCase[1]).PositionY;;
						currentTask.add(myTask4);
					}
					else
					{
						Task myTask3 = new Task(1);
						myTask3.isMoveTo = true;
						myTask3.isAssemple = true;
						myTask3.target_x = Allresource.get(checkCase[1]).PositionX;
						myTask3.target_y = Allresource.get(checkCase[1]).PositionY;;
						currentTask.add(myTask3);
						Task myTask4 = new Task(5);
						myTask4.isMoveTo = true;
						myTask4.isAssemple =true;
						myTask4.target_x = Allresource.get(checkCase[0]).PositionX;
						myTask4.target_y = Allresource.get(checkCase[0]).PositionY;;
						currentTask.add(myTask4);
					}
					
				}
			}
			else
			{
				System.out.println("leastTime"+leastTimes+"  case3-6 ");
				if(normalMid == false&&leastTimes!=1)
				{
					Task myTask = new Task(5);
					myTask.isMoveTo = true;
					myTask.target_x = 0;
					myTask.target_y = Allresource.get(regMid).PositionY;
					currentTask.add(myTask);
					Task myTask1 = new Task(4);
					myTask1.isMoveTo = true;
					myTask1.target_x = 0;
					myTask1.target_y = Allresource.get(regMid).PositionY;
					currentTask.add(myTask1);
					Task myTask2 = new Task(3);
					myTask2.isMoveTo = true;
					myTask2.target_x = 0;
					myTask2.target_y = Allresource.get(regMid).PositionY;
					currentTask.add(myTask2);
					if(Allresource.get(checkCase[0]).PositionY<Allresource.get(checkCase[1]).PositionY)
					{
						Task myTask3 = new Task(1);
						myTask3.isMoveTo = true;
						myTask3.isAssemple=true;
						myTask3.target_x = Allresource.get(checkCase[0]).PositionX;
						myTask3.target_y = Allresource.get(checkCase[0]).PositionY;
						currentTask.add(myTask3);
						Task myTask4 = new Task(2);
						myTask4.isMoveTo = true;
						myTask4.isAssemple =true;
						myTask4.target_x = Allresource.get(checkCase[1]).PositionX;
						myTask4.target_y = Allresource.get(checkCase[1]).PositionY;
						currentTask.add(myTask4);
					}
					else
					{
						Task myTask3 = new Task(1);
						myTask3.isMoveTo = true;
						myTask3.isAssemple=true;
						myTask3.target_x = Allresource.get(checkCase[1]).PositionX;
						myTask3.target_y = Allresource.get(checkCase[1]).PositionY;
						currentTask.add(myTask3);
						Task myTask4 = new Task(2);
						myTask4.isMoveTo = true;
						myTask4.isAssemple=true;
						myTask4.target_x = Allresource.get(checkCase[0]).PositionX;
						myTask4.target_y = Allresource.get(checkCase[0]).PositionY;
						currentTask.add(myTask4);
					}
				}
				else
				{	
					System.out.println(Allresource.get(checkCase[0]).PositionY+"  case2 "+Allresource.get(checkCase[1]).PositionY);
					Task myTask = new Task(2);
					myTask.isMoveTo = true;
					myTask.target_x = 0;
					myTask.target_y = 128-15;
					currentTask.add(myTask);
					Task myTask1 = new Task(3);
					myTask1.isMoveTo = true;
					myTask1.target_x = 0;
					myTask1.target_y = 128;
					currentTask.add(myTask1);
					Task myTask2 = new Task(4);
					myTask2.isMoveTo = true;
					myTask2.target_x = 0;
					myTask2.target_y = 128+15;
					currentTask.add(myTask2);
					if(Me == 1)
					{
						Task myTask3 = new Task(1);
						myTask3.isMoveTo = true;
						myTask3.isAssemple=true;
						myTask3.target_x = -512+15;
						myTask3.target_y = -384+15;
						currentTask.add(myTask3);
						Task myTask4 = new Task(5);
						myTask4.isMoveTo = true;
						myTask4.isAssemple = true;
						myTask4.target_x = 0;
						myTask4.target_y = 640-15;
						currentTask.add(myTask4);
					}
					else
					{
						Task myTask3 = new Task(1);
						myTask3.isMoveTo = true;
						myTask3.isAssemple=true;
						myTask3.target_x = 512-15;
						myTask3.target_y = -384+15;
						currentTask.add(myTask3);
						Task myTask4 = new Task(5);
						myTask4.isMoveTo = true;
						myTask4.isAssemple=true;
						myTask4.target_x = 0+15;
						myTask4.target_y = 640-15;
						currentTask.add(myTask4);
					}
				}
			}
		}
		
		for(int m=0;m<Myship.size();m++)
		{
			int cases = 0;
			if((cases = isCollision(Myship.get(m),Allresource))!=0)
			{
		
				if(cases == 1) /*avoid to hit boundary*/
				{
					/*avoid to hit the boundary*/
					Task lastTask = getMyLastAssignTask(Myship.get(m).ID,currentTask);
					if(lastTask != null)
					{	
						currentTask.remove(lastTask);
					}	
					avoidCol[Myship.get(m).ID] = true;
					Task myTask = new Task(Myship.get(m).ID);
					myTask.priority = 100;
					myTask.isStartRotating = true;
					myTask.rotation = 180;
					currentTask.add(myTask);
					
				//	System.out.println("cases IN 1 --------"+Myship.get(col).PositionX+" "+Myship.get(col).PositionY);
				}
				else /*avoid to hit island*/ 
				{
					
					Task lastTask = getMyLastAssignTask(Myship.get(m).ID,currentTask);
					if(lastTask != null)
					{
						currentTask.remove(lastTask);
					}
						avoidCol[Myship.get(m).ID] = true;
						Task myTask = new Task(Myship.get(m).ID);
						myTask.priority = 100;
						myTask.isStartRotating = true;
						myTask.rotation = rotationDegree(Myship.get(m),Allresource);
						currentTask.add(myTask);
						//System.out.println("cases IN 2 --------");
					
					
				}
			}
			else
			{
				avoidCol[Myship.get(m).ID] = false;
			}
		}
		for(int i=0;i<Myship.size();i++)
		{
			if(Myship.get(i).IsBlocked)
			{
				Task myTask = getMyLastAssignTask(Myship.get(i).ID,currentTask);
				int[] rotateArray = {0,-1,1,-2,2,-3,3};
				int l=0;
				if(myTask != null)
				{
					currentTask.remove(myTask);
				}
				blockTimes[Myship.get(i).ID]++;
				Task newTask = new Task(Myship.get(i).ID);
				newTask.isStartRotating = true;
				if(blockTimes[Myship.get(i).ID]<=6)
				{
					l = random.nextInt()%7;
					newTask.rotation = 30*rotateArray[l];
				}
				else
				{
					l = random.nextInt()%7;
					newTask.rotation = 60*rotateArray[l];
				}
				
				currentTask.add(newTask);
				System.out.println("is blocked!!!!!!!!!!!!!");
			}
			else
			{
				blockTimes[Myship.get(i).ID]=0;
			}
		}
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
					System.out.println("!!!!!command for this turn "+command);
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
		
		/*it is used to rem Enemyship to prediction*/
		if(lastSeenEnemy.size()>0)
				lastSeenEnemy.clear();
		for(int i=0;i<Enemyship.size();i++)
		{
			lastSeenEnemy.add(Enemyship.get(i));
			
		}
		lastTime = currentTime;
		
		turn++;
		/*
		a = new ArrayList<ShipInfo>();
		b = new ArrayList<ShipInfo>();
		for (int i = 0; i < ships.size(); i++)
			if (ships.get(i).Faction == Interface.Faction) a.add(ships.get(i));
			else b.add(ships.get(i));
		
		for (int i = 0; i < a.size(); i++)
		{
			d = -1;
			for (int j = 0; j < b.size(); j++)
			{
				float t = dist(a.get(i), b.get(j));
				if (t < d || d == -1)
				{
					d = t;
					k = j;
				}
			}
			
			if (b.size()>0)
			{
				Interface.MoveTo(a.get(i).ID, b.get(k).PositionX+(float)(random.nextDouble() - 0.5) * 200f, b.get(k).PositionY+(float)(random.nextDouble() - 0.5) * 200f);
				Interface.Attack(a.get(i).ID, b.get(k).ID);
			}
			else Interface.StartMoving(a.get(i).ID);
		}*/
	
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
