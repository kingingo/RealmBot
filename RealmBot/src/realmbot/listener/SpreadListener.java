package realmbot.listener;

import java.util.ArrayList;
import java.util.HashMap;

import org.bouncycastle.crypto.tls.HashAlgorithm;

import realmbase.Client;
import realmbase.GetXml;
import realmbase.data.EntityData;
import realmbase.data.Type;
import realmbase.data.portal.PortalData;
import realmbase.listener.PacketListener;
import realmbase.listener.PacketManager;
import realmbase.packets.Packet;
import realmbase.packets.server.UpdatePacket;
import realmbot.bot.Bot;

public class SpreadListener implements PacketListener{
	private final HashMap<String,ArrayList<PortalData>> portals = new HashMap<>();
	private final HashMap<String, HashMap<Bot, PortalData>> bots = new HashMap<>();
	private final ArrayList<Bot> freeBots = new ArrayList<>();
	
	public SpreadListener(){
		PacketManager.addListener(this);
	}
	
	public ArrayList<PortalData> getPortalList(Client client,boolean create){
		String get = client.getRemoteSocket().getInetAddress().getHostAddress()+":"+client.getRemoteSocket().getPort();
		if(!portals.containsKey(get)){
			if(create){
				portals.put(get, new ArrayList<>());
			}else{
				return null;
			}
		}
		return portals.get(get);
	}

	@Override
	public boolean onReceive(Client client, Packet packet, Type from) {
		if(packet.getId() == GetXml.getPacketMapName().get("UPDATE")){
			UpdatePacket upacket = (UpdatePacket)packet;
			
			for(int i = 0; i < upacket.getNewObjs().length; i++){
				EntityData e = upacket.getNewObjs()[i];
			
				if(GetXml.getPortalsMap().containsKey(e.getObjectType())
						&& GetXml.getPortalsMap().get(e.getObjectType()).equalsIgnoreCase("Nexus Portal")){
					getPortalList(client, true).add( ((PortalData)e) );
					//SEND BOT!
				}
			}
			
			for(int i = 0; i < upacket.getDrops().length; i++){
				int objectId = upacket.getDrops()[i];
				ArrayList<PortalData> list = getPortalList(client, false);
				
				if(list!=null){
					for(int j = 0; j < list.size(); j++){
						if(list.get(i).getStatus().getObjectId()==objectId){
							list.remove(i);
							break;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onSend(Client client, Packet packet, Type to) {
		return false;
	}

}
