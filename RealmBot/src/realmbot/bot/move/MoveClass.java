package realmbot.bot.move;

import realmbase.Client;
import realmbase.data.Location;

public interface MoveClass {
	public int getlastMoveTime();
	public void setLastMoveTime(int time);
	public void setPosition(Location pos);
	public Location getPosition();
	public Location move(int time);
	public void setClient(Client client);
	public Client getClient();
}
