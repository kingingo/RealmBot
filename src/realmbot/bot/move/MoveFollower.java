package realmbot.bot.move;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;
import realmbase.Parameter;
import realmbase.RealmBase;
import realmbase.data.EntityData;
import realmbase.data.Location;
import realmbase.data.PlayerData;
import realmbase.data.portal.PortalData;
import realmbase.listener.PacketListener;
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
		this(null,followTarget);
	}
	
	public MoveFollower(Bot client, String followTarget) {
		this.followTarget=followTarget;
		this.client=client;
	}
	
	@Override
	public Location move() {
		int time = client.time()-lastMoveTime;
		setLastMoveTime(client.time());
		PlayerData player = followTarget.isEmpty()?null:PacketListener.getPlayerData(getClient(), followTarget);
	
		if (player != null) {
			followPosition = player.getStatus().getPosition();
			if (teleport && player.getStatus().getPosition().distanceSquaredTo(getPosition()) > 144 && client.getMapInfo().isAllowPlayerTeleport()) {
				client.teleport(player.getStatus().getObjectId());
			}
			if (player.getStatus().getPosition().distanceSquaredTo(getPosition()) > followDist) {
				return getFollowPosition(time, player.getStatus().getPosition());
			}
		}else{
			if(followPosition!=null){
				HashMap<Integer, EntityData> portals = PacketListener.getClone(client);
				
				for(EntityData e : portals.values()){
					if(e instanceof PortalData){
						PortalData portal = (PortalData)e;
						if(portal.getStatus().getPosition().distanceSquaredTo(followPosition)<1){
							RealmBase.println(getClient(), "Portal: "+portal.getName());
							client.usePortal(portal);
							break;
						}
					}
				}
			}
		}
		return (followPosition!=null?followPosition:getPosition());
	}
	
	public void reconnect(){
		followPosition = null;
		position = null;
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
