package realmbot.bot.attack;

import realmbase.data.Location;
import realmbase.packets.client.PlayerShootPacket;
import realmbase.packets.client.ShootAckPacket;
import realmbase.packets.server.ShootPacket;
import realmbot.bot.Bot;

public interface AttackClass {
	public void setClient(Bot client);
	public Bot getClient();
	public void shoot(Location location);
	public void cancel();
}
