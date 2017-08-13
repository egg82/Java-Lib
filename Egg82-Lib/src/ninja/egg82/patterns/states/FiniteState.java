package ninja.egg82.patterns.states;

import java.util.ArrayList;

import ninja.egg82.exceptions.ArgumentNullException;

public abstract class FiniteState {
	//vars
	private ArrayList<Class<?>> exitStates = new ArrayList<Class<?>>();
	protected FiniteStateMachine finiteStateMachine = null;
	
	//constructor
	public FiniteState(FiniteStateMachine machine) {
		if (machine == null) {
			throw new ArgumentNullException("machine");
		}
		this.finiteStateMachine = machine;
	}
	
	//public
	public abstract void enter();
	public abstract void exit();
	
	public final void addExitState(Class<?> state) {
		if (state == null) {
			throw new ArgumentNullException("state");
		}
		if (!exitStates.contains(state)) {
			exitStates.add(state);
		}
	}
	public final void removeExitState(Class<?> state) {
		if (state == null) {
			throw new ArgumentNullException("state");
		}
		exitStates.remove(state);
	}
	public final boolean hasExitState(Class<?> state) {
		if (state == null) {
			throw new ArgumentNullException("state");
		}
		return exitStates.contains(state);
	}
	
	//private
	
}