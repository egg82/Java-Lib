package ninja.egg82.patterns.states;

import java.util.ArrayList;

public abstract class FiniteState {
	//vars
	private ArrayList<Class<?>> exitStates = new ArrayList<Class<?>>();
	protected FiniteStateMachine finiteStateMachine = null;
	
	//constructor
	public FiniteState(FiniteStateMachine machine) {
		if (machine == null) {
			throw new IllegalArgumentException("machine cannot be null.");
		}
		this.finiteStateMachine = machine;
	}
	
	//public
	public abstract void enter();
	public abstract void exit();
	
	public void addExitState(Class<?> state) {
		if (state == null) {
			throw new IllegalArgumentException("state cannot be null.");
		}
		if (!exitStates.contains(state)) {
			exitStates.add(state);
		}
	}
	public void removeExitState(Class<?> state) {
		if (state == null) {
			throw new IllegalArgumentException("state cannot be null.");
		}
		exitStates.remove(state);
	}
	public boolean hasExitState(Class<?> state) {
		if (state == null) {
			throw new IllegalArgumentException("state cannot be null.");
		}
		return exitStates.contains(state);
	}
	
	//private
	
}