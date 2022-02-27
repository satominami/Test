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

	/*ここから加・オブジェクト宣言 */
	int c; 
	int mycolor; //STARTで受け取った自分の色を保存
	Socket s;
	InputStream sIn;
	OutputStream sOut;
	BufferedReader br;
	PrintWriter pw;
	String str;
	StringTokenizer stn;
	int[][] board = new int[8][8]; 
	int loop;
	/*ここまで */

    public OthelloClient() {
	/*ここから加・ソケット立てる */
	try{
		s = new Socket(InetAddress.getLocalHost(), 25033); //ソケットを開く
	}catch(IOException ee){
		System.err.println("caught IOException");
		System.exit(1);
	}
	/*ここまで */
	this.setSize(640,320);
	this.addWindowListener(new WindowAdapter() { //イベントハンドラー
		public void windowClosing(WindowEvent e)  {
		    /* ウインドウが閉じられた時の処理 */
		    System.exit(0);
		}
	    });	 

	tf = new JTextField(40);
	tf.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    /* テキストフィールドに文字が入力された時の処理 */
		    if(tf.getText().equals("quit")){
			System.exit(0);
		    }

		    //ここに送信部分追加
			/*ここから加・送信 */
			try{
				sOut = s.getOutputStream(); //Soketクラス
				pw = new PrintWriter(new OutputStreamWriter(sOut), true);
				String smsg = tf.getText(); //打ち込まれた文字を格納
				pw.println(smsg);
				ta.append(smsg);
			}catch(IOException e1){
				System.err.println("Caught IOException");
				System.exit(1);
			}
			/*ここまで */

		    System.out.println(tf.getText()); //チャットに書いたらターミナルに表示された
		    tf.setText(""); //テキストフィールドの文字を初期化
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

	//受信部分追加
	/*ここから加・受信 */
	try{
		sIn = s.getInputStream(); //ソケットの入力ストリーム
		br = new BufferedReader(new InputStreamReader(sIn));
		sOut = s.getOutputStream(); //Soketクラス
		pw = new PrintWriter(new OutputStreamWriter(sOut), true);
		while(true){ //無限に回る
			str = br.readLine();
			System.out.println(str);
			stn = new StringTokenizer(str, " ", false); //スペースで区切って読む
			String first = stn.nextToken();
			if(first.equals("BOARD")){ //１単語目BOARD
				for(int j=0; j<8; j++){ //2次元配列に格納
					for(int i=0; i<8; i++){
						int koma = Integer.parseInt(stn.nextToken());
						board[i][j] = koma;
					}
				}
				for(int i=0; i<8; i++){ //出力
					for(int j=0; j<8; j++){
						System.out.print(board[i][j]);
					}
					System.out.println(); //改行
				}
			}else if(first.equals("TURN")){ //１単語目TURN
				int turn = Integer.parseInt(stn.nextToken());
				if(turn == mycolor){//if(2単語目＝自分の数値(STARTでもらう))のとき
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
				mycolor = Integer.parseInt(stn.nextToken()); //文字列を数値に変換して格納
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
				ta.append("接続が中断されました\n");
				System.exit(1);
			}else if(first.equals("ERROR")){
				String error = stn.nextToken();
				if(error.equals("1")){
					ta.append("書式が違う\n");
				}else if(error.equals("2")){
					ta.append("そこは置けない\n");
					pw.println(aiRandom(board, mycolor));
					ta.append(aiRandom(board, mycolor) + "\n");					
				}else if(error.equals("3")){
					ta.append("あなたの番ではない\n");
				}else{
					ta.append("処理できない命令です\n");
				}	
			}
		}
	}catch(IOException e2){
		System.err.println("Caught IOException");
		System.exit(1);
	}
	/*ここまで・受信*/

    }

	public String aiRandom(int board[][], int mycolor){
		int x = (int)(Math.random()*8);
		int y = (int)(Math.random()*8);
		String result = ("PUT " + x + " " + y);
		return result;
	}

	public String aiWeightSum(int board[][], int mycolor){
		int weightsum[][] = {{30, -12, 0, -1, -1, 0, -12, 30}, {-12, -15, -3, -3, -3, -3, -15, -12}, {0, -3, 0, -1, -1, 0, -3, 0}, {-1, -3, -1, -1, -1, -1, -3, -1},
		{-1, -3, -1, -1, -1, -1, -3, -1}, {0, -3, 0, -1, -1, 0, -3, 0}, {-12, -15, -3, -3, -3, -3, -15, -12}, {30, -12, 0, -1, -1, 0, -12, 30}}; //8x8のマス目の重要度を自分で初期化しておく
		int resx = -1;
		int resy = -1; //x,y座標
		int posx, posy;
		int max = -200;
		int[][] copy = new int[8][8];
		int[][] copy2 = new int[8][8];

		for(int i=0; i<8; i++){ //boardのコピーを作る
			for(int j=0; j<8; j++){
				copy[i][j] = board[i][j];
			}
		}

		ArrayList<ArrayList<Integer>> possibles = new ArrayList<>(); //置けるところの座標のみ集める
		for(int i=0; i<8; i++){
			for(int j=0; j<8; j++){
				ArrayList<Integer> possible = new ArrayList<>();
				if(check(i, j, mycolor, copy)){ //checkがtrueのもの
					possible.add(i);
					possible.add(j);
					possibles.add(possible);
					// System.out.println(check(i, j, mycolor, copy));
				}
			}
		}
		System.out.println("possibles[0][0],[0][1]:" + (possibles.get(0).get(0)) + (possibles.get(0).get(1)) );
		

		for(int i=0; i<possibles.size(); i++){ //置けるところの中から重み最大を調べる 
			for(int j=0; j<8; j++){ //copy2を毎回copyで初期化
				for(int k=0; k<8; k++){
					copy2[j][k] = copy[j][k];
				}
			}
			posx = possibles.get(i).get(0);
			posy = possibles.get(i).get(1);
			reverse(posx, posy, mycolor, copy2); //ひっくり返して、copy2に格納する
			int sum = sumweight(weightsum, copy2, mycolor);
			if( sum >= max){ //石の重みの総和を返す
				max = sum;
				resx = posx;
				resy = posy;
			}
		}

			String result = ("PUT " + resx + " " + resy);
			return result;
	}

	public String aiWeightKoma(int board[][], int mycolor){ //次のコマの優先順位つける
		int weightkoma[][] = {{8, 2, 7, 5, 5, 7, 2, 8}, {2, 1, 3, 3, 3, 3, 1, 2}, {7, 3, 6, 4, 4, 6, 3, 7}, {5, 3, 4, 4, 4, 4, 3, 5},
		{5, 3, 4, 4, 4, 4, 3, 5}, {7, 3, 6, 4, 4, 6, 3, 7}, {2, 1, 3, 3, 3, 3, 1, 2}, {8, 2, 7, 5, 5, 7, 2, 8}}; //8x8のマス目の重要度を自分で初期化しておく
		int resx = -1;
		int resy = -1; //x,y座標
		int posx, posy;
		int max = 0;
		int[][] copy = new int[8][8];

		for(int i=0; i<8; i++){ //boardのコピーを作る
			for(int j=0; j<8; j++){
				copy[i][j] = board[i][j];
			}
		}

		ArrayList<ArrayList<Integer>> possibles = new ArrayList<>(); //置けるところの座標のみ集める
		for(int i=0; i<8; i++){
			for(int j=0; j<8; j++){
				ArrayList<Integer> possible = new ArrayList<>();
				if(check(i, j, mycolor, copy)){ //checkがtrueのとき、正しく置けた
					possible.add(i);
					possible.add(j);
					possibles.add(possible);
					// System.out.println(check(i, j, mycolor, copy));
				}
			}
		}

		for(int i=0; i<possibles.size(); i++){ //置けるところの中で重み最大のものを選択
			posx = possibles.get(i).get(0);
			posy = possibles.get(i).get(1);
			if(weightkoma[posy][posx] >= max){
				max = weightkoma[posy][posx];
				resx = posx;
				resy = posy;
			}			
		}
		System.out.print("max:"); //デバッグ用
		System.out.println(max);
		System.out.println(weightkoma[resy][resx]);
		String result = ("PUT " + resx + " " + resy);
		return result;
	}


	private int sumweight(int weightsum[][], int copy[][], int mycolor){ //石の重みの総和を返す
		int count = 0;
		for(int i=0; i<8; i++){ //boardのコピーを作る
			for(int j=0; j<8; j++){
				if(copy[i][j] == mycolor){
					count = count + weightsum[i][j];
				}
			}
		}
		return count;
	} 


	private boolean check(int x, int y, int mycolor, int copy[][]){ //trueだったら置ける
		int a = ck2(x, y, 1, 1, mycolor, copy), b = ck2(x, y, -1, -1, mycolor, copy), c = ck2(x, y, 1, -1, mycolor, copy),
		d = ck2(x, y, -1, 1, mycolor, copy), e = ck2(x, y, 1, 0, mycolor, copy), f = ck2(x, y, -1, 0, mycolor, copy), 
		g = ck2(x, y, 0, 1, mycolor, copy), h = ck2(x, y, 0, -1, mycolor, copy);

		if (a > 0 || b > 0 || c > 0 || d > 0 || e > 0 || f > 0 || g > 0 || h > 0) {
			return true;
		}else{
			return false;
		}
	}

	private int ck2(int x, int y, int dx, int dy, int mycolor, int copy[][]){ //座標xy, 8方向を示すdx,dy, 自分の色t, 返り値が1以上なら置ける
        int count = 0;
        if (ck3(x + dx, y + dy) && (copy[y + dy][x + dx] != mycolor) && (copy[y + dy][x + dx] != 0) && (copy[y][x])==0) { //盤内かつ選択した隣が自分の色や0でないかつ選択したとこが空
            for (int i = 2; ck3(x + i * dx, y + i * dy); i++) {
				if (copy[y + i * dy][x + i * dx] == 0) return 0; //空
                if (copy[y + i * dy][x + i * dx] != mycolor) continue; 
                if (copy[y + i * dy][x + i * dx] == mycolor) {
                    count++;
					break;
                }
            }
			return count;
        } //隣が自分の色
		return 0;
	}

	private boolean ck3(int x, int y) { //盤内にあるかどうか確かめる
        return (0 <= x && x < 8 && 0 <= y && y < 8);
    }

	private void reverse(int x, int y, int mycolor, int copy[][]){ //ひっくり返す
		int a = rev2(x, y, 1, 1, mycolor, copy), b = rev2(x, y, -1, -1, mycolor, copy), c = rev2(x, y, 1, -1, mycolor, copy),
		d = rev2(x, y, -1, 1, mycolor, copy), e = rev2(x, y, 1, 0, mycolor, copy), f = rev2(x, y, -1, 0, mycolor, copy), 
		g = rev2(x, y, 0, 1, mycolor, copy), h = rev2(x, y, 0, -1, mycolor, copy);
	}

	private int rev2(int x, int y, int dx, int dy, int mycolor, int copy[][]){ //座標xy, 8方向を示すdx,dy, 自分の色t, 返り値が1以上なら置ける
        int count = 0;
        if (ck3(x + dx, y + dy) && (copy[y + dy][x + dx] != mycolor) && (copy[y + dy][x + dx] != 0) && (copy[y][x])==0) { //盤内かつ選択した隣が自分の色や0でないかつ選択したとこが空
            for (int i = 2; ck3(x + i * dx, y + i * dy); i++) {
				if (copy[y + i * dy][x + i * dx] == 0) return 0; //空
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
        } //隣が自分の色
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
    };  //サンプルデータ

    public OthelloCanvas(Socket s){
	this.addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    /* 盤目上でマウスがクリックされた時の処理 */
		    Point p = e.getPoint();
		    System.out.println(""+p); //デバッグ用表示
			System.out.println(""+p.x);
			System.out.println(""+p.y);
		    //ここに送信部分追加
			try{
				sOut = s.getOutputStream(); //Soketクラス
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