package MSTCOSSDK;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AI extends AbstractAI {
	public AI(OSInterface Interface) {
		super(Interface);
	}
	
	List<ShipInfo> ships,a,b;
	List<ShipInfo> lastEnemy = new ArrayList<ShipInfo>();
	List<ResourceInfo> rsrInfo;
	Random random = new Random();
	float d;
	int k;
	
	private float dist(ShipInfo a, ShipInfo b)
	{
		return (float)Math.ceil(Math.sqrt((a.PositionX - b.PositionX) * (a.PositionX - b.PositionX) + (a.PositionY - b.PositionY) * (a.PositionY - b.PositionY)));
	}
	private float distance(ShipInfo a,ResourceInfo b)
	{
		return (float)Math.ceil(Math.sqrt((a.PositionX-b.PositionX)*(a.PositionX-b.PositionX)+(a.PositionY-b.PositionY)*(a.PositionY-b.PositionY)));
	}
	private int minDist(ShipInfo a,List<ResourceInfo> b)
	{
		float min = -1;
		int flag = -1;
		
		for(int j=0;j<b.size();j++)
		{	
			if(min == -1)
			{
				min = distance(a,b.get(j));
				flag = j;
			}
			else if(min > distance(a,b.get(j)))
			{
				min = distance(a,b.get(j));
				flag = j;
			}
		}
		return flag;
	}
	private float minDistR(ShipInfo a,List<ResourceInfo> b)
	{
		float min = -1;
		int flag = -1;
		
		for(int j=0;j<b.size();j++)
		{	
			if(min == -1)
			{
				min = distance(a,b.get(j));
				flag = j;
			}
			else if(min > distance(a,b.get(j)))
			{
				min = distance(a,b.get(j));
				flag = j;
			}
		}
		return min;
	}
	private int[] minMyTeam(List<ShipInfo> list)
	{
		float min = -1;
		int[] min2 = new int[2];
		for(int i=0;i<list.size();i++)
			for(int j=0;j<list.size();j++)
			{
				float tmp = dist(list.get(i),list.get(j));
				if(min == -1)
				{
					min =tmp;
					min2[0] = i;
					min2[1] = j;
				}
				else if(tmp<min)
				{
					min  = tmp;
					min2[0] = i;
					min2[1] = j;
				}
			}
		return min2;
	}
	private float distShopPoint(ShipInfo ship,float x,float y)
	{
		return (float)Math.ceil(Math.sqrt((ship.PositionX-x)*(ship.PositionX-x)+(ship.PositionY-y)*(ship.PositionY-y)));
	}
	private int minShip(ShipInfo my,List<ShipInfo> list)
	{
		float min = -1;
		int a = -1;
		for(int j=0;j<list.size();j++)
		{
			if(-1 == min)
			{	min = dist(my,list.get(j));
				a = j;
			}
			else if(min > dist(my,list.get(j)))
			{
				min = dist(my,list.get(j));
				a = j;
			}
		}
		return a;
	}
	private float degree2Vector(Vector2d v1,Vector2d v2)
	{
		v1.normalize();
		v2.normalize();
		float dot = 0;
		float degree = 0;
		double epsilon = 1.0e-6;
		double angle = 0.0;
		dot = v1.dot(v2);
		if(Math.abs(dot-1) <= epsilon)
		{
			degree = 0;
			System.out.println("degree1 = "+degree);
		}
		else if(Math.abs(dot+1) <= epsilon)
		{
			degree = -180;
			System.out.println("degree2 = "+degree);
		}
		else
		{
			double cross;
			angle = Math.acos(dot);
			cross = v1.cross(v2);
			if(cross < 0)
			{
				degree = (float) -Math.toDegrees(angle);
				System.out.println("degree3 = "+degree);
			}
			else
			{	
				degree = (float) Math.toDegrees(angle);
				System.out.println("degree4 = "+degree);
			}
		}
		return degree;
	}
	@Override
	public void Iteration()
	{
		Interface.Data();
		
		ships = Interface.Ship;
		rsrInfo = Interface.Resource;
		float averageX = 0;
		float averageY = 0;
		float degree = 0;
		float Mindist = -1;
		float dist = -1;
		int battleNum = 0;
		
		int[] MinRsr = null;
		boolean rich =false;
		a = new ArrayList<ShipInfo>();
		
		b = new ArrayList<ShipInfo>();
	//	HashMap<String,ShipInfo> my = new HashMap<String,ShipInfo>();
		ShipInfo[] myt = new ShipInfo[4];
		for (int i = 0; i < ships.size(); i++)
			if (ships.get(i).Faction == Interface.Faction) 
				{
				a.add(ships.get(i)); System.out.println(ships.get(i).ID);
				}
			else b.add(ships.get(i));
		
		for(ShipInfo iter:a)
		{
			averageX += iter.PositionX;
			averageY += iter.PositionY;
		}
		averageX /=5;
		averageY /=5;
		int last = 0;
		System.out.println("----Faction  "+ Interface.Faction);
		
		ShipInfo shipTest =a.get(1);
	
		for(int i=0;i<a.size();i++)
			{
				//Interface.MoveTo(a.get(i).ID, 100, 100);
				System.out.println("a.rotation..."+a.get(i).Rotation);
			}
		for(int j = 0;j<rsrInfo.size();j++)
		{
			System.out.println("rsrInfo "+rsrInfo.get(j).ID+" "+rsrInfo.get(j).PositionX+" "+rsrInfo.get(j).PositionY);
		}
		if(b.size()>0)
		{
			int j = minShip(a.get(2),b);
			for(int m=0;m<a.size();m++)
				{
					Interface.StartRotating(a.get(m).ID, b.get(j).Rotation+90);
					System.out.println("rotation..."+b.get(j).Rotation);
				}
		}
		
		/*
		if(!shipTest.IsMoving)
		{
			last = minDist(shipTest,rsrInfo);
			if(distance(shipTest,rsrInfo.get(last))>196&&!shipTest.IsBlocked)
			{
				
				Interface.StartRotatingTo(shipTest.ID, rsrInfo.get(last).PositionX, rsrInfo.get(last).PositionY);
				Interface.StartMoving(shipTest.ID);
			}
			if(shipTest.IsBlocked)
			{
				Interface.StartMoving(shipTest.ID);
			}
		}
		
		if(shipTest.IsBlocked)
		{
			if(!shipTest.IsRotating)
			{
				Interface.StartRotating(shipTest.ID, 45);
			}
			last = minDist(shipTest,rsrInfo);
			if(distance(shipTest,rsrInfo.get(last))>196)
			{
				if(!shipTest.IsMoving)
				{
				Interface.StartRotatingTo(shipTest.ID, rsrInfo.get(last).PositionX, rsrInfo.get(last).PositionY);
				Interface.StartMoving(shipTest.ID);
				}
			}
			
		}
		if(shipTest.IsMoving)
		{
			System.out.println("distance "+distance(shipTest,rsrInfo.get(last)));
			if(distance(shipTest,rsrInfo.get(last))<=300)
			{
				rich = true;
				//Interface.StopMoving(shipTest.ID);
				Vector2d v1 = new Vector2d(shipTest.VelocityX,shipTest.VelocityY);
				Vector2d v2 = new Vector2d(rsrInfo.get(last).PositionX-shipTest.PositionX,rsrInfo.get(last).PositionY-shipTest.PositionY);
				float degree2 = degree2Vector(v1,v2);
				System.out.println("rotating degree"+degree2);
				if(degree2 == 0)
				{
					Interface.StartRotating(shipTest.ID, 90);
				}
				else if(Math.abs(degree2)<90)
				{
					
					if(!shipTest.IsRotating)						
					{
						System.out.println("rotating degree <90"+degree2);
						if(degree2>0)
						 Interface.StartRotating(shipTest.ID, -(90-Math.abs(degree2)));
						else
							Interface.StartRotating(shipTest.ID, 90-Math.abs(degree2));
					}
						
				}
				else if(Math.abs(degree2)>90)
				{
					
					if(!shipTest.IsRotating)
					{
						System.out.println("rotating degree >90"+degree2);
						if(degree2>0)
						{
							Interface.StartRotating(shipTest.ID, degree2-90);
						}
						else
						{
							Interface.StartRotating(shipTest.ID, -(Math.abs(degree2)-90));
						}
					}
				}
				
			}
			else if(rich ==true)
			{
				if(!shipTest.IsMoving)
				{
					Interface.StartMoving(shipTest.ID);
					rich =false;
				}
			}*/
			/*
			else
			{
				Vector2d v1 = new Vector2d(shipTest.VelocityX,shipTest.VelocityY);
				Vector2d v2 = new Vector2d(rsrInfo.get(last).PositionX-shipTest.PositionX,rsrInfo.get(last).PositionY-shipTest.PositionY);
				float degree2 = degree2Vector(v1,v2);
				if(degree2 == 0)
				{
					Interface.StartRotating(shipTest.ID, 90);
				}
				else if(Math.abs(degree2)<90)
				{
					if(!shipTest.IsRotating)
					{
						if(degree2>0)
						 Interface.StartRotating(shipTest.ID, -(90-Math.abs(degree2)));
						else
							Interface.StartRotating(shipTest.ID, 90-Math.abs(degree2));
					}
						
				}
				else if(Math.abs(degree2)>90)
				{
					if(!shipTest.IsRotating)
					{
						if(degree2>0)
						{
							Interface.StartRotating(shipTest.ID, degree2-90);
						}
						else
						{
							Interface.StartRotating(shipTest.ID, -(Math.abs(degree2)-90));
						}
					}
				}
			}
			/*
			if(minDistR(shipTest,rsrInfo)<196)
			{
				if(!shipTest.IsRotating)
				{	
					Interface.StartRotating(shipTest.ID, 180);
				}
			}
			if(b.size()>0)
			{
				if(!shipTest.IsRotating)
				{
					MinRsr = new int[2];
				

				   MinRsr = minMyTeam(a);
				  Interface.StartRotatingTo(shipTest.ID,(a.get(MinRsr[0]).PositionX+a.get(MinRsr[1]).PositionX)/2,(a.get(MinRsr[0]).PositionY+a.get(MinRsr[1]).PositionY )/2);
			
				}
			}
		}*/
		/*
		for(int i=0;i<a.size();i++)
		{
			//for(int j=0;j<rsrInfo.size();j++)
			//{
			    int j = minDist(a.get(i),rsrInfo);
			    if(a.get(i).IsBlocked)
			    {
			    	j = last;
			    	Interface.Stop(a.get(i).ID);
			    }
			   
				if(!a.get(i).IsMoving)
				{
					last = j;
					System.out.println(rsrInfo.get(j).PositionX+" "+rsrInfo.get(j).PositionY);
					Interface.StartRotatingTo(a.get(i).ID, rsrInfo.get(j).PositionX, rsrInfo.get(j).PositionY);
					Interface.StartMoving(a.get(i).ID);
				}
				else if(distance(a.get(i),rsrInfo.get(j))<196)
				{
					Interface.StartRotatingTo(a.get(i).ID, 0, 0);
					Interface.StopMoving(a.get(i).ID);
				}
				else if((1024 - a.get(i).PositionX)<=100)
				{
					Interface.StartRotating(a.get(i).ID, 90);
				}
				else if((a.get(i).PositionX +1024)<=100)
				{
					Interface.StartRotating(a.get(i).ID, 90);
				}
				else if((a.get(i).PositionY+1024)<=100)
				{
					Interface.StartRotating(a.get(i).ID, 90);
				}
				else if((1024-a.get(i).PositionY)<=100)
				{
					Interface.StartRotating(a.get(i).ID, 90);
				}
				else
				{
					if(b.size()>0)
					{
						for(int k=0;k<b.size();k++)
						{
							if(dist(a.get(i),b.get(k))<=400*350)
							{
								Interface.MoveTo(a.get(i).ID,a.get((i+1)%a.size()).PositionX,a.get((i+1)%a.size()).PositionY);
							}
						}
					}
				}
				rsrInfo.remove(j);
			//}
		}
		/*
		for(ResourceInfo iter:rsrInfo)
		{
		
			dist = (iter.PositionX - averageX)*(iter.PositionX - averageX)+(iter.PositionY - averageY)*(iter.PositionY - averageY);
			if(-1 == Mindist)
			{
				Mindist = dist;
				MinRsr = iter;
			}
			else if(dist < Mindist)
			{

				Mindist = dist;
				MinRsr = iter;
			}
		}*/
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
			
			
		}
		
	}
	
	public static void main(String args[])
	{
		String[] temp =new String[2];
		temp[0] = "buaajava";
		temp[1] = "111.22.33";
		SDK.StartGame(temp);
	}
}

