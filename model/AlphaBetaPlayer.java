/* Name: ComputerPlayer
 * Author: Devon McGrath
 * Description: This class represents a computer player which can update the
 * game state without user interaction.
 */

package src.model;

import java.util.List;
import java.util.ArrayList;

/**
 * The {@code ComputerPlayer} class represents a computer player and updates
 * the board based on a model.
 */
public class AlphaBetaPlayer extends MinMaxPlayer {

	//public boolean player;
	
	public AlphaBetaPlayer(boolean joueur) {
		super(joueur);
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
	public int minValue(Game game, int depth, int alpha, int beta) {
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
		Move currentMove;
		
		for (int i = 0; i < moves.size(); i++) {
			gameCopy = game.copy();
			currentMove = moves.get(i);
			startIndex = currentMove.getStartIndex();
			endIndex = currentMove.getEndIndex();
				
			if (gameCopy.move(startIndex, endIndex)) {
				if (gameCopy.isP2Turn()) {
					v = minValue(gameCopy, depth - 1, alpha, beta);
				}
				if (this.transpositionTableMax.getValue(gameCopy) != null) {
					v = Math.min(v, this.transpositionTableMax.getValue(gameCopy).intValue());
				}
				else {
					int max_value = maxValue(gameCopy, depth - 1, alpha, beta);
					this.transpositionTableMax.add(gameCopy, max_value);
					v = Math.min(v, max_value);
				}
				if (v <= alpha) {
					return v;
				}
				beta = Math.min(beta, v);
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
	public int maxValue(Game game, int depth, int alpha, int beta) {
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
		
		for (int i = 0; i < moves.size(); i++) {
			gameCopy = game.copy();
			startIndex = moves.get(i).getStartIndex();
			endIndex = moves.get(i).getEndIndex();
			if (gameCopy.move(startIndex, endIndex)) {
				if (!gameCopy.isP2Turn()) {
					v = maxValue(gameCopy, depth - 1, alpha, beta);
				}
				if (this.transpositionTableMin.getValue(gameCopy) != null) {
					v = Math.max(v, this.transpositionTableMin.getValue(gameCopy).intValue());
				}
				else {
					int min_value = minValue(gameCopy, depth - 1, alpha, beta);
					this.transpositionTableMin.add(gameCopy, min_value);
					v = Math.max(v, min_value);
				}
				if (v >= beta) {
					return v;
				}
				alpha = Math.max(alpha, v);
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
	public List<Move> alphaBetaSearch(Game game, int depth) {
		
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		int v = (this.player) ? minValue(game, depth, alpha, beta) : maxValue(game, depth, alpha, beta) ;
		
		List<Move> moves = this.getMoves(game);
		List<Move> bestMoves = new ArrayList<Move>();
		Game gameCopy;
		int startIndex, endIndex;
		Move currentMove;
		int currentValue = 0;
		
		for (int i = 0; i < moves.size(); i++) {
			gameCopy = game.copy();
			currentMove = moves.get(i);
			startIndex = currentMove.getStartIndex();
			endIndex = currentMove.getEndIndex();
			if (gameCopy.move(startIndex, endIndex)) {
				if (gameCopy.isGameOver()) {
					currentValue = (gameCopy.isP2Turn() == this.player) ? Integer.MAX_VALUE : Integer.MIN_VALUE; 
				}
				else {
					if (this.player) {
						currentValue =  maxValue(gameCopy, depth - 1, alpha, beta);	
					}
					else {
						currentValue = minValue(gameCopy, depth - 1, alpha, beta);
					}
				}
				if (currentValue == v) {
					bestMoves.add(currentMove);
				}
			}
		}
		return bestMoves;
	}
	
	
	@Override
	public void updateGame(Game game) {
		
		// Nothing to do
		if (game == null || game.isGameOver()) {
			return;
		}
		List<Move> moves = this.getMoves(game);
		
		int depth = 5;
		List<Move> bestMoves = alphaBetaSearch(game, depth);
		
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
}
