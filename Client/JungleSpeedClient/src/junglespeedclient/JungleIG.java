package junglespeedclient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;


public class JungleIG extends JFrame  {
    
    
    private static final long serialVersionUID = 1280296165382645118L;
    
    private JPanel panConn;
    private JTextField textServerIP;
    private JTextField textPseudo;
    public JLabel labelInfoPb;
    private JButton butConnect;
    
    private JPanel panInit;
    public JTextArea textInfoInit;
    private JButton butListParty;
    private JButton butCreateParty;
    private JSpinner spinNbPlayer;
    private JButton butJoinParty;
    private JTextField textJoin;
    public JLabel labelNomJ1;
    
    private JPanel panParty;
    public JTextArea textInfoParty;
    private JTextField textPlay;
    public JButton butTakeT;
    public JButton butHandT;
    private JButton butQuit;
    public JLabel labelNomJ2;
    
    private ActionListener listener;
    private Synchro sync;
    
    public JungleIG() {
        sync = new Synchro();
        ThreadCom imp = new  ThreadCom(sync, this);
        Thread th = new Thread(imp);
        th.start();
        // un même listener pour tous les boutons de tous les panels
        listener = new ImpActionListener();
        createWidget();
        pack();
        setVisible(true);
        //pour fermer l'application quand on clique sur la croix rouge d'un panneau
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    
    public void createWidget() {
        panConn = createPanelConnect();
        panInit = createPanelInit();
        panParty = createPanelPlay();
        
        setContentPane(panConn);
    }
    
    public JPanel createPanelConnect() {
        
        JPanel panAll = new JPanel(new BorderLayout());
        
        JPanel panPseudo = new JPanel();
        textPseudo = new JTextField("",20);
        textPseudo.setMaximumSize(textPseudo.getPreferredSize());
        panPseudo.add(new JLabel("Pseudo: "));
        panPseudo.add(textPseudo);
        
        JPanel panConn = new JPanel();
        textServerIP = new JTextField("127.0.0.1",15);
        panConn.add(new JLabel("Server IP: "));
        panConn.add(textServerIP);
        
        
        JPanel panSouth = new JPanel(new BorderLayout());
        labelInfoPb = new JLabel("Pseudo déjà utilisé ou invalide");
        labelInfoPb.setForeground(Color.RED);
        Font font = new Font("Courier", Font.BOLD, 12);
        labelInfoPb.setFont(font);
        labelInfoPb.setVisible(false);		//label invisible, possibilité de le rendre visible plus tard
        labelInfoPb.setHorizontalAlignment(JLabel.CENTER);
        butConnect = new JButton("Connect");
        // ajout d'un listener à butConnect
        butConnect.addActionListener(listener);
        panSouth.add(labelInfoPb, BorderLayout.NORTH);
        panSouth.add(butConnect, BorderLayout.CENTER);
        
        
        panAll.add(panPseudo,BorderLayout.NORTH);
        panAll.add(panConn,BorderLayout.CENTER);
        panAll.add(panSouth, BorderLayout.SOUTH);
        
        return panAll;
    }
    
    public JPanel createPanelPlay() {
        
        JPanel panAll = new JPanel(new BorderLayout());
        
        JPanel panNorth = new JPanel();
        labelNomJ2 = new JLabel("");		// possibilité de le modifier plus tard
        labelNomJ2.setForeground(Color.BLUE);
        Font font = new Font("Courier", Font.BOLD, 12);
        labelNomJ2.setFont(font);
        labelNomJ2.setHorizontalAlignment(JLabel.CENTER);
        panNorth.add(labelNomJ2);
        
        textInfoParty = new JTextArea(20,100);
        textInfoParty.setLineWrap(true);
        JScrollPane scroll = new JScrollPane (textInfoParty,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        // Ajout d'un bouton "prendre le totem" avec son listener
        butHandT = new JButton("Main Totem");
        butHandT.addActionListener(listener);
        butHandT.setEnabled(false);
        // Ajout d'un bouton "mettre la main sur le totem" avec son listener
        butTakeT = new JButton("Prendre Totem");
        butTakeT.addActionListener(listener);
        butTakeT.setEnabled(false);
        // enableOrder(false);		//pour interdire la saisie et l'envoi d'un ordre.
        
        JPanel panRight = new JPanel();
        panRight.setLayout(new BoxLayout(panRight, BoxLayout.PAGE_AXIS));
        panRight.add(Box.createVerticalGlue());
        panRight.add(butTakeT);
        panRight.add(butHandT);
        panRight.add(Box.createVerticalGlue());
        
        butQuit = new JButton("quit");
        // ajout d'un listener à butQuit
        butQuit.addActionListener(listener);
        
        panAll.add(panNorth,BorderLayout.NORTH );
        panAll.add(scroll,BorderLayout.CENTER);
        panAll.add(butQuit,BorderLayout.SOUTH);
        panAll.add(panRight, BorderLayout.EAST);
        
        return panAll;
    }
    
    public JPanel createPanelInit() {
        
        JPanel panNorth = new JPanel();
        labelNomJ1 = new JLabel("");	// possibilité de le modifier plus tard
        labelNomJ1.setForeground(Color.BLUE);
        Font font = new Font("Courier", Font.BOLD, 12);
        labelNomJ1.setFont(font);
        labelNomJ1.setHorizontalAlignment(JLabel.CENTER);
        panNorth.add(labelNomJ1);
        
        JPanel panRight = new JPanel();
        panRight.setLayout(new BoxLayout(panRight, BoxLayout.Y_AXIS));
        
        butListParty = new JButton("List parties");
        // ajout d'un listener
        butListParty.addActionListener(listener);
        
        JPanel panCreate = new JPanel();
        panCreate.setLayout(new BoxLayout(panCreate, BoxLayout.X_AXIS));
        butCreateParty = new JButton("Create party");
        // ajout d'un listener
        butCreateParty.addActionListener(listener);
        SpinnerModel model = new SpinnerNumberModel(3, 2, 8 , 1);
        spinNbPlayer = new JSpinner(model);
        spinNbPlayer.setMaximumSize(spinNbPlayer.getPreferredSize());
        panCreate.add(Box.createHorizontalStrut(20));
        panCreate.add(new JLabel("number of players: "));
        panCreate.add(spinNbPlayer);
        panCreate.add(butCreateParty);
        
        JPanel panJoin = new JPanel();
        panJoin.setLayout(new BoxLayout(panJoin, BoxLayout.X_AXIS));
        textJoin = new JTextField("",2);
        textJoin.setMaximumSize(textJoin.getPreferredSize());
        butJoinParty = new JButton("Join party");
        // ajout d'un listener
        butJoinParty.addActionListener(listener);
        panJoin.add(new JLabel("party number: "));
        panJoin.add(textJoin);
        panJoin.add(butJoinParty);
        
        panRight.add(butListParty);
        panRight.add(panCreate);
        panRight.add(panJoin);
        panRight.add(Box.createVerticalGlue());
        
        textInfoInit = new JTextArea(20,100);
        textInfoInit.setLineWrap(true);
        JScrollPane scroll = new JScrollPane (textInfoInit,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        JPanel panAll = new JPanel(new BorderLayout());
        panAll.add(panNorth, BorderLayout.NORTH);
        panAll.add(scroll, BorderLayout.CENTER);
        panAll.add(panRight, BorderLayout.EAST);
        
        return panAll;
    }
    
    //pour interdire/autoriser la saisie et l'envoi d'un ordre.
    /* public void enableOrder(boolean state) {
        textPlay.setEnabled(state);
        butPlay.setEnabled(state);
        
        if (!state) {
            textPlay.setText("");
        }
    } */
    
    // pour afficher le JPanel panConn
    public void setConnectionPanel() {
        setContentPane(panConn); //modification du contentPane de la JFrame
        pack();
    }
    
    // pour afficher le JPanel panInit
    public void setInitPanel() {
        setContentPane(panInit);
        pack();
    }
    
    // pour afficher le JPanel panParty
    public void setPartyPanel() {
        setContentPane(panParty);
        pack();
    }
    
    public String getIpServConnexion(){
        return this.textServerIP.getText();
    }
    
    public String getPseudoConnexion(){
        return this.textPseudo.getText();
    }
    
    public int getNombreJoueursCreationPartie(){
        return (int)this.spinNbPlayer.getValue();
    }
    
    public String getIdPartieARejoindre(){
        return this.textJoin.getText();
    }
    
    
    
    /* définition de la classe interne ImpActionListener,
    tous les boutons des différents JPanel "partagent" le même listener,
    qui est une instance de la classe ImpActionListener.
    */
    private class ImpActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            
            if (e.getSource() == butConnect) {
                System.out.println("Appui sur butConnect");
                sync.signalerDemandeConnexion();
            }
            else if (e.getSource() == butListParty) {
                System.out.println("Appui sur butListParty");
                sync.setNomAction("LIST");
                sync.SignalerdemandeAction();
            }
            else if (e.getSource() == butCreateParty) {
                System.out.println("Appui sur butCreateParty");
                sync.setNomAction("CREATE");
                sync.SignalerdemandeAction();
            }
            else if (e.getSource() == butJoinParty) {
                System.out.println("Appui sur butJoinParty");
                sync.setNomAction("JOIN");
                sync.SignalerdemandeAction();
            }
            else if (e.getSource() == butTakeT) {
                System.out.println("Appui sur TakeTotem");
                sync.signalerDemandeOrdreJoueur("TT");
            }
            else if (e.getSource() == butHandT) {
                System.out.println("Appui sur HandTotem");
                sync.signalerDemandeOrdreJoueur("HT");
            }
            else if (e.getSource() == butQuit) {
                System.out.println("Appui sur butQuit");
            }
        }
    }
    
}
