/* Name: ComputerPlayer
 * Author: Devon McGrath
 * Description: This class represents a computer player which can update the
 * game state without user interaction.
 */

package src.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import src.logic.MoveGenerator;

/**
 * The {@code ComputerPlayer} class represents a computer player and updates
 * the board based on a model.
 */
public class MinMaxPlayer extends ComputerPlayer {

	protected boolean player;
	
	protected StateSet transpositionTableMax, transpositionTableMin;
	
	public MinMaxPlayer(boolean joueur) {
		this.player = joueur;
		this.transpositionTableMax = new StateSet();
		this.transpositionTableMin = new StateSet();
	}
	@Override
	public boolean isHuman() {
		return false;
	}
	
	/** 
	 * Computes the minimum value of all of the minimax values computed at a higher depth
	 * @return int The computed value
	 * @param game The current state
	 * @param depth The maximal depth of the game searching tree
	 * */
	public int minValue(Game game, int depth) {
		if (game.isGameOver()) {
			return (game.isP2Turn() == this.player) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
		}
		
		if (depth == 0) {
			if (this.player) {
				this.transpositionTableMin.add(game, game.goodHeuristic(!this.player));
				return game.goodHeuristic(!this.player);
			}
			else {
				this.transpositionTableMax.add(game, game.goodHeuristic(this.player));
				return game.goodHeuristic(this.player);
			}
		}
		
		int v = Integer.MAX_VALUE;
		List<Move> moves = this.getMoves(game);
		Game gameCopy;
		int startIndex, endIndex;
		
		for (Move currentMove : moves) {
			gameCopy = game.copy();
			startIndex = currentMove.getStartIndex();
			endIndex = currentMove.getEndIndex();
				
			if (gameCopy.move(startIndex, endIndex)) {
				
				if (gameCopy.isP2Turn()) {
					v = minValue(gameCopy, depth - 1);
				}
				
				if (this.transpositionTableMax.getValue(gameCopy) != null) {
					v = Math.min(v, this.transpositionTableMax.getValue(gameCopy).intValue());
				}
				else {
					int max_value = maxValue(gameCopy, depth - 1);
					this.transpositionTableMax.add(gameCopy, max_value);
					v = Math.min(v, max_value);
				}
			}
		}
		this.transpositionTableMin.add(game, v);
		return v;
	}
	
	/** 
	 * Computes the maximum value of all of the minimax values computed at a higher depth 
	 * @return int The computed value
	 * @param game The current state
	 * @param depth The maximal depth of the game searching tree
	 * */
	public int maxValue(Game game, int depth) {
		if (game.isGameOver()) {
			return (game.isP2Turn() == this.player) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
		}
		
		if (depth == 0) {
			if (this.player) {
				this.transpositionTableMin.add(game, game.goodHeuristic(!this.player));
				return game.goodHeuristic(!this.player);
			}
			else {
				this.transpositionTableMax.add(game, game.goodHeuristic(this.player));
				return game.goodHeuristic(this.player);
			}
		}
		
		int v = Integer.MIN_VALUE;
		List<Move> moves = this.getMoves(game);
		Game gameCopy;
		int startIndex, endIndex;
		
		for (Move currentMove : moves) {
			gameCopy = game.copy();
			startIndex = currentMove.getStartIndex();
			endIndex = currentMove.getEndIndex();
			if (gameCopy.move(startIndex, endIndex)) {
				
				if (!gameCopy.isP2Turn()) {
					v = maxValue(gameCopy, depth - 1);
				}
				if (this.transpositionTableMin.getValue(gameCopy) != null) {
					v = Math.max(v, this.transpositionTableMin.getValue(gameCopy).intValue());
				}
				else {
					int min_value = minValue(gameCopy, depth - 1);
					this.transpositionTableMin.add(gameCopy, min_value);
					v = Math.max(v, min_value);
				}
			}
		}
		this.transpositionTableMax.add(game, v);
		return v;
	}

