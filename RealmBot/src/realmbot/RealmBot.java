package realmbot;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;

import realmbase.GetXml;
import realmbase.Parameter;
import realmbase.RealmBase;
import realmbase.data.Callback;
import realmbase.utils.Milis;
import realmbot.bot.Bot;
import realmbot.bot.BotListener;
import realmbot.bot.move.MoveFollower;
import realmbot.bot.move.MoveFollowerCrazy;
import realmbot.bot.move.MoveTarget;
import realmbot.bot.move.StationaryMover;

public class RealmBot {

	public static void main(String[] args){
		RealmBase.init();
		RealmBase.println("starting...");
		new BotListener();

		HashMap<String,String> bots = GetXml.getBotList();
		
		String email = (String) bots.keySet().toArray()[0];
		Bot bot = new Bot(email,bots.get(email), new StationaryMover());
		bot.connect();

//		for(String email : bots.keySet()){
//			Bot bot = new Bot(email, bots.get(email), new MoveFollowerCrazy(2, Milis.SECOND));
//			bot.connect("EUNORTH");
//		}
		
		boolean con = true;
		while (con) {
			int cores = Runtime.getRuntime().availableProcessors();
			Thread[] threads = new Thread[cores];
			int core = 0;
			Iterator<Bot> i = Bot.getBots().iterator();
			while (i.hasNext()) {
				final Bot client = i.next();
				if (client == null) {
					if(client != null)client.disconnect();
					i.remove();
					continue;
				}
				if (threads[core] != null) {
					try {
						threads[core].join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				(threads[core] = new Thread(new Runnable() {
					
					@Override
					public void run() {
						client.process();
					}
					
				})).start();
				core = (core + 1) % cores;
			}
			for (Thread thread: threads) {
				if (thread == null) {
					continue;
				}
				try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Thread.yield();
		}
		
		Iterator<Bot> k = Bot.getBots().iterator();
		while (k.hasNext()) {
			Bot user = k.next();
			user.disconnect();
		}
	}
	
}