class Seek
{
	/** Target object */
	
	protected ResourceInfo r_target; 
	protected ShipInfo s_target; 
	public Seek(ResourceInfo target)
	{
		r_target = target;
	}
	public Seek(ShipInfo target)
	{
		s_target = target;
	}
	public int SeekResource(ShipInfo source,OSInterface Interface)
	{
		Interface.StartRotatingTo(source.ID,r_target.PositionX, r_target.PositionY);
		return 1;
	}
	public Vector2d calculate_r(ShipInfo ship,float shipMaxSpeed)
	{
		float distance;
		if(r_target == null) return new Vector2d(0, 0);
		Point2d target = new Point2d(r_target.PositionX,r_target.PositionY);
		Point2d source = new Point2d(ship.PositionX,ship.PositionY);
		Vector2d des_vel = target.sub(source);
		distance = des_vel.length();
		des_vel.scale(shipMaxSpeed / distance);
		des_vel = des_vel.sub(ship.VelocityX,ship.VelocityY);
		
		return des_vel;
	}
	public float calcudegree_r(ShipInfo ship)
	{
		float PositionSourceX = r_target.PositionX;
		float PositionSourceY = r_target.PositionY;
		float dot = 0;
		float degree;
		double epsilon = 1.0e-6;
		double angle = 0.0;
		Vector2d des_vel = new Vector2d(PositionSourceX - ship.PositionX,PositionSourceY-ship.PositionY);
		des_vel.normalize();
		System.out.println("ship.VelocityX,ship.VelocityY"+ship.VelocityX+" "+ship.VelocityY);
		
		Vector2d src_vel = new Vector2d(ship.VelocityX,ship.VelocityY);
		src_vel.normalize();
		dot = src_vel.dot(des_vel);
		if(Math.abs(dot-1) <= epsilon)
		{
			degree = 0;
			System.out.println("degree1 = "+degree);
		}
		else if(Math.abs(dot+1) <= epsilon)
		{
			degree = -180;
			System.out.println("degree2 = "+degree);
		}
		else
		{
			double cross;
			angle = Math.acos(dot);
			cross = src_vel.cross(des_vel);
			if(cross < 0)
			{
				degree = (float) -Math.toDegrees(angle);
				System.out.println("degree3 = "+degree);
			}
			else
			{	
				degree = (float) Math.toDegrees(angle);
				System.out.println("degree4 = "+degree);
			}
		}
		return degree;
	}
	public float calculateAngle_r(ShipInfo ship,float shipMaxSpeed)
	{
		float distance;
		float dot;
		float cos;
		float acos;
		float degree = 0;
		double angle;
		double epsilon = 1.0e-6;
		Vector2d ship_vel;
		if(r_target == null) return 0;
		Point2d target = new Point2d(r_target.PositionX,r_target.PositionY);
		Point2d source = new Point2d(ship.PositionX,ship.PositionY);
		Vector2d des_vel = target.sub(source);
		distance = des_vel.length();
		des_vel.scale(shipMaxSpeed / distance);
		ship_vel = new Vector2d(ship.VelocityX,ship.VelocityY);
		des_vel = des_vel.sub(ship_vel);
		
		/*normalize*/
		ship_vel.normalize();
		des_vel.normalize();
		dot = des_vel.dot(ship_vel);
		if(Math.abs(dot-1) <= epsilon)
		{
			angle = 0;
		}
		else if(Math.abs(dot+1) <= epsilon)
		{
			angle = -Math.PI;
		}
		else{
			double cross;
			angle = Math.acos(dot);
			cross = ship_vel.cross(des_vel);
			if(cross < 0)
			{
				degree = (float) -Math.toDegrees(angle);
			}
			else
			{	
				degree = (float) Math.toDegrees(angle);
			}
			
		}
	
		
		
		return degree;
	}
}

