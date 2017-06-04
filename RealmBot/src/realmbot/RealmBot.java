package realmbot;

import java.util.Iterator;

import realmbase.RealmBase;
import realmbot.bot.Bot;
import realmbot.bot.BotListener;
import realmbot.bot.move.StationaryMover;

public class RealmBot {

	public static void main(String[] args){
		RealmBase.init();
		RealmBase.println("starting...");
		new BotListener();
		
		Bot bot = new Bot("kingingohd@gmail.com", "e8PKU1dSVNvKiXmEKKEq", new StationaryMover());
		bot.connect("USMIDWEST");
		boolean con = true;
		while (con) {
			int cores = Runtime.getRuntime().availableProcessors();
			Thread[] threads = new Thread[cores];
			int core = 0;
			Iterator<Bot> i = Bot.getBots().iterator();
			while (i.hasNext()) {
				final Bot client = i.next();
				if (client == null || (client.getRemoteSocket()!=null&&client.getRemoteSocket().isClosed())) {
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
