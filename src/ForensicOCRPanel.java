/**
* 
*	This is where the input file is processed and 
*	used to determine the closest matching letter(s).
*
**/

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.Image;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ForensicOCRPanel extends JPanel
{
	//Instantiate all variables
	private JLabel inputLabel, filePathLabel, imageLabel, outputLabel, letterLabel;
	private Image inputImage; 
	private JFileChooser fc;
	private int returnVal;
	private long fileSize;
	private JButton btnUpload;
	private ImageIcon uploadedImageIcon;
	private String imagePath, imageName, currentFile, currentPath, strOutput;

	public ForensicOCRPanel()
	{
		//Set up file chooser
		fc = new JFileChooser();
		File workingDirectory = new File(System.getProperty("user.dir"));
		fc.setCurrentDirectory(workingDirectory);
		btnUpload = new JButton("Browse...");
		btnUpload.addActionListener(new TempListener());
		inputLabel = new JLabel("Select Image: ");
		inputLabel.setForeground(Color.WHITE);
		filePathLabel = new JLabel("");
		filePathLabel.setForeground(Color.WHITE);
		imagePath = new String("AddAFile.png");
		uploadedImageIcon = new ImageIcon(imagePath);
		outputLabel = new JLabel("Text in the image above: ");
		outputLabel.setForeground(Color.WHITE);
		letterLabel = new JLabel("");
		letterLabel.setForeground(Color.GREEN);


		//Add design and visual changes to GUI
		Font font = new Font("Courier", Font.BOLD,40);
		letterLabel.setFont(font);
		
		add(inputLabel);
		add(filePathLabel);
		add(btnUpload);

		imageLabel = new JLabel("", uploadedImageIcon, JLabel.CENTER);
		add(imageLabel, BorderLayout.CENTER );
		add(outputLabel);
		add(letterLabel);

		setPreferredSize(new Dimension(700, 500));
		setBackground(Color.gray);
	}

	private class TempListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			//Handle upload button action.
			if (event.getSource() == btnUpload) 
			{
				int returnVal = fc.showOpenDialog(ForensicOCRPanel.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) 
				{
                	File file = fc.getSelectedFile(); //get path of selected file
                	fileSize = file.length();

                	if (fileSize < 20000)
                	{

	                	imagePath = file.toString(); //set variable equal to image path
	                	filePathLabel.setText(imagePath); //change GUI label to display new path

	                	uploadedImageIcon = new ImageIcon(imagePath); //set image icon equal to image
	                	Image image = uploadedImageIcon.getImage(); //transform it 
						Image newimg = image.getScaledInstance(600, 400,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
						uploadedImageIcon = new ImageIcon(newimg);  //transform it back
	                	imageLabel.setIcon(uploadedImageIcon); //display new image  

	                	try
	                	{
	    					//////////////////////////////////////////////////////////////////////
	    					//convert to black and white pixels only
	    					//////////////////////////////////////////////////////////////////////
	                		BufferedImage coloredImage = ImageIO.read(file);
	                		BufferedImage blackAndWhite = new BufferedImage(coloredImage.getWidth(),coloredImage.getHeight(),BufferedImage.TYPE_BYTE_BINARY);
	                		Graphics2D graphics = blackAndWhite.createGraphics();
	                		graphics.drawImage(coloredImage, 0, 0, null);
	                		ImageIO.write(blackAndWhite, "png", new File("blackAndWhite.png"));

							//////////////////////////////////////////////////////////////////////
							//resize image
							//////////////////////////////////////////////////////////////////////
	                		int w = 20;
	                		int h = 20;
	                		BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
	                		int a, b;
	                		int ww = blackAndWhite.getWidth();
	                		int hh = blackAndWhite.getHeight();
	                		int[] ys = new int[h];
	                		for (b = 0; b < h; b++)
	                			ys[b] = b * hh / h;
	                		for (a = 0; a < w; a++) 
	                		{
	                			int newA = a * ww / w;
	                			for (b = 0; b < h; b++) 
	                			{
	                				int col = blackAndWhite.getRGB(newA, ys[b]);
	                				img.setRGB(a, b, col);
	                			}
	                		}
	                		blackAndWhite = img;

						    //////////////////////////////////////////////////////////////////////
							//convert the black and white w x h image to byte array
							//////////////////////////////////////////////////////////////////////
	                		byte[][] arrImg = new byte[blackAndWhite.getWidth()][];
	                		for (int x = 0; x < blackAndWhite.getWidth(); x++) 
	                		{
	                			arrImg[x] = new byte[blackAndWhite.getHeight()];

	                			for (int y = 0; y < blackAndWhite.getHeight(); y++) 
	                			{
	                				arrImg[x][y] = (byte)(blackAndWhite.getRGB(x, y) == 0xFFFFFFFF ? 0 : 1);
	                			}
	                		}

							//////////////////////////////////////////////////////////////////////
							//output byte array to a text file
							//////////////////////////////////////////////////////////////////////
	                		FileOutputStream fout = new FileOutputStream(new File(System.getProperty("user.dir"), "inputMap.txt"));
	                		for(int i = 0; i < arrImg.length; i++) 
	                		{ 							
	                			for(int j = 0; j < arrImg[i].length; j++) 
	                			{
	                				fout.write(String.valueOf(arrImg[i][j]).getBytes());
	                			}
	                			fout.write(System.getProperty("line.separator").getBytes());
	                		}
	                		fout.close();

							//////////////////////////////////////////////////////////////////////
							//compare input image text file to each template
							//////////////////////////////////////////////////////////////////////

	                		int[][] inputArr = new int[w][h];
	                		File inputFile = new File("inputMap.txt");
	                		Scanner inputScanner = new Scanner(inputFile);

	                		for (int row = 0; row < h; row++)
	                		{
	                			String line = inputScanner.nextLine();
	                			for (int col = 0; col < w; col++)
	                			{
	                				if(line!=null && line.length()>0) 
	                				{
	                					inputArr[row][col] = line.charAt(col);
	                				}
	                			}
	                		}
	                		inputScanner.close();
							/////////////////////////////////////////////////////////////////////
	                		int currentCounter = 0;
	                		int largestCounter = 0;
	                		char mostLikelyLetter = '0';

	                		for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++)
	                		{
	                			int[][] templateArr = new int[w][h];
	                			File templateFile = new File("Templates/" + alphabet + "Map.txt");
	                			Scanner scanner = new Scanner(templateFile);

	                			for (int row = 0; row < h; row++)
	                			{
	                				String line = scanner.nextLine();
	                				for (int col = 0; col < w; col++)
	                				{
	                					if(line!=null && line.length()>0) 
	                					{	
	                						templateArr[row][col] = line.charAt(col);
	                					}
	                				}
	                			}
	                			scanner.close();
								// DEBUG: System.out.println(Arrays.deepToString(templateArr));
								/////////////////////////////////////////////////////////////////////

	                			currentCounter = 0;
	                			for (int row = 0; row < h; row++)
	                			{
	                				for (int col = 0; col < w; col++)
	                				{
	                					if (inputArr[row][col] == templateArr[row][col])
	                					{
	                						currentCounter++;
	                					}
	                				}
	                			}
	                			if (currentCounter > largestCounter)
	                			{
	                				largestCounter = currentCounter;
	                				mostLikelyLetter = alphabet;
	                			}
	                		}
							letterLabel.setText(String.valueOf(mostLikelyLetter)); //change GUI label to display new path
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						strOutput = "";
						imagePath = file.toString(); //set variable equal to image path
						imageName = file.getName();
						filePathLabel.setText(imagePath); //change GUI label to display new path

						uploadedImageIcon = new ImageIcon(imagePath); //set image icon equal to image
	                	Image image = uploadedImageIcon.getImage(); //transform it 
						Image newimg = image.getScaledInstance(600, 200,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
						uploadedImageIcon = new ImageIcon(newimg);  //transform it back
	                	imageLabel.setIcon(uploadedImageIcon); //display new image  

						Runtime rt = Runtime.getRuntime();
						try
						{
							Process pr = rt.exec("convert " + imagePath + " -crop 7x1@ +repage +adjoin out_%d.png");
						} 
						catch (IOException e) 
						{
							e.printStackTrace();
						}
						try 
						{
							TimeUnit.SECONDS.sleep(3);
						}
						catch (InterruptedException e) 
						{
							e.printStackTrace();
						}
						imagePath = imagePath.replace(imageName, "");

						for (int numFile = 0; numFile < 7; numFile++)
							{
								currentFile = "out_" + numFile + ".png";
								currentPath = imagePath + currentFile;
								//DEBUG: System.out.println("Current Path: " + currentPath);

		                	try
		                	{
		    					//////////////////////////////////////////////////////////////////////
		    					//convert to black and white pixels only
		    					//////////////////////////////////////////////////////////////////////
		                		BufferedImage coloredImage = ImageIO.read(new File(currentPath));
		                		BufferedImage blackAndWhite = new BufferedImage(coloredImage.getWidth(),coloredImage.getHeight(),BufferedImage.TYPE_BYTE_BINARY);
		                		Graphics2D graphics = blackAndWhite.createGraphics();
		                		graphics.drawImage(coloredImage, 0, 0, null);
		                		ImageIO.write(blackAndWhite, "png", new File("blackAndWhite.png"));

								//////////////////////////////////////////////////////////////////////
								//resize image
								//////////////////////////////////////////////////////////////////////
		                		int w = 20;
		                		int h = 20;
		                		BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		                		int a, b;
		                		int ww = blackAndWhite.getWidth();
		                		int hh = blackAndWhite.getHeight();
		                		int[] ys = new int[h];
		                		for (b = 0; b < h; b++)
		                			ys[b] = b * hh / h;
		                		for (a = 0; a < w; a++) 
		                		{
		                			int newA = a * ww / w;
		                			for (b = 0; b < h; b++) 
		                			{
		                				int col = blackAndWhite.getRGB(newA, ys[b]);
		                				img.setRGB(a, b, col);
		                			}
		                		}
		                		blackAndWhite = img;

							    //////////////////////////////////////////////////////////////////////
								//convert the black and white w x h image to byte array
								//////////////////////////////////////////////////////////////////////
		                		byte[][] arrImg = new byte[blackAndWhite.getWidth()][];
		                		for (int x = 0; x < blackAndWhite.getWidth(); x++) 
		                		{
		                			arrImg[x] = new byte[blackAndWhite.getHeight()];

		                			for (int y = 0; y < blackAndWhite.getHeight(); y++) 
		                			{
		                				arrImg[x][y] = (byte)(blackAndWhite.getRGB(x, y) == 0xFFFFFFFF ? 0 : 1);
		                			}
		                		}

								//////////////////////////////////////////////////////////////////////
								//output byte array to a text file
								//////////////////////////////////////////////////////////////////////
		                		FileOutputStream fout = new FileOutputStream(new File(System.getProperty("user.dir"), "inputMap.txt"));
		                		for(int i = 0; i < arrImg.length; i++) 
		                		{ 							
		                			for(int j = 0; j < arrImg[i].length; j++) 
		                			{
		                				fout.write(String.valueOf(arrImg[i][j]).getBytes());
		                			}
		                			fout.write(System.getProperty("line.separator").getBytes());
		                		}
		                		fout.close();

								//////////////////////////////////////////////////////////////////////
								//compare input image text file to each template
								//////////////////////////////////////////////////////////////////////

		                		int[][] inputArr = new int[w][h];
		                		File inputFile = new File("inputMap.txt");
		                		Scanner inputScanner = new Scanner(inputFile);

		                		for (int row = 0; row < h; row++)
		                		{
		                			String line = inputScanner.nextLine();
		                			for (int col = 0; col < w; col++)
		                			{
		                				if(line!=null && line.length()>0) 
		                				{
		                					inputArr[row][col] = line.charAt(col);
		                				}
		                			}
		                		}
		                		inputScanner.close();
								/////////////////////////////////////////////////////////////////////
		                		int currentCounter = 0;
		                		int largestCounter = 0;
		                		char mostLikelyLetter = '0';

		                		for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++)
		                		{
		                			int[][] templateArr = new int[w][h];
		                			File templateFile = new File("Templates/" + alphabet + "Map.txt");
		                			Scanner scanner = new Scanner(templateFile);

		                			for (int row = 0; row < h; row++)
		                			{
		                				String line = scanner.nextLine();
		                				for (int col = 0; col < w; col++)
		                				{
		                					if(line!=null && line.length()>0) 
		                					{	
		                						templateArr[row][col] = line.charAt(col);
		                					}
		                				}
		                			}
		                			scanner.close();
									//DEBUG: System.out.println(Arrays.deepToString(templateArr));
									/////////////////////////////////////////////////////////////////////

		                			currentCounter = 0;
		                			for (int row = 0; row < h; row++)
		                			{
		                				for (int col = 0; col < w; col++)
		                				{
		                					if (inputArr[row][col] == templateArr[row][col])
		                					{
		                						currentCounter++;
		                					}
		                				}
		                			}
		                			if (currentCounter > largestCounter)
		                			{
		                				largestCounter = currentCounter;
		                				mostLikelyLetter = alphabet;
		                			}
		                		}

		                		//Store each letter that is determined to be in the word
		                		strOutput = strOutput + mostLikelyLetter;
						
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
							//Output the resulting word
							letterLabel.setText(String.valueOf(strOutput));
					}
				}
			}
		}
	}
}
