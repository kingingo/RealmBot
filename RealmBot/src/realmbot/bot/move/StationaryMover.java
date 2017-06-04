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

	@Override
	public Location move() {
		return getPosition();
	}

	@Override
	public int getlastMoveTime() {
		return lastMoveTime;
	}
}
