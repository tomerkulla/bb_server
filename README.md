![BB Logo](https://vignette.wikia.nocookie.net/althistory/images/7/73/Blockbuster_Online_logo_%28Alternity%29.png/revision/latest?cb=20140103023924)

BB-Server is an online movie rental server-client system.<br/> 
The communication between the server and the client(s) is performed using a text based communication protocol, which supports renting, listing and returning of movies.<br/> 
The thread-management-design of the server is based on the **Thread-Per-Client** (TPC) or **Reactor** (Thread Pool) .<br/> 
Implantation using **Java**, **C++** and **JSON**
<br/> 
## Supported Commands:

### Server:
1. **ACK** [message]
1. **ERROR** <error message>
1. **BROADCAST** <message> <br/> 
**note:** an example database is provided (Users and Movies).
  
### Client:
1. **REGISTER** [username] [password] country=”[country name]”
1. **LOGIN** [username] [password]
1. **SIGNOUT**
1. **REQUEST**
	* **REQUEST** balance info
	* **REQUEST** balance add [amount]
	* **REQUEST** info “[movie name]”<br/> 
		for **all** movies info do not insert movie name.
	* **REQUEST** rent [”movie name”]
	* **REQUEST** return [”movie name”]

### Admin:

1. **REQUEST** addmovie [”movie name”] [amount] [price] [“banned country”,…]
1. **REQUEST**  remmovie [”movie name”]
1. **REQUEST** changeprice [”movie name”] [price]


## Run:
### Linux:

1. Establishing Server:
	* inside ../Server:
		* Run `mvn clean compile`
		* for **Reactor** Thread-Management:<br/>
		`mvn exec:java -Dexec.mainClass=bgu.spl181.net.impl.BBreactor.ReactorMain -Dexec.args=[port]`
		* for **TCP** Thread-Management:<br/> 
		`mvn exec:java -Dexec.mainClass=bgu.spl181.net.impl.BBtpc.TPCMain -Dexec.args=”[port]”`
1. Clients:
	* inside ../Client:
		* Run `make`.
		* Run `bin/BBclient [ip] [port]`
		
### Requirements:

* Boost library:<br/>
https://www.boost.org/doc/libs/1_61_0/more/getting_started/unix-variants.html
* Maven <br/>
https://maven.apache.org/install.html

## Tested:
* Compiles successfully on 64bit Ubuntu-16.78 . Should also compile on other linux variants.

	



	
	

