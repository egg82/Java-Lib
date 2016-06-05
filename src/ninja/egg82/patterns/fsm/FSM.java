package ninja.egg82.patterns.fsm;

import java.util.HashMap;

import ninja.egg82.patterns.fsm.interfaces.IFSM;
import ninja.egg82.patterns.fsm.interfaces.IFSMState;

public class FSM implements IFSM {
	//vars
	private IFSMState currentState = null;
	private HashMap<String, IFSMState> states = new HashMap<String, IFSMState>();
	
	//constructor
	public FSM(String initStateName, Class<IFSMState> initState) {
		if (initStateName == null || initStateName.isEmpty()) {
			throw new Error("initStateName cannot be null or empty");
		}
		if (initState == null) {
			throw new Error("initState cannot be null");
		}
		
		try {
			states.put(initStateName, initState.getDeclaredConstructor(IFSM.class).newInstance(this));
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			throw new Error("initState could not be instantiated");
		}
		currentState = states.get(initStateName);
		currentState.enter(null);
	}
	
	//public
	public void destroy() {
		currentState.exit(null);
	}
	
	public void addState(String name, Class<IFSMState> state) {
		if (name == null || name.isEmpty() || state == null) {
			return;
		}
		
		if (currentState == states.get(name)) {
			try {
				states.put(name, state.getDeclaredConstructor(IFSM.class).newInstance(this));
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
				return;
			}
			
			IFSMState tmp = currentState;
			IFSMState tmp2 = states.get(name);
			currentState.exit(tmp2);
			currentState = tmp2;
			currentState.enter(tmp);
		} else {
			try {
				states.put(name, state.getDeclaredConstructor(IFSM.class).newInstance(this));
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
	}
	public void removeState(String name) {
		if (currentState == states.get(name)) {
			return;
		}
		
		states.remove(name);
	}
	
	public void swapStates(String name) {
		if (!states.containsKey(name) || !currentState.exitStateNames().contains(name)) {
			return;
		}
		
		IFSMState tmp = states.get(name);
		IFSMState tmp2 = currentState;
		currentState.exit(tmp);
		currentState = tmp;
		currentState.enter(tmp2);
	}
	
	//private
	
}