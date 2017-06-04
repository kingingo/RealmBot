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
import realmbase.GetUrl;
import realmbase.GetXml;
import realmbase.RealmBase;
import realmbase.data.AccountData;
import realmbase.data.Callback;
import realmbase.data.EntityData;
import realmbase.data.Type;
import realmbase.listener.ObjectListener;
import realmbase.listener.PacketManager;
import realmbase.packets.Packet;
import realmbase.packets.client.HelloPacket;
import realmbase.packets.client.TeleportPacket;
import realmbase.packets.server.MapInfoPacket;
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
	private MapInfoPacket mapInfo;
	
	public Bot(String username,String password,MoveClass move){
		this.username=username;
		this.password=password;
		this.accountData=GetUrl.loadAccount(username, password);
		this.move=move;
		this.move.setClient(this);
		this.Bots.add(this);
	}
	
	public boolean teleport(int objectId){
		EntityData e = ObjectListener.getObject(this, objectId);
		
		if(e!=null&&mapInfo.isAllowPlayerTeleport()){
			TeleportPacket tpacket = new TeleportPacket(e.getStatus().getObjectId());
			sendPacketToServer(tpacket);
			return true;
		}
		return false;
	}
	
	public void connect(InetSocketAddress adress){
		connect(adress, new Callback<Client>() {
			
			@Override
			public void call(Client client, Throwable exception) {
				RealmBase.println("Send HelloPacket...");
				sendPacketToServer(new HelloPacket(username,password));
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
							if(packetId!=74&&packetId!=33&&packetId!=18&&packetId!=101&&packetId!=1&&packetId!=35&&packetId!=52&&packetId!=102&&packetId!=69)
								RealmBase.println("Server -> Client: Id:"+(GetXml.getPacketMap().containsKey(String.valueOf(packetId)) ? GetXml.getPacketMap().get(String.valueOf(packetId)) : packetId));
							
							Packet packet = Packet.create(packetId, packetBytes);
							PacketManager.receive(this, packet, Type.SERVER);
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
