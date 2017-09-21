package realmbot.bot.attack;

import java.util.ArrayList;

import lombok.Setter;
import realmbase.event.EventHandler;
import realmbase.event.EventListener;
import realmbase.event.EventManager;
import realmbase.event.events.ShootEvent;
import realmbot.bot.Bot;

public class AttackThread implements Runnable,EventListener {

	@Setter
	private Bot client;
	private Thread thread;
	private ArrayList<Projectile> projectiles = new ArrayList<>();

	public AttackThread() {
		EventManager.register(this);
		start();
	}
	
	public void start(){
		cancel();
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
	
	@EventHandler
	public void ProjectileShoot(ShootEvent event){
		Projectile projectile = new Projectile(((Bot)client).time(),event.getOwnerId(), event.getBulletId(),event.getStartingPos(), event.getAngle(), 1, event.getProjectileData());
		this.projectiles.add(projectile);
	}
	
	@Override
	public void run() {
		Projectile projectile;
		while(true){
			try {
				Thread.sleep(1);
				
				if(!this.projectiles.isEmpty()){
					for(int i = 0; i < this.projectiles.size() ; i++){
						projectile = this.projectiles.get(i);
						if(!projectile.update(client, client.time())){
							this.projectiles.remove(projectile);
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
