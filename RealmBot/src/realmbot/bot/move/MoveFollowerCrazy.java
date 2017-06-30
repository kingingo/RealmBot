package realmbot.bot.move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;
import realmbase.Client;
import realmbase.Parameter;
import realmbase.RealmBase;
import realmbase.data.Callback;
import realmbase.data.Location;
import realmbase.data.PlayerData;
import realmbase.data.portal.PortalData;
import realmbase.list.Sort;
import realmbase.listener.ObjectListener;
import realmbase.utils.Milis;
import realmbase.utils.UtilClient;
import realmbot.bot.Bot;

@Getter
@Setter
public class MoveFollowerCrazy implements MoveClass {
	private Location position;
	private int lastMoveTime = 0;
	private Bot client;
	private float followDist = 0.25f;
	private boolean teleport = false;
	private String followTarget;
	private Location followPosition;
	private long distance = 10;
	private long change = Milis.SECOND * 15;
	private long lastChange;
	
	public MoveFollowerCrazy() {}
	
	public MoveFollowerCrazy(long distance, long change) {
		this.distance=distance;
		this.change=change;
	}
	
	public void reconnect(){
		followPosition = null;
		position = null;
	}
	
	@Override
	public Location move() {
		if(client != null){
			if((System.currentTimeMillis()-lastChange) > change)followTarget="";
			PlayerData e = null;
			
			if(followTarget.isEmpty() && ObjectListener.getEntities().containsKey(client)){
				ArrayList<Sort<PlayerData>> list = UtilClient.getOrdedList(ObjectListener.getEntities().get(client), followPosition, distance, new Callback<ArrayList<PlayerData>>() {
					
					@Override
					public void call(ArrayList<PlayerData> list, Throwable exception) {
						for(int i = 0; i < list.size(); i++){
							PlayerData data = list.get(i);
							
							for(Bot bot : Bot.getBots()){
								if(bot.getClientId() == data.getAccountId() ){
									list.remove(data);
									break;
								}
							}
						}
					}
				});
				
				if(!list.isEmpty()){
					Collections.shuffle(list);
					e = list.get(0).getObject();
					followTarget = e.getName();
					lastChange = System.currentTimeMillis();
				}
			}else{
				e = followTarget.isEmpty()?null:ObjectListener.getPlayerData(getClient(), followTarget);
			}
			
			int time = client.time()-lastMoveTime;
			setLastMoveTime(client.time());
		
			if (e != null) {
				followPosition = e.getStatus().getPosition();
				if (teleport && e.getStatus().getPosition().distanceSquaredTo(getPosition()) > 144 && client.getMapInfo().isAllowPlayerTeleport()) {
					client.teleport(e.getStatus().getObjectId());
				}
				if (e.getStatus().getPosition().distanceSquaredTo(getPosition()) > followDist) {
					return getFollowPosition(time, e.getStatus().getPosition());
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
