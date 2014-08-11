import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
//EXCEPTIONS
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

public class PictoUserKeyDerivation {

    protected String picFile;//path to user image
	protected BufferedImage image;
	protected int height,width;
	protected int M,N,K=0;
	protected String algorithm;//the encryption algorithm
	
	public PictoUserKeyDerivation(){
		picFile=null;
		image=null;
		height=0;
		width=0;
		M=0;
		N=0;
		K=0;
		algorithm="";
	}
	
	//@picPath The users picture file path
	//@m User input M(between 0 and image height)
	//@n User input N(between 0 and image width)
	//@k The number of pixels to be extracted
	//@alg The encryption algorithm
	public PictoUserKeyDerivation(String picPath, int m, int n, int k, String alg) throws IOException{
		picFile=picPath;
		image=ImageIO.read(new File(picFile));
		M=m;
		N=n;
		K=k;
		height=image.getHeight();
		width=image.getWidth();
		algorithm=alg;
	}

	//Get seed for RNG. The seed will be the combination of the 3 RGB values of the middle pixel. 
	//RNG is needed to select the values(M,N) to determine the key 
	protected long getSeed(){
		long seed=0;
		String strSeed="";
		int pix=image.getRGB(M, N);
		strSeed+=((pix >> 16) & 255)+"";//get red value ,could use 0xFF or 255. OxFF=255 in decimal
		strSeed+=((pix >> 8) & 0xff)+"";//get green value
		strSeed+=((pix) & 0xff)+"";//get blue value
		strSeed=String.format("%09d", Integer.parseInt(strSeed));//make a seed with 9 digits in length
		seed=Long.parseLong(strSeed, 10);
		return seed;
	}
	
//Derives the selected RGB values based on the seed, M and N	
protected int[] deriveRGBKey(int numPixels){
	//The number of pixels to extract.
	int numExtractPixels=numPixels;
	
	SecureRandom sr=new SecureRandom();
	sr.setSeed(getSeed());
	
	//Store the extracted Red, Green, and Blue values for each pixel. 
	int[] rgbValues=new int[numExtractPixels*3];

	//Holds the selected pixel value.
    int pixel=0;    
    
    //First get the pixel at (M,N) and then determine and store the 3 RGB values based off the pixel.
    //M and N are determined by the SecureRNG and the seed. 
    try{
    	for(int index=0;index<numExtractPixels*3;index+=3){
    		pixel = image.getRGB(M, N);//get the pixel. Only uses 8 bits of precision as of now
    		rgbValues[index]=(pixel >> 16) & 255;//get red value ,could use 0xFF or 255. OxFF=255 in decimal
    		rgbValues[index+1]=(pixel >> 8) & 0xff;//get green value
    		rgbValues[index+2]=(pixel) & 0xff;//get blue value
    		M=sr.nextInt(width);
    		N=sr.nextInt(height);
    	}
    }
    catch(Exception e){
    	System.out.println("Pixel extraction error. "+e.getMessage());
    }
    return rgbValues;
}
    //Begin transformation of pixels to key. First we take the bit form of each RGB value taken and concatenate them into one long bit-string.
    //The number of bits  will be K*BPP, where BPP is the number of bits-per-pixel. The bit-string could be the key, but will require a fixed number K. K would have to be around 5 or 11 to get
    //valid key lengths.
    protected String keyToBinaryString(int[] rgbValues){
    	String binStr="";
    	for(int i=0;i<rgbValues.length;i++){
    		String tempPixValue=Integer.toBinaryString(rgbValues[i]);
    		tempPixValue=String.format("%08d", Integer.parseInt(tempPixValue));//pad to make 8 bits for each value
    		binStr+=tempPixValue;
    	}  
    	return binStr;
    }
			
}
