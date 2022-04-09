package cs451.Messages;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Message_sign {
    private final int org_seq_nb;
    private final int org_sender_id;
    private final MessageType type;
    private  AtomicIntegerArray VC;

    public Message_sign(int org_seq_nb,int org_sender_id,MessageType type){
        this.org_seq_nb=org_seq_nb;
        this.org_sender_id=org_sender_id;
        this.type=type;
    }

    public void setVC (AtomicIntegerArray VC){
        this.VC=VC;
    }

    public AtomicIntegerArray getVC(){
        return this.VC;
    }

    public int getOrg_sender_id() {
        return org_sender_id;
    }

    public int getOrg_seq_nb() {
        return org_seq_nb;
    }

    public MessageType getType() {
        return type;
    }

    @Override
    public boolean equals (Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message_sign sign=(Message_sign)o;
        return (this.org_seq_nb == sign.org_seq_nb) && (this.org_sender_id == sign.org_sender_id) && (this.type == sign.type);
    }
    @Override
    public int hashCode() {
        return Objects.hash(org_seq_nb, org_sender_id, type);
    }



}
