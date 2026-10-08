package proposal.structure;

import grafo.optilib.structure.Instance;
import grafo.optilib.tools.RandomManager;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class DROMDInstance implements Instance {
    private String name;
    private int numNodes;
    private int numEdges;
    private List<Integer> [] adjacencyList;
    private List<Integer> sortedByDegree;
    private List<Integer> reverseDegrees;
    private Map<Integer,List<Integer>> differentDegrees;
    private List<Integer> candidates;
    private Set<Integer> fixedNodes;

    public DROMDInstance(String path){
        String separator = Pattern.quote(File.separator);
        String[] tokens = path.split(separator);
        //name=path.split(separator)[2];
        name = tokens[tokens.length - 1];
        //System.out.print(name+" ");
        readInstance(path);
    }
    @Override
    public void readInstance(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))){
            String line;
            String[] lineContent;
            line = br.readLine().strip();
            while (line.startsWith("%")) {
                line = br.readLine().strip();
            }
            lineContent = line.split("\\s+");
            if(lineContent.length>2) {
                numNodes = Integer.parseInt(lineContent[0]);
                numEdges = Integer.parseInt(lineContent[2]);
            } else{
                numNodes = Integer.parseInt(lineContent[0]);
                numEdges = Integer.parseInt(lineContent[1]);
            }
            sortedByDegree =new ArrayList<>();
            candidates =new ArrayList<>();
            adjacencyList=new ArrayList[numNodes];
            for (int i=0; i<numNodes; i++){
                sortedByDegree.add(i);
                candidates.add(i);
                adjacencyList[i]=new ArrayList<>();
            }
            int count=0;
            for (int i=0; i< numEdges;i++){
                line= br.readLine().strip();
                lineContent = line.split("\\s+");
                int node1 = Integer.parseInt(lineContent[0])-1;
                int node2 = Integer.parseInt(lineContent[1])-1;
                if(node2!=node1){
                    count++;
                    adjacencyList[node1].add(node2);
                    adjacencyList[node2].add(node1);
                }
            }
            sortedByDegree.sort(Comparator.comparing(e->this.adjacencyList[e].size()));
            differentDegrees =new TreeMap<>();
            int previousDegree=-1;
            int currentDegree=-1;
            int lastIndex=0;
            fixedNodes=new HashSet<>();
            for (int i=0; i<numNodes;i++){
                int node=sortedByDegree.get(i);
                currentDegree=this.adjacencyList[node].size();
                if(currentDegree==0){
                    fixedNodes.add(node);
                }
                if(previousDegree!=currentDegree){
                    if(previousDegree!=-1){
                        differentDegrees.put(previousDegree,sortedByDegree.subList(lastIndex,i));
                    }
                    previousDegree=currentDegree;
                    lastIndex=i;
                }
            }
            differentDegrees.put(currentDegree,sortedByDegree.subList(lastIndex,numNodes));
            reverseDegrees = new ArrayList<>(degrees());
            reverseDegrees.sort(Comparator.reverseOrder());
            numEdges=count;
        } catch (FileNotFoundException e){
            System.out.println(("File not found " + path));
        } catch (IOException e){
            System.out.println("Error reading line");
        }
    }
    public String getName() {
        return name;
    }
    public int getNumNodes() {
        return numNodes;
    }
    public int getNumEdges() {
        return numEdges;
    }
    public List<Integer> getAdjacent(int node) {
        return adjacencyList[node];
    }
    public int getNumDegrees() {
        return differentDegrees.size();
    }
    public List<Integer> nodesWithDegree(int degree) {
        Collections.shuffle(differentDegrees.get(degree), RandomManager.getRandom());
        return differentDegrees.get(degree);
    }
    public Integer nodeWithDegree(int degree,int node) {
        return differentDegrees.get(degree).get(node);
    }
    public Set<Integer> degrees() {
        return differentDegrees.keySet();
    }
    public Set<Integer> fixedNodes() {
        return fixedNodes;
    }
    public List<Integer> reverseDegrees() {
        return reverseDegrees;
    }
    public List<Integer> getCandidates() {
        Collections.shuffle(candidates, RandomManager.getRandom());
        return candidates;
    }


}
