package realmbot.bot.attack2;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;

import realmbase.Client;
import realmbase.RealmBase;
import realmbase.data.EntityData;
import realmbase.data.Location;
import realmbase.data.PlayerData;
import realmbase.list.Sort;
import realmbase.listener.PacketListener;
import realmbase.utils.UtilClient;
import realmbase.xml.GetXml;
import realmbase.xml.datas.ProjectileData;
import realmbot.bot.Bot;

public class Projectile {
//	private static int bullet = 0; //Kugel zähler
	private int ownerId = 0;
	private int bulletId = 0; //Kugel ID
//	private int numShots = 0; // pro Schuss wie viel Kugeln
	private float angle;
	private Location startPosition;
	private Location currentPosition;
	private long startTime=0;
	private ProjectileData projectileData;
	
	public Projectile(int startTime,int ownerId,int bulletId,Location startPosition,float angle,int numShots, ProjectileData projectileData){
//		this.numShots=numShots;
		this.ownerId=ownerId;
		this.projectileData=projectileData;
		this.angle=angle;
		this.bulletId=bulletId;
		this.startPosition=startPosition.clone();
		this.currentPosition=startPosition.clone();
		this.startTime=startTime;
	}
	
	public void shoot(){
		
	}
	
	public boolean update(Client client, long time){
		try{
			long life = (time-this.startTime);
			Bot bot = ((Bot)client);
			
			if(life > this.projectileData.lifetimeMS)return false;
			Double[] point = positionAt(life);
			
			this.currentPosition.x = point[0].floatValue();
			this.currentPosition.y = point[1].floatValue();
			
			if(bot.getMove()!=null&&bot.getMove().getPosition()!=null){
				if(bot.getMove().getPosition().distanceSquaredTo(this.currentPosition)<0.5){
					RealmBase.println("Near "+bot.getMove().getPosition().distanceSquaredTo(this.currentPosition));
					RealmBase.println("HIT "+bot.getName());
					return true;
				}
			}
			
			HashMap<Integer,EntityData> list = (HashMap<Integer,EntityData>) PacketListener.get(client).clone();
			EntityData entity;
			for(Integer id : list.keySet()){
				entity = list.get(id);
				if(ownerId==id)continue;
				
				if(entity.getStatus().getPosition().distanceSquaredTo(this.currentPosition)<0.5){
					if(entity instanceof PlayerData){
						RealmBase.println("HIT "+((PlayerData)entity).getName());
					}
//					else{
//						RealmBase.println("HIT "+id);
//					}
				}
			}
			return true;
		}catch(ConcurrentModificationException ex){
			ex.printStackTrace();
			return false;
		}
	}
	
	public Double[] positionAt(long life){
		double speed = life * (this.projectileData.speed/10000);
		Location startPosition = this.startPosition.clone();
		double _loc4_ = this.bulletId % 2 == 0?0:Math.PI;
		Double[] point = new Double[]{Double.valueOf(startPosition.x), Double.valueOf(startPosition.y)};
		
		if(this.projectileData.wavy){
			double _loc5_ = 6 * Math.PI;
			double _loc6_ = Math.PI / 64;
			double _loc7_ = this.angle + _loc6_ * Math.sin(_loc4_ + _loc5_ * life / 1000);
			point[0] = point[0] + speed * Math.cos(_loc7_);
			point[1] = point[1] + speed * Math.sin(_loc7_);
		}else if(this.projectileData.parametric){
			 double _loc8_ = life / this.projectileData.lifetimeMS * 2 * Math.PI;
			 double _loc9_ = Math.sin(_loc8_) * ((this.bulletId % 2 != 0)?1:-1);
			 double _loc10_ = Math.sin(2 * _loc8_) * (this.bulletId % 4 < 2?1:-1);
			 double _loc11_ = Math.sin(this.angle);
			 double _loc12_ = Math.cos(this.angle);
			 point[0] = point[0] + (_loc9_ * _loc12_ - _loc10_ * _loc11_) * this.projectileData.magnitude;
			 point[1] = point[1] + (_loc9_ * _loc11_ + _loc10_ * _loc12_) * this.projectileData.magnitude;
		}else{
			if(this.projectileData.boomerang){
				 double _loc13_ = this.projectileData.lifetimeMS * (this.projectileData.speed / 10000) / 2;
	             if(speed > _loc13_){
	            	 speed = _loc13_ - (speed - _loc13_);
	             }
			}
			point[0] = point[0] + speed * Math.cos(this.angle);
			point[1] = point[1] + speed * Math.sin(this.angle);
            if(this.projectileData.amplitude != 0){
               double _loc14_ = this.projectileData.amplitude * Math.sin(_loc4_ + life / this.projectileData.lifetimeMS * this.projectileData.frequency * 2 * Math.PI);
               point[0] = point[0] + _loc14_ * Math.cos(this.angle + Math.PI / 2);
               point[1] = point[1] + _loc14_ * Math.sin(this.angle + Math.PI / 2);
            }
		}
		return point;
	}
}
