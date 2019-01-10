import simulator.*;

import java.util.*;

public class RRKernel implements Kernel {
	
	private Deque<ProcessControlBlock> readyQueue;
	int time;
        
    public RRKernel(int time) {
		readyQueue = new ArrayDeque<>();
		this.time = time;
    }
    
    private ProcessControlBlock dispatch() {
		ProcessControlBlock processBlock = null;
		if(!Config.getCPU().isIdle()){
    		processBlock = Config.getCPU().getCurrentProcess();
    	}
    	if(!readyQueue.isEmpty()){
    		ProcessControlBlock other = readyQueue.removeFirst();
    		Config.getCPU().contextSwitch(other);
    		other.setState(ProcessControlBlock.State.RUNNING);
    	}
    	else{
    		Config.getCPU().contextSwitch(null);
    	}
		if (!Config.getCPU().isIdle()){
 Config.getSystemTimer().scheduleInterrupt(time,this,Config.getCPU().getCurrentProcess().getPID(),Config.getCPU().getCurrentProcess());
    	}
    	return processBlock;
	}
            
    
                
    public int syscall(int number, Object... varargs) {
        int result = 0;
        switch (number) {
             case MAKE_DEVICE:
                {
                    IODevice device = new IODevice((Integer)varargs[0], (String)varargs[1]);
                    Config.addDevice(device);
                }
                break;
             case EXECVE: 
                {
					ProcessControlBlockImpl.total++;
                    ProcessControlBlock pcb = this.loadProgram((String)varargs[0]);
                    if (pcb!=null) {
                        pcb.setState(ProcessControlBlock.State.READY);
                    	readyQueue.addLast(pcb);
                    	if(Config.getCPU().isIdle()){
                    		dispatch();
						}
                    }
                    else {
                        result = -1;
                    }
                }
                break;
             case IO_REQUEST: 
                {
					ProcessControlBlock processBlock = Config.getCPU().getCurrentProcess();
                	IODevice device = Config.getDevice((int)varargs[0]);	
                	device.requestIO((int)varargs[1], processBlock, this);
                	processBlock.setState(ProcessControlBlock.State.WAITING);
					Config.getSystemTimer().cancelInterrupt(processBlock.getPID());
                	dispatch();
                }
                break;
             case TERMINATE_PROCESS:
                {
					ProcessControlBlock processBlock = Config.getCPU().getCurrentProcess();
                	processBlock.setState(ProcessControlBlock.State.TERMINATED);
					Config.getSystemTimer().cancelInterrupt(processBlock.getPID());
                	dispatch();
                }
                break;
             default:
                result = -1;
        }
        return result;
    }
   
    
    public void interrupt(int interruptType, Object... varargs){
        switch (interruptType) {
            case TIME_OUT:
                ProcessControlBlock processBlock = Config.getCPU().getCurrentProcess();
        		if (!readyQueue.isEmpty()){                	
        			processBlock.setState(ProcessControlBlock.State.READY);
        		}
                readyQueue.add(processBlock);
            	dispatch();
				break;
            case WAKE_UP:
				
				ProcessControlBlock block = (ProcessControlBlock) varargs[1];
				block.setState(ProcessControlBlock.State.READY);
            	readyQueue.addLast(block);
            	if(Config.getCPU().isIdle()){
            		dispatch();
            	}
                
				break;
            default:
                throw new IllegalArgumentException("FCFSKernel:interrupt("+interruptType+"...): unknown type.");
        }
    }
    
    private static ProcessControlBlock loadProgram(String filename) {
        try {
            return ProcessControlBlockImpl.loadProgram(filename);
        }
        catch (Throwable e) {
            return null;
        }
    }
}

