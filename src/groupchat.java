import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class groupchatui extends JFrame implements ActionListener, KeyListener,Runnable{ 
	JButton addfriends=new JButton("��Ӻ���");
	JButton delfriends=new JButton("ɾ������");
	JButton flush=new JButton("ˢ��");
	private DatagramSocket sendSocket,  receiveSocket;
    private DatagramPacket sendPacket,  receivePacket;
    boolean sign=true;//��Ƿ��������Ƿ�Ϊ��
    Thread s;
    String user;
	String password;
	JPanel contentpane;
	JTextArea record=new JTextArea();//�����¼����
	JScrollPane Record=new JScrollPane(record);
	//JScrollBar Jbar= Record.getVerticalScrollBar();
	
	JTextArea sendcontent=new JTextArea("i'm sendcontent");//�������ݽ���
	JScrollPane Sendcontent=new JScrollPane(sendcontent);
	
	JLabel label_online=new JLabel();
	JTextArea online=new JTextArea("");//Ⱥ�ĺ��ѽ���
	JScrollPane Online=new JScrollPane(online);
	
	int num=0;//��������
	String[] ip=new String[100];
	String[] name=new String[100];
	String[] port=new String[100];
	String m_ip;
	String m_port="8000";
	String m_name="ziv";
	String mysql;//��ѯ���
	ResultSet result=null;//��ѯ���
	mysqlmanager mm;
	
	public groupchatui(String usr,String passwd){

		
		try {
			InetAddress addr=InetAddress.getLocalHost();

			m_ip=addr.getHostAddress();
			m_port="8000";
			m_name=addr.getHostName();
		} catch (UnknownHostException e2) {
			// TODO �Զ����ɵ� catch ��
			e2.printStackTrace();
		}
		try {
			receiveSocket=new DatagramSocket(8000);
			s=new Thread(this);
			s.start();
		} catch (SocketException e1) {
			// TODO �Զ����ɵ� catch ��
			e1.printStackTrace();
		}//�����������ݶ˿�
		
		sendcontent.addKeyListener(this);
		user=usr;
		password=passwd;
		
		mysql="select * from chatusr";
		mm=new mysqlmanager(user,password);
		mm.linkdb();//�������ݿ�
		result=mm.queue(mysql);//ִ�в�ѯ
		int i=0;
		try {
			while(result.next()) {
				ip[i]=result.getString(1);
				port[i]=result.getString(2);
				name[i]=result.getString(3);
				online.append("  "+name[i]+"("+ip[i]+")\n");
				i++;
			}
		} catch (SQLException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		num=i;
		contentpane=(JPanel) this.getContentPane();
		contentpane.setLayout(null);
		this.setSize(800, 600);
		this.setLocation(400, 150);
		
		Record.setBounds(5,5,600,400);
		record.setEditable(false);
		record.setLineWrap(true);//�Զ�����
		contentpane.add(Record,null);
		
		//sendcontent.setText(user);
		sendcontent.setEditable(true);
		sendcontent.setLineWrap(true);
		Sendcontent.setBounds(5,420,600,140);
		contentpane.add(Sendcontent,null);
		
		label_online.setText("��ǰȺ������("+(num+1)+")");
		label_online.setBounds(610,5,170,20);
		contentpane.add(label_online,null);
		
		online.append("  "+m_name+"\n");
		online.setEditable(false);
		online.setLineWrap(false);
		Online.setBounds(610,25,170,380);
		contentpane.add(Online,null);

		addfriends.setBounds(650,440,80,20);
		addfriends.addActionListener(this);
		contentpane.add(addfriends);
		
		delfriends.setBounds(650,470,80,20);
		delfriends.addActionListener(this);
		contentpane.add(delfriends);
		
		flush.setBounds(650,410,80,20);
		flush.addActionListener(this);
		contentpane.add(flush);
	}
	public int getlength(byte[] conf) {
		int c=0,i;
		for(i=0;i<250;++i)
			if(conf[i]==10)
				break;
		return i+1;
	}
	public void send(String message) {
			
			String string=m_port; 
			//string+=sendcontent.getText();
			string+=message;
            byte[] databyte = new byte[250];
            try {
				databyte=string.getBytes("GBK");
			} catch (UnsupportedEncodingException e1) {
				// TODO �Զ����ɵ� catch ��
				e1.printStackTrace();
			}
            //string.getBytes(0, string.length(), databyte, 0);
		//���͸��Լ�
		try {
			int len=getlength(databyte);
            DatagramPacket sendPacket = new DatagramPacket(databyte,len, java.net.InetAddress.getByName(m_ip), Integer.parseInt(m_port));
            sendSocket=new DatagramSocket();
            sendSocket.send(sendPacket);
        } catch (IOException ioe) {
            record.append("����ͨ�ų��ִ�����������" + ioe.toString());
        }
		//���͸�������
		for(int ti=0;ti<num;++ti) {
            try {
            	DatagramPacket sendPacket = new DatagramPacket(databyte, string.length(), java.net.InetAddress.getByName(ip[ti]), Integer.parseInt(port[ti]));
            
				sendSocket=new DatagramSocket();
				sendSocket.send(sendPacket);
			} catch (IOException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO �Զ����ɵķ������
		String str=arg0.getActionCommand();
		if(str.equals(addfriends.getText())) {
			addusr addui=new addusr(user,password);
			addui.setVisible(true);
		}
		else if(str.equals(delfriends.getText())) {
			delusr delui=new delusr(user,password);
			delui.setVisible(true);
		}
		else if(str.equals(flush.getText())) {
			online.setText("");
			online.append("  "+m_name+"\n");
			mysql="select * from chatusr";
			result=mm.queue(mysql);//ִ�в�ѯ
			int i=0;
			try {
				while(result.next()) {
					ip[i]=result.getString(1);
					port[i]=result.getString(2);
					name[i]=result.getString(3);
					online.append("  "+name[i]+"("+ip[i]+")\n");
					i++;
				}
			} catch (SQLException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
			num=i;
			label_online.setText("��ǰȺ������("+(num+1)+")");
		}
	}
	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO �Զ����ɵķ������
		if(arg0.getKeyCode()==KeyEvent.VK_ENTER) {
			if(sendcontent.getText().isEmpty()) {
				JOptionPane.showMessageDialog(null, "��������Ϊ��","����",JOptionPane.WARNING_MESSAGE);
				sign=true;
			}
			else
				sign=false;
		}
	}
	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO �Զ����ɵķ������
		if(arg0.getKeyCode()==KeyEvent.VK_ENTER) {
			if(sign) {
				sendcontent.setText("");
				JOptionPane.showMessageDialog(null, "��������Ϊ��","����",JOptionPane.WARNING_MESSAGE);
			}
			else {
				send(sendcontent.getText());
			}
			sendcontent.setText("");
		}
	}
	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO �Զ����ɵķ������
		
	}
	@Override
	public void run() {
		// TODO �Զ����ɵķ������
		while (true) {
			
            try {
                byte buf[] = new byte[250];
                receivePacket = new DatagramPacket(buf, buf.length);
                receiveSocket.receive(receivePacket);
                String t_ip = receivePacket.getAddress().toString().trim();
                t_ip=t_ip.substring(1);//��ȡip
                String t_name="unname";
                Date date=new Date();
                byte[] data = receivePacket.getData();
                String receivedString = new String(data, "GBK");
                String t_port = receivedString.substring(0,4);//��ȡ�˿�
                receivedString=receivedString.substring(4);
                if(t_ip.equals(m_ip)) {
                	t_name=m_name;
                    record.append("���Ա���:\\" + t_ip + "\n�˿�:" + receivePacket.getPort()+"\nʱ��:"+ date.toString());
                    record.append("\n"+t_name+":\t");
                }
                else {
                	int ti=0;
                	for(ti=0;ti<num;++ti) {
                		if(t_ip.equals(ip[ti])) {
                			break;
                		}
                	}
                	if(ti==num) {
                		mysql="Insert Into chatusr Values ('"+t_ip+"','"+Integer.parseInt(t_port)+"','"+t_name+"')";
                		if(mm.add(mysql))
                			System.out.println("����ɹ�");
                    	t_name="unnamed";
                	}
                	else {
                		t_name=name[ti];
                	}
                    record.append("\n��������:\\" + t_ip + "\n�˿�:" + receivePacket.getPort()+"\nʱ��:"+ date.toString());
                    record.append("\n"+t_name+":\t");
                }
                record.append(receivedString+"\n");
            } catch (IOException e) {
                record.append("����ͨ�ų��ִ���,��������" + e.toString());
            }
            record.selectAll();
        }
	}
}

//public class groupchat {
//	public static void main(String[] args) {
//		groupchatui ui=new groupchatui("root","ziv404");
//		ui.setVisible(true);
//		ui.setDefaultCloseOperation(ui.EXIT_ON_CLOSE);
//	}
//}