class Point2d
{
	/** X position */
	protected float m_x;
	/** Y position */
	protected float m_y;
	
	public Point2d() 
	{
		m_x = 0;
		m_y = 0;
	}
	public Point2d(float x,float y)
	{
		m_x = x;
		m_y = y;
	}	
	public Point2d(Point2d v)
	{
		m_x = v.getX();
		m_y = v.getY();
	}
	public void setX(float x) {m_x = x;}
	public void setY(float y) {m_y = y;}
	public float getX() { return m_x;}
	public float getY() { return m_y;}
	
	public Point2d add(Vector2d v)
	{
		return (new Point2d(m_x + v.getX(),m_y + v.getY()));
	}
	public Vector2d sub(Point2d p)
	{
		return (new Vector2d(m_x - p.getX(),m_y - p.getY()));
	}
}

class Vector2d
{
	/** X member of vector */
	protected float m_x;
    /** Y member of vector */
	protected float m_y;
	public Vector2d()
	{
		m_x = 0;
		m_y = 0;
	}
	public Vector2d(float x,float y)
	{
		m_x = x;
		m_y = y;
	}
	public Vector2d(Vector2d v)
	{
		m_x = v.getX();
		m_y = v.getY();
	}
	public Vector2d(Point2d p)
	{
		m_x = p.getX();
		m_y = p.getY();
	}
	public void setX(float x) {m_x = x;}
	public void setY(float y) {m_y = y;}
	public float getX() {return m_x;}
	public float getY() {return m_y;}
	public Vector2d add(Vector2d v)
	{
		return (new Vector2d(m_x+v.getX(),m_y+v.getY()));
	}
	public Vector2d sub(Vector2d v)
	{
		return (new Vector2d(m_x-v.getX(),m_y-v.getY()));
	}
	public Vector2d sub(float v_x,float v_y)
	{
		return (new Vector2d(m_x-v_x,m_y-v_y));
	}
	public float length() 
	{ 
		float result;
		try
		{
			result=  (float) Math.sqrt(m_x*m_x+m_y*m_y); 
			
		}
		catch (java.lang.ArithmeticException e)
		{
			result = 0;
		}
		return result;
	}
	public float lengthSquared() 
	{ 
		return (m_x*m_x+m_y*m_y); 
	}
	public void scale(float a) 
	{ 
		m_x*=a; 
		m_y*=a; 
	}
	public void normalize() 
	{ 
		try
		{
			scale(1/length()); 
		}
		catch (java.lang.ArithmeticException e)
		{
			m_x = 0;
			m_y = 0;
		}
	}
	public float dot(Vector2d v) 
	{ 
		return m_x * v.getX() + m_y * v.getY(); 
	}
	public float cross(Vector2d v)
	{
		return m_x * v.getY() - v.getY()* m_y;
	}
}