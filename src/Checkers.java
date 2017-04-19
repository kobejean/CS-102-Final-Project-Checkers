import java.awt.Color;
import java.awt.event.KeyEvent;
import java.lang.Math;
import javax.swing.JOptionPane;
import java.util.*;

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
        reset();

        StdDraw.setCanvasSize(_width, _height);  //default is 400 x 400

        // Set the drawing scale to dimentions
        StdDraw.setXscale(0, 8);
        StdDraw.setYscale(0, 8);

        // Set 1 px pen radius
        StdDraw.setPenRadius(1.0 / _width);
    }

    /***************************************************************************
	* METHODS                                                                  *
	***************************************************************************/

    public void reset(){
        _currentPlayer = Player.BLACK;
        _selectedIndex = new CellIndex();
        _redScore = 0;
        _blackScore = 0;
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
    }

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
                        StdDraw.filledCircle(x,y,0.4);
                        break;
                    case RED_KING:
                        StdDraw.setPenColor(Color.RED);
                        StdDraw.filledCircle(x,y,0.4);
                        StdDraw.setPenColor(Color.WHITE);
                        StdDraw.filledCircle(x,y,0.1);
                        break;
                    case BLACK:
                        StdDraw.setPenColor(Color.BLACK);
                        StdDraw.filledCircle(x,y,0.4);
                        break;
                    case BLACK_KING:
                        StdDraw.setPenColor(Color.BLACK);
                        StdDraw.filledCircle(x,y,0.4);
                        StdDraw.setPenColor(Color.WHITE);
                        StdDraw.filledCircle(x,y,0.1);
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
                CellIndex clickedIndex = new CellIndex(i,j);
                cellClicked(clickedIndex);
            }
            _wasMousePressed = _isMousePressed;

            StdDraw.clear();
            draw();
            StdDraw.show();
            StdDraw.pause(shortDelay);

            // check winner
            checkWinner();
        }
    }

    public void nextTurn(){
        _currentPlayer = _currentPlayer.enemy();
    }

    public void checkWinner(){
        if (_blackScore == 0 || _redScore == 0){
            String title = "";
            if (_redScore == 0){
                title = "BLACK WINS!";
            }else if (_blackScore == 0){
                title = "RED WINS!";
            }
            String confirmMsg = "Would you like to play again?";
            int option = JOptionPane.YES_NO_OPTION;
            int reply = JOptionPane.showConfirmDialog(null, confirmMsg, title, option);
            if (reply == JOptionPane.YES_OPTION){
                reset();
            }
        }
    }

    public void updateKings(){
        for (int x = 0; x < 8; x++){
            // Check bottom row for non king red cells
            CellState bottomState = _states[x][0];
            if (bottomState.isRED()){
                _states[x][0] = CellState.RED_KING;
            }
            // Check bottom row for non king black cells
            CellState topState = _states[x][7];
            if (topState.isBLACK()){
                _states[x][7] = CellState.BLACK_KING;
            }
        }
    }

    public void reduceEnemyScore(){
        if (_currentPlayer.isRED()){
            _blackScore--;
        }else if(_currentPlayer.isBLACK()){
            _redScore--;
        }
    }

    private Set<CellIndex> possibleMoves(){
        Set<CellIndex> possibleMoves = new HashSet<CellIndex>();
        if (_selectedIndex.isOnBoard()){
            CellState selectedCellState = getCellState(_selectedIndex);
            if (selectedCellState.isBLACK()){
                possibleMoves.addAll(possibleMovesNE());
                possibleMoves.addAll(possibleMovesNW());
            }else if(selectedCellState.isRED()){
                possibleMoves.addAll(possibleMovesSE());
                possibleMoves.addAll(possibleMovesSW());
            }else if(selectedCellState.isRED_KING() || selectedCellState.isBLACK_KING()){
                possibleMoves.addAll(possibleMovesNE());
                possibleMoves.addAll(possibleMovesSE());
                possibleMoves.addAll(possibleMovesSW());
                possibleMoves.addAll(possibleMovesNW());
            }
        }
        return possibleMoves;
    }

    private Set<CellIndex> possibleMovesNE(){
        Set<CellIndex> possibleMoves = new HashSet<CellIndex>();
        // indices to consider
        CellIndex NE = _selectedIndex.NE(); // north east
        CellIndex DSNE = NE.NE(); // double skip north east
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
        return possibleMoves;
    }

    private Set<CellIndex> possibleMovesSE(){
        Set<CellIndex> possibleMoves = new HashSet<CellIndex>();
        // indices to consider
        CellIndex SE = _selectedIndex.SE(); // north east
        CellIndex DSSE = SE.SE(); // double skip north east
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
        return possibleMoves;
    }

    private Set<CellIndex> possibleMovesSW(){
        Set<CellIndex> possibleMoves = new HashSet<CellIndex>();
        // indices to consider
        CellIndex SW = _selectedIndex.SW(); // north east
        CellIndex DSSW = SW.SW(); // double skip north east
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
        return possibleMoves;
    }

    private Set<CellIndex> possibleMovesNW(){
        Set<CellIndex> possibleMoves = new HashSet<CellIndex>();
        // indices to consider
        CellIndex NW = _selectedIndex.NW(); // north west
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
        return possibleMoves;
    }

    public void cellClicked(CellIndex clickedIndex){
        CellState cellState = getCellState(clickedIndex);
        if(cellState == CellState.NONE){
            for (CellIndex moveIndex : possibleMoves()){
                if (clickedIndex.equals(moveIndex)){
                    // if clicked on a possible move
                    if (Math.abs(_selectedIndex.j-clickedIndex.j) == 2){
                        // if making a capture
                        int capI = (_selectedIndex.i+clickedIndex.i)/2;
                        int capJ = (_selectedIndex.j+clickedIndex.j)/2;
                        CellIndex capIndex = new CellIndex(capI,capJ);
                        // capture
                        setCellState(capIndex, CellState.NONE);
                        reduceEnemyScore();
                    }
                    // move chip
                    setCellState(clickedIndex,getCellState(_selectedIndex));
                    setCellState(_selectedIndex, CellState.NONE);
                    // unselect
                    _selectedIndex = new CellIndex();
                    // add kings
                    updateKings();
                    // next turn
                    nextTurn();
                    break;
                }
            }
        }else if (cellState.belongsTo(_currentPlayer)){
            _selectedIndex = clickedIndex;
        }
    }

    private CellState getCellState(CellIndex c){
        return _states[c.i][c.j];
    }

    private void setCellState(CellIndex c, CellState state){
        _states[c.i][c.j] = state;
    }

    /***************************************************************************
    *                        - CellState Enumeration -                         *
    ***************************************************************************/

    public enum CellState {
        NONE, RED, RED_KING, BLACK, BLACK_KING;
        public boolean isNONE(){
            return this == CellState.NONE;
        }
        public boolean isRED(){
            return this == CellState.RED;
        }
        public boolean isBLACK(){
            return this == CellState.BLACK;
        }
        public boolean isRED_KING(){
            return this == CellState.RED_KING;
        }
        public boolean isBLACK_KING(){
            return this == CellState.BLACK_KING;
        }
        public boolean belongsTo(Player player){
            if (player.isBLACK()){
                return (this.isBLACK() || this.isBLACK_KING());
            }else{
                return (this.isRED() || this.isRED_KING());
            }
        }
    }

    /***************************************************************************
    *                         - Player Enumeration -                           *
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

    /***************************************************************************
    *                           - CellIndex Class -                            *
    ***************************************************************************/

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

        @Override
        public boolean equals(Object object) {
            if (object == this)
                return true;   //If objects equal, is OK
            if (object instanceof CellIndex){
                CellIndex that = (CellIndex)object;
                return (i == that.i) && (j == that.j);
            }
            return false;
        }

        // Idea from effective Java : Item 9
        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + i;
            result = 31 * result + j;
            return result;
        }
    }
}
