package ninja.egg82.patterns.states;

import org.eclipse.collections.impl.map.mutable.UnifiedMap;

public final class FiniteStateMachine {
	//vars
	private FiniteState currentState = null;
	private UnifiedMap<Class<FiniteState>, FiniteState> states = new UnifiedMap<Class<FiniteState>, FiniteState>();
	
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