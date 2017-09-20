package realmbot.bot;

import java.net.InetSocketAddress;

import realmbase.Parameter;
import realmbase.RealmBase;
import realmbase.data.AccountData.Char;
import realmbase.data.EntityData;
import realmbase.data.LocationRecord;
import realmbase.data.Status;
import realmbase.event.EventHandler;
import realmbase.event.EventListener;
import realmbase.event.EventManager;
import realmbase.event.events.AllyShootEvent;
import realmbase.event.events.CharakterDeadEvent;
import realmbase.event.events.PacketReceiveEvent;
import realmbase.event.events.ServerFullEvent;
import realmbase.listener.PacketListener;
import realmbase.packets.Packet;
import realmbase.packets.client.CreatePacket;
import realmbase.packets.client.GoToAckPacket;
import realmbase.packets.client.HelloPacket;
import realmbase.packets.client.LoadPacket;
import realmbase.packets.client.MovePacket;
import realmbase.packets.client.PlayerTextPacket;
import realmbase.packets.client.PongPacket;
import realmbase.packets.client.ShootAckPacket;
import realmbase.packets.client.UpdateAckPacket;
import realmbase.packets.server.AllyShootPacket;
import realmbase.packets.server.FailurePacket;
import realmbase.packets.server.MapInfoPacket;
import realmbase.packets.server.NewTickPacket;
import realmbase.packets.server.PingPacket;
import realmbase.packets.server.ReconnectPacket;
import realmbase.packets.server.TextPacket;
import realmbase.packets.server.UpdatePacket;
import realmbase.xml.GetUrl;
import realmbase.xml.GetXml;
import realmbase.xml.datas.ProjectileData;

public class BotListener implements EventListener{

	public BotListener(){
		EventManager.register(this);
	}
	
