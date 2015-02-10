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

public class chattyServerV2 {
    private static final int BUFFER_SIZE = 255;
    private static final int PORT = 100000;

    private ServerSocketChannel serverSocket;
    private Selector accept
}
