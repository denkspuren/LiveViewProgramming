package lvp.views;

import lvp.Clerk;
import lvp.Server;

public class TicTacToe implements Clerk {
    public final String ID;
    public final int width, height;
    final String libPath = "views/tictactoe/tictactoe.js";
    Server server;
    
    int[] fields = {0,0,0,0,0,0,0,0,0};
    int turn = 1;

    public TicTacToe(Server server, int width, int height) {
        this.server = server;
        this.width  = Math.max(1, Math.abs(width));  // width is at least of size 1
        this.height = Math.max(1, Math.abs(height)); // height is at least of size 1
        Clerk.load(server, libPath);
        ID = Clerk.getHashID(this);

        Clerk.write(server, "<canvas id='tttCanvas" + ID + "' width='" + this.width + "' height='" + this.height + "' style='border:1px solid #000;'></canvas>");
        Clerk.script(server, "const ttt" + ID + " = new TicTacToe(document.getElementById('tttCanvas" + ID + "'), 'ttt" + ID + "');");
        
        this.server.createResponseContext("/ttt" + ID, response -> {
            int i = Integer.parseInt(response);
            if (i >= 0 && i < 9) {
                move(i);
                int[] winnerPos = getWinnerPos();
                if (winnerPos.length == 3) {
                    this.sendWinPosition(winnerPos[0], winnerPos[2]);
                }
            }
        });
    }

    public TicTacToe(Server server) { this(server, 500, 500); }
    public TicTacToe(int width, int height) { this(Clerk.serve(), width, height); }
    public TicTacToe() { this(Clerk.serve());}

    public int[] getWinnerPos() {
        /*
            code
         */
        return new int[0];
    }

    public TicTacToe sendWinPosition(int start, int end) {
        Clerk.call(server, "ttt" + ID + ".showWinner(" + start + ", " + end + ")");
        return this;
    }

    public TicTacToe move(int position) {
        if (fields[position] == 0) {
            fields[position] = turn;
            Clerk.call(server, "ttt" + ID + ".drawToken(" + (turn == 1) + ", " + position + ")");
            turn = -turn;            
        }
        return this;
    }
}