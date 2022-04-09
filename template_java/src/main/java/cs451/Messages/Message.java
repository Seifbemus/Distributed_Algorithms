package cs451.Messages;

import java.net.InetAddress;
import java.util.Objects;

public class Message  {

    private final  Message_sign sign;
    private final int sender_id;
    private final int seq_nb;
    private final int to_port;
    private final InetAddress toIp;

    public Message( Message_sign sign,int sender_id,int seq_nb,  int to_port,InetAddress toIp ){
        this.sender_id=sender_id;
        this.seq_nb=seq_nb;
        this.sign=sign;
        this.to_port=to_port;
        this.toIp=toIp;
    }

    public int getOrgSender_id(){
        return this.sign.getOrg_sender_id();
    }

    public MessageType getMessageType(){
        return this.sign.getType();
    }

    public Message_sign getSign(){
        return this.sign;
    }

    public int getOrgSeq_nb(){
        return this.getSign().getOrg_seq_nb();
    }
    public int getSeq_nb() {
        return this.seq_nb;
    }

    public int getSender_id(){
        return this.sender_id;
    }

    public int getTo_port(){
        return this.to_port;
    }

    public InetAddress getToIp(){
        return toIp;
    }

    @Override
    public boolean equals (Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message m=(Message)o;
        return this.toIp.equals(m.getToIp()) && this.to_port == m.to_port && this.sign.equals(m.getSign());
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.sender_id,this.seq_nb,sign.getOrg_sender_id(),sign.getOrg_seq_nb(),sign.getType(),this.to_port,this.toIp.toString());
    }

}
