package cs451;

import cs451.Broadcasts.FIFOBroadcast;
import cs451.Broadcasts.LocalizedCausalBroadcast;
import cs451.Messages.Message;
import cs451.Messages.MessageType;
import cs451.Messages.Message_sign;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Main {

    public static Logger logger= new Logger();;
    public static String output_path;


    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
        try {
            write_output_file(logger.get_logs(), output_path);
            System.out.println("Finished writing output file");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static HashMap<Integer, HashSet<Integer>> getCausalDependencies(String filepath){

        HashMap<Integer, HashSet<Integer>> causalities = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            String line = null;
            reader.readLine();
            int hostNum = 1;
            while((line = reader.readLine()) != null) {
                String[] separated = line.split(" ");
                HashSet<Integer> dependencies = new HashSet<>();
                for(int i = 0; i < separated.length;i++){
                    dependencies.add(Integer.parseInt(separated[i]));
                }
                causalities.put(hostNum,dependencies);
                hostNum +=1;
            }

            System.out.println(causalities.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(causalities.toString());
        return causalities;

    }

    public static int get_num_messages(String filepath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filepath));
        String line = reader.readLine();
        reader.close();
        return Integer.parseInt(line.split(" ")[0]);
    }


    public static void write_output_file(ArrayList<String> lines, String filepath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
        for (String line : lines) {
            writer.write(line);
            writer.newLine();
        }
        writer.close();
    }

    public static InetAddress get_my_Ip(List<Host> hosts, int my_id) throws UnknownHostException {
        for (Host host : hosts) {
            if (host.getId() == my_id) {
                return InetAddress.getByName(host.getIp());
            }
        }
        return null;
    }

    public static Integer get_my_port(List<Host> hosts, int my_id) {
        for (Host host : hosts) {
            if (host.getId() == my_id) {
                return host.getPort();
            }
        }
        return null;
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        int num_messages = get_num_messages(parser.config());
        System.out.println("Number of messages each process should send "+ num_messages);

        HashMap<Integer, HashSet<Integer>> Causal_dependencies= getCausalDependencies(parser.config());

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");
        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host : parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");

        System.out.println("Broadcasting and delivering messages...\n");

        //get my IP and port
        InetAddress my_Ip = get_my_Ip(parser.hosts(), parser.myId());
        int my_port = get_my_port(parser.hosts(), parser.myId());


        //Create instance of FIFOBroadcast
        LocalizedCausalBroadcast lcb = new LocalizedCausalBroadcast(parser.myId(), my_Ip, my_port, parser.hosts(),Causal_dependencies.get(parser.myId()), logger);

        output_path = parser.output();

        //Broadcast Messages
        for (int i = 1; i <= num_messages; i++) {
            Message_sign ms = new Message_sign(i, parser.myId(), MessageType.BROADCAST);
            lcb.broadcast(ms);
        }


        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
