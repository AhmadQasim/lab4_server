package lab4_server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

public class server {
	ServerSocket listener;
	Socket sock;
	public static void main(String[] args) {
		server serv = new server();
		try {
		serv.listener = new ServerSocket(5000);
		while(true){
			Vector<String> vc = new Vector<String>();
			String msg = null;

				serv.sock = serv.listener.accept();
			InputStream in = serv.sock.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			for (int i = 0;i<5;i++){
				msg=br.readLine();vc.add(msg);
			}
			if (msg!=null){
				StringTokenizer st = new StringTokenizer(vc.firstElement()," ");
				String method = st.nextToken();
				String path = st.nextToken();
				String version = st.nextToken();
				if (method.equals("GET")){
					File file = new File(path);
					if (!file.exists()){
						serv.sendCode(404);
						serv.sock.close();
					}
					else {
						String msgf;
						FileReader fr = new FileReader(path);
						BufferedReader bufferedReader = new BufferedReader(fr);
						BufferedWriter out = new BufferedWriter(new OutputStreamWriter(serv.sock.getOutputStream()));
						out.write("HTTP/1.0 200 OK\r\n");
						while((msgf = bufferedReader.readLine()) != null){
							out.write(msgf);
						}
						out.write("\r\n");
					}
				}
				else if (method.equals("POST")){
					
				}
			}
		}
		} catch (IOException e) {
			System.out.println(e.getCause());
		}
	}
	public void sendCode(int code) throws IOException{
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		out.write("HTTP/1.0 "+code+" Not Found\r\n");out.flush();
		out.close();
	}
}
