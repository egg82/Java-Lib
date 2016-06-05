package ninja.egg82.patterns.fsm.interfaces;

import java.util.ArrayList;

public interface IFSMState {
	void enter(IFSMState fromState);
	void exit(IFSMState toState);
	ArrayList<String> exitStateNames();
	void addExitState(String name);
	void removeExitState(String name);
}