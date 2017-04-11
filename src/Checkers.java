import java.awt.Color;
import java.lang.Math;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.JOptionPane;

public class Checkers {
    private int _width = 400, _height = 400;
    private CellState[][] _states  = new CellState[8][8];
    private CellIndex _selectedIndex = new CellIndex(); // selected chip index
    private boolean _isMousePressed = false;
    private boolean _wasMousePressed = false;
    private Player _currentPlayer = Player.BLACK;
    private int _redScore = 0;
    private int _blackScore = 0;
    /***************************************************************************
	 * CONSTRUCTORS                                                            *
	 **************************************************************************/

    public Checkers(){
        // Set up cell states
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                boolean isOnLightGrey = ((i+j)%2) == 0;
                boolean isOnTop3Rows = j > 4;
                boolean isOnBottom3Rows = j < 3;
                if (isOnLightGrey && isOnTop3Rows){
                    _states[i][j] = CellState.RED;
                    _redScore++;
                }else if (isOnLightGrey && isOnBottom3Rows){
                    _states[i][j] = CellState.BLACK;
                    _blackScore++;
                }else{ // Set state to NONE
                    _states[i][j] = CellState.NONE;
                }
            }
        }
        // _states[5][3] = CellState.RED;
        // _states[2][4] = CellState.BLACK;


        StdDraw.setCanvasSize(_width, _height);  //default is 400 x 400

        // Set the drawing scale to dimentions
        StdDraw.setXscale(0, 8);
        StdDraw.setYscale(0, 8);

        // Set 1 px pen radius
        StdDraw.setPenRadius(1.0 / _width);
    }

    /***************************************************************************
	 * METHODS                                                                 *
	 **************************************************************************/

    public void draw(){
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                double x = i + 0.5, y = j + 0.5, r = 0.5;
                // draw background
                if ((i+j)%2 == 0){
                    StdDraw.setPenColor(Color.LIGHT_GRAY);
                    StdDraw.filledSquare(x,y,r);
                }else{
                    assert (i+j)%2 == 1;
                    StdDraw.setPenColor(Color.DARK_GRAY);
                    StdDraw.filledSquare(x,y,r);
                }

                // draw chip
                CellState state = _states[i][j];
                switch(state){
                    case NONE:
                        break;
                    case RED:
                        StdDraw.setPenColor(Color.RED);
                        StdDraw.filledCircle(x,y,r-0.1);
                        break;
                    case BLACK:
                        StdDraw.setPenColor(Color.BLACK);
                        StdDraw.filledCircle(x,y,r-0.1);
                        break;
                }


            }
        }

        // Draw selection ring
        StdDraw.setPenRadius(2.5 / _width);
        double x = _selectedIndex.i + 0.5;
        double y = _selectedIndex.j + 0.5;
        StdDraw.setPenColor(Color.YELLOW);
        StdDraw.circle(x,y,0.4);

        // Draw possible moves
        for (CellIndex c : possibleMoves()){
            double cx = c.i + 0.5;
            double cy = c.j + 0.5;
            StdDraw.setPenColor(Color.YELLOW);
            StdDraw.square(cx,cy,0.5);
        }
    }

    public void reduceEnemyScore(){
        if (_currentPlayer == Player.RED){
            _blackScore--;
            if (_blackScore == 0){
                String title = "RED WINS!";
                String confirmMsg = "Would you like to play again?";
                int option = JOptionPane.YES_NO_OPTION;
                int reply = JOptionPane.showConfirmDialog(null, confirmMsg, title, option);
                if (reply == JOptionPane.YES_OPTION){
                    // reset
                }
            }
        }else if(_currentPlayer == Player.BLACK){
            _redScore--;
            if (_redScore == 0){
                String title = "BLACK WINS!";
                String confirmMsg = "Would you like to play again?";
                int option = JOptionPane.YES_NO_OPTION;
                int reply = JOptionPane.showConfirmDialog(null, confirmMsg, title, option);
                if (reply == JOptionPane.YES_OPTION){
                    // reset
                }
            }
        }
    }

    public void nextTurn(){
        if (_currentPlayer == Player.RED){
            _currentPlayer = Player.BLACK;
        }else if(_currentPlayer == Player.BLACK){
            _currentPlayer = Player.RED;
        }
    }

    public void cellClicked(int i, int j){
        CellState cellState = _states[i][j];
        switch(cellState){
            case NONE:
                for (CellIndex c : possibleMoves()){
                    if (c.i == i && c.j == j){
                        // if clicked on a possible move
                        if (Math.abs(_selectedIndex.j-c.j) == 2){
                            // if making a capture
                            int capI = (_selectedIndex.i+c.i)/2;
                            int capJ = (_selectedIndex.j+c.j)/2;
                            CellIndex capIndex = new CellIndex(capI,capJ);
                            // capture
                            setCellState(capIndex, CellState.NONE);
                            reduceEnemyScore();
                        }
                        // move chip
                        setCellState(c,getCellState(_selectedIndex));
                        setCellState(_selectedIndex, CellState.NONE);
                        // unselect
                        _selectedIndex = new CellIndex();
                        // next turn
                        nextTurn();
                        break;
                    }
                }
                break;
            case RED:
                if (_currentPlayer == Player.RED){
                    _selectedIndex.i = i;
                    _selectedIndex.j = j;
                }
                break;
            case BLACK:
                if (_currentPlayer == Player.BLACK){
                    _selectedIndex.i = i;
                    _selectedIndex.j = j;
                }
                break;
        }
    }

    private Set<CellIndex> possibleMoves(){
        Set<CellIndex> possibleMoves = new HashSet<CellIndex>();
        if (!_selectedIndex.isOutOfBounds()){
            if (_currentPlayer.isBLACK()){
                // indices to consider
                CellIndex NE = _selectedIndex.NE(); // north east
                CellIndex NW = _selectedIndex.NW(); // north west
                CellIndex DSNE = NE.NE(); // double skip north east
                CellIndex DSNW = NW.NW(); // double skip north west
                if (NW.isOnBoard()){
                    // north east is still on the board
                    CellState stateNW = getCellState(NW);
                    if (stateNW.isNONE()){
                        // north west is empty we can go there
                        possibleMoves.add(NW);
                    }else if (stateNW.belongsTo(_currentPlayer.enemy())){
                        // north west is enemy so see if we can capture
                        if (!DSNW.isOutOfBounds())
                            // double skip is still on the board
                            if (getCellState(DSNW).isNONE())
                                // double skip is empty so we can make capture
                                possibleMoves.add(DSNW);
                    }
                }
                if (NE.isOnBoard()){
                    // north east is still on the board
                    CellState stateNE = getCellState(NE);
                    if (stateNE.isNONE()){
                        // north east is empty we can go there
                        possibleMoves.add(NE);
                    }else if (stateNE.belongsTo(_currentPlayer.enemy())){
                        // north east is enemy so see if we can capture
                        if (!DSNE.isOutOfBounds())
                            // double skip is still on the board
                            if (getCellState(DSNE).isNONE())
                                // double skip is empty so we can make capture
                                possibleMoves.add(DSNE);
                    }
                }
            }else if(_currentPlayer.isRED()){
                // indices to consider
                CellIndex SE = _selectedIndex.SE(); // south east
                CellIndex SW = _selectedIndex.SW(); // south west
                CellIndex DSSE = SE.SE(); // double skip south east
                CellIndex DSSW = SW.SW(); // double skip south west

                if (SW.isOnBoard()){
                    // south west is still on the board
                    CellState stateSW = getCellState(SW);
                    if (stateSW.isNONE()){
                        // south west is empty we can go there
                        possibleMoves.add(SW);
                    }else if (stateSW.belongsTo(_currentPlayer.enemy())){
                        // south west is enemy so see if we can capture
                        if (!DSSW.isOutOfBounds())
                            // double skip is still on the board
                            if (getCellState(DSSW).isNONE())
                                // double skip is empty so we can make capture
                                possibleMoves.add(DSSW);
                    }
                }
                if (SE.isOnBoard()){
                    // south east is still on the board
                    CellState stateSE = getCellState(SE);
                    if (stateSE.isNONE()){
                        // south east is empty we can go there
                        possibleMoves.add(SE);
                    }else if (stateSE.belongsTo(_currentPlayer.enemy())){
                        // south east is enemy so see if we can capture
                        if (DSSE.isOnBoard())
                            // double skip is still on the board
                            if (getCellState(DSSE).isNONE())
                                // double skip is empty so we can make capture
                                possibleMoves.add(DSSE);
                    }
                }
            }
        }
        return possibleMoves;
    }

    private CellState getCellState(CellIndex c){
        return _states[c.i][c.j];
    }

    private void setCellState(CellIndex c, CellState state){
        _states[c.i][c.j] = state;
    }

    public void run(){
        // control when to show to save running time
        StdDraw.enableDoubleBuffering();

        int shortDelay = 20;
        int normalDelay = 100;

        while (true){
            _isMousePressed = StdDraw.mousePressed();
            if (!_wasMousePressed && _isMousePressed){
                // new click just happened
                int i = (int) Math.floor(StdDraw.mouseX());
                int j = (int) Math.floor(StdDraw.mouseY());
                cellClicked(i, j);
            }
            _wasMousePressed = _isMousePressed;

            StdDraw.clear();
            draw();
            StdDraw.show();
            StdDraw.pause(shortDelay);
        }
    }

    /***************************************************************************
    *                       - CellState ENUMERATION -                          *
    ***************************************************************************/

    public enum CellState {
        NONE, RED, BLACK;
        public boolean isNONE(){
            return this == CellState.NONE;
        }
        public boolean isRED(){
            return this == CellState.RED;
        }
        public boolean isBLACK(){
            return this == CellState.BLACK;
        }
        public boolean belongsTo(Player player){
            if (player.isBLACK()){
                return this.isBLACK();
            }else{
                return this.isRED();
            }
        }
    }

    /***************************************************************************
    *                         - Player ENUMERATION -                           *
    ***************************************************************************/

    public enum Player {
        RED, BLACK;
        public boolean isRED(){
            return this == Player.RED;
        }
        public boolean isBLACK(){
            return this == Player.BLACK;
        }
        public Player enemy(){
            if (this.isBLACK()){
                return Player.RED;
            }else{
                return Player.BLACK;
            }
        }
    }

    public class CellIndex {
        int i = -1, j = -1;
        public CellIndex(int i, int j){
            this.i = i;
            this.j = j;
        }

        public CellIndex(){
            this.i = -1;
            this.j = -1;
        }

        public void set(int i, int j){
            this.i = i;
            this.j = j;
        }

        public boolean isOutOfBounds(){
            return i < 0 || i >= 8 || j < 0 || j >= 8;
        }

        public boolean isOnBoard(){
            return !isOutOfBounds();
        }

        public CellIndex NE(){
            return new CellIndex(i + 1, j + 1);
        }

        public CellIndex SE(){
            return new CellIndex(i + 1, j - 1);
        }

        public CellIndex SW(){
            return new CellIndex(i - 1, j - 1);
        }

        public CellIndex NW(){
            return new CellIndex(i - 1, j + 1);
        }
    }
}
