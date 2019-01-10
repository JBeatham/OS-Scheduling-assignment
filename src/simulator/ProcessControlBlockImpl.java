package simulator;

import java.util.*;
import java.io.*;


public class ProcessControlBlockImpl implements ProcessControlBlock{
		private ArrayList<Instruction> list;	
		public static int total = 0;
		private int PID;
		private String name;
		private int priority;
		private int index;
		private ProcessControlBlock.State state;
	
		public ProcessControlBlockImpl(){
			name = "";
			priority = -1;
			index  = -1;
			list = new ArrayList<>();
		}
		
		public static ProcessControlBlockImpl loadProgram(String filename){
			try{
				ProcessControlBlockImpl p = new ProcessControlBlockImpl();
				p.PID = total;
				p.setProgramName(filename);
				p.index = 0;


				BufferedReader reader = new BufferedReader(new FileReader(filename));
				String line = reader.readLine();
			
				while(line !=null){
					List<String> instruction = new ArrayList<String>(Arrays.asList(line.split(" ")));
					if(line.startsWith("CPU")){
						p.list.add(new CPUInstruction(Integer.parseInt(instruction.get(1))));
					}
					else if(line.startsWith("IO")){
						p.list.add(new IOInstruction(Integer.parseInt(instruction.get(1)),Integer.parseInt(instruction.get(2))));
					}
					line = reader.readLine();
				}
				reader.close();
				return p;
			}catch(Exception e){
				return null;
			}
		}
		
		public int getPID(){
    		return PID;
    	}

		
    	public String getProgramName(){
    		return name;
    	}
		
		public void setProgramName(String other){
    		name = other;
    	}
		
		public int getPriority(){
			return priority;
		}

		
		public int setPriority(int value){
			int old  = priority;
			priority = value;
			return old;
		}


		
		public Instruction getInstruction(){
			return list.get(index);
		}
		
		
		public boolean hasNextInstruction(){
			return (index < list.size()-1);
		}

		
		public void nextInstruction(){
			index++;
		}
		
		
		public State getState(){
			return state;
		}
		
		public void setState(State state){
			if (this.state != ProcessControlBlock.State.TERMINATED){
			    this.state = state;
			}
		}
		
		public String toString(){
		    return String.format("process(pid=%d, state=%s, name=\"%s\")", PID, state, name);
		}
}
