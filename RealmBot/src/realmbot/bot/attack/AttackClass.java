package realmbot.bot.attack;

import realmbase.data.Location;
import realmbot.bot.Bot;

public interface AttackClass {
	public void setClient(Bot client);
	public Bot getClient();
	public void shoot(Location location);
	public void cancel();
}
