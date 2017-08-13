package ninja.egg82.patterns.states;

import java.util.HashMap;

import ninja.egg82.exceptions.ArgumentNullException;

public final class FiniteStateMachine {
	//vars
	private FiniteState currentState = null;
	private HashMap<Class<FiniteState>, FiniteState> states = new HashMap<Class<FiniteState>, FiniteState>();
	
	//constructor
	public FiniteStateMachine() {
		
	}
	
	//public
	public void addState(Class<FiniteState> state) {
		if (state == null) {
			throw new ArgumentNullException("state");
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
			throw new ArgumentNullException("state");
		}
		return states.containsKey(state);
	}
	public void removeState(Class<FiniteState> state) {
		if (state == null) {
			throw new ArgumentNullException("state");
		}
		states.remove(state);
	}
	
	public boolean trySwapStates(Class<FiniteState> state) {
		if (state == null) {
			throw new ArgumentNullException("state");
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