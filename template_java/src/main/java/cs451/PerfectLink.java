package cs451;

import cs451.Messages.Message;
import cs451.Messages.MessageType;
import cs451.Messages.Message_sign;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class PerfectLink {
    public int processID;
    private DatagramSocket socket;
    private  Set<Message>  unACKed;
    private Set<Message> delivered ;
    private Observer obs;



    public PerfectLink (int processID,InetAddress Ip , int port ,Observer obs){
        this.processID=processID;
        this.obs= obs;
        this.unACKed= ConcurrentHashMap.newKeySet();
        this.delivered= new HashSet<>();
        try {
            this.socket = new DatagramSocket(port, Ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread thread1 = new Thread(sender);
        Thread thread2 = new Thread(receiver);
        thread1.start();
        thread2.start();
    }

    Runnable sender =()-> {
        while(true){
            for (Message message :unACKed){
                send(message);
            }
            try{
                Thread.sleep(3*10000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

    };

    Runnable receiver=()-> {
        while (true){
            receive();
        }
    };

    public void send(Message m){

        try {
            DatagramPacket packet = new DatagramPacket(serialize(m), serialize(m).length, m.getToIp(), m.getTo_port());
            socket.send(packet);
            if (m.getMessageType()== MessageType.BROADCAST && !this.unACKed.contains(m)){
                this.unACKed.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void  receive (){
            byte[] received = new byte[100];
            DatagramPacket packet = new DatagramPacket(received, received.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            InetAddress address = packet.getAddress();
            int port= packet.getPort();
            Message msg=deserialize(packet);
            if (msg.getMessageType()==MessageType.ACK){
                Message_sign sign=new Message_sign(msg.getOrgSeq_nb(),msg.getOrgSender_id(), MessageType.BROADCAST);
                Message Acked_message=new Message(sign,msg.getSender_id(),msg.getSeq_nb(),port,address);
                unACKed.remove(Acked_message);
            } else {
                Message_sign sign=new Message_sign(msg.getOrgSeq_nb(),msg.getOrgSender_id(), MessageType.ACK);
                this.send(new Message(sign, msg.getSender_id(), msg.getSeq_nb(),port,address));
                if (!delivered.contains(msg)){
                    delivered.add(msg);
                    obs.deliver(msg);
                }
            }

        }

    public byte[] serialize( Message m){
        ByteBuffer bb=ByteBuffer.allocate(10000);
        bb.putInt(m.getSender_id());
        bb.putInt(m.getOrgSender_id());
        bb.putInt(m.getSeq_nb());
        bb.putInt(m.getOrgSeq_nb());

        if (m.getMessageType()==MessageType.ACK){
            bb.putInt(0);
        }else {
            bb.putInt(1);
            Message_sign ms=m.getSign();
            AtomicIntegerArray VC=ms.getVC();
            bb.putInt(VC.length());
            for (int i=0;i<VC.length();i++){
                bb.putInt(VC.get(i));
            }
        }
        return bb.array();
    }

    public Message deserialize(DatagramPacket packet){

        ByteBuffer bb=ByteBuffer.wrap(packet.getData());
        int sender_id=bb.getInt();
        int org_sender_id=bb.getInt();
        int seq_nb=bb.getInt();
        int org_seq_nb= bb.getInt();
        int type_bin=bb.getInt();
        if (type_bin==0){
            Message_sign sign=new Message_sign(org_seq_nb,org_sender_id,MessageType.ACK);
            return new Message(sign,sender_id,seq_nb,packet.getPort(),packet.getAddress());
        }else{
            int len_VC=bb.getInt();
            Message_sign sign=new Message_sign(org_seq_nb,org_sender_id,MessageType.BROADCAST);
            AtomicIntegerArray VC=new AtomicIntegerArray(len_VC);
            for (int i=0;i<VC.length();i++){
                VC.addAndGet(i, bb.getInt());
            }
            sign.setVC(VC);
            return new Message(sign,sender_id,seq_nb,packet.getPort(),packet.getAddress());
        }
    }

}
