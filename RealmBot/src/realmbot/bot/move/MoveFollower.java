package realmbot.bot.move;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;
import realmbase.Parameter;
import realmbase.RealmBase;
import realmbase.data.Location;
import realmbase.data.PlayerData;
import realmbase.data.portal.PortalData;
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
	private Location followPosition;
	
	public MoveFollower() {}
	
	public MoveFollower(String followTarget) {
		this.followTarget=followTarget;
	}
	
	@Override
	public Location move() {
		int time = client.time()-lastMoveTime;
		setLastMoveTime(client.time());
		PlayerData e = followTarget.isEmpty()?null:ObjectListener.getPlayerData(getClient(), followTarget);
	
		if (e != null) {
			followPosition = e.getStatus().getPosition();
			if (teleport && e.getStatus().getPosition().distanceSquaredTo(getPosition()) > 144 && client.getMapInfo().isAllowPlayerTeleport()) {
				client.teleport(e.getStatus().getObjectId());
			}
			if (e.getStatus().getPosition().distanceSquaredTo(getPosition()) > followDist) {
				return getFollowPosition(time, e.getStatus().getPosition());
			}
		}else{
			if(followPosition!=null){
				HashMap<Integer,PortalData> portals = ObjectListener.getPortals().get(client);
				
				for(PortalData portal : portals.values()){
					if(portal.getStatus().getPosition().distanceTo(followPosition)<1){
						RealmBase.println(getClient(), "Portal: "+portal.getName());
						client.usePortal(portal);
						followPosition = null;
						break;
					}
				}
			}
		}
		return (followPosition!=null?followPosition:getPosition());
	}
	
	public Location getFollowPosition(float time, Location targ) {
		float angle = getPosition().getAngleTo(targ);
		Location loc = getPosition();
		
		double speed = Parameter.SPEED_BASE + Parameter.SPEED_MULTIPLIER;
		if (time > 600) time = 600; 
		
		loc.x += time * speed * Math.sin(Math.toRadians(angle));
		loc.y -= time * speed * Math.cos(Math.toRadians(angle));
		return loc;
	}

	@Override
	public int getlastMoveTime() {
		return lastMoveTime;
	}
}
