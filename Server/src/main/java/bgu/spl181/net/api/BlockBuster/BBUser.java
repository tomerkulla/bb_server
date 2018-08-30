package bgu.spl181.net.api.BlockBuster;

import bgu.spl181.net.api.USTP.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class BBUser extends User {

        @SerializedName("country")
        @Expose
        private String country;
        @SerializedName("movies")
        @Expose
        private List<Movie> movies;
        @SerializedName("balance")
        @Expose
        private int balance;



        public BBUser(String username,String password,String country){
            this.username = username;
            this.password = password;
            this.country = country;
            this.movies = new ArrayList<Movie>();
            type = "normal";
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public List<Movie> getMovies() {
            return movies;
        }

        public void setMovies(List<Movie> movies) {
            this.movies = movies;
        }

        public int getBalance() {
            return balance;
        }

        public void setBalance(int balance) {
            this.balance = balance;
        }

        public boolean checkIfMovieRented(Movie movie){
            if(movies.size()>=1) {
            }

            for(Movie rentedMovie: movies)
                if(movie.getName().equals(rentedMovie.getName()))
                    return true;
            return false;
        }

        public void addMovie(Movie movie){
            movies.add(movie);
            balance = balance - movie.getPrice();
        }

        public void returnMovie(Movie movie){
            movies.remove(movie);
        }

        public boolean isAdmin(){
            return type.equals("admin");
        }

}

