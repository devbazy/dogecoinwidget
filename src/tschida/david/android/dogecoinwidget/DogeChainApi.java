package tschida.david.android.dogecoinwidget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public final class DogeChainApi
{
	private static final String TAG = "DogeChainApi";
	private static String baseurl = "http://dogechain.info";
	
	public static double addressbalance(String address)
	{
		return Double.parseDouble(apicall("/chain/CHAIN/q/addressbalance/"
				+ address));
	}
	
	public static String addresstohash(String address)
	{
		return apicall("/chain/CHAIN/q/addresstohash/" + address);
	}
	
	public static String checkaddress(String address)
	{
		return apicall("/chain/CHAIN/q/checkaddress/" + address);
	}
	
	public static String decode_address(String address)
	{
		return apicall("/chain/CHAIN/q/decode_address/" + address);
	}
	
	public static int getblockcount()
	{
		return Integer.parseInt(apicall("/chain/Dogecoin/q/getblockcount"));
	}
	
	public static double getdifficulty()
	{
		return Double.parseDouble(apicall("/chain/Dogecoin/q/getdifficulty"));
	}
	
	public static double getrecievedbyaddress(String address)
	{
		return Double
				.parseDouble(apicall("/chain/CHAIN/q/getreceivedbyaddress/"
						+ address));
	}
	
	public static double getsentbyaddress(String address)
	{
		return Double.parseDouble(apicall("/chain/CHAIN/q/getsentbyaddress/"
				+ address));
	}
	
	public static String hashtoaddress(String hash)
	{
		return apicall("/chain/CHAIN/q/hashtoaddress/" + hash);
	}
	
	public static String nethash()
	{
		return apicall("/chain/Dogecoin/q/nethash");
	}
	
	public static double totalbc()
	{
		return Double.parseDouble(apicall("/chain/Dogecoin/q/totalbc"));
	}
	
	public static String transactions(String hash)
	{
		return apicall("/chain/Dogecoin/q/transactions");
	}
	
	public static String apicall(String myurl)
	{
		InputStream is = null;
		try
		{
			URL url = new URL(baseurl + myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// Starts the query
			conn.connect();
			int response = conn.getResponseCode();
			Log.d(TAG, "The response is: " + response);
			is = conn.getInputStream();
			
			// Convert the InputStream into a string
			String contentAsString = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null)
			{
				contentAsString += line;
			}
			return contentAsString;
			
			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		} finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return "0";
	}
}
