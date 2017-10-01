package realmbot.listener;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.reflect.Parameter;

import realmbase.Client;
import realmbase.RealmBase;
import realmbase.data.Callback;
import realmbase.data.portal.PortalData;
import realmbase.data.portal.PortalType;
import realmbase.event.EventHandler;
import realmbase.event.EventListener;
import realmbase.event.EventManager;
import realmbase.event.events.PortalNewEvent;
import realmbase.event.events.PortalUpdateEvent;
import realmbase.event.events.ServerFullEvent;
import realmbase.xml.GetUrl;
import realmbase.xml.GetXml;
import realmbot.bot.Bot;
import realmbot.bot.move.MoveTarget;
import realmbot.bot.move.StationaryMover;

public class SpreadListener implements EventListener{
	private static final HashMap<String, HashMap<Bot, PortalData>> bots = new HashMap<>();
	private static final HashMap<String, Bot> lobbyBots = new HashMap<>();
	private static final HashMap<Bot,PortalData> fullServerBots = new HashMap<>();
	private static final ArrayList<Bot> freeBots = new ArrayList<>();
	
	public SpreadListener(){
		EventManager.register(this);
		
		//Laden der Bot daten liste
		HashMap<String,String> botdatas = GetXml.getBotList();	
		
		//erstellen der Bots
		for(String email : botdatas.keySet())
			freeBots.add(new Bot(email, botdatas.get(email), null));
		
		HashMap<String, InetSocketAddress> serverAdresses = new HashMap<String, InetSocketAddress>();
		serverAdresses.put("EuNorth".toUpperCase(), realmbase.Parameter.remoteHost);
		
		//Bots auf den Lobbys der einzelnen Main Server verteilen
//		for(String server : GetUrl.getServerAdresses().keySet()){
		for(String server : serverAdresses.keySet()){
			//Fügt alle Server zu der Bot liste hinzu damit spare ich mir eine Überprüfung
			bots.put(server, new HashMap<Bot, PortalData>());
			
			final Bot bot = getFreeBot();
			if(bot!=null){
				//Zur Lobby Bot liste hinzufügen
				lobbyBots.put(server, bot);
				
				RealmBase.println(bot, "send Bot "+server);
				
				//Bewegt sich zu den Portalen der Unter Server
				bot.setMove(new MoveTarget(realmbase.Parameter.PORTAL_POS, new Callback<MoveTarget>() {
					
					@Override
					public void call(MoveTarget obj, Throwable exception) {
						//Wenn er angekommen ist bleibt er Stehen
						obj.getClient().setMove(new StationaryMover());
						RealmBase.println(bot, "An der Position angekommen");
					}
				}));
				//Verbindet sich zum Server
				bot.connect(GetUrl.getServerAdresses().get(server));
			}
		}
	}
	
	public Bot getFreeBot(){
		//Überprüfen ob noch freie Bots da sind.
		if(!freeBots.isEmpty()){
			Bot bot = freeBots.get(0);
			//Entfernt den Bot von der Freien Bot liste
			freeBots.remove(bot);
			return bot;
		}else{
			//Keine Freien Bots mehr!?
			return null;
		}
	}
	
	public String getServerFromBot(Client bot){
		//Überprüft ob es ein Lobby Bot ist
		if(lobbyBots.containsValue(bot)){
			//Sucht den Bot
			for(String server : lobbyBots.keySet())
				if(lobbyBots.get(server).getClientId()==bot.getClientId())return server;
		}
		
		for(String server : bots.keySet()){
			if(bots.get(server).containsKey(bot)){
				return server;
			}
		}
		
		return null;
	}
	
	@EventHandler
	public void ServerFull(ServerFullEvent ev){
		String server = getServerFromBot(ev.getClient());
		
		if(server!=null){
			//Get Portal vom Client wo der Server voll ist
			PortalData portal = bots.get(server).get(ev.getClient());
			sendBotPortal(((Bot)ev.getClient()), portal, server);
		}
	}
	
	@EventHandler
	public void updatePortal(PortalUpdateEvent ev){
		if(ev.getPortal().getType() == PortalType.NEXUS && fullServerBots.containsKey(ev.getClient())){
			Bot bot = (Bot) ev.getClient();
			if(ev.getPortal().getPopulation()!=85){
				fullServerBots.remove(ev.getClient());
				bot.usePortal(ev.getPortal());
			}
		}
	}
	
	@EventHandler
	public void newPortal(PortalNewEvent ev){
		//Prüfen ob es ein Nexus Portal ist && Überprüfen ob es ein Lobby Bot ist
		if(ev.getPortal().getType() == PortalType.NEXUS && lobbyBots.containsValue(ev.getClient())){
			Bot bot = getFreeBot();
			if(bot!=null){
				RealmBase.println(ev.getClient(), "Unterserver wargenommen "+ev.getPortal().getName()+" sende -> "+bot.getName());
				String server = getServerFromBot(ev.getClient());
				bots.get(server).put(bot, ev.getPortal());
				sendBotPortal(bot, ev.getPortal(), server);
			}
		}
	}
	
	public void sendBotPortal(final Bot bot,final PortalData portal,String server){
		//Bewegt sich zu den Portalen der Unter Server
		bot.setMove(new MoveTarget(realmbase.Parameter.PORTAL_POS, new Callback<MoveTarget>() {
			
			@Override
			public void call(MoveTarget mover, Throwable exception) {
				//Wenn Portal erreicht betritt er den unter Server
				mover.setReachTarget(new Callback<MoveTarget>() {

					@Override
					public void call(MoveTarget mover, Throwable exception) {
						RealmBase.println(bot, "Unterserver betretten");
						//Er bleibt am Spawn des Unterserver Stehen
						mover.getClient().setMove(new StationaryMover());
						
						//Prüfen ob der Server voll ist.
						if(portal.getPopulation() != 85){
							//Unterserver beitretten
							bot.usePortal(portal);
						}else{
							//Server voll.
							fullServerBots.put(bot,portal);
						}
					}
				});
				//Bewegt sich zum Portal
				mover.setTarget(portal.getStatus().getPosition());
			}
		}));
		
		bot.connect(server);
	}
}
