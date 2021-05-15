import java.awt.*;
import javax.swing.*;

public class MyButton extends JButton{
	public MyButton(String text){
		super(text);
		setFont(new Font("Caitlins Font", Font.PLAIN, 20));
		setBackground(Color.WHITE);
	}
}