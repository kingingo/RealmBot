package realmbot.bot;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Vector;

import lombok.Getter;
import lombok.Setter;
import realmbase.Client;
import realmbase.Parameter;
import realmbase.RealmBase;
import realmbase.data.AccountData;
import realmbase.data.Callback;
import realmbase.data.EntityData;
import realmbase.data.Type;
import realmbase.data.portal.PortalData;
import realmbase.event.EventManager;
import realmbase.event.events.PacketReceiveEvent;
import realmbase.listener.PacketListener;
import realmbase.packets.Packet;
import realmbase.packets.client.HelloPacket;
import realmbase.packets.client.TeleportPacket;
import realmbase.packets.client.UsePortalPacket;
import realmbase.packets.server.MapInfoPacket;
import realmbase.xml.GetUrl;
import realmbase.xml.GetXml;
import realmbot.bot.attack.AttackThread;
import realmbot.bot.move.MoveClass;

@Getter
@Setter
public class Bot extends Client{
	@Getter
	private static final List<Bot> Bots = new Vector<>();

	private String username;
	private String password;
	private AccountData accountData;
	private MoveClass move;
	private AttackThread attack;
	private MapInfoPacket mapInfo;
	
	public Bot(String username,String password,MoveClass move){
		this.username=username;
		this.password=password;
		this.accountData=GetUrl.loadAccount(username, password);
		this.move=move;
		this.attack=new AttackThread();
		this.attack.setClient(this);
		if(move!=null)this.move.setClient(this);
		this.Bots.add(this);
	}
	
	public void setMove(MoveClass move){
		if(this.move!=null && this.move.getPosition()!=null)move.setPosition(this.move.getPosition());
		this.move=move;
		this.move.setClient(this);
	}
	
	public void usePortal(PortalData portal){
		if(getMove().getPosition().distanceTo(portal.getStatus().getPosition()) < 1){
			UsePortalPacket packet = new UsePortalPacket(portal);
			sendPacketToServer(packet);
		}
	}
	
	public int time(){
		return (int) (System.currentTimeMillis() - getConnectTime());
	}
	
	public boolean teleport(int objectId){
		EntityData e = PacketListener.getObject(this, objectId);
		
		if(e!=null&&mapInfo.isAllowPlayerTeleport()){
			TeleportPacket tpacket = new TeleportPacket(e.getStatus().getObjectId());
			sendPacketToServer(tpacket);
			return true;
		}
		return false;
	}
	
	public void connect(){
		connect(Parameter.remoteHost);
	}
	
	public void connect(InetSocketAddress adress){
		connect(new HelloPacket(username, password), adress);
	}
	
	public void connect(HelloPacket packet, InetSocketAddress adress){
		connect(adress, new Callback<Client>() {
			
			@Override
			public void call(Client client, Throwable exception) {
				if(exception != null){
					exception.printStackTrace();
					disconnect();
				}else{
//					RealmBase.println(client,"Send HelloPacket... ");
					sendPacketToServer(packet);
				}
			}
		});
	}
	
	public void connect(String server){
		connect(GetUrl.getServerAdresses().get(server.toUpperCase()));
	}
	
	public void process(){
		if (this.remoteSocket != null) {
			try {
				InputStream in = this.remoteSocket.getInputStream();
				if (in.available() > 0) {
					int bytesRead = this.remoteSocket.getInputStream().read(this.remoteBuffer, this.remoteBufferIndex, this.remoteBuffer.length - this.remoteBufferIndex);
					if (bytesRead == -1) {
						throw new SocketException("end of stream");
					} else if (bytesRead > 0) {
						this.remoteBufferIndex += bytesRead;
						while (this.remoteBufferIndex >= 5) {
							int packetLength = ((ByteBuffer) ByteBuffer.allocate(4).put(this.remoteBuffer[0]).put(this.remoteBuffer[1]).put(this.remoteBuffer[2]).put(this.remoteBuffer[3]).rewind()).getInt();
							if (this.remoteBufferIndex < packetLength) {
								break;
							}
							byte packetId = this.remoteBuffer[4];
							byte[] packetBytes = new byte[packetLength - 5];
							System.arraycopy(this.remoteBuffer, 5, packetBytes, 0, packetLength - 5);
							if (this.remoteBufferIndex > packetLength) {
								System.arraycopy(this.remoteBuffer, packetLength, this.remoteBuffer, 0, this.remoteBufferIndex - packetLength);
							}
							this.remoteBufferIndex -= packetLength;
							this.remoteRecvRC4.cipher(packetBytes);
							if(packetId != GetXml.packetMapName.get("UPDATE")
								&& packetId != GetXml.packetMapName.get("TEXT")
								&& packetId != GetXml.packetMapName.get("NEWTICK")
								&& packetId != GetXml.packetMapName.get("SHOWEFFECT")
								&& packetId != GetXml.packetMapName.get("PING")
								&& packetId != GetXml.packetMapName.get("NOTIFICATION")
								&& packetId != GetXml.packetMapName.get("DAMAGE")
								&& packetId != GetXml.packetMapName.get("SERVERPLAYERSHOOT")
								&& packetId != GetXml.packetMapName.get("ALLYSHOOT")
								&& packetId != GetXml.packetMapName.get("ENEMYSHOOT"))
								RealmBase.println("Server->Client: P:"+(GetXml.packetMap.containsKey(String.valueOf(packetId)) ? GetXml.packetMap.get(String.valueOf(packetId)) : packetId)+" Id:"+packetId+" L:"+packetBytes.length);
							
							Packet packet = Packet.create(packetId, packetBytes);
							EventManager.callEvent(new PacketReceiveEvent(packet,Type.SERVER,this,false));
						}
					}
					this.remoteNoDataTime = System.currentTimeMillis();
				} else if (System.currentTimeMillis() - this.remoteNoDataTime >= 10000) {
					throw new SocketException("remote data timeout");
				}
			} catch (Exception e) {
				if (!(e instanceof SocketException)) {
					e.printStackTrace();
				}
				this.disconnect();
			}
		}
	}
}
