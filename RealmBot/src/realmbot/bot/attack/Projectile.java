package realmbot.bot.attack;

import java.util.HashMap;

import realmbase.RealmBase;
import realmbase.data.EntityData;
import realmbase.data.Location;
import realmbase.data.PlayerData;
import realmbase.listener.PacketListener;
import realmbase.packets.client.EnemyHitPacket;
import realmbase.packets.client.PlayerHitPacket;
import realmbase.packets.client.PlayerShootPacket;
import realmbase.xml.datas.ItemData;
import realmbase.xml.datas.ProjectileData;

class Projectile{
	private static int bullet = 0;
	
	private ItemData data;
	private int bulletId;
	private Location location;
	private float life = 0;
	private float angle;
	private AttackClass attack;
	private boolean initialized;
	
	public Projectile(AttackClass attack,ItemData data){
		this.data=data;
		this.attack=attack;
		this.bulletId=++bullet;
	}
	
	public void shoot(Location attackLoc){
		this.location = attack.getClient().getMove().getPosition().clone();
		this.angle=(float) attackLoc.getAngleTo(this.location);
		PlayerShootPacket spacket = new PlayerShootPacket();
		spacket.setTime(this.attack.getClient().time());
		spacket.setAngle((float) Math.cos(Math.toRadians(angle)));
		spacket.setBulletId(this.bulletId);
		spacket.setContainerType(this.data.type);
		spacket.setStartingPos(this.location);
		attack.getClient().sendPacketToServer(spacket);
	}
	
	public boolean move(float timePerFrame){
		ProjectileData pdata = data.projectiles.get(0);
		location.x += timePerFrame * (pdata.speed/10f) * Math.sin(Math.toRadians(angle));
		location.y -= timePerFrame * (pdata.speed/10f) * Math.cos(Math.toRadians(angle));
		life+=timePerFrame;
		
		HashMap<Integer,EntityData> entities = PacketListener.getEntities().get(attack.getClient());
		
		if(entities!=null){
			EntityData e;
			for(int i = 0; i < entities.size(); i++){
				e=(EntityData)entities.get(i);
				if(e!=null&&e.getStatus().getPosition()!=null&&e.getStatus().getPosition().distanceTo(location) < 0.5f){
					if(e instanceof PlayerData){
						RealmBase.println("PlayerHitPacket "+e.getName());
						attack.getClient().sendPacketToServer(new PlayerHitPacket(bulletId,e.getStatus().getObjectId()));
					}else{
						RealmBase.println("EnemyHitPacket "+e.getStatus().getObjectId());
						attack.getClient().sendPacketToServer(new EnemyHitPacket(attack.getClient().time(), bulletId, e.getStatus().getObjectId(), false));
					}
					return true;
				}
			}
		}
		
		if(life > pdata.lifetimeMS){
			return true;
		}else{
			return false;
		}
	}
}