package realmbot.bot.move;

import lombok.Getter;
import lombok.Setter;
import realmbase.Client;
import realmbase.Parameter;
import realmbase.RealmBase;
import realmbase.data.EntityData;
import realmbase.data.Location;
import realmbase.data.PlayerData;
import realmbase.listener.ObjectListener;
import realmbot.bot.Bot;

@Getter
@Setter
public class MoveFollower implements MoveClass {
	private Location position;
	private int lastMoveTime;
	private Bot client;
	private float followDist = 0.25f;
	private boolean teleport = false;
	private String followTarget;
	private int firstSteps=10;
	
	public MoveFollower() {}
	
	public MoveFollower(String followTarget) {
		this.followTarget=followTarget;
	}
	
	@Override
	public Location move(int time) {
		if(firstSteps>0){
			firstSteps--;
			return getPosition();
		}
		
		PlayerData e = (followTarget.isEmpty()?null:ObjectListener.getPlayerData(getClient(), followTarget));
		if (e != null) {
			if (teleport && e.getStatus().getPosition().distanceSquaredTo(getPosition()) > 144 && client.getMapInfo().isAllowPlayerTeleport()) {
				client.teleport(e.getStatus().getObjectId());
			}
			if (e.getStatus().getPosition().distanceSquaredTo(getPosition()) > followDist) {
				return getFollowPosition(time, e.getStatus().getPosition());
			}
		}
		return getPosition();
	}
	
	public Location getFollowPosition(int time, Location targ) {
		float angle = getPosition().getAngleTo(targ);
		Location loc = getPosition().clone();
		boolean xLess = false, yLess = false;
		if (loc.x < targ.x){
			xLess = true;
			RealmBase.println("xLess true "+loc.x +"<"+ targ.x);
		}
		if (loc.y < targ.y){
			yLess = true;
			RealmBase.println("yLess true "+loc.y +"<"+ targ.y);
		}
		double speed = Parameter.SPEED_BASE + Parameter.SPEED_MULTIPLIER;
		if (time > 600) time = 600; 
		loc.x += time * speed * Math.sin(Math.toRadians(angle));
		loc.y -= time * speed * Math.cos(Math.toRadians(angle));
		
		if (xLess && loc.x < targ.x){
			RealmBase.println("1 -> "+loc.x + "<" +targ.x);
			loc.x = targ.x;
		}
		
		if (!xLess && loc.x > targ.x){
			RealmBase.println("2 -> "+loc.x + ">" +targ.x);
			loc.x = targ.x;
		}

		if (yLess && loc.y < targ.y){
			RealmBase.println("3 -> "+loc.y + "<" +targ.y);
			loc.y = targ.y;
		}
		
		if (!yLess && loc.y > targ.y){
			RealmBase.println("4 -> "+loc.y + ">" +targ.y);
			loc.y = targ.y;
		}
		return loc;
	}

	@Override
	public int getlastMoveTime() {
		return lastMoveTime;
	}

	@Override
	public void setClient(Client client) {
		this.client=(Bot)client;
	}

}
