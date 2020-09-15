package realmbot.bot.move;

import lombok.Getter;
import lombok.Setter;
import realmbase.data.Location;
import realmbot.bot.Bot;

@Setter
@Getter
public class StationaryMover implements MoveClass {
	private Location position;
	private int lastMoveTime;
	private Bot client;

	public StationaryMover(){}
	
	public StationaryMover(Bot client){
		this.client=client;
	}
	
	@Override
	public Location move() {
		return getPosition();
	}

	public void reconnect(){
		position = null;
	}
	
	@Override
	public int getlastMoveTime() {
		return lastMoveTime;
	}
}
