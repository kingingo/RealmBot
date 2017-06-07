package realmbot.bot.move;

import lombok.Getter;
import lombok.Setter;
import realmbase.Parameter;
import realmbase.RealmBase;
import realmbase.data.Callback;
import realmbase.data.Location;
import realmbot.bot.Bot;

@Getter
@Setter
public class MoveTarget implements MoveClass {
	private Location position;
	private int lastMoveTime;
	private Bot client;
	private Location target;
	private Callback<MoveTarget> reachTarget;
	
	public MoveTarget() {}
	
	public MoveTarget(Location target, Callback<MoveTarget> reachTarget) {
		this.target=target;
		this.reachTarget=reachTarget;
	}
	
	@Override
	public Location move() {
		int time = client.time()-lastMoveTime;
		setLastMoveTime(client.time());
		if(getPosition().distanceSquaredTo(getTarget()) < 0.2){
			if(reachTarget!=null)
				reachTarget.call(this, null);
			
			return getPosition();
		}
		return getCalculatePosition(time, getTarget());
	}
	
	public Location getCalculatePosition(int time, Location targ) {
		float angle = getPosition().getAngleTo(targ);
		Location loc = getPosition();
		
		double speed = Parameter.SPEED_BASE + Parameter.SPEED_MULTIPLIER;
		if (time > 600) time = 600; 
		
		loc.x += time * speed * Math.sin(Math.toRadians(angle));
		loc.y -= time * speed * Math.cos(Math.toRadians(angle));
		return loc;
	}

	public int getlastMoveTime() {
		return lastMoveTime;
	}
}

