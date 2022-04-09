package cs451.Broadcasts;

import cs451.Host;
import cs451.Messages.Message;
import cs451.Messages.Message_sign;
import cs451.Observer;
import cs451.PerfectLink;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class BestEffortBroadcast implements Observer {

    private final List<Host> hosts;
    private final PerfectLink perfectLink;
    private final Observer obs;
    private int counter;


    public BestEffortBroadcast(int processID,InetAddress Ip , int port,Observer obs, List<Host> hosts ) {
        this.perfectLink = new PerfectLink(processID,Ip,port,this);
        this.hosts = new ArrayList<>(hosts);
        this.obs=obs;
        this.counter=0;
    }

    public void broadcast(Message_sign ms)  {
        counter+=1;
        for (Host h : hosts) {
            try{
                Message m = new Message(ms,perfectLink.processID,counter, h.getPort(), InetAddress.getByName(h.getIp()));
                perfectLink.send(m);
            } catch (UnknownHostException e){
                System.out.println("InetAddress not found");
            }
        }

    }
    @Override
    public void deliver(Message m){
        obs.deliver(m);
        }



}


