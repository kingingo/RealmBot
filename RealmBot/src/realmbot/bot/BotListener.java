package realmbot.bot;

import java.io.IOException;
import java.net.InetSocketAddress;

import realmbase.Client;
import realmbase.GetXml;
import realmbase.Parameter;
import realmbase.RealmBase;
import realmbase.data.LocationRecord;
import realmbase.data.Status;
import realmbase.data.Type;
import realmbase.listener.PacketListener;
import realmbase.listener.PacketManager;
import realmbase.packets.Packet;
import realmbase.packets.client.GoToAckPacket;
import realmbase.packets.client.LoadPacket;
import realmbase.packets.client.MovePacket;
import realmbase.packets.client.PongPacket;
import realmbase.packets.client.UpdateAckPacket;
import realmbase.packets.server.Create_SuccessPacket;
import realmbase.packets.server.FailurePacket;
import realmbase.packets.server.GoToPacket;
import realmbase.packets.server.MapInfoPacket;
import realmbase.packets.server.New_TickPacket;
import realmbase.packets.server.PingPacket;
import realmbase.packets.server.ReconnectPacket;

public class BotListener implements PacketListener{

	public BotListener(){
		PacketManager.addListener(this);
	}
	
	@Override
	public boolean onReceive(Client c, Packet packet, Type from) {
		Bot client = (Bot)c;
		if(packet.getId() == GetXml.getPacketMapName().get("MAPINFO")){
//			RealmBase.println("Received MapInfo -> sending LoadPacket");
			client.sendPacketToServer(new LoadPacket(Integer.valueOf(client.getAccountData().getCharakters()[0].id),false));
		}else if(packet.getId() == GetXml.getPacketMapName().get("NEW_TICK")){
			New_TickPacket ntpacket = (New_TickPacket)packet;
			
			if(client.getMove().getPosition() == null){
				for(Status s: ntpacket.getStatuses()){
					if(s.getObjectId() == client.getClientId()){
						client.getMove().setPosition(s.getPosition());
//						RealmBase.println("Save Position!");
						break;
					}
				}
			}
			
			MovePacket mpacket = new MovePacket();
			mpacket.setTickId(ntpacket.getTickId());
			mpacket.setTime(client.time());
			mpacket.setNewPosition(client.getMove().move());
			mpacket.setRecords(new LocationRecord[0]);
//			RealmBase.println("Answering to New_TickPacket -> MovePacket ");
			client.sendPacketToServer(mpacket);
		}else if(packet.getId() == GetXml.getPacketMapName().get("UPDATE")){
//			RealmBase.println("Answering to UpdatePacket -> UpdateAckPacket");
			client.sendPacketToServer(new UpdateAckPacket());
		}else if(packet.getId() == GetXml.getPacketMapName().get("PING")){
//			RealmBase.println("Answering to PingPacket -> PongPacket");
			PingPacket ppacket = (PingPacket)packet;
			client.sendPacketToServer(new PongPacket(ppacket.getSerial(), client.time()));
		}else if(packet.getId() == GetXml.getPacketMapName().get("FAILURE")){
			FailurePacket fpacket = (FailurePacket)packet;
			RealmBase.println("FailurePacket-> "+fpacket.getErrorId()+" "+fpacket.getErrorDescription());
		}else if(packet.getId() == GetXml.getPacketMapName().get("CREATE_SUCCESS")){
			Create_SuccessPacket cpacket = (Create_SuccessPacket)packet;
			client.setClientId(cpacket.getObjectId());
//			RealmBase.println("Connected successfull!");
		}else if(packet.getId() == GetXml.getPacketMapName().get("GOTO")){
			GoToPacket gpacket = (GoToPacket)packet;
			
			client.sendPacketToServer(new GoToAckPacket(client.time()));
		}else if(packet.getId() == GetXml.getPacketMapName().get("MAPINFO")){
			client.setMapInfo(((MapInfoPacket)packet));
		}else if(packet.getId() == GetXml.getPacketMapName().get("RECONNECT")){
			ReconnectPacket rpacket = (ReconnectPacket)packet;
//			System.out.println("Detailes: "+rpacket.toString());
			
			String host = rpacket.getHost();
			int port = rpacket.getPort();
			
			if(port == -1){
				host = Parameter.remoteHost.getHostString();
				port = Parameter.remoteHost.getPort();
			}
			
			try {
				client.getRemoteSocket().close();
				client.setRemoteSocket(null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			client.connect(new InetSocketAddress(rpacket.getHost(), rpacket.getPort()));
		}
		
		return false;
	}

	@Override
	public boolean onSend(Client client, Packet packet, Type to) {
		return false;
	}

}
