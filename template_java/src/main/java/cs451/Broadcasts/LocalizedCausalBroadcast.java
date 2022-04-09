package cs451.Broadcasts;

import cs451.Host;
import cs451.Logger;
import cs451.Messages.Message;
import cs451.Messages.Message_sign;
import cs451.Observer;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;



public class LocalizedCausalBroadcast implements Observer  {

    private final Logger logger;
    private final UniformReliableBroadcast urb;
    private final AtomicIntegerArray VC;
    private final List<Host> hosts;
    private final Set<Message> pending;
    private final int processID;
    private final HashSet<Integer> causal_processes;


    public LocalizedCausalBroadcast (int processID, InetAddress Ip, int port, List<Host> hosts,HashSet<Integer> causal_processes , Logger logger){
        this.logger=logger;
        this.processID=processID;
        this.urb=new UniformReliableBroadcast(processID,Ip,port,hosts,this);
        this.VC=new AtomicIntegerArray(hosts.size());
        this.hosts=hosts;
        this.pending=ConcurrentHashMap.newKeySet();
        this.causal_processes=causal_processes;

    }

    public void broadcast (Message_sign ms){
        logger.broadcast(ms);
        ms.setVC(getCausalVC());
        this.urb.broadcast(ms);
        logger.deliver(ms);
        this.VC.getAndIncrement(processID-1);

    }
    //return the Causal vector with only the values associated to causal processes
    //Note that the process running is included in causal processes.
    public AtomicIntegerArray getCausalVC(){
        AtomicIntegerArray CausalVC= new AtomicIntegerArray(hosts.size());
        for (Integer i : causal_processes){
            CausalVC.getAndSet(i-1,VC.get(i-1));
        }
        return CausalVC;
    }

    @Override
    public void deliver (Message m){
        if (!(m.getOrgSender_id()==processID)){
            pending.add(m);
            boolean found;
            do {
                found=check_pending();
            } while (found==true);

        }
    }

    public boolean check_pending (){
        for (Message m : pending){
            if (CanCausallyDeliver(m)){
                pending.remove(m);
                VC.getAndIncrement((m.getOrgSender_id()-1));
                logger.deliver(m);
                return true;
            }
        }
        return false;
    }

    public boolean CanCausallyDeliver(Message m){
        AtomicIntegerArray mVC=m.getSign().getVC();
        boolean CanDeliver=true;
        for (int i=0;i< hosts.size();i++){
            if(!(mVC.get(i)<=this.VC.get(i))){
                CanDeliver=false;
                break;
            }
        }
        return CanDeliver;

    }








}
