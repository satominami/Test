import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


class OthelloClient extends JFrame{
    final static int BLACK = 1;
    final static int WHITE = -1;

    private JTextField tf;
    private JTextArea ta;
    private JLabel label;
    private OthelloCanvas canvas;

	/*����������E�I�u�W�F�N�g�錾 */
	int c; 
	int mycolor; //START�Ŏ󂯎���������̐F��ۑ�
	Socket s;
	InputStream sIn;
	OutputStream sOut;
	BufferedReader br;
	PrintWriter pw;
	String str;
	StringTokenizer stn;
	int[][] board = new int[8][8]; 
	int loop;
	/*�����܂� */

    public OthelloClient() {
	/*����������E�\�P�b�g���Ă� */
	try{
		s = new Socket(InetAddress.getLocalHost(), 25033); //�\�P�b�g���J��
	}catch(IOException ee){
		System.err.println("caught IOException");
		System.exit(1);
	}
	/*�����܂� */
	this.setSize(640,320);
	this.addWindowListener(new WindowAdapter() { //�C�x���g�n���h���[
		public void windowClosing(WindowEvent e)  {
		    /* �E�C���h�E������ꂽ���̏��� */
		    System.exit(0);
		}
	    });	 

	tf = new JTextField(40);
	tf.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    /* �e�L�X�g�t�B�[���h�ɕ��������͂��ꂽ���̏��� */
		    if(tf.getText().equals("quit")){
			System.exit(0);
		    }

		    //�����ɑ��M�����ǉ�
			/*����������E���M */
			try{
				sOut = s.getOutputStream(); //Soket�N���X
				pw = new PrintWriter(new OutputStreamWriter(sOut), true);
				String smsg = tf.getText(); //�ł����܂ꂽ�������i�[
				pw.println(smsg);
				ta.append(smsg);
			}catch(IOException e1){
				System.err.println("Caught IOException");
				System.exit(1);
			}
			/*�����܂� */

		    System.out.println(tf.getText()); //�`���b�g�ɏ�������^�[�~�i���ɕ\�����ꂽ
		    tf.setText(""); //�e�L�X�g�t�B�[���h�̕�����������
		}
	    }
	    );
	ta = new JTextArea(18,40);
	ta.setLineWrap(true);
	ta.setEditable(false);
	label = new JLabel();
	
	JPanel mainp = (JPanel)getContentPane();
	JPanel ep = new JPanel();
	JPanel wp = new JPanel();
	canvas = new OthelloCanvas(s);
	GridLayout gl = new GridLayout(1,2);
	gl.setHgap(5);
	mainp.setLayout(gl);
	ep.setLayout(new BorderLayout());
	ep.add(new JScrollPane(ta),BorderLayout.CENTER);
	ep.add(tf,BorderLayout.SOUTH);
	wp.setLayout(new BorderLayout());
	wp.add(label,BorderLayout.SOUTH);
	wp.add(canvas,BorderLayout.CENTER);
	mainp.add(wp);
	mainp.add(ep);
	this.setVisible(true);

	//��M�����ǉ�
	/*����������E��M */
	try{
		sIn = s.getInputStream(); //�\�P�b�g�̓��̓X�g���[��
		br = new BufferedReader(new InputStreamReader(sIn));
		sOut = s.getOutputStream(); //Soket�N���X
		pw = new PrintWriter(new OutputStreamWriter(sOut), true);
		while(true){ //�����ɉ��
			str = br.readLine();
			System.out.println(str);
			stn = new StringTokenizer(str, " ", false); //�X�y�[�X�ŋ�؂��ēǂ�
			String first = stn.nextToken();
			if(first.equals("BOARD")){ //�P�P���BOARD
				for(int j=0; j<8; j++){ //2�����z��Ɋi�[
					for(int i=0; i<8; i++){
						int koma = Integer.parseInt(stn.nextToken());
						board[i][j] = koma;
					}
				}
				for(int i=0; i<8; i++){ //�o��
					for(int j=0; j<8; j++){
						System.out.print(board[i][j]);
					}
					System.out.println(); //���s
				}
			}else if(first.equals("TURN")){ //�P�P���TURN
				int turn = Integer.parseInt(stn.nextToken());
				if(turn == mycolor){//if(2�P��ځ������̐��l(START�ł��炤))�̂Ƃ�
					pw.println(aiRandom(board, mycolor));
					ta.append(aiRandom(board, mycolor) + "\n");
					// pw.println(aiWeightKoma(board, mycolor));
					// ta.append(aiWeightKoma(board, mycolor) + "\n");
					// pw.println(aiWeightSum(board, mycolor));
					// ta.append(aiWeightSum(board, mycolor) + "\n");
				}else{
					System.out.println("It's not your turn.");
				}
			}else if(first.equals("START")){
				mycolor = Integer.parseInt(stn.nextToken()); //������𐔒l�ɕϊ����Ċi�[
				pw.println("NICK RandomAI");
				ta.append("NICK RandomAI\n");
				// pw.println("NICK WeightKomaAI");
				// ta.append("NICK WeightKomaAI\n");
				// pw.println("NICK WeightSumAI");
				// ta.append("NICK WeightSumAI\n");
				if(mycolor == 1){
					ta.append("BLACK\n");
				}else{
					ta.append("WHITE\n");
				}
			}else if(first.equals("END")){
				System.exit(1);
			}else if(first.equals("CLOSE")){
				ta.append("�ڑ������f����܂���\n");
				System.exit(1);
			}else if(first.equals("ERROR")){
				String error = stn.nextToken();
				if(error.equals("1")){
					ta.append("�������Ⴄ\n");
				}else if(error.equals("2")){
					ta.append("�����͒u���Ȃ�\n");
					pw.println(aiRandom(board, mycolor));
					ta.append(aiRandom(board, mycolor) + "\n");					
				}else if(error.equals("3")){
					ta.append("���Ȃ��̔Ԃł͂Ȃ�\n");
				}else{
					ta.append("�����ł��Ȃ����߂ł�\n");
				}	
			}
		}
	}catch(IOException e2){
		System.err.println("Caught IOException");
		System.exit(1);
	}
	/*�����܂ŁE��M*/

    }

	public String aiRandom(int board[][], int mycolor){
		int x = (int)(Math.random()*8);
		int y = (int)(Math.random()*8);
		String result = ("PUT " + x + " " + y);
		return result;
	}

	public String aiWeightSum(int board[][], int mycolor){
		int weightsum[][] = {{30, -12, 0, -1, -1, 0, -12, 30}, {-12, -15, -3, -3, -3, -3, -15, -12}, {0, -3, 0, -1, -1, 0, -3, 0}, {-1, -3, -1, -1, -1, -1, -3, -1},
		{-1, -3, -1, -1, -1, -1, -3, -1}, {0, -3, 0, -1, -1, 0, -3, 0}, {-12, -15, -3, -3, -3, -3, -15, -12}, {30, -12, 0, -1, -1, 0, -12, 30}}; //8x8�̃}�X�ڂ̏d�v�x�������ŏ��������Ă���
		int resx = -1;
		int resy = -1; //x,y���W
		int posx, posy;
		int max = -200;
		int[][] copy = new int[8][8];
		int[][] copy2 = new int[8][8];

		for(int i=0; i<8; i++){ //board�̃R�s�[�����
			for(int j=0; j<8; j++){
				copy[i][j] = board[i][j];
			}
		}

		ArrayList<ArrayList<Integer>> possibles = new ArrayList<>(); //�u����Ƃ���̍��W�̂ݏW�߂�
		for(int i=0; i<8; i++){
			for(int j=0; j<8; j++){
				ArrayList<Integer> possible = new ArrayList<>();
				if(check(i, j, mycolor, copy)){ //check��true�̂���
					possible.add(i);
					possible.add(j);
					possibles.add(possible);
					// System.out.println(check(i, j, mycolor, copy));
				}
			}
		}
		System.out.println("possibles[0][0],[0][1]:" + (possibles.get(0).get(0)) + (possibles.get(0).get(1)) );
		

		for(int i=0; i<possibles.size(); i++){ //�u����Ƃ���̒�����d�ݍő�𒲂ׂ� 
			for(int j=0; j<8; j++){ //copy2�𖈉�copy�ŏ�����
				for(int k=0; k<8; k++){
					copy2[j][k] = copy[j][k];
				}
			}
			posx = possibles.get(i).get(0);
			posy = possibles.get(i).get(1);
			reverse(posx, posy, mycolor, copy2); //�Ђ�����Ԃ��āAcopy2�Ɋi�[����
			int sum = sumweight(weightsum, copy2, mycolor);
			if( sum >= max){ //�΂̏d�݂̑��a��Ԃ�
				max = sum;
				resx = posx;
				resy = posy;
			}
		}

			String result = ("PUT " + resx + " " + resy);
			return result;
	}

	public String aiWeightKoma(int board[][], int mycolor){ //���̃R�}�̗D�揇�ʂ���
		int weightkoma[][] = {{8, 2, 7, 5, 5, 7, 2, 8}, {2, 1, 3, 3, 3, 3, 1, 2}, {7, 3, 6, 4, 4, 6, 3, 7}, {5, 3, 4, 4, 4, 4, 3, 5},
		{5, 3, 4, 4, 4, 4, 3, 5}, {7, 3, 6, 4, 4, 6, 3, 7}, {2, 1, 3, 3, 3, 3, 1, 2}, {8, 2, 7, 5, 5, 7, 2, 8}}; //8x8�̃}�X�ڂ̏d�v�x�������ŏ��������Ă���
		int resx = -1;
		int resy = -1; //x,y���W
		int posx, posy;
		int max = 0;
		int[][] copy = new int[8][8];

		for(int i=0; i<8; i++){ //board�̃R�s�[�����
			for(int j=0; j<8; j++){
				copy[i][j] = board[i][j];
			}
		}

		ArrayList<ArrayList<Integer>> possibles = new ArrayList<>(); //�u����Ƃ���̍��W�̂ݏW�߂�
		for(int i=0; i<8; i++){
			for(int j=0; j<8; j++){
				ArrayList<Integer> possible = new ArrayList<>();
				if(check(i, j, mycolor, copy)){ //check��true�̂Ƃ��A�������u����
					possible.add(i);
					possible.add(j);
					possibles.add(possible);
					// System.out.println(check(i, j, mycolor, copy));
				}
			}
		}

		for(int i=0; i<possibles.size(); i++){ //�u����Ƃ���̒��ŏd�ݍő�̂��̂�I��
			posx = possibles.get(i).get(0);
			posy = possibles.get(i).get(1);
			if(weightkoma[posy][posx] >= max){
				max = weightkoma[posy][posx];
				resx = posx;
				resy = posy;
			}			
		}
		System.out.print("max:"); //�f�o�b�O�p
		System.out.println(max);
		System.out.println(weightkoma[resy][resx]);
		String result = ("PUT " + resx + " " + resy);
		return result;
	}


	private int sumweight(int weightsum[][], int copy[][], int mycolor){ //�΂̏d�݂̑��a��Ԃ�
		int count = 0;
		for(int i=0; i<8; i++){ //board�̃R�s�[�����
			for(int j=0; j<8; j++){
				if(copy[i][j] == mycolor){
					count = count + weightsum[i][j];
				}
			}
		}
		return count;
	} 


	private boolean check(int x, int y, int mycolor, int copy[][]){ //true��������u����
		int a = ck2(x, y, 1, 1, mycolor, copy), b = ck2(x, y, -1, -1, mycolor, copy), c = ck2(x, y, 1, -1, mycolor, copy),
		d = ck2(x, y, -1, 1, mycolor, copy), e = ck2(x, y, 1, 0, mycolor, copy), f = ck2(x, y, -1, 0, mycolor, copy), 
		g = ck2(x, y, 0, 1, mycolor, copy), h = ck2(x, y, 0, -1, mycolor, copy);

		if (a > 0 || b > 0 || c > 0 || d > 0 || e > 0 || f > 0 || g > 0 || h > 0) {
			return true;
		}else{
			return false;
		}
	}

	private int ck2(int x, int y, int dx, int dy, int mycolor, int copy[][]){ //���Wxy, 8����������dx,dy, �����̐Ft, �Ԃ�l��1�ȏ�Ȃ�u����
        int count = 0;
        if (ck3(x + dx, y + dy) && (copy[y + dy][x + dx] != mycolor) && (copy[y + dy][x + dx] != 0) && (copy[y][x])==0) { //�Փ����I�������ׂ������̐F��0�łȂ����I�������Ƃ�����
            for (int i = 2; ck3(x + i * dx, y + i * dy); i++) {
				if (copy[y + i * dy][x + i * dx] == 0) return 0; //��
                if (copy[y + i * dy][x + i * dx] != mycolor) continue; 
                if (copy[y + i * dy][x + i * dx] == mycolor) {
                    count++;
					break;
                }
            }
			return count;
        } //�ׂ������̐F
		return 0;
	}

	private boolean ck3(int x, int y) { //�Փ��ɂ��邩�ǂ����m���߂�
        return (0 <= x && x < 8 && 0 <= y && y < 8);
    }

	private void reverse(int x, int y, int mycolor, int copy[][]){ //�Ђ�����Ԃ�
		int a = rev2(x, y, 1, 1, mycolor, copy), b = rev2(x, y, -1, -1, mycolor, copy), c = rev2(x, y, 1, -1, mycolor, copy),
		d = rev2(x, y, -1, 1, mycolor, copy), e = rev2(x, y, 1, 0, mycolor, copy), f = rev2(x, y, -1, 0, mycolor, copy), 
		g = rev2(x, y, 0, 1, mycolor, copy), h = rev2(x, y, 0, -1, mycolor, copy);
	}

	private int rev2(int x, int y, int dx, int dy, int mycolor, int copy[][]){ //���Wxy, 8����������dx,dy, �����̐Ft, �Ԃ�l��1�ȏ�Ȃ�u����
        int count = 0;
        if (ck3(x + dx, y + dy) && (copy[y + dy][x + dx] != mycolor) && (copy[y + dy][x + dx] != 0) && (copy[y][x])==0) { //�Փ����I�������ׂ������̐F��0�łȂ����I�������Ƃ�����
            for (int i = 2; ck3(x + i * dx, y + i * dy); i++) {
				if (copy[y + i * dy][x + i * dx] == 0) return 0; //��
                if (copy[y + i * dy][x + i * dx] != mycolor) continue; 
                if (copy[y + i * dy][x + i * dx] == mycolor) {
                    count++;
					for (int j = 0; j < i; j++) {
						copy[y + j * dy][x + j * dx] = mycolor; 
					}
					break;
                }
            }
			return count;
        } //�ׂ������̐F
		return 0;
	}

    public static void main(String args[]) {
	new OthelloClient();
    }
}

