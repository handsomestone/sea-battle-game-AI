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
	List<Task> lastAssignTask = new ArrayList<Task>();
	List<ShipInfo> lastSeenEnemy = new ArrayList<ShipInfo>();
	int lastDelta = 0;
	
	Random random = new Random();
	float d;
	int k;
	private float distBetween2Ship(ShipInfo a,ShipInfo b)
	{
		return (float)Math.ceil(Math.sqrt((a.PositionX - b.PositionX) * (a.PositionX - b.PositionX) + (a.PositionY - b.PositionY) * (a.PositionY - b.PositionY)));
	}
	private float dist(ShipInfo a, ShipInfo b)
	{
		return (a.PositionX - b.PositionX) * (a.PositionX - b.PositionX) + (a.PositionY - b.PositionY) * (a.PositionY - b.PositionY);
	}
	private int assempleCenterShip(List<ShipInfo> myShips)
	{
		int centerShipID = 1;
		int maxNearShip = 0; 
		float nearRadius = 90;
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
			if(i_nearShips > maxNearShip)
			{
				System.out.println("inearShips . maxNearShip"+i_nearShips+" "+maxNearShip);
				System.out.println("choose ID "+myShips.get(i).ID);
				centerShipID = myShips.get(i).ID;
				maxNearShip = i_nearShips;
			}
		}
		return centerShipID;
	}
	
	private  int turn =0;
	private  int Me ;
	private  int He ;
	private int StartTime = 0;

	@Override
	public void Iteration()
	{
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
		ShipInfo[] getMyshipByID = new ShipInfo[5];
		ShipInfo[] getHeshipByID = new ShipInfo[5];

		int currentDelta = Interface.TimeLeft;
		System.out.println("currentDelta "+currentDelta);
		boolean[] freeShip = {false,false,false,false,false};

		StartTime = Interface.TimeLeft;
		Interface.Data();
		/*for test*/
		/*
		  TreeSet<Task> taskTest = new TreeSet<Task>();
		  Task lmyTask = new Task(1);
		  lmyTask.score = 10;
		  lmyTask.priority = 1;
		  taskTest.add(lmyTask);
		  Task lmyTask1 = new Task(2);
		  lmyTask1.score = 8;
		  lmyTask1.priority = 2;
		  taskTest.add(lmyTask1);
		  Task lmyTask2 = new Task(3);
		  lmyTask2.score = 8;
		  lmyTask2.priority = 0;
		  taskTest.add(lmyTask2);
		  Task lmyTask3 = new Task(4);
		  lmyTask3.score = 12;
		  lmyTask3.priority = 5;
		  taskTest.add(lmyTask3);
		 
		  
		  for(Task iter:taskTest)
		  {
			  System.out.println(iter.ShipID);
		  }
		/* it is used to test sort the highest score*/
		Allship = Interface.Ship;
		Allresource = Interface.Resource;
		
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
				getMyshipByID[Allship.get(i).ID-1] = Allship.get(i);
			
			}	
			else
			{
				Enemyship.add(Allship.get(i));
				getHeshipByID[Allship.get(i).ID-1] = Allship.get(i);
			}
		}
		for(int k=0;k<Allresource.size();k++)
		{
			System.out.println("Allresource "+Allresource.get(k).PositionX+" "+Allresource.get(k).PositionY);
			if((Allresource.get(k).Faction) == Me)
				Myresource.add(Allresource.get(k));
			else if(Allresource.get(k).Faction == He)
				Enemyresource.add(Allresource.get(k));
			else
				Neutralresource.add(Allresource.get(k));
		}
		
		if(lastAssignTask.size()>0)
		{
			Iterator<Task> iterTask = lastAssignTask.iterator();
			while(iterTask.hasNext())
			{
				System.out.println("!!!!!!iterTask IN....");
				Task iter = iterTask.next();
				System.out.println("------iterTask ID "+iter.ShipID);
				/*occupy neutral land*/
				if(iter.priority == 0)
				{
					int shipID = iter.ShipID;
					System.out.println("!!!!!!iterTask 0...."+shipID);
					float leastXais = iter.target_x-OSInterface.ResourceRadius+50;
					float maxXais = iter.target_x+OSInterface.ResourceRadius-50;
					float leastYais = iter.target_y-OSInterface.ResourceRadius+50;
					float maxYais = iter.target_y+OSInterface.ResourceRadius-50;
					if((shipID>=1)&&(shipID<=5))
					{
						if(getMyshipByID[shipID-1].PositionX>leastXais&&getMyshipByID[shipID-1].PositionX<maxXais)
						{
							if(getMyshipByID[shipID-1].PositionY>leastYais&&getMyshipByID[shipID-1].PositionY<maxYais)
							{
								System.out.println("!!!!!!iterTask FINISH...."+shipID);
								if(iter.isAssemple)
								{
									System.out.println("!!!!!!iterTask assemeple...."+shipID);
									int centerShipID = assempleCenterShip(Myship);
									System.out.println("!!!!!!centerShipID...."+centerShipID);
									Task myTask = new Task(shipID);
									myTask.priority = 1;
									myTask.centerID = centerShipID;
									myTask.isStartRotatingTo = true;
									myTask.isStartMoving = true;
									if(shipID == centerShipID)
									{
										iterTask.remove();
										continue;
									}
									if(getMyshipByID[centerShipID-1].IsMoving)
									{
										float VelocityX = getMyshipByID[centerShipID-1].VelocityX;
										float VelocityY = getMyshipByID[centerShipID-1].VelocityY;
										float delta = Math.abs(currentDelta-lastDelta);
										myTask.target_x = getMyshipByID[centerShipID-1].PositionX+(delta*VelocityX)/1000;
										myTask.target_y = getMyshipByID[centerShipID-1].PositionY+(delta*VelocityY)/1000;
									}
									else
									{
										myTask.target_x = getMyshipByID[centerShipID-1].PositionX;
										myTask.target_y = getMyshipByID[centerShipID-1].PositionY;
									}
									currentTask.add(myTask);
									
								}
								System.out.println("before remove"+lastAssignTask.size());
								iterTask.remove();
								System.out.println("after remove"+lastAssignTask.size());
								freeShip[shipID-1] = true;
							}
						}
					}
				}
				if(iter.priority == 1)
				{
					System.out.println("priority ----1");
					System.out.println("iterSHIpID ----"+iter.ShipID+" iter.centerID "+iter.centerID);
					if((iter.ShipID>=1&&iter.ShipID<=5)&&((iter.centerID>=1)&&(iter.centerID<=5)))
					{
						float distance = distBetween2Ship(getMyshipByID[iter.ShipID-1],getMyshipByID[iter.centerID-1]);
						if(getMyshipByID[iter.ShipID-1].IsMoving&&distance>50)
						{
							int shipID = iter.ShipID;
							float VelocityX = getMyshipByID[iter.centerID-1].VelocityX;
							float VelocityY = getMyshipByID[iter.centerID-1].VelocityY;
							float delta = Math.abs(currentDelta-lastDelta);
							Task myTask = new Task(shipID);
							myTask.priority = 1;
							myTask.centerID = iter.centerID;
							myTask.isStartRotatingTo = true;
							myTask.isStartMoving = true;
							
							myTask.target_x = getMyshipByID[iter.centerID-1].PositionX+(delta*VelocityX)/1000;
							myTask.target_y = getMyshipByID[iter.centerID-1].PositionY+(delta*VelocityY)/1000;
							currentTask.add(myTask);
							iterTask.remove();
							System.out.println("if iterTask size in end"+lastAssignTask.size());
						}
						else if(distance<50&&getMyshipByID[iter.ShipID-1].IsMoving)
						{
							int shipID = iter.ShipID;
							Task myTask = new Task(shipID);
							myTask.priority =1;
							myTask.centerID = iter.centerID;
							System.out.println("shipID "+shipID+" centerID"+iter.centerID);
							myTask.rotation = getMyshipByID[iter.centerID-1].Rotation;
							myTask.isStartRotating = true;
							if(!getMyshipByID[iter.centerID-1].IsMoving)
							{
								myTask.isStopMoving = true;
							}
							currentTask.add(myTask);
							iterTask.remove();
							System.out.println("else if iterTask size in end"+lastAssignTask.size());
							System.out.println("else if currentTask size in end"+currentTask.size());
						}
						else if(!getMyshipByID[iter.centerID-1].IsMoving)
						{
							System.out.println("stop moving ship~~~~~");
							Task myTask = new Task(iter.ShipID);
							myTask.isStopMoving = true;
							iterTask.remove();
							System.out.println("else iterTask size in end"+lastAssignTask.size());
							System.out.println("else  currentTask size in end"+currentTask.size());
						}
					}
				}
			}
		}
		/*it is just for test*/
		for(int t=0;t<5;t++)
		{
			if(freeShip[t])
			{
				if(Enemyresource.size()>0)
				{
					ResourceInfo conquer = Enemyresource.get(0);
					Task myTask = new Task(t);
					myTask.isMoveTo = true;
					myTask.priority = 0; 
					myTask.target_x = conquer.PositionX+random.nextInt()%100;
					myTask.target_y = conquer.PositionY+random.nextInt()%100;
					currentTask.add(myTask);
				}
			}
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
						System.out.println("turn == 0 in Me == 1 getMyshipByID[0].PositionY"+getMyshipByID[0].PositionY);
						Task myTask = new Task(1);
						myTask.isMoveTo =true;						
						myTask.target_x = 0;
						myTask.target_y = getMyshipByID[0].PositionY;
						currentTask.add(myTask);
						Task myTask1 = new Task(2);
						myTask1.isMoveTo =true;
						myTask1.target_x = 0;
						myTask1.target_y = getMyshipByID[1].PositionY;
						currentTask.add(myTask1);
						Task myTask2 = new Task(3);
						myTask2.isMoveTo =true;
						myTask2.target_x = 0;
						myTask2.target_y = getMyshipByID[2].PositionY;
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
						myTask.target_y = getMyshipByID[2].PositionY;
						currentTask.add(myTask);
						Task myTask1 = new Task(4);
						myTask1.isMoveTo =true;
						myTask1.target_x = 0;
						myTask1.target_y = getMyshipByID[3].PositionY;
						currentTask.add(myTask1);
						Task myTask2 = new Task(5);
						myTask2.isMoveTo =true;
						myTask2.target_x = 0;
						myTask2.target_y = getMyshipByID[4].PositionY;
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
					myTask.target_y = getMyshipByID[1].PositionY;
					currentTask.add(myTask);
					Task myTask1 = new Task(3);
					myTask1.isMoveTo = true;
					myTask1.target_x = 0;
					myTask1.target_y = getMyshipByID[2].PositionY;;
					currentTask.add(myTask1);
					Task myTask2 = new Task(4);
					myTask2.isMoveTo = true;
					myTask2.target_x = 0;
					myTask2.target_y = getMyshipByID[3].PositionY;;
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
				if(l.isMoveTo)
				{
					Interface.MoveTo(l.ShipID,l.target_x,l.target_y);
				}
				if(l.isStartRotating)
				{
					Interface.StartRotating(l.ShipID,l.rotation);
				}
				if(l.isStartRotatingTo)
				{
					Interface.StartRotatingTo(l.ShipID,l.target_x,l.target_y);
				}
				if(l.isStopMoving)
				{
					Interface.StopMoving(l.ShipID);
				}
				lastAssignTask.add(l);
			}
			turn = 1;
			currentTask.clear();
		}
		/*it is used to rem Enemyship to prediction*/
		if(lastSeenEnemy.size()>0)
				lastSeenEnemy.clear();
		for(int i=0;i<Enemyship.size();i++)
		{
			lastSeenEnemy.add(Enemyship.get(i));
			
		}
		lastDelta = currentDelta;
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
class Task implements Comparable<Task>
{
	
		public int priority=0;
		public int centerID; /*it is used to assemple my ship*/
		public int ShipID;
		public float score;
		public float rotation;
		public float target_x;
		public float target_y;

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