// Uncomment these lines to access image processing.
//import java.awt.Image;
//import java.io.File;
//import javax.imageio.ImageIO;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Your Agent for solving Raven's Progressive Matrices. You MUST modify this
 * file.
 * 
 * You may also create and submit new files in addition to modifying this file.
 * 
 * Make sure your file retains methods with the signatures:
 * public Agent()
 * public char Solve(RavensProblem problem)
 * 
 * These methods will be necessary for the project's main method to run.
 * 
 */
public class Agent {

    private HashMap<String, Integer> rScores;
    private HashMap<String, HashMap<String,Frame>> FigFrames;
    private static RavensProblem prob;
    /**
     * The default constructor for your Agent. Make sure to execute any
     * processing necessary before your Agent starts solving problems here.
     * 
     * Do not add any variables to this signature; they will not be used by
     * main().
     * 
     */
    public Agent() {
        rScores = new HashMap<String, Integer>();
        rScores.put("shape", 5);
        rScores.put("alignment",4);
        rScores.put("fill", 3);
        rScores.put("angle",4);
        rScores.put("size", 2);
        rScores.put("inside", 1);
        rScores.put("left-of", 1);
        rScores.put("above", 1);
        rScores.put("overlaps", 1);

        FigFrames = new HashMap<>();
    }
    /**
     * The primary method for solving incoming Raven's Progressive Matrices.
     * For each problem, your Agent's Solve() method will be called. At the
     * conclusion of Solve(), your Agent should return a String representing its
     * answer to the question: "1", "2", "3", "4", "5", or "6". These Strings
     * are also the Names of the individual RavensFigures, obtained through
     * RavensFigure.getName().
     * 
     * In addition to returning your answer at the end of the method, your Agent
     * may also call problem.checkAnswer(String givenAnswer). The parameter
     * passed to checkAnswer should be your Agent's current guess for the
     * problem; checkAnswer will return the correct answer to the problem. This
     * allows your Agent to check its answer. Note, however, that after your
     * agent has called checkAnswer, it will *not* be able to change its answer.
     * checkAnswer is used to allow your Agent to learn from its incorrect
     * answers; however, your Agent cannot change the answer to a question it
     * has already answered.
     * 
     * If your Agent calls checkAnswer during execution of Solve, the answer it
     * returns will be ignored; otherwise, the answer returned at the end of
     * Solve will be taken as your Agent's answer to this problem.
     * 
     * @param problem the RavensProblem your agent should solve
     * @return your Agent's answer to this problem
     */
    public int Solve(RavensProblem problem) {

        prob = problem;
        int bestGuess = 2;
        String problemName = problem.getName();
        String problemType = problem.getProblemType();
        HashMap<String, RavensFigure> figures = problem.getFigures();
        //Hashmap transformation matrix: figure->object->figure->attribute-changes (encapsulated into frame)
        if( problemType.equals("2x2"))
        {
            RavensFigure figureA = figures.get("A");
            RavensFigure figureB = figures.get("B");
            RavensFigure figureC = figures.get("C");
            RavensFigure figure1 = figures.get("1");
            RavensFigure figure2 = figures.get("2");
            RavensFigure figure3 = figures.get("3");
            RavensFigure figure4 = figures.get("4");
            RavensFigure figure5 = figures.get("5");
            RavensFigure figure6 = figures.get("6");

            /**
             * Setting up the frame data structure other than the internal relationships between objects etc.
             * which will be set up next
             * Then we figure out the transformations from A to B and try generate and test for C to #
             */
            for(RavensFigure fig: figures.values())
            {
                //For each figure, add the object->figure->attribute:changes transformation matrix
                FigFrames.put(fig.getName(), new HashMap<String, Frame>());
                HashMap<String, Frame> frames = FigFrames.get(fig.getName());
                for(RavensObject obj: fig.getObjects().values())
                {
                    frames.put(obj.getName(), new Frame(obj, fig.getName()));
                }
            }
            HashMap<String, Frame> figureAFrames = FigFrames.get("A");
            HashMap<String, Frame> figureBFrames = FigFrames.get("B");
            HashMap<String, Frame> figureCFrames = FigFrames.get("C");
            HashMap<String, Frame> figure1Frames = FigFrames.get("1");
            HashMap<String, Frame> figure2Frames = FigFrames.get("1");
            HashMap<String, Frame> figure3Frames = FigFrames.get("3");
            HashMap<String, Frame> figure4Frames = FigFrames.get("4");
            HashMap<String, Frame> figure5Frames = FigFrames.get("5");
            HashMap<String, Frame> figure6Frames = FigFrames.get("6");

            /**
             * Match objects from figures A to B and from A to C
             * Later we match them from C to answer choices
             */
            objectmatch(figureA, figureB);
            objectmatch(figureA, figureC);
            for(Map.Entry<String,HashMap<String, Frame>> fr: FigFrames.entrySet())
            {
                for(Frame f: fr.getValue().values())
                {
                    f.formRelationships();
                }
            }
            for(Frame frame: figureBFrames.values())
            {
                System.out.println(frame + "----> problem:"+problemName);
                System.out.println("In FIGURE A:\n");
                System.out.println(figureAFrames.get(frame.getName()));
            }

        }
        else
            System.out.println(problemType);
        return bestGuess;
    }

