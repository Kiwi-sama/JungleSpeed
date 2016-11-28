
class JungleClient  {
    
    public static void main(String []args) {

	if (args.length != 0) {
	    System.err.println("usage: JungleClient");
	    System.exit(1);
	}
	// Classe anonyme implementant une interface
	javax.swing.SwingUtilities.invokeLater( new Runnable() {
		public void run() {

            /*JungleIG ig =*/ new JungleIG();
        }
	    });
    }
}
		
