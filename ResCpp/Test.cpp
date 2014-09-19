//Test.cpp

#include "Rijndael.h"
#include <String>;
#include <iostream>;
#include <fstream>;
#include <bitset>;
#include<random>;
#include <cstdio>;
#include<conio.h>;
#include<C:\\Users\\RG\\Desktop\\ResCpp\\CImg-1.5.9\\CImg.h>
//#include <gdiplus.h>
#include<windows.h>
#include <wmmintrin.h>
#include <stdlib.h>
#include<iomanip>


using namespace std;
//using namespace Gdiplus;
using namespace cimg_library;

#ifndef cimg_imagepath
#define cimg_imagepath "img/"
#endif
using namespace std;

//Function to convert unsigned char to string of length 2
void Char2Hex(unsigned char ch, char* szHex)
{
	unsigned char byte[2];
	byte[0] = ch/16;
	byte[1] = ch%16;
	for(int i=0; i<2; i++)
	{
		if(byte[i] >= 0 && byte[i] <= 9)
			szHex[i] = '0' + byte[i];
		else
			szHex[i] = 'A' + byte[i] - 10;
	}
	szHex[2] = 0;
}

//Function to convert string of length 2 to unsigned char
void Hex2Char(char const* szHex, unsigned char& rch)
{
	rch = 0;
	for(int i=0; i<2; i++)
	{
		if(*(szHex + i) >='0' && *(szHex + i) <= '9')
			rch = (rch << 4) + (*(szHex + i) - '0');
		else if(*(szHex + i) >='A' && *(szHex + i) <= 'F')
			rch = (rch << 4) + (*(szHex + i) - 'A' + 10);
		else
			break;
	}
}    

//Function to convert string of unsigned chars to string of chars
void CharStr2HexStr(unsigned char const* pucCharStr, char* pszHexStr, int iSize)
{
	int i;
	char szHex[3];
	pszHexStr[0] = 0;
	for(i=0; i<iSize; i++)
	{
		Char2Hex(pucCharStr[i], szHex);
		strcat(pszHexStr, szHex);
	}
}

//Function to convert string of chars to string of unsigned chars
void HexStr2CharStr(char const* pszHexStr, unsigned char* pucCharStr, int iSize)
{
	int i;
	unsigned char ch;
	for(i=0; i<iSize; i++)
	{
		Hex2Char(pszHexStr+2*i, ch);
		pucCharStr[i] = ch;
	}
}
//Input: Passphrase, bitmap picture and plain text message
//Output: A cipher text encrypted with AES 256 using passphrase and picture
//The passphrase as of now must be 22 characters in length and only the first 22 will be used. It cannot be lower or less.
//Be sure to use a bitmap image. The passphrase will be used to generate random numbers to be used as X,Y coordinates for the image. Pixels
//will be extracted at the XY coordinates, their RGB values converted to hexadecimal and concatenated together to form a 256 bit key or 32 byte key.
//Purpose of the key derivation function is to allow a faster key derivation time while hoping to reduce computations compared with PBKDF2 algorithm.
//Because our KDF is faster, I reccomend you should change at least your passphrase every 6 months. 
int main()
{
	
   char hexBuff[4];
   string passPhrase;
   string pic="";
   const int NUMPIXELS=11;
   //Holds each character for passphrase
   char seeds[1024];
   //Holds extracted RGB values for key
   int rgbKeyDecimal[NUMPIXELS*3];
   //Holds the xy,coordinates. First 2 elements are XY pairs, the next 2 elements are the 
   //second XY pairs and so on.
   int xy[NUMPIXELS*2];
   string key="";
   string message="";
   default_random_engine generator;

   try{
	   cout<<"Enter a message to encrypt:"<<endl;
	   getline(cin,message);
   }
   catch(exception e){
		cout<<"Error with message."<<endl;
   }

   try{
		cout << "Input PassPhrase:" << endl; 
		getline(cin,passPhrase);
   }
   catch(exception e){
	   cout<< "Please use a different passphrase"<<endl;
   }

   strncpy_s(seeds, passPhrase.c_str(), sizeof(seeds)); //copy the passPhrase to a char array
  
   

    for(int x=0;x<22;x++){
		   //put the first character in the passphrase as the seed. Generate that random number. 
		   uniform_int_distribution<int> distribution(0,seeds[x]);
		   xy[x]=distribution(generator);
	}

	try{
      cout<<"Enter the location of your BMP picture: Remember to use 2 backslashes instead of 1"<<endl;
	  cin>>pic;
	  CImg<unsigned char> src(pic.c_str());
	
	  //Get RGB values for each pixel at X,Y. pos is equal to x coordinate and pos+1 is the y coordinate	
	  int pos=0;
	  for(int index=0;index<NUMPIXELS*3;index+=3){
		 rgbKeyDecimal[index+0]=(int)src(xy[pos],xy[pos+1],0,0); //red.
		 rgbKeyDecimal[index+1]=(int)src(xy[pos],xy[pos+1],0,1); //green
		 rgbKeyDecimal[index+2]=(int)src(xy[pos],xy[pos+1],0,2); //blue
		 pos+=2;
	  }

	 //convert the key to hexadecimal format
	 for(int i=0;i<NUMPIXELS*3;i++){
		key+=itoa (rgbKeyDecimal[i],hexBuff,16);
	 }

	 //Use only the first 32 bytes of the passphrase. 
	 key=key.substr(0,31);
	}
	catch(exception e){
		cout<<e.what()<<endl;
	}
		try
			{
				//Start AES Encryption
				CRijndael oRijndael;
				//Key will be 32 bytes, or 256 bits.
				oRijndael.MakeKey(key.c_str(), "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0", 32, 32);
				char szDataIn[1024];//the plaintext
				for(int i=0;i<message.size();i++){
					szDataIn[i]=message[i];
				}
				
				
				char szDataOut[33] = "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0"; //the cipher text output
				oRijndael.EncryptBlock(szDataIn, szDataOut);
				
				memset(szDataIn, 0, 16);
				oRijndael.DecryptBlock(szDataOut, szDataIn);
				
				cout<<"Plaintext: "<<endl;
				for(int i=0;i<message.size();i++){
					cout<<szDataIn[i];
				}
				cout<<" \nCipherText:"<<szDataOut<<endl;
				cout<<"Enter anything to exit:"<<endl;
				getline(cin,message);
			}
			catch(exception e)
			{
				cout << e.what() << endl;
			}
	

	return 0;
};