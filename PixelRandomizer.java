import java.security.SecureRandom;
import java.util.*;
import java.awt.image.*;
import java.awt.*;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.eclipse.swt.widgets.MessageBox;

public class PixelRandomizer {
	
	protected BufferedImage userImage;
	protected int[][] pixelMNcoordinates;
	protected int numPixels;
	
	
	//@i = The user's image which holds the key.
	//@MN = The (M,N) coordinates which were randomly selected which will be used to derive the binary key.
	protected PixelRandomizer(BufferedImage i, int[][] MN, int numPix){
		userImage=i;
		pixelMNcoordinates=MN;
		numPixels=numPix;
	}
	
	
	/*
	This method replaces the selected pixels, which are to be used to derive the key, with new RGB values.
	The purpose is to make the derived key come from more random RGB values. There is a possibility the selected pixels
	could have many or all similar RGB values, making it easier to guess the encryption key for an attacker.
	*/
	protected void RandomizeImageKeyPixels(){
		SecureRandom secRNG= new SecureRandom();
		Color randomRGB;
		int[] colorHolder=new int[numPixels];
		
		//Generate and store a new RGB value for each pixel
		for(int index=0;index<numPixels;index++){
			randomRGB= new Color(secRNG.nextInt(),secRNG.nextInt(),secRNG.nextInt());
			colorHolder[index]=randomRGB.getRGB();
		}
		
		//Set new pixel value at (M,N) coordinates for the users image
		try
		{
			for(int index=0;index<pixelMNcoordinates.length;index++)
				userImage.setRGB(pixelMNcoordinates[index][0],pixelMNcoordinates[index][1],colorHolder[index]);
		}
		catch(Exception e){
			//Show an error pop-up message
			JOptionPane.showMessageDialog(null, "Error", "Operation aborted. New image not constructed",JOptionPane.ERROR_MESSAGE);
		}
		
			
	}
	}

