import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class ConnectFourAnimationPanel extends JPanel{
	
	// PROPERTIES
	BufferedImage homePage;
	BufferedImage board;
	BufferedImage winnerScreen;
	BufferedImage loserScreen;
	BufferedImage tieScreen;
	BufferedImage vs;
	BufferedImage howToPlayScreen;
	BufferedImage gameControlsInfo;

	boolean startScreen = true;
	boolean rulesScreen = false;
	boolean startServerScreen = false;
	boolean joinServerScreen = false;
	boolean controlsScreen = false;
	boolean gameInSession = false;

	int playerID; // 1 is server, 2 is client
	boolean myTurn = false;

	// int array tracking the state of the board
	//   0 = empty, 1 = P1, 2 = P2
	int boardArray[][] = new int[6][7];
	int mouseX;
	int mouseY;

	Color emtpySpace = Color.WHITE;
	Color p1Color = new Color(255, 102, 102);
	Color p2Color = new Color(255, 255, 128);
	Color myColor;
	Color oppColor;

	int gameWinner = 0; // 0 = no winner yet, 1 = P1, 2 = P2
	boolean tieGame = false;
	
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -	

	// METHODS
	public void paintComponent(Graphics g) {
		Color bgColor = new Color(156, 217, 255);
		g.setColor(bgColor);
		g.fillRect(0,0,1280,650);
		if (startScreen) {
			g.drawImage(homePage, 0, 0, null);
		}
		if (rulesScreen) {
			g.drawImage(howToPlayScreen, 0, 0, null);
		}
		if (controlsScreen) {
			g.drawImage(gameControlsInfo, 0, 0, null);
		}
		if (gameInSession) {
			g.drawImage(board, 20, 20, null);
			g.drawImage(vs, 370, 550, null);
			// drawing the pieces on the board
			for (int i = 0; i < 6; ++i) {
				for (int j = 0; j < 7; ++j) {
					if (boardArray[i][j] == 0) {
						g.setColor(emtpySpace);
					} else if (boardArray[i][j] == 1) {
						g.setColor(p1Color);
					} else if (boardArray[i][j] == 2) {
						g.setColor(p2Color);
					}
					g.fillOval(114 * j + 50, 83 * i + 30, 60, 60);
				}
			}
			if (playerID == 1) {
				myColor = p1Color;
				oppColor = p2Color;
			} else {
				myColor = p2Color;
				oppColor = p1Color;
			}
			g.setColor(myColor);
			g.fillRoundRect(20, 550, 300, 75, 20, 20);
			g.setColor(oppColor);
			g.fillRoundRect(520, 550, 300, 75, 20, 20);
		}
		// Hovering over potential columns to place a tile in
		if (myTurn && mouseX >= 20 && mouseX <= 820 && mouseY >= 20 && mouseY <= 520) {
			Color hoverColor = new Color(26, 178, 255, 50);
			g.setColor(hoverColor);
			g.fillRect((mouseX - 20) / 114 * 114 + 20, 20, 114, 500);
		}

		// End game screens:
		if (gameInSession && gameWinner == playerID) {
			g.drawImage(winnerScreen, 150, 150, null);
		} else if (gameInSession && gameWinner == (3 - playerID)) {
			g.drawImage(loserScreen, 150, 150, null);
		} else if (tieGame) {
			g.drawImage(tieScreen, 150, 150, null);
		}
	}	
	
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -	
	
	// CONSTRUCTOR
	public ConnectFourAnimationPanel() {
		super();
		try {
			homePage = ImageIO.read(new File("img/homePage.jpg"));
			board = ImageIO.read(new File("img/board.png"));
			winnerScreen = ImageIO.read(new File("img/winnerScreen.jpg"));
			loserScreen = ImageIO.read(new File("img/loserScreen.jpg"));
			tieScreen = ImageIO.read(new File("img/tieScreen.jpg"));
			vs = ImageIO.read(new File("img/vs.png"));
			howToPlayScreen = ImageIO.read(new File("img/rules.jpg"));
			gameControlsInfo = ImageIO.read(new File("img/controls.jpg"));
		} catch(IOException e) {
			System.out.println("Unable to load image");
		}
	}	
}
