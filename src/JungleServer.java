
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


/**
 *
 * @author Kiwi
 */
public class JungleServer {
    
    
    
    public static void main(String[] args) {
        ServerSocket sockConn = null;
        Socket sockComm = null;
        
        int portServ = 4000;
        ArrayList<String> listPseudo = new ArrayList<>();
        try
        {
            sockConn = new ServerSocket(portServ);
        }
        catch(IOException e) {
            System.out.println("pb creation serveur : "+e.toString());
            System.exit(1) ;
        }
        
        try
        {
            while (true)
            {
                sockComm = sockConn.accept();
                JungleServerThread t = new JungleServerThread(listPseudo);
                t.start();
                
            }
        }
        catch(IOException e)
        {
            System.out.println("pb connexion client : "+e.toString());
            e.printStackTrace();
        }
        finally{
            try{
                if (sockConn !=  null)
                    sockConn.close();
            }
            catch(IOException e) {
                System.out.println(e.toString());
                e.printStackTrace();
            }
        }
    }
    
}
