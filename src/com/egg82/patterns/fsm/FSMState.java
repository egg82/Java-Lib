package com.egg82.patterns.fsm;

import java.util.ArrayList;

import com.egg82.patterns.fsm.interfaces.IFSM;
import com.egg82.patterns.fsm.interfaces.IFSMState;

public class FSMState implements IFSMState {
	//vars
	private ArrayList<String> exitStates = new ArrayList<String>();
	
	protected IFSM machine = null;
	
	//constructor
	public FSMState(IFSM machine) {
		this.machine = machine;
	}
	
	//public
	public void enter(IFSMState fromState) {
		
	}
	public void exit(IFSMState toState) {
		
	}
	
	public ArrayList<String> exitStateNames() {
		return exitStates;
	}
	
	public void addExitState(String name) {
		exitStates.add(name);
	}
	public void removeExitState(String name) {
		exitStates.remove(name);
	}
	
	//private
	
}