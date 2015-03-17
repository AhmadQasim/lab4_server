package work;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class httpServer {
  public static void main(String args[]) {
    int port;
    ServerSocket servsock;
    try {
      port = Integer.parseInt(args[0]);
    } catch (Exception e) {
      port = 5003;
    }
    try {

      servsock = new ServerSocket(port);

      while (true) {
        Socket socket = servsock.accept();
        try {
          httpRequest request = new httpRequest(socket);
          Thread thread = new Thread(request);
          thread.start();
        } catch (Exception e) {
          System.out.println(e);
        }
      }
    } catch (IOException e) {
      System.out.println(e);
    }
  }
}

class httpRequest implements Runnable {

  Socket sock;
  InputStream in;
  OutputStream out;
  BufferedReader br;

  public httpRequest(Socket socket) throws Exception {
    this.sock = socket;
    this.in = socket.getInputStream();
    this.out = socket.getOutputStream();
    this.br = new BufferedReader(new InputStreamReader(socket
        .getInputStream()));
  }

  public void run() {
    try {
      processRequest();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  private void processRequest() {
	  boolean session_keep = false;
    while (true) {

      String headerLine;
	try {
		headerLine = br.readLine();
		if (headerLine.contains("keep-alive"))
			session_keep=true;
      if ((headerLine.equals("\r\n") || headerLine.equals(""))&&!session_keep)
    	  break;

      StringTokenizer s = new StringTokenizer(headerLine);
      String temp = s.nextToken();
      if (temp.equals("GET") || temp.equals("HEAD")) {

        String fileName = s.nextToken();
        String version = s.nextToken();
        if (!version.contains("1.1")){
        	String statusLine = "HTTP/1.1 505 HTTP Version Not Supported" + "\r\n";
    		try {
    			out.write(statusLine.getBytes());
    			out.write("\r\n".getBytes());
    		} catch (IOException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
        }
        fileName = "." + fileName;

        FileInputStream fis = null;
        boolean fileExists = true;
        try {
          fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
        	fileExists = false;
        }
        String statusLine = null;
        String contentTypeLine = null;
        String contentLengthLine = "error";
        if (fileExists) {
          statusLine = "HTTP/1.1 200 OK" + "\r\n";
          contentTypeLine = "Content-type: " + Type(fileName)+ "\r\n";
          contentLengthLine = "Content-Length: " + (new Integer(fis.available())).toString() + "\r\n";
          out.write(statusLine.getBytes());
          out.write(contentTypeLine.getBytes());
          out.write(contentLengthLine.getBytes());
          out.write("\r\n".getBytes());
        } else {
        	
         statusLine = "HTTP/1.1 404 Not Found" + "\r\n";
         contentTypeLine = "text/html";
         out.write(statusLine.getBytes());
         out.write(contentTypeLine.getBytes());
         out.write("\r\n".getBytes());
        }
        

        // Send the entity body.
        if (!temp.equals("HEAD")){
        if (fileExists) {
          send(fis, out);
          fis.close();
        } else {
        	FileInputStream fis1 = new FileInputStream("404.html");
        	send(fis1, out);
            fis1.close();
            break;
        }

      }
      }
      else if (temp.equals("POST")){
    	  String buff;int length=0;
    	  while((buff =br.readLine())!=null){
    		  if (buff.contains("Content-Length: ")){
    			  buff = buff.substring(16, buff.length());
    			  length = Integer.parseInt(buff);
    		  }
    		  if (buff.equals("")||!session_keep)
    			  break;
    	  }
    	  buff = "";
    	  for (int i =0;i<length;i++){
    		  buff = buff+(char)br.read();
    	  }
    	  StringTokenizer st = new StringTokenizer(buff, "&");
    	  String statusLine = "HTTP/1.1 200 OK" + "\r\n";
    	  String contentTypeLine = "text/html";
    	  out.write(statusLine.getBytes());
    	  out.write(contentTypeLine.getBytes());
    	  out.write("\r\n".getBytes());
    	  out.write(st.nextToken().getBytes());
    	  out.write(st.nextToken().getBytes());
    	  break;
      }
   
	} catch (IOException e1) {
		String statusLine = "HTTP/1.1 502 Bad Gateway" + "\r\n";
		try {
			out.write(statusLine.getBytes());
			out.write("\r\n".getBytes());
		} catch (IOException e11) {
			// TODO Auto-generated catch block
			e11.printStackTrace();
		}
	}
    }

    try {
      out.close();
      br.close();
      sock.close();
    } catch (Exception e) {
    	String statusLine = "HTTP/1.1 502 Bad Gateway" + "\r\n";
		try {
			out.write(statusLine.getBytes());
			out.write("\r\n".getBytes());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
  }

  private static void send(FileInputStream fis, OutputStream os) {

    byte[] buffer = new byte[1024];
    int bytes = 0;

    try {
		while ((bytes = fis.read(buffer)) != -1) {
		  os.write(buffer, 0, bytes);
		}
	} catch (IOException e) {
    	  String statusLine = "HTTP/1.1 500 Internal Server Error" + "\r\n";
  		try {
  			os.write(statusLine.getBytes());
  			os.write("\r\n".getBytes());
  		} catch (IOException e11) {
  			// TODO Auto-generated catch block
  			e11.printStackTrace();
  		}
	}
  }

  private static String Type(String fileName) {
    if (fileName.endsWith(".htm") || fileName.endsWith(".html")
        || fileName.endsWith(".txt")) {
      return "text/html";
    } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
      return "image/jpeg";
    } else if (fileName.endsWith(".gif")) {
      return "image/gif";
    } else {
      return "application/octet-stream";
    }
  }
}