    /**
     * Matching the objects within figureA to those inside figureB. We change the names of the objects in B to match those
     * in A.
     * @param figureA Figure to match objects in next parameter with
     * @param figureB Figure whose objects need to be matched with prior figure's
     * @return an integer 0 to confirm success
     */
    public int objectmatch(RavensFigure figureA, RavensFigure figureB)
    {
        boolean nomatch = true;
        for(RavensObject objectA: figureA.getObjects().values())
        {
            nomatch = true;
            HashMap<String, Integer> scores = new HashMap<>();
            HashMap<String, String> attrA = objectA.getAttributes();
            /**
             * Instantiating the transformation matric of inner class Frame for each frame in figureA
             * to represent transformations to its corresponding object in figureB
             */
            for( Map.Entry<String, Frame> a: FigFrames.get(figureA.getName()).entrySet())
            {
                //Here we add the FigureB->{string:string} to show transformation from frame in figureA
                //to figureB in terms of attribute:changes or 0 for none
                a.getValue().transformationMatrix.put(figureB.getName(), new HashMap<String, String>());
            }
            for(RavensObject objectB: figureB.getObjects().values())
            {
                int currscore = 0;
                HashMap<String, String> attrB = objectB.getAttributes();
                if(attrA.size() == attrB.size())
                    currscore += 10;
                for(String attr: rScores.keySet())
                {
                    if(attrA.containsKey(attr) && attrB.containsKey(attr)) {
                        if (attrA.get(attr).equals(attrB.get(attr))) {
                            currscore += rScores.get(attr);
                            if (attr.equals("shape")) {
                                nomatch = false;
                            }
                        }
                        else
                        {
                            if(attr.equals("inside"))
                            {
                                if(attrA.get(attr).split(",").length == attrB.get(attr).split(",").length)
                                    currscore += 5;
                            }
                            else if(attr.equals("above"))
                            {
                                if(attrA.get(attr).split(",").length == attrB.get(attr).split(",").length)
                                    currscore += 5;
                            }
                            else if(attr.equals("overlaps"))
                            {
                                if(attrA.get(attr).split(",").length == attrB.get(attr).split(",").length)
                                    currscore += 5;
                            }
                            else if(attr.equals("left-of"))
                            {
                                if(attrA.get(attr).split(",").length == attrB.get(attr).split(",").length)
                                    currscore += 5;
                            }
                        }
                    }
                }
                scores.put(objectB.getName(), currscore);
            }
            if(nomatch)
            {
                System.out.println("NO MATCH WAS FOUND FOR OBJECT: "+objectA.getName()+objectA.getAttributes().get("shape")+" IN FIGURE: "+figureA.getName()
                +" TO ANY OBJECT IN FIGURE: "+figureB.getName()+" FOR PROBLEM: "+Agent.prob.getName());
                FigFrames.get(figureA.getName()).get(objectA.getName()).transformationMatrix.get(figureB.getName()).put("deleted","1"); //marking it as deleted since no match
            }
            else {
                Map.Entry<String, Integer> maxMatch = (Map.Entry<String, Integer>) scores.entrySet().toArray()[0];
                for (Map.Entry<String, Integer> a : scores.entrySet()) {
                    if (maxMatch.getValue().compareTo(a.getValue()) <= 0) {
                        maxMatch = a;
                    }
                }
                // We have a match for objectA in figureB. Now change name of matched obj to match objectA
                HashMap<String, Frame> figBFrames = FigFrames.get(figureB.getName());
                Frame targetFrame = figBFrames.get(maxMatch.getKey());
                if (targetFrame != null) {
                    targetFrame.setName(objectA.getName());       //Set the new name of frame in figureB
                    figBFrames.put(objectA.getName(), figBFrames.get(maxMatch.getKey()));  //Put the modified frame as value to key with new name in figBframes
                    figBFrames.remove(maxMatch.getKey());                               //Finally, remove the modified frame from the old obsolete key
                    /**
                     * Fix relationship names in entire stupid frame structure
                     * by changing the attributes hashmap for each affected frame in figureB
                     */
                    for (Frame fr : figBFrames.values()) {
                        for (Map.Entry<String, String> entry : fr.getAttributes().entrySet()) {
                            if (entry.getKey().equals("inside") || entry.getKey().equals("above") || entry.getKey().equals("left-of") ||
                                    entry.getKey().equals("overlaps"))
                                if (entry.getValue().contains(maxMatch.getKey())) {
                                    fr.getAttributes().put(entry.getKey(), entry.getValue().replace(maxMatch.getKey(), objectA.getName()));
                                }
                        }
                    }
                }
            }
        }
        return 0;
    }

