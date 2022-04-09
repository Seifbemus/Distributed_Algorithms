package cs451.Broadcasts;

import cs451.Host;
import cs451.Messages.Message;
import cs451.Messages.Message_sign;
import cs451.Observer;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UniformReliableBroadcast implements Observer {
    private final BestEffortBroadcast beb;
    private final Set<Message_sign> pending;
    private final Set<Message_sign> delivered;
    private final ConcurrentHashMap<Message_sign, Set<Host>> ACK;
    private final List<Host> hosts;
    private final Observer obs;

    public UniformReliableBroadcast(int processID, InetAddress Ip, int port, List<Host> hosts, Observer obs) {
        this.beb=new BestEffortBroadcast( processID, Ip ,  port,this,hosts);
        this.pending=ConcurrentHashMap.newKeySet();
        this.delivered=ConcurrentHashMap.newKeySet();
        this.ACK=new ConcurrentHashMap<>();
        this.hosts=hosts;
        this.obs=obs;
    }

    public void broadcast(Message_sign ms ) {
        this.pending.add(ms);
        beb.broadcast(ms);
    }

    public void deliver(Message m)  {
        Message_sign sign = m.getSign();
        Host sender=get_host_with_id(m.getSender_id());
        if (!delivered.contains(sign)) {
            if (!ACK.getOrDefault(sign, new HashSet<>()).contains(sender)) {
                ACK.putIfAbsent(sign,new HashSet<>());
                ACK.get(sign).add(sender);
                if (canDeliver(sign) ) {
                    delivered.add(sign);
                    ACK.remove(sign);
                    pending.remove(sign);
                    obs.deliver(m);
                }
            }
            if (!pending.contains(sign) && !delivered.contains(sign)) {
                pending.add(sign);
                beb.broadcast(sign);
            }
        }
    }

    public  Host get_host_with_id( int id) {
        for (Host host: this.hosts){
            if (host.getId()==id) {
                return host;
            }
        }
        return null;
    }

    private boolean canDeliver(Message_sign sign){
        return (!(ACK.get(sign)==null) && (ACK.get(sign).size()>=((this.hosts.size()/2)+1)));
    }

}
