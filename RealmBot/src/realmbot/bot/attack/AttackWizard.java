package realmbot.bot.attack;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;
import realmbase.RealmBase;
import realmbase.data.EntityData;
import realmbase.data.Location;
import realmbase.list.Sort;
import realmbase.listener.PacketListener;
import realmbase.utils.Milis;
import realmbase.utils.UtilClient;
import realmbase.xml.GetXml;
import realmbase.xml.datas.ItemData;
import realmbot.bot.Bot;

@Getter
@Setter
public class AttackWizard implements AttackClass,Runnable{

	private Bot client;
	private Thread thread;
	private ArrayList<Projectile> projectiles = new ArrayList<>();
	
//	PlayerShoot -> Client schießt wenn trifft PlayerHit/EnemyHit
//	Shoot -> Mob schießt -> answer ShootACK
	public AttackWizard(){
		this.thread = new Thread(this);
		this.thread.start();
	}
	
	public void cancel(){
		if(this.thread!=null){
			if(this.thread.isAlive()){
				this.thread.stop();
			}
			this.thread=null;
		}
	}

	@Override
	public void run() {
		long nextShoot = 0;
		long last = 0;
		
		while(true){
			try {
				Thread.sleep(1);
				if(!projectiles.isEmpty()){
					Projectile projectile;
					for(int i = 0; i < projectiles.size(); i++){
						projectile = projectiles.get(i);
						if(projectile.move( ((System.currentTimeMillis()-last)/1000) ))projectiles.remove(projectile);
					}
				}
				
				if(((System.currentTimeMillis()-nextShoot) > Milis.SECOND * 0.01) && client.getMove().getPosition()!= null && PacketListener.get(client)!=null){
					nextShoot = System.currentTimeMillis();
					ArrayList<Sort<EntityData>> list = UtilClient.getOrdedEntityList((HashMap<Integer,EntityData>)PacketListener.get(client).clone(), client.getMove().getPosition().clone(), 8.5);

					RealmBase.println(client,"Found: "+list.size());
					if(!list.isEmpty()){
						EntityData e = list.get(0).getObject();
						shoot(e.getStatus().getPosition());
						RealmBase.println(client,"Attack: "+GetXml.objectMap.get(e.getObjectType()).id);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			last=System.currentTimeMillis();
		}
	}

	@Override
	public void shoot(Location location) {
		ItemData data = GetXml.itemMap.get(2711);
		for(int i = 0; i<data.numProjectiles; i++){
			Projectile projectile = new Projectile(this, data);
			projectile.shoot(location);
			this.projectiles.add(projectile);
		}
	}
}