	@EventHandler
	public boolean PacketReceiveEvent(PacketReceiveEvent event) {
		Packet packet = event.getPacket();
		
		Bot client = (Bot)event.getClient();
		if(packet.getId() == GetXml.packetMapName.get("MAPINFO")){
			if(((Bot)client).getAccountData().getCharakters().length==0){
				client.sendPacketToServer(new CreatePacket(Parameter.WIZARD, 0));
//				RealmBase.println(client,"Received MapInfo -> sending CreatePacket");
			}else{
				client.sendPacketToServer(new LoadPacket(Integer.valueOf(client.getAccountData().getCharakters()[0].id),false));
//				RealmBase.println(client,"Received MapInfo -> sending LoadPacket");
			}
		}
		else if(packet.getId() == GetXml.packetMapName.get("SERVERPLAYERSHOOT")){
//			ServerPlayerShootPacket spacket = (ServerPlayerShootPacket)packet;
//			RealmBase.println(client,"ShootPackets -> "+spacket.toString());
//			client.sendPacketToServer(new ShootAckPacket(client.time()));
//			ProjectileData projectileData = GetXml.itemMap.get(spacket.getBulletType()).projectiles.get(0);
//			EventManager.callEvent(
//					new AllyShootEvent(spacket.getBulletId()
//							,spacket.getOwnerId()
//							,spacket.getBulletType()
//							,spacket.getStartingPos()
//							,spacket.getAngleInc()
//							,spacket.getDamage()
//							,projectileData
//							,client));
		}else if(packet.getId() == GetXml.packetMapName.get("ALLYSHOOT")){
			AllyShootPacket spacket = (AllyShootPacket)packet;
//			RealmBase.println(client, "AllyShoot ID: "+spacket.getContainerType()+" "+spacket.getBulletId()+" "+GetXml.itemMap.containsKey(Integer.valueOf(spacket.getContainerType())));
			if(GetXml.itemMap.containsKey(Integer.valueOf(spacket.getContainerType()))){
				ProjectileData projectileData = GetXml.itemMap.get(Integer.valueOf(spacket.getContainerType())).projectiles.get(0);
				EventManager.callEvent(
						new AllyShootEvent(spacket.getBulletId()
								,spacket.getOwnerId()
								,Integer.valueOf(spacket.getContainerType())
								,PacketListener.getPlayerData(client, spacket.getOwnerId()).getStatus().getPosition()
								,spacket.getAngle()
								,projectileData
								,client));
			}
			
		}else if(packet.getId() == GetXml.packetMapName.get("ENEMYSHOOT")){
			client.sendPacketToServer(new ShootAckPacket(client.time()));
		}else if(packet.getId() == GetXml.packetMapName.get("NEWTICK")){
			NewTickPacket ntpacket = (NewTickPacket)packet;
			
			if(client.getMove().getPosition() == null){
				for(Status s: ntpacket.getStatuses()){
					if(s.getObjectId() == client.getClientId()){
						client.getMove().setPosition(s.getPosition());
//						RealmBase.println(client,"Save Position!");
						break;
					}
				}
			}
			
			if(client.getMove().getPosition() != null){
				MovePacket mpacket = new MovePacket();
				mpacket.setTickId(ntpacket.getTickId());
				mpacket.setTime(client.time());
				mpacket.setNewPosition(client.getMove().move());
				mpacket.setRecords(new LocationRecord[0]);
//				RealmBase.println(client,"Answering to NewTickPacket -> MovePacket ");
				client.sendPacketToServer(mpacket);
			}else{
//				RealmBase.println(client,"I don't know where I am!");
			}
		}else if(packet.getId() == GetXml.packetMapName.get("UPDATE")){
			if(client.getName().isEmpty()){
				UpdatePacket upacket = (UpdatePacket)packet;
				for(EntityData e : upacket.getNewObjs()){
					if(e.getStatus().getObjectId() == client.getClientId()){
						client.setName(e.getName());
						break;
					}
				}
			}
//			RealmBase.println(client,"Answering to UpdatePacket -> UpdateAckPacket");
			client.sendPacketToServer(new UpdateAckPacket());
		}else if(packet.getId() == GetXml.packetMapName.get("PING")){
//			RealmBase.println(client,"Answering to PingPacket -> PongPacket");
			PingPacket ppacket = (PingPacket)packet;
			client.sendPacketToServer(new PongPacket(ppacket.getSerial(), client.time()));
		}else if(packet.getId() == GetXml.packetMapName.get("FAILURE")){
			FailurePacket fpacket = (FailurePacket)packet;
			
			if(fpacket.getErrorDescription().equalsIgnoreCase("{\"key\":\"server.realm_full\"}")){
				EventManager.callEvent(new ServerFullEvent(client));
			}else if(fpacket.getErrorDescription().equalsIgnoreCase("Character is dead")){
				EventManager.callEvent(new CharakterDeadEvent(client));
				
				client.getAccountData().setCharakters(new Char[0]);
				GetUrl.saveAccountData(client.getUsername(), client.getAccountData());
			}
			RealmBase.println(client,"FailurePacket -> "+fpacket.getErrorId()+" "+fpacket.getErrorDescription());
		}else if(packet.getId() == GetXml.packetMapName.get("GOTO")){
//			GoToPacket gpacket = (GoToPacket)packet;
			client.sendPacketToServer(new GoToAckPacket(client.time()));
		}else if(packet.getId() == GetXml.packetMapName.get("TEXT")){
			TextPacket tpacket = (TextPacket) packet;
			
			if(tpacket.getName().equalsIgnoreCase("kingingoo")||tpacket.getName().equalsIgnoreCase("kingingo")){
				client.sendPacketToServer(new PlayerTextPacket(tpacket.getText()));
			}
		}else if(packet.getId() == GetXml.packetMapName.get("MAPINFO")){
			client.setMapInfo(((MapInfoPacket)packet));
		}else if(packet.getId() == GetXml.packetMapName.get("RECONNECT")){
			ReconnectPacket rpacket = (ReconnectPacket)packet;
			RealmBase.println(client,"Detailes: "+rpacket.toString());
			
			String host = rpacket.getHost();
			int port = rpacket.getPort();
			
			if(port == -1){
				host = Parameter.remoteHost.getHostString();
				port = Parameter.remoteHost.getPort();
			}
			
			client.getMove().reconnect();
			client.disconnect();
			HelloPacket hello = new HelloPacket(rpacket, client.getUsername(), client.getPassword());
			RealmBase.println(client,"Detailes: "+hello.toString());
			client.connect(hello,new InetSocketAddress(host, port));
		}
		
		return false;
	}
}
