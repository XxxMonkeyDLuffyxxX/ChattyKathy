/**------------------------------------------------------------------------------
 | Author : Dontae Malone
 | Company: EAI Design Services LLC
 | Project: Simple Multiplexing TCP/IP Chat program
 | Copyright (c) 2015 EAI Design Services LLC
 ------------------------------------------------------------------------------ */
/**---------------------------------------------------------------------------------------------
 | Classification: UNCLASSIFIED
 |
 | Abstract: This is the server code for the chat program
 |
 \---------------------------------------------------------------------------------------------*/
/**---------------------------------------------------------------------------------------------
 VERSION HISTORY:
 1.0  - 02042015 - Initial Creation


 \---------------------------------------------------------------------------------------------**/

import java.lang.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.*;
import java.util.*;

public class chattyServer implements Runnable {

    private static final int PORT = 10000; //Initial port number outside blocked range
    private static final long TIMEOUT = 10000; //10 sec timeout

    //TODO allow user to enter server port in UI/console
    private static final String ADDRESS = "localhost"; // 127.0.0.1 on most systems. Standard loop back address for test

    private static int uniqueID; //a unique ID for each connection

    private ServerGUI serverGUI; //Used for GUI application, not used if console only
    private ServerSocketChannel serverChannel; //Creates a server socket object
    private Selector selector; //Creates a selector object
    private ArrayList<ClientThread> arrayList; //Creates an ArrayList object to keep a list of Client IDs

    /**
     * Because reading/writing takes place asynchronously, this HashMap will keep track of the data to be
     * written to the clients.
     */
    private Map<SocketChannel, byte[]> dataTracking = new HashMap<SocketChannel, byte[]>();

    public static void main (String args []){
        chattyServer chatServer = new chattyServer(); //Instantiate the ChattyKathy server and get it going
        chatServer.run();
    }


        public void chattyServer() {
            clients = new LinkedList();;
            initServerSocket();
        }

        //Initialize Server
        private void initServerSocket() {
            System.out.println("Beaming you up, Scotty."); //Dialog message to alert user that server is initializing

            /**
            *Verifies an instance of selector and the server channel are not already open. If they are, it returns back
             *to the main code
            */
            if (selector != null || serverChannel != null) {
                System.out.println("Selector");
                return;
            }

            try {
                //Open a non-blocking ServerSocketChannel
                serverChannel = ServerSocketChannel.open();
                serverChannel.configureBlocking(false);

                //Bind Channel to localhost on port
                serverChannel.socket().bind(new InetSocketAddress(ADDRESS, PORT));

                //New Selector(Multiplexing)
                selector = Selector.open();

                //Register the server Channel object with the Selector object and configure it to accept a connection
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            } catch (IOException e) {
                System.out.println("Well...this happened: " + e);
            }
        }

    @Override
    public void run() {
        System.out.println("Now accepting connections...");
        try {
            //Infinite loop as long as the thread is not interrupted
            while (!Thread.currentThread().isInterrupted()) {

                /**
                 * Waits for an operation to be ready and blocks in the mean time. The private field TIMEOUT is to make
                 * sure it doesn't block indefinately
                 */
                selector.select(TIMEOUT);

                //Creates an Iterator to cycle through keys
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                /**
                 * Iterates through the SelectionKeys in the KeySet from the Selector one at a time.
                 */
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();

                    keys.remove(); //Takes the key out of the ring so we don't process it again

                    //Simple check to make sure the current key is valid
                    if (!key.isValid()) {
                        continue;
                    }
                    /**
                     * The server is now sitting and awaiting connections via the server socket Channel. If the key from
                     *the KeySet is accepted, the Selector is now prepped for the next task (i.e. read() or write()
                     * methods) based on the next key in the Set
                     */
                    if (key.isAcceptable()) {
                        System.out.println("Connecting to Socket on port " + PORT + ". Please wait. Seriously, " +
                                "chill out."); //Current status is sent to the user
                        accept(key);
                    }

                    if (key.isWritable()) {
                        System.out.println("Writing..."); //Current status is sent to the user
                        write(key);
                    }

                    if (key.isReadable()) {
                        System.out.println("Reading..."); //Current status is sent to the user
                        read(key);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Well...this happened: " + e);
        } finally {
            closeConnection(); //Housekeeping
        }


                /*TODO Server messages to clients. Create Random generater or and messages  ie.
                System.out.println ("Awesome! You are now connected to the Chatty Kathy Server. Good for you--now you can waste
                more time not working. No Tweets, please.");*/
    }

    /**
     * This method accepts new Clients and instantiates a serverSocketChannel using the SelectionKey method channel()
     *
     * @param key
     * @throws IOException
     */
    public void accept(SelectionKey key) throws IOException {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);

            socketChannel.register(selector, SelectionKey.OP_WRITE);
            byte[] greeting = new String("It appears you knew the secret password. Welcome...accepting connection.\nYou " +
                    "are now connected on IP: ").getBytes(); //Greeting message for the user

            //Sends a message to the user advising them they are now connected to the server
            dataTracking.put(socketChannel, greeting);
        } catch (IOException e) {
            System.out.println("Well...this happened: " + e);
        }
    }

    /**
     * This methods writes the data to the channel and sends it to the Clients
     *
     * @param key
     * @throws IOException
     */
    public void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel(); //Create a new channel to communicate with the Clients

        byte[] data = dataTracking.get(channel); //Create a new hasmap to keep track of data to write
        dataTracking.remove(channel); //Clear the hasmap

        channel.write(ByteBuffer.wrap(data)); //Write hasmap contents to channel via buffer

        key.interestOps(SelectionKey.OP_READ); //Register with the selector to read next
    }

    /**
     * This method takes reads from the channel, echoes the information back to the Clients via the echo() method
     *
     * @param key
     * @throws IOException
     */
    public void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel(); //Create a new channel to communicate with the Clients

        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        readBuffer.clear();

        int read;

        try {
            read = channel.read(readBuffer);
        } catch (IOException e) {
            System.out.println("Well...this reading issue happened. I'm gonna close this now.");
            key.cancel(); //Delete the key
            channel.close(); //Close the current channel
            return;
        }

        if (read == -1) {
            System.out.println("Nothing, Boss.");
            key.cancel(); //Delete the key
            channel.close(); //Close the current channel
            return;
        }

        readBuffer.flip();
        byte[] data = new byte[1000];
        readBuffer.get(data, 0, read);
        System.out.println();

        echo(key, data);
    }

    private void echo(SelectionKey key, byte[] data) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        dataTracking.put(socketChannel, data);
        key.interestOps(SelectionKey.OP_WRITE);
    }

    /**
     * This method closes the connection
     */
    public void closeConnection() {
        System.out.println("Shuttin'er down..."); //Send status message to user
        if (selector != null) {
            try {
                selector.close();
                serverChannel.socket().close();
                serverChannel.close();
            } catch (IOException e) {
                System.out.println("Well...this happened: " + e);
            }
        }
    }

}

