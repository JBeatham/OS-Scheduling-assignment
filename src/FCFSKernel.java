import simulator.Config;
import simulator.IODevice;
import simulator.Kernel;
import simulator.ProcessControlBlock;
import simulator.ProcessControlBlockImpl;
//
import java.io.FileNotFoundException;
import java.io.IOException;
//
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Concrete Kernel type
 * 
 * @author Stephan Jamieson
 * @version 8/3/15
 */
public class FCFSKernel implements Kernel {
    

    private Deque<ProcessControlBlock> readyQueue;
        
    public FCFSKernel() {
		readyQueue = new ArrayDeque<>();
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
                	dispatch();
                }
                break;
             case TERMINATE_PROCESS:
                {
					ProcessControlBlock processBlock = Config.getCPU().getCurrentProcess();
                	processBlock.setState(ProcessControlBlock.State.TERMINATED);
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
                throw new IllegalArgumentException("FCFSKernel:interrupt("+interruptType+"...): this kernel does not suppor timeouts.");
            case WAKE_UP:
				
				ProcessControlBlock processBlock = (ProcessControlBlock) varargs[1];
				processBlock.setState(ProcessControlBlock.State.READY);
            	readyQueue.addLast(processBlock);
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
