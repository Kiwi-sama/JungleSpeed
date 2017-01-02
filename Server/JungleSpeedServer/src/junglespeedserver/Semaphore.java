package junglespeedserver;

public class Semaphore {
    int nbTokens;

    public Semaphore(int nbTokens) {
        this.nbTokens = nbTokens;
    }

    public synchronized void put(int nb) {
        nbTokens += nb;
        notifyAll();
    }

    public synchronized void get(int nb) {
        while (nb > nbTokens) {
            try {
                wait();
            }
            catch(InterruptedException e) {}
        }
        nbTokens -= nb;
    }
}
