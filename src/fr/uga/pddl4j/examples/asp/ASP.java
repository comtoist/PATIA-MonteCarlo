package fr.uga.pddl4j.examples.asp;

import fr.uga.pddl4j.heuristics.state.*;
import fr.uga.pddl4j.heuristics.state.FastForward;
import fr.uga.pddl4j.parser.PDDLAction;
import fr.uga.pddl4j.heuristics.state.Max;
import fr.uga.pddl4j.problem.operator.*;
import fr.uga.pddl4j.parser.ParsedProblem;
import fr.uga.pddl4j.plan.Plan;
import fr.uga.pddl4j.plan.SequentialPlan;
import fr.uga.pddl4j.planners.AbstractPlanner;
import fr.uga.pddl4j.planners.Planner;
import fr.uga.pddl4j.planners.PlannerConfiguration;
import fr.uga.pddl4j.planners.SearchStrategy;
import fr.uga.pddl4j.planners.statespace.search.StateSpaceSearch;
import fr.uga.pddl4j.problem.ADLProblem;
import fr.uga.pddl4j.problem.State;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.problem.operator.ConditionalEffect;
import fr.uga.pddl4j.problem.operator.Action;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import picocli.CommandLine;

import java.util.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Random;

import java.lang.*;//Max


//Tester avec sokoban 3 lignes
/**

 * The class is an example. It shows how to create a simple A* search planner able to

 * solve an ADL problem by choosing the heuristic to used and its weight.

 *

 * @author D. Pellier

 * @version 4.0 - 30.11.2021

 */

