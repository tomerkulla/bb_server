package bgu.spl181.net.api.BlockBuster;

import bgu.spl181.net.api.USTP.ServerUsers;
import bgu.spl181.net.api.USTP.USTProtocol;
import bgu.spl181.net.api.USTP.*;


public class BBProtocol extends USTProtocol {

    MovieDataBase movieDataBase;

    public BBProtocol(ServerUsers args1, MovieDataBase args2){
        super(args1);
        this.movieDataBase = args2;

    }

    protected void register (String detail) {
        String[] details = detail.split("\\s+");
        if((details.length != 3)||(user != null)||! details[2].startsWith("country=")) { //logged from a diffrent computer
            connections.send(connectionId, "ERROR registration failed");
            return;
        }
        details[2] = details[2].substring(details[2].indexOf('=') + 1);
        details[2] = details[2].replace("\"", "" );
        if(users.Register(details[0], details[1], details[2])) // need to check if the name is taken SYNC
            connections.send(connectionId,"ACK registration succeeded");
        else
            connections.send(connectionId, "ERROR registration failed");

        }

    protected void request(String detail) {
        String requestName;
        String requestDet;
        if(detail.indexOf(" ") == -1){
            requestName = detail;
            requestDet = "";
        }
        else {
            requestName = detail.substring(0, detail.indexOf(" "));
            requestDet = detail.substring(detail.indexOf(" ") + 1);
        }
        if (user == null){
            connections.send(connectionId, "ERROR request " + requestName + " failed");
            return;
        }
        if(requestName.equals("balance")){
            requestBalance(requestDet);
            return;
        }
        if(requestName.equals("info")) {
            requestInfo(requestDet);
            return;
        }
        if(requestName.equals("rent")) {
            requestRent(requestDet);
            return;
        }
        if(requestName.equals("return")) {
            requestReturn(requestDet);
            return;
        }

        //---------------------admin------------------------------

        if(requestName.equals("addmovie")) {
            requestAddMovie(requestDet);
            return;
        }

        if(requestName.equals("remmovie")) {
            requestRemoveMovie(requestDet);
            return;
        }

        if(requestName.equals("changeprice")) {
            requestChangePrice(requestDet);
            return;
        }

    }

    private void requestBalance(String detail){
        String[] details = detail.split("\\s+");
        if(details[0].equals("info") ) {
            connections.send(connectionId, "ACK balance " + ((BBUser)user).getBalance());
            return;
        }
        if(details[0].equals("add") ) {
            ((BBServerUsers)users).addToBalance(user, details[1]);
            connections.send(connectionId, "ACK balance " + ((BBUser)user).getBalance() + " added " + details[1]);
            return;
        }
    }

    private void requestInfo(String detail) {
        if (detail.length() == 0)
            connections.send(connectionId, "ACK info " + movieDataBase.getAllNames()); // all database to 1 string
        else {
            detail =  detail.replace("\"","");
            String info = movieDataBase.getMovieInfo(detail);
            if (info == null)
                connections.send(connectionId, "ERROR request info failed");
            else
                connections.send(connectionId, "ACK info " + info);

        }
    }

    private void requestRent(String detail) {
        detail = detail.replace("\"", "" );
        Movie movie = movieDataBase.getMovie(detail);
        if((movie == null)||(movie.checkBannedIn(((BBUser)user).getCountry()))||(((BBUser)user).checkIfMovieRented(movie))
                ||(!movieDataBase.rentMovie(movie, ((BBUser)user).getBalance()))){ //check copies should be sync but only on the movie
            connections.send(connectionId, "ERROR request rent failed");
            return;
        }
        ((BBServerUsers)users).addMovie(((BBUser)user), movie);
        connections.send(connectionId,"ACK rent \"" + movie.getName() + "\" success");
        connections.broadcast("BROADCAST movie " +  movie.toString());
    }

    private void requestReturn(String detail){
        detail = detail.replace("\"", "" );
        Movie movie = movieDataBase.getMovie(detail);
        if((movie == null)||! (((BBUser)user).checkIfMovieRented(movie))||(!movieDataBase.returnMovie(movie))){
            connections.send(connectionId, "ERROR request return failed");
            return;
        }
        ((BBServerUsers)users).returnMovie(user, movie);
        connections.send(connectionId,"ACK return \"" + movie.getName() + "\" success");
        connections.broadcast("BROADCAST movie " +  movie.toString());
        }

    // ---------------------------- admin ------------------------------------

    private void requestAddMovie(String detail){
        String movieName = detail.substring(detail.indexOf('"') + 1);
        movieName = movieName.substring(0, movieName.indexOf('"'));
        movieName = movieName.replace("\"", "" );
        detail = detail.substring(detail.indexOf('"', 1) + 2);
        String[] bannedCountries;
        String[] details;
        if(detail.indexOf('"') != -1) {
            bannedCountries = (detail.substring(detail.indexOf('"'))).split("\\s+");
            details = (detail.substring(0, detail.indexOf('"') - 1)).split("\\s+");
        }
        else{
            bannedCountries = new String[0];
            details = detail.split("\\s+");
        }
         if (!(((BBUser)user).isAdmin()) || !(movieDataBase.addMovieToDataBase(movieName, details[0], details[1], bannedCountries)))
             connections.send(connectionId, "ERROR request addmovie failed");
            // need check if the price is bigger than 0 and to check if the name availble (SYNC)
         else {
             Movie movie = movieDataBase.getMovie(movieName);
             connections.send(connectionId, "ACK addmovie \"" + movie.getName() + "\" success");
             connections.broadcast("BROADCAST movie " + movie.toString()); // need to check conccurency
            }
    }

    private void requestRemoveMovie(String detail) {
        detail = detail.replace("\"", "" );
        if (!(((BBUser)user).isAdmin()) || !(movieDataBase.removeMovieFromDataBase(detail)))
            connections.send(connectionId, "ERROR request remmovie failed");
            // checks 2 and 3 (pdf) need to be sync on the movie
        else {
            connections.send(connectionId, "ACK remmovie \"" + detail + "\" success");
            connections.broadcast("BROADCAST movie \"" + detail + "\" removed");
        }
    }

    private void requestChangePrice(String detail) {
        String movieName = detail.substring(detail.indexOf('"') + 1);
        movieName = movieName.substring(0, movieName.indexOf('"'));
        movieName = movieName.replace("\"", "" );
        String newPrice = detail.substring(detail.indexOf('"', 1) + 2);
        if (!(((BBUser)user).isAdmin()) || !(movieDataBase.changeMoviePrice(movieName,newPrice)))
            connections.send(connectionId, "ERROR request changeprice failed");
            // checks 2 and 3 (pdf) need to be sync on the movie
        else {
            Movie movie = movieDataBase.getMovie(movieName);
            connections.send(connectionId, "ACK changeprice \"" + movieName + "\" success");
            connections.broadcast("BROADCAST movie \"" + movieName + "\" " + movie.getAvailableAmount() + " " + newPrice);
        }

    }

}
