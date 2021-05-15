import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ConnectFour implements ActionListener, MouseListener, MouseMotionListener {
	// PROPERTIES
	JFrame theframe;
	ConnectFourAnimationPanel thepanel;
	Timer thetimer;

	MyButton createServer;
	MyButton joinServer;
	MyButton rules;
	MyButton controls;
	MyButton home;
	JLabel addAddress;
	JTextField addFriend;
	JLabel enterName;
	JTextField collectName;
	MyButton sReady;
	MyButton cReady;

	String myName;
	String oppName;
	String friendIP;

	JLabel myNametag;
	JLabel oppNametag;

	JTextArea chatHistory;
	JScrollPane chatBoxScroll;
	JTextField chatBoxSend;

	JLabel turnIndicator;
	int trackFilled[] = new int[7];

	MyButton gameOverOk;
	
	SuperSocketMaster ssm;
	
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -		
	
	// METHODS - ACTIONLISTENER
	public void mouseMoved(MouseEvent evt) {
		thepanel.mouseX = evt.getX();
		thepanel.mouseY = evt.getY();
	}
	public void mouseDragged(MouseEvent evt) {
		
	}
	public void mousePressed(MouseEvent evt) {
		
	}
	public void mouseReleased(MouseEvent evt) {
		
	}
	
	public void mouseClicked(MouseEvent evt) {
		// If user selects a column and it's their turn to place a piece
		if (thepanel.myTurn && thepanel.mouseX >= 20 && thepanel.mouseX <= 820 && thepanel.mouseY >= 20 && thepanel.mouseY <= 520) {
			int column = (thepanel.mouseX - 20) / 114;
			int row = 5 - trackFilled[column];
			// if there is space in the column
			if (trackFilled[column] < 6) {
				thepanel.boardArray[row][column] = thepanel.playerID;
				++(trackFilled[column]);
				thepanel.myTurn = false;
				if (checkForWin(1, 0, row, column) || // horizontal connections: -
				checkForWin(0, 1, row, column) ||     // vertical connections: |
				checkForWin(1, 1, row, column) ||     // diagonal connections: /
				checkForWin(1, -1, row, column)) {    //diagonal connections: \
					thepanel.gameWinner = thepanel.playerID;
					ssm.sendText("Win:" + row + column);
					leaveGame();
				} else if (checkForTie()) {
					ssm.sendText("Tie:" + row + column);
					thepanel.tieGame = true;
					leaveGame();
				}else {
					ssm.sendText("Move:" + (5 - (trackFilled[column] - 1)) + column);
					oppTurn();
				}
			} else {
				turnIndicator.setText("That column is full. Select another one.");
			}
		}
	}

	public void mouseEntered(MouseEvent evt) {
		
	}

	public void mouseExited(MouseEvent evt) {
		
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == thetimer) {
			thepanel.repaint();
		}
		if (evt.getSource() == createServer) {
			leaveStartScreen();
			thepanel.startServerScreen = true;
			ssm = new SuperSocketMaster(2468, this);
			ssm.connect();
			addAddress.setText("This is your IP Address: " + ssm.getMyAddress());
			addAddress.setVisible(true);
			enterName.setVisible(true);
			collectName.setVisible(true);
			sReady.setVisible(true);
		} else if (evt.getSource() == joinServer) {
			leaveStartScreen();
			thepanel.joinServerScreen = true;
			addAddress.setText("Enter your friend's IP Address");
			addAddress.setVisible(true);
			addFriend.setVisible(true);
			enterName.setVisible(true);
			collectName.setVisible(true);
			cReady.setVisible(true);
		} else if (evt.getSource() == rules) {
			leaveStartScreen();
			thepanel.rulesScreen = true;
		} else if (evt.getSource() == controls) {
			leaveStartScreen();
			thepanel.controlsScreen = true;
		} else if (evt.getSource() == home) {
			goToStartScreen();
		} else if (evt.getSource() == sReady) {
			thepanel.startServerScreen = false;
			thepanel.playerID = 1;
			leaveConnectionScreen();
			startGame();
		} else if (evt.getSource() == cReady) {
			thepanel.joinServerScreen = false;
			friendIP = addFriend.getText();
			ssm = new SuperSocketMaster(friendIP, 2468, this);
			ssm.connect();
			System.out.println("Server joined");
			thepanel.playerID = 2;
			leaveConnectionScreen();
			startGame();
		} else if (evt.getSource() == gameOverOk) {
			// resetting screen
			chatHistory.setVisible(false);
			chatBoxScroll.setVisible(false);
			chatBoxSend.setVisible(false);
			gameOverOk.setVisible(false);
			thepanel.gameInSession = false;
			myNametag.setVisible(false);
			oppNametag.setVisible(false);
			ssm.disconnect();			

			thepanel.startScreen = true;
			createServer.setVisible(true);
			joinServer.setVisible(true);
			rules.setVisible(true);
			controls.setVisible(true);

			// resetting data and variables
			thepanel.playerID = 0;
			for (int i = 0; i < 6; ++i) {
				for (int j = 0; j < 7; ++j) {
					thepanel.boardArray[i][j] = 0;
				}
			}
			thepanel.gameWinner = 0;
			thepanel.tieGame = false;
			// addFriend and collectName become autofilled after first game
			// addFriend.setText("");
			// collectName.setText("");
			chatHistory.setText("");
			chatBoxSend.setText("");
			myName = "";
			oppName = "";
			friendIP = "";
			myNametag.setText("");
			oppNametag.setText("");
			for (int i = 0; i < 7; ++i) {
				trackFilled[i] = 0;
			}
			turnIndicator.setText("Waiting for an opponent...");
		}

		if (evt.getSource() == chatBoxSend) {
			//Text field to send messages across socket using the SuperSocketMaster class
			//Networking protocol: Distinguished by "Chat:" at the beginning of the string received
			ssm.sendText("Chat:" + myName + ": " + chatBoxSend.getText());
			chatHistory.append(myName + ": " + chatBoxSend.getText() + "\n");
			chatBoxSend.setText("");
		}

		if (evt.getSource() == ssm) {
			String data = ssm.readText();
			int dataLength = data.length();

			// EXCHANGING NAMES
			if (dataLength >= 5 && data.substring(0, 5).equals("Name:")) {
				oppName = data.substring(5, dataLength);
				oppNametag.setText(oppName);
				oppNametag.setVisible(true);
				if (thepanel.playerID == 1) {
					ssm.sendText("Name:" + myName);
					playerTurn();
				}
			}

			// CHAT
			if (dataLength >= 5 && data.substring(0, 5).equals("Chat:")) {
				chatHistory.append(data.substring(5, dataLength)  + "\n");
			}

			// GAMEPLAY
			if (dataLength >= 5 && data.substring(0, 5).equals("Move:")) {
				int row = Integer.parseInt(data.substring(5, 6));
				int col = Integer.parseInt(data.substring(6, 7));
				thepanel.boardArray[row][col] = 3 - thepanel.playerID; // 3 - playerID = opponent's ID
				++(trackFilled[col]);
				playerTurn();
			}

			// END GAME
			if (dataLength >= 4 && data.substring(3, 4).equals(":")) { // win or tie
				int row = Integer.parseInt(data.substring(4, 5));
				int col = Integer.parseInt(data.substring(5, 6));
				thepanel.boardArray[row][col] = 3 - thepanel.playerID; // 3 - playerID = opponent's ID
				++(trackFilled[col]);
				turnIndicator.setVisible(false);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (data.substring(0, 3).equals("Win")) {
					thepanel.gameWinner = 3 - thepanel.playerID;
				} else if (data.substring(0, 3).equals("Tie")) {
					thepanel.tieGame = true;
				}
				leaveGame();
			}
		}
	}

	public void leaveStartScreen() {
		thepanel.startScreen = false;
		createServer.setVisible(false);
		joinServer.setVisible(false);
		rules.setVisible(false);
		controls.setVisible(false);
		home.setVisible(true);
	}

	public void goToStartScreen() {
		if (thepanel.startServerScreen || thepanel.joinServerScreen) {
			if (ssm != null) {
				ssm.disconnect();
			}
			addAddress.setVisible(false);
			addFriend.setVisible(false);
			enterName.setVisible(false);
			collectName.setVisible(false);
			sReady.setVisible(false);
			cReady.setVisible(false);
		}
		thepanel.startServerScreen = false;
		thepanel.joinServerScreen = false;
		thepanel.rulesScreen = false;
		thepanel.controlsScreen = false;
		thepanel.startScreen = true;
		createServer.setVisible(true);
		joinServer.setVisible(true);
		rules.setVisible(true);
		controls.setVisible(true);
		home.setVisible(false);
	}

	public void leaveConnectionScreen() {
		myName = collectName.getText();
		addAddress.setVisible(false);
		addFriend.setVisible(false);
		enterName.setVisible(false);
		collectName.setVisible(false);
		home.setVisible(false);
		sReady.setVisible(false);
		cReady.setVisible(false);
		thepanel.gameInSession = true;
	}

	public void startGame() {
		chatHistory.setVisible(true);
		chatBoxScroll.setVisible(true);
		chatBoxSend.setVisible(true);
		chatHistory.append("Server Address: " + ssm.getMyAddress() + "\n");
		chatHistory.append(myName + " has joined the game.\n");
		turnIndicator.setVisible(true);
		myNametag.setText(myName);
		myNametag.setVisible(true);
		if (thepanel.playerID == 2) {
			ssm.sendText("Chat:" + myName + " has joined the game.");
			ssm.sendText("Name:" + myName);
			oppTurn();
		}
	}

	public void playerTurn() {
		// NOTE: P1 always goes first
		turnIndicator.setText("Select a column to place your piece in!");
		thepanel.myTurn = true;
	}

	public void oppTurn() {
		thepanel.myTurn = false;
		// wait to receive opponent's name over socket connection (only neeeded on the first turn)
		if (oppName == null || oppName == "") {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		turnIndicator.setText("Waiting for " + oppName + " to place a piece...");
	}

	public boolean checkForTie() {
		// assumes board is not in a state where either player has 4 in a row
		for (int i = 0; i < 7; ++i) {
			if (trackFilled[i] < 6) {
				return false;
			}
		}
		return true;
	}

	public boolean checkForWin(int deltaX, int deltaY, int row, int col) {
		int ahead = 0;
		int behind = 0;
		// count number of connected pieces "ahead"
		int j = row - deltaY;
		for (int i = col + deltaX; i < 7; i += deltaX) {
			if (j >= 0 && j < 6) {
				if (thepanel.boardArray[j][i] == thepanel.playerID) {
					++ahead;
				} else {
					break; // broke the connection
				}
			} else {
				break; // reached the edge of the board
			}
			j -= deltaY;
		}
		// count number of connected pieces "behind"
		j = row + deltaY;
		for (int i = col - deltaX; i >= 0; i -= deltaX) {
			if (j < 6 && j >= 0) {
				if (thepanel.boardArray[j][i] == thepanel.playerID) {
					++behind;
				} else {
					break; // broke the connection
				}
			} else {
				break; // reached the edge of the board
			}
			j += deltaY;
		}
		return (ahead + 1 + behind) >= 4;
	}

	public void leaveGame() {
		// end game sequence
		turnIndicator.setVisible(false);
		gameOverOk.setVisible(true);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -	
	


// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	// CONSTRUCTOR
	public ConnectFour(){
		theframe = new JFrame("Connect Four");
		thepanel = new ConnectFourAnimationPanel();
		thepanel.setLayout(null);
		thepanel.setPreferredSize(new Dimension(1280,650));
		theframe.setResizable(false);
		thepanel.addMouseListener(this);
		thepanel.addMouseMotionListener(this);
		
		thetimer = new Timer(1000/60, this);
		thetimer.start();

		createServer = new MyButton("Host a Game");
		createServer.addActionListener(this);
		createServer.setBounds(550, 200, 200, 50);
		joinServer = new MyButton("Join a Game");
		joinServer.addActionListener(this);
		joinServer.setBounds(550, 260, 200, 50);
		rules = new MyButton("How to Play");
		rules.addActionListener(this);
		rules.setBounds(550, 320, 200, 50);
		controls = new MyButton("Controls");
		controls.addActionListener(this);
		controls.setBounds(550, 380, 200, 50);

		home = new MyButton("Return");
		home.addActionListener(this);
		home.setBounds(0, 600, 100, 50);
		home.setVisible(false);

		addAddress = new JLabel();
		addAddress.setBounds(300, 100, 800, 50);
		addAddress.setVisible(false);
		
		addFriend = new JTextField();
		addFriend.setBounds(300, 210, 800, 20);
		addFriend.setVisible(false);
		
		enterName = new JLabel("Enter your name below:");
		enterName.setBounds(300, 320, 800, 50);
		enterName.setVisible(false);

		collectName = new JTextField();
		collectName.setBounds(300, 430, 800, 20);
		collectName.setVisible(false);

		sReady = new MyButton("Ready");
		sReady.addActionListener(this);
		sReady.setBounds(300, 520, 150, 50);
		sReady.setVisible(false);

		cReady = new MyButton("Ready");
		cReady.addActionListener(this);
		cReady.setBounds(300, 520, 150, 50);
		cReady.setVisible(false);

		turnIndicator = new JLabel("Waiting for an opponent...");
		turnIndicator.setBounds(320, 510, 800, 50);
		turnIndicator.setVisible(false);

		myNametag = new JLabel("");
		myNametag.setFont(new Font("Caitlins Font", Font.BOLD, 40));
		myNametag.setBounds(40, 570, 260, 55);
		myNametag.setVisible(false);

		oppNametag = new JLabel("");
		oppNametag.setFont(new Font("Caitlins Font", Font.BOLD, 40));
		oppNametag.setBounds(540, 570, 260, 55);
		oppNametag.setVisible(false);

		chatHistory = new JTextArea();
		chatHistory.setVisible(false);
		chatHistory.setEditable(false);
		chatBoxSend = new JTextField();
		chatBoxSend.setBounds(880, 590, 400, 60);
		chatBoxSend.setVisible(false);
		chatBoxSend.addActionListener(this);
		chatBoxScroll = new JScrollPane(chatHistory);
		chatBoxScroll.setSize(400, 550);
		chatBoxScroll.setLocation(880, 40);
		chatBoxScroll.setVisible(false);

		gameOverOk = new MyButton("OK");
		gameOverOk.addActionListener(this);
		gameOverOk.setBounds(500, 500, 150, 75);
		gameOverOk.setVisible(false);

		thepanel.add(createServer);
		thepanel.add(joinServer);
		thepanel.add(rules);
		thepanel.add(controls);
		thepanel.add(home);
		thepanel.add(addAddress);
		thepanel.add(addFriend);
		thepanel.add(enterName);
		thepanel.add(addFriend);
		thepanel.add(enterName);
		thepanel.add(collectName);
		thepanel.add(sReady);
		thepanel.add(cReady);
		thepanel.add(chatBoxSend);
		thepanel.add(chatBoxScroll);
		thepanel.add(turnIndicator);
		thepanel.add(myNametag);
		thepanel.add(oppNametag);
		thepanel.add(gameOverOk);

		theframe.setContentPane(thepanel);
		theframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theframe.pack();
		theframe.setVisible(true);
	}
	
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -	
	public static void main(String[] args) {
		new ConnectFour();
	}
}
