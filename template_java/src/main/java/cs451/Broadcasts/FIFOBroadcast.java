package cs451.Broadcasts;

import cs451.Host;
import cs451.Logger;
import cs451.Messages.Message;
import cs451.Messages.Message_sign;
import cs451.Observer;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FIFOBroadcast implements Observer {

    private final UniformReliableBroadcast urb;
    private final List<Host> hosts;
    private final ConcurrentHashMap<Integer,Integer> next;
    private final Set<Message_sign> pending;
    private final Set<Message_sign> delivered ;
    private final Logger logger;


    public  FIFOBroadcast(int processID, InetAddress Ip, int port, List<Host> hosts, Logger logger){
        this.urb=new UniformReliableBroadcast(processID,Ip,port,hosts,this);
        this.hosts=hosts;
        this.next=new ConcurrentHashMap<>();
        this.pending=ConcurrentHashMap.newKeySet();
        this.delivered=ConcurrentHashMap.newKeySet();
        this.logger=logger;
        for (Host host:hosts){
            next.put(host.getId(),1);
        }
    }

    //finds if a message signature is in pending and should be delivered
    public boolean find (Set<Message_sign> pending,ConcurrentHashMap<Integer,Integer> next){
        for (Message_sign sign :pending){
            if (next.get(sign.getOrg_sender_id())==sign.getOrg_seq_nb()){
                next.put(sign.getOrg_sender_id(), next.get(sign.getOrg_sender_id())+1);
                pending.remove(sign);
                this.delivered.add(sign);
                logger.deliver(sign);
                return true;
            }

        }
        return false;
    }

    public void broadcast(Message_sign sign)  {
        logger.broadcast(sign);
        urb.broadcast(sign);
    }
    @Override
    public void deliver(Message m){
        Message_sign sign = m.getSign();
        pending.add(sign);
        boolean found=false;
        do {
            found=find(pending,next);
        } while (found);









    }

}
