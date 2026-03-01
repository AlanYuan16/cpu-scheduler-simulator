/**
 * CPU Scheduler Simulator - Priority-based Round Robin Implementation
 * Simulates CPU scheduling with configurable time quantum and process priorities.
 * Extra Credit: Combines priority scheduling with Round Robin for improved throughput.
 * 
 * @author Alan Yuan
 * @date December 2025
 */
import java.io.*;
import java.util.*;

public class Main {
    static class Process {
        int id, arrivalTime, burstTime, priority, remainingTime, waitTime = 0;
        int startTime = -1, completionTime, responseTime;

        Process(int id, int arrivalTime, int burstTime, int priority) {
            this.id = id;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.priority = priority;
            this.remainingTime = burstTime;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Quantum Time: ");
        int quantum = sc.nextInt();
        
        System.out.print("Enter File Name: ");
        String filePath = sc.next();
        
        ArrayList<Process> processes = readProcesses(filePath);

        runPriorityRoundRobin(processes, quantum);
        sc.close();
    }

    private static ArrayList<Process> readProcesses(String filePath) {
        ArrayList<Process> processes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                int id = Integer.parseInt(columns[0].trim());
                int arrival = Integer.parseInt(columns[1].trim());
                int burst = Integer.parseInt(columns[2].trim());
                int priority = Integer.parseInt(columns[3].trim());
                processes.add(new Process(id, arrival, burst, priority));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return processes;
    }

    private static void runPriorityRoundRobin(ArrayList<Process> processes, int quantum) {
        Queue<Process> queue = new LinkedList<>();
        int time = 0, completedProcesses = 0, contextSwitches = 0, idleTime = 0;
        float totalWaitTime = 0, totalTurnaroundTime = 0, totalResponseTime = 0;

        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        while (completedProcesses < processes.size()) {
            for (Process p : processes) {
                if (p.arrivalTime <= time && p.remainingTime > 0 && !queue.contains(p)) {
                    queue.add(p);
                }
            }
            
            Process current = getHighestPriorityProcess(queue);
            if (current != null) {
                queue.remove(current);
                contextSwitches++;

                if (current.startTime == -1) { 
                    current.startTime = time;
                    current.responseTime = current.startTime - current.arrivalTime;
                    totalResponseTime += current.responseTime;
                }

                int execTime = Math.min(current.remainingTime, quantum);
                current.remainingTime -= execTime;
                time += execTime;

                for (Process p : processes) {
                    if (p != current && p.remainingTime > 0 && p.arrivalTime <= time) {
                        p.waitTime += execTime;
                    }
                }

                if (current.remainingTime > 0) {
                    queue.add(current);
                } else {
                    completedProcesses++;
                    current.completionTime = time;
                    totalTurnaroundTime += (current.completionTime - current.arrivalTime);
                    totalWaitTime += current.waitTime;
                }
            } else {
                idleTime++;
                time++;
            }
        }

        float avgTurnaroundTime = totalTurnaroundTime / processes.size();
        float avgWaitTime = totalWaitTime / processes.size();
        float avgResponseTime = totalResponseTime / processes.size();
        float cpuUtilization = (1 - (float) idleTime / time) * 100;
        float throughput = (float) processes.size() / time;

        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaroundTime);
        System.out.printf("Average Waiting Time: %.2f\n", avgWaitTime);
        System.out.printf("Average Response Time: %.2f\n", avgResponseTime);
        System.out.printf("CPU Utilization: %.2f%%\n", cpuUtilization);
        System.out.printf("Throughput: %.2f processes/unit time\n", throughput);
        System.out.printf("Context Switches: %d\n", contextSwitches);
    }

    private static Process getHighestPriorityProcess(Queue<Process> queue) {
        Process highestPriorityProcess = null;
        for (Process p : queue) {
            if (highestPriorityProcess == null || p.priority < highestPriorityProcess.priority) {
                highestPriorityProcess = p;
            }
        }
        return highestPriorityProcess;
    }
}
