import java.util.Scanner;

import simulator.Config;
import simulator.Kernel;
import simulator.SystemTimer;
import simulator.TRACE;

public class SimulateFCFS {
    public static void main(String [] args){

        Scanner s = new Scanner(System.in);
        System.out.println("*** FCFS Simulator ***");
        System.out.print("Enter configuration file name: ");
        String configFileName = s.next();
        System.out.print("Enter cost of system call: ");
        int syscallCost = s.nextInt();
        System.out.print("Enter cost of context switch: ");
        int dispatchCost = s.nextInt();
        System.out.print("Enter trace level: ");
        int level = s.nextInt();
        s.close();
        
        TRACE.SET_TRACE_LEVEL(level);
		final Kernel kernel = new FCFSKernel();
		Config.init(kernel, dispatchCost, syscallCost);
		Config.buildConfiguration(configFileName);
		Config.run();
		SystemTimer timer = Config.getSystemTimer();
		System.out.println(timer);
		System.out.println("Context switches: "+Config.getCPU().getContextSwitches());
		System.out.printf("CPU utilization: %.2f\n",((double)timer.getUserTime())/timer.getSystemTime()*100);
	}
}
