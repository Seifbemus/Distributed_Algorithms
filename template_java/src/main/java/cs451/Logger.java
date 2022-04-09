package cs451;
import cs451.Messages.Message;
import cs451.Messages.MessageType;
import cs451.Messages.Message_sign;

import java.util.ArrayList;
public class Logger {
    private ArrayList<String> logs = new ArrayList<String>();

    public void deliver(Message m) {
        logs.add("d " + m.getOrgSender_id() + " " + m.getOrgSeq_nb());
    }

    public void deliver (Message_sign ms){
        logs.add("d " + ms.getOrg_sender_id() + " " + ms.getOrg_seq_nb());
    }

    public void send(Message m) {
        if (m.getMessageType()== MessageType.BROADCAST){
            logs.add("b " + m.getSeq_nb());

        }

    }

    public void broadcast(Message_sign ms){
        if (ms.getType()==MessageType.BROADCAST){
            logs.add("b " + ms.getOrg_seq_nb());
        }

    }

    public ArrayList<String> get_logs() {
        return this.logs;
    }
}
