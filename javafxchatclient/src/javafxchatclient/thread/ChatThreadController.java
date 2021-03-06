/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxchatclient.thread;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

import Interface.client.IChatTabController;
import Interface.client.IChatThreadController;
import Interface.client.IChatclientController;
import clientMessage.Message;
import clientMessage.MessageData.ClientToServer.MessageLogin;
import clientMessage.MessageData.IClientMessage;
import javafx.scene.text.Text;
import javafxchatclient.NewChattabController;
import javafxchatclient.Tree.Item.ConnectionTreeItem;
import javafxchatclient.Tree.Item.ServerTreeItem;
import socketconnection.MessagesToSend;
import socketconnection.RC;
import socketconnection.ServerInfo;
import socketconnection.Socketwrapper;

/**
 * @author julien
 */
public class ChatThreadController implements IChatThreadController {


    private volatile AtomicBoolean running;

    private ServerInfo serverInfo;
    private Socketwrapper sw;
    private IChatTabController chattabController;
    private ServerTreeItem server;
    private Thread connection;
    private IChatclientController chatclientController;
    private boolean connected = false;
    private STATUS status;


    public boolean isRunning() {
        return running.get();

    }

    public ChatThreadController(ServerInfo login, IChatclientController chatclientController) {
        status = STATUS.initialized;
        this.serverInfo = login;
        sw = new Socketwrapper();
        running = new AtomicBoolean();
        this.server = server;
        this.chatclientController = chatclientController;
    }


    private Thread trec;
    private Thread tsend;
    private RecChatThread recChatThread;
    private SendChatThread sendChatThread;
    private MessagesToSend<Message> mts;

    public void setuptree() {

        server = new ServerTreeItem(serverInfo.getServername(), this);
        chatclientController.addToTreeChannelView(server);
    }

    public void connectingToServer() {
        connection = new Thread(new ServerConnection());
        connection.start();
    }

    public void init() {
        setuptree();
        if (server.getChildren().size() == 1 && server.getChildren().get(0) instanceof ConnectionTreeItem) {
            ConnectionTreeItem connecting = (ConnectionTreeItem) server.getChildren().get(0);
            status = STATUS.connecting;

            for (int i = 0; i < connecting.getMax(); i++) {
                connecting.setValue(new Text("Connecting ....(" + i + ")"));
                try {
                    sw.connect(serverInfo.getIp(), serverInfo.getPort());
                    start();
                    mts.addMessage(new MessageLogin(serverInfo));
                    connecting.setValue(new Text("Waiting for the server"));
                    status = STATUS.waitingforresponse;
                    return;
                } catch (IOException e) {
                        connecting.setValue(new Text("Sleeping for 10 seconds"));
                    try {
                        Thread.currentThread().sleep(10000);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
                connecting.setValue(new Text("Failed to connect"));
                status = STATUS.failedtoconnect;
            }
    }

    public void start() {

        if (status == STATUS.connected) {
            status = STATUS.started;
            running.set(true);
            recChatThread = new RecChatThread();
            sendChatThread = new SendChatThread();
            trec = new Thread(recChatThread);
            tsend = new Thread(sendChatThread);
            mts = new MessagesToSend<>();
            trec.start();
            tsend.start();
        }

    }

    enum STATUS {

        initialized,
        started,
        waitingforresponse,
        failedtoconnect,
        connecting,
        connected, disconnected
    }


    public void end() {
        status = STATUS.disconnected;
        running.set(false);
        try {
            sw.closeSocket();
            server.getParent().getChildren().remove(server);
            chatclientController.remove(this);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMessageLoop() {
        synchronized (mts.getMessagetosend()) {
            while (isRunning()) {
                mts.waitOnMessage();
                Message message = mts.getRemMessage();
                try {
                    sw.sendMessage(message);
                } catch (IOException e) {
                    //TODO create a different error handler for each different message
                    mts.addFrontMessage(message);
                }
            }
        }
    }

    public void sendMessage(Message message) {
        mts.addMessage(message);
    }

    public void processMessage() {
        Message message = null;
        while (isRunning()) {
            try {
                message = sw.receivemessage();
                ((IClientMessage) message).setDefaultAction(this);
                message.activateAction();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private class RecChatThread implements Runnable {
        @Override
        public void run() {


            ChatThreadController.this.processMessage();
        }
    }

    private class ServerConnection implements Runnable {

        public void run() {
            ChatThreadController.this.init();
        }


    }

    private class SendChatThread implements Runnable {
        @Override
        public void run() {
            ChatThreadController.this.sendMessageLoop();
        }
    }


}
