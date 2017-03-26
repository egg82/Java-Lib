package ninja.egg82.patterns.states;

import com.koloboke.collect.map.hash.HashObjObjMap;
import com.koloboke.collect.map.hash.HashObjObjMaps;

public class FiniteStateMachine {
	//vars
	private FiniteState currentState = null;
	private HashObjObjMap<Class<FiniteState>, FiniteState> states = HashObjObjMaps.<Class<FiniteState>, FiniteState> newMutableMap();
	
	//constructor
	public FiniteStateMachine() {
		
	}
	
	//public
	public void addState(Class<FiniteState> state) {
		if (state == null) {
			throw new IllegalArgumentException("state cannot be null.");
		}
		
		FiniteState ns = null;
		try {
			ns = state.getDeclaredConstructor(FiniteStateMachine.class).newInstance(this);
		} catch (Exception ex) {
			throw new RuntimeException("Cannot create state.", ex);
		}
		
		FiniteState oldState = states.get(state);
		if (oldState != null) {
			if (currentState == oldState) {
				currentState = ns;
			}
		}
		states.put(state, ns);
		
		if (currentState == null) {
			currentState = ns;
			currentState.enter();
		}
	}
	public boolean hasState(Class<FiniteState> state) {
		if (state == null) {
			throw new IllegalArgumentException("state cannot be null.");
		}
		return states.containsKey(state);
	}
	public void removeState(Class<FiniteState> state) {
		if (state == null) {
			throw new IllegalArgumentException("state cannot be null.");
		}
		states.remove(state);
	}
	
	public boolean trySwapStates(Class<FiniteState> state) {
		if (state == null) {
			throw new IllegalArgumentException("state cannot be null.");
		}
		if (currentState == null || !states.containsKey(state) || !currentState.hasExitState(state)) {
			return false;
		}
		
		currentState.exit();
		currentState = states.get(state);
		currentState.enter();
		
		return true;
	}
	
	//private
	
}