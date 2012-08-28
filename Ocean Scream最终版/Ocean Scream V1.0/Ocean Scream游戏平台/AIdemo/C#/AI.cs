using System;
using System.Collections.Generic;
using System.Text;
using MSTCOS.SDK;

namespace MSTCOS.SDK
{
    public class AI : AbstractAI
    {
        public AI(OSInterface Interface)
            : base(Interface)
        {
        }

        List<ShipInfo> ships, a, b;
        Random random = new Random();

        private float dist(ShipInfo a, ShipInfo b)
        {
            return (a.PositionX - b.PositionX) * (a.PositionX - b.PositionX) + (a.PositionY - b.PositionY) * (a.PositionY - b.PositionY);
        }

        public override void Iteration()
        {
            int i, j, k;
            float d, t;

            Interface.Data();
            ships = Interface.Ship;

            a = new List<ShipInfo>();
            b = new List<ShipInfo>();

            for (i = 0; i < ships.Count; i++)
                if (ships[i].Faction == Interface.Faction) a.Add(ships[i]);
                else b.Add(ships[i]);

            for (i = 0; i < a.Count; i++)
            {
                d = -1;
                k = 0;
                for (j = 0; j < b.Count; j++)
                {
                    t = dist(a[i], b[j]);
                    if (t < d || d == -1)
                    {
                        d = t;
                        k = j;
                    }
                }
                if (b.Count > 0)
                {
                    Interface.MoveTo(a[i].ID, b[k].PositionX + (float)random.NextDouble() * 200 - 100, b[k].PositionY + (float)random.NextDouble() * 200 - 100);
                    if (d < OSInterface.CannonRange * OSInterface.CannonRange) Interface.Attack(a[i].ID, b[k].ID);
                }
                else Interface.StartMoving(a[i].ID);
            }

        }

        public static void Main(String[] args)
        {
            String[] temp = new String[2];
            temp[0] = "buaacs";
            temp[1] = "111.222.33";
            SDK.StartGame(temp);
        }
    }
}