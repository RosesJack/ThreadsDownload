package com.example.download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private Context context = this;
	Button button;
	EditText et1;
	TextView tv1;
	String path;
	private final int SUCESS = 0;
	// �߳���
	int threadCount = 3;

	// �̼߳���
	int countEnd = 3;
	int finishThread = 0;
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SUCESS:
				Toast.makeText(context, "���سɹ�", 0).show();
				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		button = (Button) findViewById(R.id.button1);
		et1 = (EditText) findViewById(R.id.et1);
		tv1 = (TextView) findViewById(R.id.tv1);
		button.setOnClickListener(this);
	}

	public void onClick(View v) {
		path = et1.getText().toString().trim();
		switch (v.getId()) {
		case R.id.button1:
			new Thread() {
				public void run() {
					try {
						URL url = new URL(path);
						HttpURLConnection conn = (HttpURLConnection) url
								.openConnection();
						conn.setConnectTimeout(5000);
						conn.setRequestMethod("GET");
						int code = conn.getResponseCode();
						if (code == 200) {
							// ��Ӧ�ɹ�
							int length = conn.getContentLength();
							// �����ܴ�С��������ļ���С��ͬ�Ŀ��ļ�
							RandomAccessFile raf = new RandomAccessFile(
									Environment.getExternalStorageDirectory()
											+ "/" + Util.pathToFilename(path),
									"rw");
							// ���ó���
							raf.setLength(length);
							raf.close();
							int size = length / threadCount;
							for (int threadId = 0; threadId < threadCount; threadId++) {
								int startIndex = threadId * size;
								int endIndex = (threadId + 1) * size - 1;
								if (threadId == threadCount - 1) {
									endIndex = length - 1;
								}
								// �����������߳�
								DownLoadThread downLoadThread = new DownLoadThread(
										startIndex, endIndex, threadId, path,
										context);
								downLoadThread
										.setFinishCallback(new FinishCallback() {

											@Override
											public void onFinish() {
												// TODO Auto-generated method
												// stub
												finishThread++;
												if (finishThread == 3) {
													System.out
															.println("xiazai jiee shu ...");
													for (int i = 0; i < threadCount; i++) {
														String pathName = Environment
																.getExternalStorageDirectory()
																.getAbsolutePath()
																+ "/"
																+ i
																+ ".position";
														File file = new File(
																pathName);
														System.out
																.println("·����"+pathName);
														file.delete();
													}
													Message message = Message
															.obtain();
													message.what = SUCESS;
													handler.sendMessage(message);
												}
											}
										});
								downLoadThread.start();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}.start();
			break;
		default:
			break;
		}
	}
}

interface FinishCallback {
	void onFinish();
}

class DownLoadThread extends Thread {
	private FinishCallback finishCallback;

	public void setFinishCallback(FinishCallback finishCallback) {
		this.finishCallback = finishCallback;
	}

	// ��ʼλ��
	private int startIndex;
	// ����λ��
	private int endIndex;
	// �߳�Id
	private int threadId;
	// ���ص�ַ
	private String path;
	// ������
	private Context context;

	// �߳�
	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	public int getThreadId() {
		return threadId;
	}

	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public DownLoadThread(int startIndex, int endIndex, int threadId,
			String path, Context context) {
		super();
		this.endIndex = endIndex;
		this.startIndex = startIndex;
		this.path = path;
		this.threadId = threadId;
		this.context = context;
		System.out.println(threadId + "�߳̿�ʼ���أ���Χ��->" + startIndex + "~"
				+ endIndex);
	}

	// ��дrun����
	@Override
	public void run() {
		try {// ��¼λ��
			int count = startIndex;
			String filePath = Environment.getExternalStorageDirectory() + "/"
					+ threadId + ".position";
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			File file = new File(Environment.getExternalStorageDirectory()
					+ "/" + threadId + ".position");
			if (file.exists() && file.length() > 0) {
				// λ���ļ�����
				InputStream is = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						is, "gbk"));
				String line = null;
				String result = "";
				while ((line = br.readLine()) != null) {
					result += line;
				}
				System.out.println("���ڽ��ȣ��������:___________________" + result);
				System.out.println("λ���ļ����:" + result);
				br.close();
				count = Integer.parseInt(result);
				startIndex = count;
			}
			conn.setRequestProperty("Range", "bytes=" + startIndex + "-"
					+ endIndex);
			InputStream is = conn.getInputStream();
			// ����ָ��λ�ÿ�ʼ�ͽ������ļ�
			RandomAccessFile raf = new RandomAccessFile(
					Environment.getExternalStorageDirectory() + "/"
							+ Util.pathToFilename(path), "rw");
			raf.seek(startIndex);
			int len;
			byte[] b = new byte[1024];
			while ((len = is.read(b)) != -1) {
				raf.write(b, 0, len);
				count += len;
				// ����newһ��File
				File files = new File(Environment.getExternalStorageDirectory()
						+ "/" + threadId + ".position");
				// �����洢��Ӳ�� �ٶȽ���
				RandomAccessFile raf2 = new RandomAccessFile(files, "rwd");
				// д���ļ�
				// OutputStream os = new FileOutputStream(files);
				raf2.write(String.valueOf(count).getBytes("gbk"));
				raf2.close();
			}
			raf.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("�߳�" + threadId + "�߳����ؽ���");

		finishCallback.onFinish();
		// ��˾���������߳��н���
		// Toast.makeText(context, "�߳�" + threadId + "�������", 0).show();
	}
}

// ���ݵ�ַ�õ��ļ���
class Util {
	public static String pathToFilename(String path) {
		int len = -1;
		len = path.lastIndexOf("/");
		if (len != -1) {
			path = path.substring(len + 1);
		}
		return path;
	}
}