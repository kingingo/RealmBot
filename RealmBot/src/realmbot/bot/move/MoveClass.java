package realmbot.bot.move;

import realmbase.Client;
import realmbase.data.Location;
import realmbot.bot.Bot;

public interface MoveClass {
	public int getlastMoveTime();
	public void setLastMoveTime(int time);
	public void setPosition(Location pos);
	public Location getPosition();
	public Location move();
	public void setClient(Bot client);
	public Client getClient();
	public void reconnect();
}
