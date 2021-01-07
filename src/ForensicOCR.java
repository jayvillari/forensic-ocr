/**
* 
*	This is where the JFrame object is created and
*	the GUI is set up.
*
**/

import javax.swing.JFrame;

public class ForensicOCR
{
	public static void main(String[] args)
	{
		JFrame frame = new JFrame("Forensic OCR");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ForensicOCRPanel panel = new ForensicOCRPanel();
		frame.getContentPane().add(panel);
		
		frame.pack();
		frame.setVisible(true);
	}
}