	/** 
	 * The optimal move determined by the minimax value of each node
	 * @return Move The optimal move
	 * @param game The current state
	 * @param depth The maximal depth of the game searching tree
	 * */
	public List<Move> minimaxDecision(Game game, int depth) {
		
		List<Move> moves = this.getMoves(game);
		List<Move> bestMoves = new ArrayList<Move>();
		Game gameCopy;
		int startIndex, endIndex;
		int currentValue = 0;
		int bestValue = this.player ? Integer.MAX_VALUE: Integer.MIN_VALUE;
		
		for (Move currentMove : moves) {
			gameCopy = game.copy();
			startIndex = currentMove.getStartIndex();
			endIndex = currentMove.getEndIndex();
			if (gameCopy.move(startIndex, endIndex)) {
				
				if (this.player) {
					currentValue = maxValue(gameCopy, depth - 1);
					this.transpositionTableMax.add(gameCopy, currentValue);
					if (currentValue <= bestValue) {
						bestValue = currentValue;
					}
				}
				else {
					currentValue = minValue(gameCopy, depth - 1);
					this.transpositionTableMin.add(gameCopy, currentValue);
					if (currentValue >= bestValue) {
						bestValue = currentValue;
					}
				}
			}
		}
		
		for (Move currentMove : moves) {
			gameCopy = game.copy();
			startIndex = currentMove.getStartIndex();
			endIndex = currentMove.getEndIndex();
			if (gameCopy.move(startIndex, endIndex)) {
				if (this.player) {
					currentValue = this.transpositionTableMax.getValue(gameCopy).intValue();
					if (currentValue == bestValue) {
						bestMoves.add(currentMove);
					}
				}
				else {
					currentValue = this.transpositionTableMin.getValue(gameCopy).intValue();
					if (currentValue == bestValue) {
						bestMoves.add(currentMove);
					}
				}
			}
		}
		return bestMoves;
	}

	/**
	 * Determines the optimal move and applies this move on the current state
	 * 
	 * @param game The current state
	 */
	@Override
	public void updateGame(Game game) {
		
		// Nothing to do
		if (game == null || game.isGameOver()) {
			return;
		}
		List<Move> moves = this.getMoves(game);
		
		int depth = 5;
		List<Move> bestMoves = minimaxDecision(game, depth);
		
		this.transpositionTableMax = new StateSet();
		this.transpositionTableMin = new StateSet();
		
		if (bestMoves.size() == 1) {
			game.move(bestMoves.get(0).getStartIndex(), bestMoves.get(0).getEndIndex());
		}
		else if (bestMoves.size() > 1) {
			int rdm = (int)(Math.random() * bestMoves.size());
			game.move(bestMoves.get(rdm).getStartIndex(), bestMoves.get(rdm).getEndIndex());
		}
		else {
			int rdm = (int)(Math.random() * moves.size());
			game.move(moves.get(rdm).getStartIndex(), moves.get(rdm).getEndIndex());
		}
	}
	
	/**
	 * Gets all the available moves and skips for the current player.
	 * 
	 * @param game	the current game state.
	 * @return a list of valid moves that the player can make.
	 */
	protected List<Move> getMoves(Game game) {
		
		// The next move needs to be a skip
		if (game.getSkipIndex() >= 0) {
			
			List<Move> moves = new ArrayList<>();
			List<Point> skips = MoveGenerator.getSkips(game.getBoard(),
					game.getSkipIndex());
			for (Point end : skips) {
				Game copy = game.copy();
				int startIndex = game.getSkipIndex(), endIndex = Board.toIndex(end);
				copy.move(startIndex,endIndex);
				moves.add(new Move(startIndex, endIndex, copy.goodHeuristic(!copy.isP2Turn())));
			}
			Collections.sort(moves);
			return moves;
		}
		
		// Get the checkers
		List<Point> checkers = new ArrayList<>();
		Board b = game.getBoard();
		if (game.isP2Turn()) {
			checkers.addAll(b.find(Board.BLACK_CHECKER));
			checkers.addAll(b.find(Board.BLACK_KING));
		} else {
			checkers.addAll(b.find(Board.WHITE_CHECKER));
			checkers.addAll(b.find(Board.WHITE_KING));
		}
		
		// Determine if there are any skips
		List<Move> moves = new ArrayList<>();
		for (Point checker : checkers) {
			int index = Board.toIndex(checker);
			List<Point> skips = MoveGenerator.getSkips(b, index);
			for (Point end : skips) {
				Game copy = game.copy();
				int endIndex = Board.toIndex(end);
				copy.move(index,endIndex);
				Move m = new Move(index, endIndex, copy.goodHeuristic(!copy.isP2Turn()));
				moves.add(m);
			}
		}
		
		// If there are no skips, add the regular moves
		if (moves.isEmpty()) {
			for (Point checker : checkers) {
				int index = Board.toIndex(checker);
				List<Point> movesEnds = MoveGenerator.getMoves(b, index);
				for (Point end : movesEnds) {
					Game copy = game.copy();
					int endIndex = Board.toIndex(end);
					copy.move(index,endIndex);
					moves.add(new Move(index, endIndex, copy.goodHeuristic(!copy.isP2Turn())));
				}
			}
		}
		Collections.sort(moves);
		return moves;
	}
}