class OthelloCanvas extends JPanel {
	OutputStream sOut;
	PrintWriter pw;
    private final static int startx = 20;
    private final static int starty = 10;
    private final static int gap = 30;
    private byte[][] board ={
	{0,0,0,0,0,0,0,0},
	{0,0,0,0,0,0,0,0},
	{0,0,0,0,0,0,0,0},
	{0,0,0,1,-1,0,0,0},
	{0,0,0,-1,1,0,0,0},
	{0,0,0,0,0,0,0,0},
	{0,0,0,0,0,0,0,0},
	{0,0,0,0,0,0,0,0}
    };  //�T���v���f�[�^

    public OthelloCanvas(Socket s){
	this.addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    /* �Ֆڏ�Ń}�E�X���N���b�N���ꂽ���̏��� */
		    Point p = e.getPoint();
		    System.out.println(""+p); //�f�o�b�O�p�\��
			System.out.println(""+p.x);
			System.out.println(""+p.y);
		    //�����ɑ��M�����ǉ�
			try{
				sOut = s.getOutputStream(); //Soket�N���X
				pw = new PrintWriter(new OutputStreamWriter(sOut), true);
				if(p.x >= 20 && p.x <= 260 && p.y >= 10 && p.y<=250){
					int x = (int)((p.x - startx) / gap);
					int y = (int)((p.y - starty) / gap);
					pw.println("PUT " + x + " " + y);
				}
			}catch(IOException e3){
				System.err.println("Caught IOException");
				System.exit(1);
			}
		}
	    });
    }

    public void paintComponent(Graphics g){
	g.setColor(new Color(0,180,0));
	g.fillRect(startx,starty,gap*8,gap*8);

	g.setColor(Color.BLACK);
	for(int i=0;i<9;i++){
	    g.drawLine(startx,starty+i*gap,startx+8*gap,starty+i*gap);
	    g.drawLine(startx+i*gap,starty,startx+i*gap,starty+8*gap);
	}
	for(int i=0;i<8;i++){
	    for(int j=0;j<8;j++){
		if(board[i][j]==OthelloClient.BLACK){
		    g.setColor(Color.BLACK);
		    g.fillOval(startx+gap*i,starty+gap*j,gap,gap);
		}else if(board[i][j]==OthelloClient.WHITE){
		    g.setColor(Color.WHITE);
		    g.fillOval(startx+gap*i,starty+gap*j,gap,gap);
		}
	    }
	}
    }
}