package bgu.spl181.net.api.USTP;

import bgu.spl181.net.api.bidi.BidiMessagingProtocol;
import bgu.spl181.net.api.bidi.Connections;


public abstract class USTProtocol implements BidiMessagingProtocol<String>{

    protected ServerUsers users;
    protected User user;
    protected Connections<String> connections;
    protected int connectionId;
    protected boolean shouldTerminate;

    public USTProtocol(ServerUsers args){this.users = args;}

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connectionId = connectionId;
        this.connections = connections;

    }

    @Override
    public void process(String message) {
        if (message.equals(""))
            return;
        if(((message.indexOf(" ") == -1) && (message.equals("SIGNOUT")))
                || ((message.indexOf(" ") == -1) && (message.equals("LOGOUT")))){
            signOut();
            return;
        }
        String commandName = message.substring(0, message.indexOf(" "));
        String commandDet = message.substring(message.indexOf(" ") + 1);
        if (commandName.equals("REGISTER"))
            register(commandDet);
        else if (commandName.equals("LOGIN"))
            login(commandDet);
            else if (commandName.equals("REQUEST"))
                request(commandDet);
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }


    private void login(String detail){
        String[] details = detail.split("\\s+");
        if((details.length != 2)||(user != null)) {
            connections.send(connectionId, "ERROR login failed");
            return;
        }
        user = users.logIn(details[0], details[1]); // need to check with sync if the user is already logged in!
        if(user == null) {
            connections.send(connectionId, "ERROR login failed");
            return;
        }
        connections.send(connectionId, "ACK login succeeded");
    }

    private void signOut(){
        if(user == null){
            connections.send(connectionId, "ERROR signout failed");
            return;
        }
        user.logOut();
        user = null;
        shouldTerminate = true;
        connections.send(connectionId, "ACK signout succeeded"); //need to check if there is a problem with the pool (try to disconnect while a pending actions.. maybe we throw it in the pool
        connections.disconnect(connectionId);
    }

    protected abstract void register(String detail);

    protected abstract void request(String detail);


}