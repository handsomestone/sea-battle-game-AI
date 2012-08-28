//Ocean Scream SDK By LiJiancheng in BUAA MSTC
#pragma once
#include <string>
#include <vector>
#include <WinSock2.h>
using namespace std;
const float MaxArmor = 1000;
const float Acceleration = 10;
const float MaxSpeed = 25;
const float AngularRate = 45;
const float ShipBoundingRadius = 15;
const float IslandBoundingRadius = 32;
const float CannonSpan = 4;
const float CannonAngle = 90;
const float CannonRange = 350;
const float ResourceRadius = 196;
const float ResourceRestoreRate[6] = { 0, 5, 10, 15, 25, 50 };
const int MapWidth = 2048;
const int MapHeight = 2048;
const float RangeOfView = 400;

#define DLL_EXPORT

#ifdef DLL_EXPORT
#define DLL_DESCRIPTOR __declspec(dllexport)
#else
#define DLL_DESCRIPTOR __declspec(dllimport)
#endif

class DLL_DESCRIPTOR ShipInfo
{
public:
	ShipInfo(int ID, int Faction, float Armor, float PositionX, float PositionY, float VelocityX, float VelocityY, float CurrentSpeed,
            float DirectionX, float DirectionY, float Rotation, bool IsMoving, bool IsBlocked, bool IsRotating, float CooldownRemain[2]);
	int ID;
	int Faction;
	float Armor;
	float CooldownRemain[2];
	float CurrentSpeed;
	float DirectionX;
	float DirectionY;
	bool IsBlocked;
	bool IsMoving;
	bool IsRotating;
	float PositionX;
	float PositionY;
	float Rotation;
	float VelocityX;
	float VelocityY;
};

class DLL_DESCRIPTOR ResourceInfo
{
public:
	ResourceInfo(int ID, int Faction, float PositionX, float PositionY);
	int ID;
	int Faction;
	float PositionX;
	float PositionY;
};

DLL_DESCRIPTOR class OSInterface
{
public:
	OSInterface(string host, int port);
	void Data();
	void Attack(int sourceShip, int targetShip);
	void MoveTo(int sourceShip, float x, float y);
	void StartMoving(int ship);
	void StartRotating(int ship,float target);
	void StartRotatingTo(int ship,float x,float y);
	void Stop(int ship);
	void StopMoving(int ship);
	void StopRotating(int ship);
	int Faction;
	int TimeLeft;
	vector<ShipInfo> Ship;
	vector<ResourceInfo> Resource;
	bool Running() const;

	void GetReady();
	void SendInfo(string name,int r,int g,int b) const;
private:
	SOCKET server;
	bool running;
	bool IsEnd(string response);
};

DLL_DESCRIPTOR class MSTCOSSDK
{
public:
	static void MSTCOSSDK::StartGame(int argc, char* argv[], void (*Iteration)(OSInterface* const Interface));
	OSInterface Interface;
private:
	MSTCOSSDK(string host, int port, string name, string color, void (*Iteration)(OSInterface* const Interface));
	void (*Iteration)(OSInterface* const Interface);
	void Start();
};
