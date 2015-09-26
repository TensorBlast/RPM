// Uncomment these lines to access image processing.
//import java.awt.Image;
//import java.io.File;
//import javax.imageio.ImageIO;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.lang.reflect.Array;
import java.util.*;

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
        rScores.put("angle",7);
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

        System.out.println("WE'RE SOLVING PROBLEM: "+problem.getName());
        FigFrames = new HashMap<>();
        prob = problem;
        int bestGuess = 2;
        String problemName = problem.getName();
        String problemType = problem.getProblemType();
        HashMap<String, RavensFigure> figures = problem.getFigures();
        //Hashmap transformation matrix: figure->object->figure->attribute-changes (encapsulated into frame)
        if( problemType.equals("2x2"))
        {
            System.out.println("WE'RE SOLVING PROBLEM: "+problem.getName());
            RavensFigure figureA = figures.get("A");
            RavensFigure figureB = figures.get("B");
            RavensFigure figureC = figures.get("C");

            RavensFigure figure1 = figures.get("1");
            RavensFigure figure2 = figures.get("2");
            RavensFigure figure3 = figures.get("3");
            RavensFigure figure4 = figures.get("4");
            RavensFigure figure5 = figures.get("5");
            RavensFigure figure6 = figures.get("6");
            ArrayList<RavensFigure> answerChoices = new ArrayList<RavensFigure>();
            answerChoices.add(figure1);
            answerChoices.add(figure2);
            answerChoices.add(figure3);
            answerChoices.add(figure4);
            answerChoices.add(figure5);
            answerChoices.add(figure6);

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
            for(Map.Entry<String,HashMap<String, Frame>> fr: FigFrames.entrySet())
            {
                for(Frame f: fr.getValue().values())
                {
                    f.formRelationships();
                }
            }
            objectmatch(figureA, figureB);
            formTransformations(figureAFrames, "A", figureBFrames, "B");
            objectmatch(figureA, figureC);
            formTransformations(figureAFrames, "A", figureCFrames, "C");
//            for(Frame frame: figureAFrames.values())
//            {
//                System.out.println(frame + "----> problem:"+problemName);
//            }
            HashMap<String, Double> answerscores = new HashMap<>();
            double A_B = 0;
            double A_C = 0;
            for(Frame ina: figureAFrames.values())
            {
                A_B += ina.transformationScore.get("B");
                A_C += ina.transformationScore.get("C");
                System.out.println(ina.transformationScore);
            }
            for(RavensFigure answerchoice: answerChoices)
            {
                objectmatch(figureC, answerchoice);
                formTransformations(figureCFrames,"C", FigFrames.get(answerchoice.getName()),answerchoice.getName());
                double score = compareTranformations(figureAFrames, "B", figureCFrames, answerchoice.getName());
                System.out.println("SCORE FOR A->B with C->"+answerchoice.getName()+": "+score+" FOR PROBLEM: "+problemName);
                answerscores.put(answerchoice.getName(),score*A_B);
            }

            for(RavensFigure answerchoice: answerChoices)
            {
                objectmatch(figureB, answerchoice);
                System.out.println("DOING A->C tranformation analysis FOR "+answerchoice.getName());
                formTransformations(figureBFrames,"B", FigFrames.get(answerchoice.getName()),answerchoice.getName());
                double score = compareTranformations(figureAFrames, "C", figureBFrames, answerchoice.getName());
                System.out.println("SCORE FOR A->C with B->"+answerchoice.getName()+": "+score+" FOR PROBLEM: "+problemName);
                answerscores.put(answerchoice.getName(),answerscores.get(answerchoice.getName())+(score*A_C));
            }
            Map.Entry<String, Double> bestEntryGuess = maxHashEntry(answerscores);
            bestGuess = Integer.parseInt(bestEntryGuess.getKey());
        }
        else
            System.out.println(problemType);

        if(problem.getName().equals("Basic Problem B-12"))
        {
            System.out.println();
        }
        return bestGuess;
    }

    /**
     * Matching the objects within figureA to those inside figureB. We change the names of the objects in B to match those
     * in A.
     * @param figureA Figure to match objects in next parameter with
     * @param figureB Figure whose objects need to be matched with prior figure's
     * @return an integer 0 to confirm success
     */
    public int objectmatch(RavensFigure figureA, RavensFigure figureB) {
        boolean nomatch = true;
        HashMap<String, HashMap<String, Integer>> scores = new HashMap<>();
        for (Frame objectA : FigFrames.get(figureA.getName()).values()) {
            nomatch = true;
            HashMap<String, String> attrA = objectA.getAttributes();
            scores.put(objectA.getName(), new HashMap<String, Integer>());
            /**
             * Instantiating the transformation matric of inner class Frame for each frame in figureA
             * to represent transformations to its corresponding object in figureB
             */

            //Here we add the FigureB->{string:string} to show transformation from frame in figureA
            //to figureB in terms of attribute:changes or 0 for none
            objectA.transformationMatrix.put(figureB.getName(), new HashMap<String, String>());
            objectA.transformationScore.put(figureB.getName(), 0.0);
            String objectAshape = objectA.getAttributes().get("shape");
            int sizeweight = 1;
            for (Frame objectB : FigFrames.get(figureB.getName()).values()) {
                int currscore = 0;
                HashMap<String, String> attrB = objectB.getAttributes();
                String objectBshape = attrB.get("shape");
                if(objectAshape.equals(objectBshape))
                {
                    currscore += 20;
                    if(objectAshape.equals("circle"))
                        sizeweight = 10;
                    else sizeweight = 1;
                }
                for (String attr : rScores.keySet()) {
//                    if(attrA.size() == attrB.size())
//                    {
//                        currscore += 10;
//                    }
                    if (attrA.containsKey(attr) && attrB.containsKey(attr)) {
                        if (attrA.get(attr).equals(attrB.get(attr))) {
                            if(attr.equals("size"))
                            {
                                currscore += rScores.get(attr) * sizeweight;
                                System.out.println(sizeweight);
                            }
                            else
                                currscore += rScores.get(attr)*2;
                        } else {
                            if (attr.equals("inside")) {
                                if (attrA.get(attr).split(",").length == attrB.get(attr).split(",").length)
                                    currscore += 15;
                            } else if (attr.equals("above")) {
                                if (attrA.get(attr).split(",").length == attrB.get(attr).split(",").length)
                                    currscore += 7;
                            } else if (attr.equals("overlaps")) {
                                if (attrA.get(attr).split(",").length == attrB.get(attr).split(",").length)
                                    currscore += 5;
                            } else if (attr.equals("left-of")) {
                                if (attrA.get(attr).split(",").length == attrB.get(attr).split(",").length)
                                    currscore += 5;
                            }
                        }
                    } else {
                        currscore -= 10;
                    }
                }
                scores.get(objectA.getName()).put(objectB.getName(), currscore);
            }
        }

        for (Frame objectA : FigFrames.get(figureA.getName()).values()) {
            boolean matched = false;
            if (true) {
                Map<String, Integer> objAscores = sortByComparator(scores.get(objectA.getName()));

                for(Map.Entry<String, Integer> entr: objAscores.entrySet()){

                    Map.Entry<String, Integer> maxMatch = entr;
                    String betterAmatch = objectA.getName();
                    for (Frame objA : FigFrames.get(figureA.getName()).values()) {
                        if (!objA.getName().equals(objectA.getName())) {

                            Map.Entry<String, Integer> alternativeMax = (Map.Entry<String, Integer>)sortByComparator(scores.get(objA.getName())).entrySet().toArray()[0];

                            if (maxMatch.getKey().equals(alternativeMax.getKey())) {
                                if (maxMatch.getValue() < alternativeMax.getValue()) {
                                    betterAmatch = objA.getName();
                                    break;
                                }
                            }
                        }
                    }
                    // We have a match for objectA in figureB. Now change name of matched obj to match objectA
                    if (FigFrames.get(figureB.getName()).get(maxMatch.getKey()) != null) {
                        if (betterAmatch.equals(objectA.getName()) && FigFrames.get(figureB.getName()).get(maxMatch.getKey()).getAttributes().get("shape").equals(objectA.getAttributes().get("shape"))) {
                            matched = true;
                            HashMap<String, Frame> figBFrames = FigFrames.get(figureB.getName());
                            Frame targetFrame = figBFrames.get(maxMatch.getKey());
                            if (targetFrame != null) {
                                targetFrame.setName(objectA.getName());       //Set the new name of frame in figureB
                                figBFrames.put(objectA.getName(), figBFrames.get(maxMatch.getKey()));  //Put the modified frame as value to key with new name in figBframes
                                if (!maxMatch.getKey().equals(objectA.getName())) {
                                    figBFrames.remove(maxMatch.getKey());                               //Finally, remove the modified frame from the old obsolete key
                                }
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
                    if(matched)
                        break;
                }
                if (!matched) {
                    objectA.transformationMatrix.get(figureB.getName()).put("deleted", "1");
                }
            }
        }
        return 0;
    }

    /**
     * Public algorithm to sort hashmap by value implemented here using lambda comparators
     * Found here http://stackoverflow.com/questions/8119366/sorting-hashmap-by-values
     * @param unsortMap
     *
     * @return
     */
    private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap)
    {

        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list,(Map.Entry<String, Integer> o1,Map.Entry<String, Integer> o2) -> o2.getValue().compareTo(o1.getValue()));

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
    public int formTransformations(HashMap<String, Frame> FigAframes,String Aname, HashMap<String, Frame> FigBframes, String Bname) {
        for(Map.Entry<String, Frame> FigAObj: FigAframes.entrySet())
        {
            String ObjAName = FigAObj.getKey();
            Frame ObjA = FigAObj.getValue();
            String FigBName = "";
            try {
                FigBName = Bname;
            }
            catch (Exception e)
            {
                System.out.println("PROBLEM ERROR IN : "+prob.getName());
                System.out.println();
            }

            Frame ObjB_A = FigBframes.get(ObjAName);
            if(ObjB_A == null)
            {
                ObjA.transformationMatrix.get(Bname).put("deleted","1");
            }
            else
            {
                HashMap<String, String> attrA = ObjA.getAttributes();
                HashMap<String, String> attrB = ObjB_A.getAttributes();
                ObjA.transformationMatrix.get(FigBName).put("deleted","0");
                for(Map.Entry<String, Integer> rscore : rScores.entrySet()) {
                    double oldScore = ObjA.transformationScore.get(FigBName);
                    if(attrA.containsKey(rscore.getKey())) {
                        if (attrB.containsKey(rscore.getKey())) {
                            switch (rscore.getKey()) {
                                case "shape":
                                    if (attrA.get("shape").equals(attrB.get("shape"))) {
                                        ObjA.transformationMatrix.get(FigBName).put("shape", "unchanged");
                                        ObjA.transformationScore.put(FigBName, oldScore + (double) rscore.getValue());
                                    } else {
                                        ObjA.transformationMatrix.get(FigBName).put("shape", "changed");
                                        ObjA.transformationScore.put(FigBName, oldScore - (double) rscore.getValue() / 2);
                                    }
                                    break;
                                case "size":
                                    if (attrA.get(rscore.getKey()).equals(attrB.get(rscore.getKey()))) {
                                        ObjA.transformationMatrix.get(FigBName).put(rscore.getKey(), "0");
                                        ObjA.transformationScore.put(FigBName, oldScore + rscore.getValue() * 1.1);
                                    } else {
                                        int t=0;
                                        if ((t=Agent.sizeCompare(attrA.get(rscore.getKey()), attrB.get(rscore.getKey()))) < 0) {
                                            ObjA.transformationMatrix.get(FigBName).put(rscore.getKey(), Integer.toString(-1*t));
                                            ObjA.transformationScore.put(FigBName, oldScore - rscore.getValue() * 0.5);
                                        } else {
                                            ObjA.transformationMatrix.get(FigBName).put(rscore.getKey(), Integer.toString(-1*t));
                                            ObjA.transformationScore.put(FigBName, oldScore - rscore.getValue() * 0.5);
                                        }
                                    }
                                    break;
                                case "angle":

                                    if (Math.abs(Integer.parseInt(attrA.get(rscore.getKey())) - Integer.parseInt(attrB.get(rscore.getKey()))) == 180) {
                                        ObjA.transformationMatrix.get(FigBName).put(rscore.getKey(), "reflected");
                                        ObjA.transformationScore.put(FigBName, oldScore - rscore.getValue() * 0.4);
                                    } else if (Math.abs(Integer.parseInt(attrA.get(rscore.getKey())) - Integer.parseInt(attrB.get(rscore.getKey()))) == 0) {
                                        ObjA.transformationMatrix.get(FigBName).put(rscore.getKey(), "unchanged");
                                        ObjA.transformationScore.put(FigBName, oldScore + rscore.getValue());
                                    } else {
                                        ObjA.transformationMatrix.get(FigBName).put(rscore.getKey(), "rotated"+
                                        (Math.abs(Integer.parseInt(attrA.get(rscore.getKey())) - Integer.parseInt(attrB.get(rscore.getKey())))));
                                        ObjA.transformationScore.put(FigBName, oldScore - rscore.getValue() * 0.9);
                                    }
                                    break;
                                case "fill":
                                case "alignment":
                                case "inside":
                                case "above":
                                case "overlaps":
                                case "left-of":
                                    if (attrA.get(rscore.getKey()).length() == attrB.get(rscore.getKey()).length()) {
                                        ObjA.transformationMatrix.get(FigBName).put(rscore.getKey(), "unchanged");
                                        ObjA.transformationScore.put(FigBName, oldScore + rscore.getValue() * 1.5);
                                    } else {
                                        ObjA.transformationMatrix.get(FigBName).put(rscore.getKey(), "changed");
                                        ObjA.transformationScore.put(FigBName, oldScore - rscore.getValue() * 0.7);
                                    }
                                    break;
                            }
                        }
                        else
                        {
                            ObjA.transformationScore.put(FigBName, oldScore - rscore.getValue());
                        }
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Looks at the transformation matrix for figureA to whatever figure BfigName says (most likely B or C)
     * and then compares that with the transformation matix for figureC(any other figure most likely B or C)
     * to the figure indicated by candidatefigName (most likely the answer choice figures)
     * After the comparison, returns a similarity score.
     * We'd most probably use this score to find closest match for transformation and hence, the answer
     * IDEA: Assign a threshold minimum similarity to decide if we go ahead with the answer or consider different transitions
     * @param figureAframes
     * @param BfigName
     * @param figureCframes
     * @param candidatefigName
     * @return
     */
    public double compareTranformations(HashMap<String, Frame> figureAframes, String BfigName,
                                        HashMap<String, Frame> figureCframes, String candidatefigName)
    {
        double score = 0;
        for(Map.Entry<String, Frame> Aentry: figureAframes.entrySet())
        {
            String aobj = Aentry.getKey();
            Frame aframe = Aentry.getValue();
            String matchedwith_inC = null;
            HashMap<String, String> aobj_trans = aframe.transformationMatrix.get(BfigName);
            HashMap<String, Double> aobj_cobj_Scores = new HashMap<>();
            if(figureCframes.containsKey(aobj)) {
                String cobj = aobj;
                Frame cframe = figureCframes.get(cobj);
                HashMap<String, String> cobj_trans = cframe.transformationMatrix.get(candidatefigName);
                aobj_cobj_Scores.put(cobj, 0.0);
                for (Map.Entry<String, String> ctrans : cobj_trans.entrySet()) {
                    double old = aobj_cobj_Scores.get(cobj);
                    if (aobj_trans.containsKey(ctrans.getKey())) {
                        if (ctrans.getKey().equals("size")) {
                            if (ctrans.getValue().equals(aobj_trans.get(ctrans.getKey()))) {
                                aobj_cobj_Scores.put(cobj, old + 10);
                            } else {
                                aobj_cobj_Scores.put(cobj, old + (10 - (2 * (Math.abs(Integer.parseInt(aobj_trans.get(ctrans.getKey())) - Integer.parseInt(ctrans.getValue()))))));
                            }
                        } else if (ctrans.getKey().equals("angle")) {
                            if (aobj_trans.get(ctrans.getKey()).equals(ctrans.getValue())) {
                                aobj_cobj_Scores.put(cobj, old + 20);
                            } else if (aobj_trans.get(ctrans.getKey()).startsWith("rotated") && ctrans.getValue().startsWith("rotated")) {
                                System.out.println(ctrans.getValue());
                                int a_b_angle = Integer.parseInt(aobj_trans.get(ctrans.getKey()).substring(7));
                                int c_candidate = Integer.parseInt(ctrans.getValue().substring(7));
                                if (Math.abs(a_b_angle - c_candidate) == 0) {
                                    aobj_cobj_Scores.put(cobj, old + 10);
                                }
                            } else {
                                aobj_cobj_Scores.put(cobj, old - 5);
                            }
                        } else if (ctrans.getKey().equals("deleted")) {
                            if (aobj_trans.get(ctrans.getKey()).equals(ctrans.getValue())) {
                                aobj_cobj_Scores.put(cobj, old + 5);
                            } else {
                                aobj_cobj_Scores.put(cobj, old - 5);
                            }
                        } else {
                            if (aobj_trans.get(ctrans.getKey()).equals(ctrans.getValue())) {
                                aobj_cobj_Scores.put(cobj, old + 10);
                            } else {
                                aobj_cobj_Scores.put(cobj, old - 5);
                            }
                        }
                    } else {
                        aobj_cobj_Scores.put(cobj, old - 20);
                    }
                }
            }
            else {
                for (Map.Entry<String, Frame> Centry : figureCframes.entrySet()) {
                    String cobj = Centry.getKey();
                    Frame cframe = Centry.getValue();
                    HashMap<String, String> cobj_trans = cframe.transformationMatrix.get(candidatefigName);
                    aobj_cobj_Scores.put(cobj, 0.0);
                    for (Map.Entry<String, String> ctrans : cobj_trans.entrySet()) {
                        double old = aobj_cobj_Scores.get(cobj);
                        if (aobj_trans.containsKey(ctrans.getKey())) {
                            if (ctrans.getKey().equals("size")) {
                                if (ctrans.getValue().equals(aobj_trans.get(ctrans.getKey()))) {
                                    aobj_cobj_Scores.put(cobj, old + 10);
                                } else {
                                    aobj_cobj_Scores.put(cobj, old + (10 - (2 * (Math.abs(Integer.parseInt(aobj_trans.get(ctrans.getKey())) - Integer.parseInt(ctrans.getValue()))))));
                                }
                            } else if (ctrans.getKey().equals("angle")) {
                                if (aobj_trans.get(ctrans.getKey()).equals(ctrans.getValue())) {
                                    aobj_cobj_Scores.put(cobj, old + 20);
                                } else if (aobj_trans.get(ctrans.getKey()).startsWith("rotated") && ctrans.getValue().startsWith("rotated")) {
                                    System.out.println(ctrans.getValue());
                                    int a_b_angle = Integer.parseInt(aobj_trans.get(ctrans.getKey()).substring(7));
                                    int c_candidate = Integer.parseInt(ctrans.getValue().substring(7));
                                    if (Math.abs(a_b_angle - c_candidate) == 0) {
                                        aobj_cobj_Scores.put(cobj, old + 10);
                                    }
                                } else {
                                    aobj_cobj_Scores.put(cobj, old - 5);
                                }
                            } else if (ctrans.getKey().equals("deleted")) {
                                if (aobj_trans.get(ctrans.getKey()).equals(ctrans.getValue())) {
                                    aobj_cobj_Scores.put(cobj, old + 5);
                                } else {
                                    aobj_cobj_Scores.put(cobj, old - 5);
                                }
                            } else {
                                if (aobj_trans.get(ctrans.getKey()).equals(ctrans.getValue())) {
                                    aobj_cobj_Scores.put(cobj, old + 10);
                                } else {
                                    aobj_cobj_Scores.put(cobj, old - 5);
                                }
                            }
                        } else {
                            aobj_cobj_Scores.put(cobj, old - 20);
                        }
                    }
                }
            }
            matchedwith_inC = maxHashEntry(aobj_cobj_Scores).getKey();
            score += aobj_cobj_Scores.get(matchedwith_inC);
        }
        return score;
    }
    public static Map.Entry<String, Double> maxHashEntry(HashMap<String, Double> hash)
    {
        if(hash.size()<1)
            return null;
        Map.Entry<String, Double> maxEntry = null;
        for(Map.Entry<String, Double> candidate: hash.entrySet())
        {
            if(maxEntry==null || maxEntry.getValue().compareTo(candidate.getValue()) < 0)
            {
                maxEntry = candidate;
            }
        }
        return maxEntry;
    }

    /**
     * Simply an easy metric to judge size differences
     * @param sizeA The first size to be compared to the latter
     * @param sizeB The second size
     * @return 0 for equal, -1...-4 for increase with -4 being largest increase from a to b and 1...4 for decrease with 4 being largest increase from a to b
     */
    public static int sizeCompare(String sizeA, String sizeB)
    {
        int ret = 0;
        switch(sizeA)
        {
            case "small":
                if(sizeB.equals("medium"))
                {
                    ret = -1;

                }
                else if(sizeB.equals("large"))
                {
                    ret = -2;
                }
                else if(sizeB.equals("very large"))
                {
                    ret = -3;
                }
                else if(sizeB.equals("huge"))
                {
                    ret = -4;
                }
                else ret = 0;
                break;
            case "medium":
                if(sizeB.equals("small"))
                    ret = 1;
                else if(sizeB.equals("large"))
                {
                    ret = -1;
                }
                else if(sizeB.equals("very large"))
                {
                    ret = -2;
                }
                else if(sizeB.equals("huge"))
                {
                    ret = -3;
                }
                else
                    ret = 0;
                break;
            case "large":
                if(sizeB.equals("medium"))
                {
                    ret = 1;
                }
                else if(sizeB.equals("small"))
                {
                    ret = 2;
                }
                else if(sizeB.equals("very large"))
                {
                    ret = -1;
                }
                else if(sizeB.equals("huge"))
                {
                    ret = -2;
                }
                else ret = 0;
                break;
            case "very large":
                if(sizeB.equals("huge"))
                    ret = -1;
                else if(sizeB.equals("large"))
                {
                    ret = 1;
                }
                else if(sizeB.equals("medium"))
                {
                    ret = 2;
                }
                else if(sizeB.equals("small"))
                {
                    ret = 3;
                }
                else ret = 0;
                break;
            case "huge":
                if(sizeB.equals("very large"))
                    ret = 1;
                else if(sizeB.equals("large"))
                {
                    ret = 2;
                }
                else if(sizeB.equals("medium"))
                {
                    ret = 3;
                }
                else if(sizeB.equals("small"))
                {
                    ret =4;
                }
                else ret = 0;
                break;
        }
        return ret;
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
        HashMap<String, Double> transformationScore;
        public Frame(String name, String figure)
        {
            this.name = name;
            this.fromFigure = figure;
            attributes = new HashMap<>();
            relationships = new HashMap<>();
            transformationMatrix = new HashMap<>();
            transformationScore = new HashMap<>();
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
                    if(prob.getName().equals("Basic Problem B-06"))
                    {

                    }
                    if (Aabove != null) {
                        relationships.get("above").add(Aabove);
                    } else {
                        System.out.println("No frame found for attribute above: " + this.name + "" +
                                "despite attribute hashmap containing valid value: " + Above);
                    }
                }
            }
            String overlaps = attributes.get("overlaps");
            if(overlaps !=null)
            {
                String isoverlap[] = overlaps.split(",");
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
            return ("Frame{" +
                    "name='" + name + '\'' +
                    ", fromFigure='" + fromFigure + '\'' +
                    ", attributes=" + attributes +
                    ", relationships=" + relationships +
                    ", transformationMatrix=" + transformationMatrix +
                    "\n transformationScore=" + transformationScore +
                    '}');
        }

        public boolean equals(Frame b)
        {
            if(this.name.equals(b.getName()))
                return true;

            HashMap<String, String> attrA, attrB;
            attrA = this.attributes;
            attrB = b.attributes;
            int currscore = 0;

            if(attrA.size() == attrB.size())
                currscore += 10;
            for(String attr: rScores.keySet())
            {
                if(attrA.containsKey(attr) && attrB.containsKey(attr)) {
                    if (attrA.get(attr).equals(attrB.get(attr))) {
                        currscore += rScores.get(attr);

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
            System.out.println("SCORE FOR COMPARING "+this.getName()+" to "+b.getName()+" = "+currscore);
            return currscore>15;
        }
    }
}