    private class Frame
    {
        public HashMap<String, String> getAttributes() {
            return attributes;
        }

        public String getName() {
            return name;
        }
        public void setName(String nm)
        {
            this.name = nm;
        }
        String name;
        String fromFigure;
        HashMap<String,String> attributes;
        HashMap<String, ArrayList<Frame>> relationships;
        HashMap<String, HashMap<String, String>> transformationMatrix;
        public Frame(String name, String figure)
        {
            this.name = name;
            this.fromFigure = figure;
            attributes = new HashMap<>();
            relationships = new HashMap<>();
            transformationMatrix = new HashMap<>();
            relationships.put("inside", new ArrayList<Frame>());
            relationships.put("above", new ArrayList<Frame>());
            relationships.put("left-of", new ArrayList<Frame>());
            relationships.put("overlaps", new ArrayList<Frame>());
        }
        public Frame(RavensObject obj, String figure)
        {
            this(obj.getName(), figure);
            attributes = obj.getAttributes();
        }
        //Forming the frame data structure here with everything in figA
        public int formRelationships()
        {

            String inside = attributes.get("inside");
            if(inside != null) {
                String in[] = inside.split(",");
                for(String Ain: in) {
                    Frame Aisinsidethis = FigFrames.get(fromFigure).get(Ain);
                    if (Aisinsidethis != null) {
                        relationships.get("inside").add(Aisinsidethis);
                        System.out.println("Adding relationships to :"+this.name);
                    } else {
                        System.out.println("No frame found for attribute inside: " + this.name + "" +
                                "despite attribute hashmap containing valid value: " + Ain);
                        System.out.println(this.attributes.get("inside"));
                    }
                }
            }
            String leftof = attributes.get("left-of");
            if(leftof !=null)
            {
                String left[] = leftof.split(",");
                for(String Aleft: left) {
                    Frame Aleftof = FigFrames.get(fromFigure).get(Aleft);
                    if (Aleftof != null) {
                        relationships.get("left-of").add(Aleftof);
                    } else {
                        System.out.println("No frame found for attribute left-of: " + this.name + "" +
                                "despite attribute hashmap containing valid value: " + Aleft);
                    }
                }
            }
            String above = attributes.get("above");
            if(above !=null)
            {
                String isAbove[] = above.split(",");
                for(String Above: isAbove) {
                    Frame Aabove = FigFrames.get(fromFigure).get(Above);
                    if (Aabove != null) {
                        relationships.get("above").add(Aabove);
                    } else {
                        System.out.println("No frame found for attribute above: " + this.name + "" +
                                "despite attribute hashmap containing valid value: " + Above);
                    }
                }
            }
            String overlaps = attributes.get("overlaps");
            if(above !=null)
            {
                String isoverlap[] = above.split(",");
                for(String Aoverlap: isoverlap) {
                    Frame Aoverlaps = FigFrames.get(fromFigure).get(Aoverlap);
                    if (Aoverlaps != null) {
                        relationships.get("above").add(Aoverlaps);
                    } else {
                        System.out.println("No frame found for attribute overlaps: " + this.name + "" +
                                "despite attribute hashmap containing valid value: " + Aoverlap);
                    }
                }
            }
            return 0;
        }

        @Override
        public String toString() {
            return "Frame{" +
                    "name='" + name + '\'' +
                    ", fromFigure='" + fromFigure + '\'' +
                    ", attributes=" + attributes +
                    ", relationships=" + relationships +
                    ", transformations=" + transformationMatrix +
                    '}';
        }
    }
}
