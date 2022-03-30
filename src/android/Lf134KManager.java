package com.intelliacc.alps2Scanner;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import cn.pda.serialport.SerialPort;
import cn.pda.serialport.SerialPort2;
import cn.pda.serialport.Tools2;

public class Lf134KManager {
	public static int Port = 13; //
	public static int Power = SerialPort2.Power_5v;
	public static int BaudRate = 9600;
	private static SerialPort mSerialPort;//
	private static InputStream mInputStream;
	public static int LF = 1004;
	/**
	 * open device
	 */
	public Lf134KManager() {
		try {
			mSerialPort = new SerialPort();
			mSerialPort = new SerialPort(Port, BaudRate, 0);
			switch (Power) {
			case SerialPort2.Power_Scaner:
				mSerialPort.scaner_poweron();
				break;
			case SerialPort2.Power_3v3:
				mSerialPort.power3v3on();
				break;
			case SerialPort2.Power_5v:
				mSerialPort.power_5Von();
				break;
			case SerialPort2.Power_Psam:
				mSerialPort.psam_poweron();
				break;
			case SerialPort2.Power_Rfid:
				mSerialPort.rfid_poweron();
				break;
			}
			mSerialPort.rfid_poweron();
			mInputStream = mSerialPort.getInputStream();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean Close() {

		try {
			mInputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		if (mSerialPort!=null) {
			mSerialPort.close(Port);
		}
		switch (Power) {
		case SerialPort2.Power_Scaner:
			mSerialPort.scaner_poweroff();
			break;
		case SerialPort2.Power_3v3:
			mSerialPort.power_3v3off();
			break;
		case SerialPort2.Power_5v:
			mSerialPort.power_5Voff();
			break;
		case SerialPort2.Power_Psam:
			mSerialPort.psam_poweroff();
			break;
		case SerialPort2.Power_Rfid:
			mSerialPort.rfid_poweroff();
			break;
		}
		mSerialPort.rfid_poweroff();
		return true;
	}


	public void ClearBuffer() {
		try {
			while (mInputStream.available()>0) {
				mInputStream.read(new byte[4906]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public Lf134kDataModel GetData(int timeout) {
		if (mInputStream == null) return null;

		long time = System.currentTimeMillis();
		int size = 0;
		int available;
		byte[] buffer = new byte[128];
		while (System.currentTimeMillis() - time<=timeout) {
			try {
				available = mInputStream.available();
				if (available>=30) {
					size = mInputStream.read(buffer);
//					Log.e("size", size+"");
					break;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
//		0230313030303030303030433930303030303030303030303030307B8403
//		156:000000000016
//		02303030303030303030303030303030303030303030303030303000FF07
//		000:000000000000

//		Log.e("buf", Tools2.Bytes2HexString(buffer, size));
		if(size>=30 && checkByte(buffer)){
			Lf134kDataModel model = getData(buffer);
			return model;
		}
		return null;
	}

	private Lf134kDataModel getData(byte[] buffer) {
		Lf134kDataModel model = new Lf134kDataModel();

		byte[] id = new byte[10];
		byte[] nation  = new byte[4];
		byte[] reserved  = new byte[4];
		byte[] extend  = new byte[6];
		System.arraycopy(buffer, 1, id, 0, 10);
		System.arraycopy(buffer, 11, nation, 0, 4);
		model.DataBlock = (byte) (buffer[15] - 30);
		model.AnamalFlag = (byte) (buffer[16] - 30);
		System.arraycopy(buffer, 17, reserved, 0, 4);
		System.arraycopy(buffer, 21, extend, 0, 6);
		for (int i = 0; i < 10; i++) {
			model.ID[i] = (byte) (id[9-i]);
		}
		for (int i = 0; i < 4; i++) {
			model.Country[i] = (byte) (nation[3-i]);
		}
		for (int i = 0; i < 4; i++) {
			model.Reserved[i] = (byte) (reserved[3-i]);
		}
		for (int i = 0; i < 6; i++) {
			model.Extend[i] = (byte) (extend[5-i]);
		}

		try {
			model.ID = Tools2.HexString2Bytes(new String(model.ID,"ASC-II"));
			model.Country = Tools2.HexString2Bytes(new String(model.Country,"ASC-II"));
			model.Reserved = Tools2.HexString2Bytes(new String(model.Reserved,"ASC-II"));
			model.Extend = Tools2.HexString2Bytes(new String(model.Extend,"ASC-II"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (buffer[29] == 0x03) {
			model.Type = "FDX-B";
		}
		if (buffer[29] == 0x07) {
			model.Type = "HDX";
		}
		if (Tools2.BytesToLong(id)==0&&Tools2.BytesToLong(nation)==0) {
			return null;
		}
		return model;
	}


	private static boolean checkByte(byte[] bs){
		byte sum = 0;
		for (int i = 1; i < 27; i++) {
			sum = (byte) (sum^bs[i]);
		}
		if (bs[0] == 0x02  && bs[27] == sum && bs[27] == ~bs[28]&&(bs[29] == 0x03 || bs[29] == 0x07)) {
			return true;
		}else {
			return false;
		}
	}
}
