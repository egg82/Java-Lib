package com.egg82.patterns.fsm.interfaces;

public interface IFSM {
	void destroy();
	void addState(String name, Class<IFSMState> state);
	void removeState(String name);
	void swapStates(String name);
}