@CommandLine.Command(name = "ASP",
    version = "ASP 1.0",
    description = "Solves a specified planning problem using A* search strategy.",
    sortOptions = false,
    mixinStandardHelpOptions = true,
    headerHeading = "Usage:%n",
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription:%n%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n")
public class ASP extends AbstractPlanner<ADLProblem> {

    private int NUM_WALK = 2000;
    private int LENGTH_WALK = 10;
    private final int MAX_STEPS = 7;
    private double alpha = 0.9;

    /*
     * The class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(ASP.class.getName());

     /*
     * The weight of the heuristic.
     */
    private double heuristicWeight;


    /**
     * The name of the heuristic used by the planner.
     */
    private StateHeuristic.Name heuristic;

    /**
     * Sets the weight of the heuristic.
     *
     * @param weight the weight of the heuristic. The weight must be greater than 0.
     * @throws IllegalArgumentException if the weight is strictly less than 0.
     */

    @CommandLine.Option(names = { "-w", "--weight" }, defaultValue = "1.0",
        paramLabel = "<weight>", description = "Set the weight of the heuristic (preset 1.0).")
    public void setHeuristicWeight(final double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight <= 0");
        }
        this.heuristicWeight = weight;
    }


    /**
     * Set the name of heuristic used by the planner to the solve a planning problem.
     *
     * @param heuristic the name of the heuristic.
     */
    @CommandLine.Option(names = { "-e", "--heuristic" }, defaultValue = "FAST_FORWARD",
        description = "Set the heuristic : AJUSTED_SUM, AJUSTED_SUM2, AJUSTED_SUM2M, COMBO, "
            + "MAX, FAST_FORWARD SET_LEVEL, SUM, SUM_MUTEX (preset: FAST_FORWARD)")
    public void setHeuristic(StateHeuristic.Name heuristic)  {
        this.heuristic = heuristic;
    }


    /**
     * Returns the name of the heuristic used by the planner to solve a planning problem.
     *
     * @return the name of the heuristic used by the planner to solve a planning problem.
     */
    public final StateHeuristic.Name getHeuristic() {
        return this.heuristic;
    }


    /**
     * Returns the weight of the heuristic.
     *
     * @return the weight of the heuristic.
     */
    public final double getHeuristicWeight() {
        return this.heuristicWeight;
    }

    /*
     * Instantiates the planning problem from a parsed problem.
     *
     * @param problem the problem to instantiate.
     * @return the instantiated planning problem or null if the problem cannot be instantiated.
     */
    @Override
    public ADLProblem instantiate(ParsedProblem problem) {
        final ADLProblem pb = new ADLProblem(problem);

        pb.instantiate();

        return pb;

    }


    /**
     * Search a solution plan to a specified domain and problem using A*.
     *
     * @param problem the problem to solve.
     * @return the plan found or null if no plan was found.
     */

    @Override
    public Plan solve(final ADLProblem problem) {

        // Creates the A* search strategy
        StateSpaceSearch search = StateSpaceSearch.getInstance(SearchStrategy.Name.ASTAR,
            this.getHeuristic(), this.getHeuristicWeight(), this.getTimeout());
        LOGGER.info("* Starting A* search y \n");

        // Search a solution
        Plan plan =MonteCarlo(problem);

        // If a plan is found update the statistics of the planner and log search information
        if (plan != null) {
            LOGGER.info("* A* search succeeded\n");
            this.getStatistics().setTimeToSearch(search.getSearchingTime());
            this.getStatistics().setMemoryUsedToSearch(search.getMemoryUsed());
        } else {
            LOGGER.info("* A* search failed\n");
        }

        // Return the plan found or null if the search fails.
        return plan;
    }

    public Plan MonteCarlo(final ADLProblem problem){
        Plan plan = null;
        State init = new State(problem.getInitialState());
     
        // Instance de l'heuristique
        StateHeuristic heuristic = StateHeuristic.getInstance(this.getHeuristic(), problem);
        Node s = new Node(init, null, -1, 0, heuristic.estimate(init, problem.getGoal()));
        Condition goal = problem.getGoal();
        double hmin = heuristic.estimate(s,goal);
        int counter = 0;
        //RANDOM WALK LENGTH DYNAMIQUE
        int hmincount = 0;
        double prevHmin = hmin;
        //NUMBER OF RANDOM WALK DYNAMIQUE
        boolean acceptable_progress = true;

        while(!s.satisfy(goal)){
            Node dead = new Node(s);
            if(counter > MAX_STEPS || s == dead){
                s = new Node(init, null, -1, 0, heuristic.estimate(init, problem.getGoal()));
                counter = 0;
                hmincount = 0;
                prevHmin = hmin;
            }
            
            s = MonteCarloRandomWalk(s,problem);
            if(s.getHeuristic()<hmin){
                hmin = s.getHeuristic();
                counter = 0;
            }
            else{
                counter++;
            }

            if(prevHmin == hmin){
                hmincount++;
            }else{
                prevHmin = hmin;
            }
            
            if(hmincount==5){
                hmincount = 0;
                LENGTH_WALK = LENGTH_WALK+1;
            }
        }
      
        plan = extractPlan(s,problem);
    
        return plan;
    }
    //On utilise des AbstractSTateHeuristic pour avoir accès a getACtions
    
    private Node MonteCarloRandomWalk(Node s,ADLProblem problem){
        double hmin = Double.POSITIVE_INFINITY;
        Node smin = null;

        StateHeuristic heuristic = StateHeuristic.getInstance(this.getHeuristic(), problem);

        Node sprime;
        Condition goal = problem.getGoal();        

        Action a;
        Random rand = new Random();
        //NUMBER OF RANDOM WALKS
        double hold = 0;
        double progress =0;
        double acceptable_progress=0;
        for(int i=0;i<NUM_WALK;i++){
            sprime = new Node(s,s.getParent(),s.getAction(),s.getCost(),s.getHeuristic());
            for(int j=0;j<LENGTH_WALK;j++){
                //get toutes les actions et voir si les preconditions sont en accord avec l etat
                List<Action> A = new ArrayList<Action>(); //liste des actions reellement applicables
            
                //A ← ApplicableActions(s′)
                for (int k = 0; k < problem.getActions().size(); k++) {
                    // We get the actions of the problem
                    a = problem.getActions().get(k);
                    // If the action is applicable in the current node on l ajoute a A
                    if (a.isApplicable(sprime)) {
                        A.add(a);
                        
                    }
                }
                if(A.isEmpty()){
                    break;
                }

                //On choisit aléatoirement un a a ← UniformlyRandomSelectFrom(A)
                
                a = A.get(rand.nextInt(A.size()));
                //On l applique a sprime s′ ← apply(s′, a)
                final List<ConditionalEffect> effects = a.getConditionalEffects();

                Node sauv = new Node(sprime,sprime.getParent(),sprime.getAction(),sprime.getCost(),sprime.getHeuristic());
                for (ConditionalEffect ce : effects) {
                    if (sprime.satisfy(ce.getCondition())) {
                        sprime.apply(ce.getEffect());
                    }
                }

                int l=0;
                
                while(problem.getActions().get(l)!=a){
                    l++;
                } 
                sprime.setCost(sauv.getCost()+1);
                sprime.setParent(sauv);
                sprime.setAction(l);
                sprime.setHeuristic(heuristic.estimate(sprime,goal));
                if(sprime.satisfy(goal)){
                    return sprime;
                }
            }
            if(sprime.getHeuristic()<hmin){
                smin=sprime;
                hmin=sprime.getHeuristic();
            }
            //P (n) = max(0, holdmin − hmin)
            //AP(1) = P(1)
            if(i==0){
                progress = 1.0;
                acceptable_progress = progress;
            }else{
                progress = Math.max(0,hold - hmin);
                acceptable_progress = (1-alpha)*acceptable_progress+alpha*progress;
            }
            if(acceptable_progress < 0){
                return smin;
            }

        }
        if(smin == null){
            return s;
        }else{
            return smin;
        }
    }

    /**
     * Extracts a search from a specified node.
     *
     * @param node the node.
     * @param problem the problem.
     * @return the search extracted from the specified node.
     */

    private Plan extractPlan(final Node node, final ADLProblem problem) {
        Node n = node;
        final Plan plan = new SequentialPlan();
        while (n.getAction() != -1) {
            final Action a = problem.getActions().get(n.getAction());
            plan.add(0, a);
            n = n.getParent();
            if(n == null){
                return plan;
            }
        }
        return plan;
    }

    /**
     * The main method of the <code>ASP</code> planner.
     *
     * @param args the arguments of the command line.
     */

    public static void main(String[] args) {
        try {
            final ASP planner = new ASP();
            CommandLine cmd = new CommandLine(planner);
            cmd.execute(args);
        } catch (IllegalArgumentException e) {
            LOGGER.fatal(e.getMessage());
        }
    }
}
