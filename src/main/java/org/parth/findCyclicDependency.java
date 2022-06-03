package org.parth;
import static org.parth.RunShellCommandFromJava.runCmd;
import static org.parth.XMLtoClasses.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import static java.util.stream.Collectors.toList;

public class findCyclicDependency {
    public static Map<String,Set<String>> edges;
    public static Map<String,Set<String>> edges2;
    private static String loop="";
    private static String baseElement="";

    public static void main(String[] args) throws ClassNotFoundException, XPathExpressionException, IOException, ParserConfigurationException, SAXException, InterruptedException {

        ArrayList<Commit> commitHistory=RunShellCommandFromJava.getGitCommits();
        String firstFaultyCommit="";
        String firstGoodCommit="";

        for(Commit commit:commitHistory)
        {
//            commit.print();

            String command="git checkout "+commit.getId();
            runCmd(command);


            ArrayList<String> allClasses = new ArrayList<>();
            ArrayList<String> beans = new ArrayList<>();
            getAllClassesfromXml("ApplicationContext.xml", allClasses);  // get classes from bean-id pairs and the packages to be scanned



            getusefulClasses(allClasses, beans);// with annotaion of : @component

            removeClassprefix_forList(beans);
//

            edges = new HashMap<>();

            makeDirectedGraph(beans);

//            printGraph();
            boolean iscycle = findCycle(edges);
            System.out.println("the graph has a cycle : " + iscycle);
            if(!iscycle)
            {
                firstGoodCommit=commit.getId();
                break;
            }

            firstFaultyCommit=commit.getId();
        }

        runCmd("git checkout mybranch");

        System.out.println("first good commit : "+firstGoodCommit+"\n"+"first faulty commit "+firstFaultyCommit);
        getFaultyEdges(firstGoodCommit,firstFaultyCommit);

        return;
    }



    public static void getusefulClasses(ArrayList<String> allClasses,ArrayList<String> beans) throws ClassNotFoundException {
        
        for(String className:allClasses)
        {
            Class<?> thisClass = Class.forName(className);
            if( thisClass.isAnnotationPresent(Component.class)!=false) {
                beans.add(thisClass.toString());
            }
            /// automatically find sub compnents
            else if (thisClass.isAnnotationPresent(Service.class)!=false || thisClass.isAnnotationPresent(Repository.class)!=false || thisClass.isAnnotationPresent(Controller.class)!=false || thisClass.isAnnotationPresent(Configuration.class)!=false) {
                beans.add(thisClass.toString());
            }

        }

        return;
    }



    private static void  getFaultyEdges(String firstGoodCommit,String firstFaultyCommit ) throws IOException, InterruptedException, XPathExpressionException, ParserConfigurationException, SAXException, ClassNotFoundException {
        // make old graph
        String command="git checkout "+firstGoodCommit;
        runCmd(command);


        ArrayList<String> allClasses = new ArrayList<>();
        // get classes from bean-id pairs and the packages to be scanned
        getAllClassesfromXml("ApplicationContext.xml", allClasses);


        ArrayList<String> beans = new ArrayList<>();
        getusefulClasses(allClasses, beans);// with @component annotation

        //remove prefix "class" from all usefull classes
        for (int i = 0; i < beans.size(); i++) {
            String ss = beans.get(i);
            ss = removeClassPrefix(ss);
            beans.set(i, ss);
        }

        edges = new HashMap<>();
        makeDirectedGraph(beans);
        System.out.println("\n In good commit graph : ");
        printGraph(edges);
        System.out.println("graph as cycle -> "+findCycle(edges));





        // make new graph:
            // iteration 1: only add if already there
            // iteration 2 : add if no cycle gets formed
        edges2=new HashMap<>();
         command="git checkout "+firstFaultyCommit;
         runCmd(command);


         allClasses = new ArrayList<>();
        // get classes from bean-id pairs and the packages to be scanned
        getAllClassesfromXml("ApplicationContext.xml", allClasses);


         beans = new ArrayList<>();
        getusefulClasses(allClasses, beans);// with @component annotation

        //remove prefix "class" from all usefull classes
        for (int i = 0; i < beans.size(); i++)
        {
            String ss = beans.get(i);
            ss = removeClassPrefix(ss);
            beans.set(i, ss);
        }






        // ab dhyaan se graph banao
            // iteration 1
        for(String className:beans){
//            System.out.println("\n"+className);

            Class<?> thisClass = Class.forName(className);    // should not have a prefix "class"


            try {

                Field[] fields=thisClass.getDeclaredFields();       // declared se private bhi aa jaaenge but inherited nahi aenge
                Constructor[] constructors= thisClass.getConstructors();


                // find auto wires in fields (works with reqd=false as well)
                for(int i=0;i<fields.length;i++){
                    if(fields[i].isAnnotationPresent(Autowired.class)!=false)
                    {
                        String newnbr=fields[i].getType().toString();
                        addCommonEdge(className,newnbr);
                    }

                }


//              finding autowires in constructors
                for(int i=0;i< constructors.length;i++)
                {
                    if(constructors[i].isAnnotationPresent(Autowired.class)!=false)
                    {
                        List<Class> arguments= Arrays.stream(constructors[i].getParameterTypes()).toList();
                        for(int j=0;j<arguments.size();j++)
                        {
                            String newnbr=arguments.get(j).toString();
                            addCommonEdge(className,newnbr);

                        }

                    }
                }



            } catch (Throwable e) {
                System.err.println(e);
            }


        }
        System.out.println("in good commit grpahs are : ");
        printGraph(edges);
        printGraph(edges2);
        // idron tak sab vdia haiga


        //iteration 2
        for(String className:beans){
//            System.out.println("\n"+className);

            Class<?> thisClass = Class.forName(className);    // should not have a prefix "class"


            try {

                Field[] fields=thisClass.getDeclaredFields();       // declared se private bhi aa jaaenge but inherited nahi aenge
                Constructor[] constructors= thisClass.getConstructors();


                // find auto wires in fields (works with reqd=false as well)
                for(int i=0;i<fields.length;i++){
                    if(fields[i].isAnnotationPresent(Autowired.class)!=false)
                    {
                        String newnbr=fields[i].getType().toString();
                        addNonLoopEdge(className,newnbr);
                    }

                }


//              finding autowires in constructors
                for(int i=0;i< constructors.length;i++)
                {
                    if(constructors[i].isAnnotationPresent(Autowired.class)!=false)
                    {
                        List<Class> arguments= Arrays.stream(constructors[i].getParameterTypes()).toList();
                        for(int j=0;j<arguments.size();j++)
                        {
                            String newnbr=arguments.get(j).toString();
                            addNonLoopEdge(className,newnbr);

                        }

                    }
                }



            } catch (Throwable e) {
                System.err.println(e);
            }


        }



        System.out.println("added edges which were not making loops :D");





        runCmd("git checkout mybranch");

        return;
    }

    private static void addNonLoopEdge(String className,String newnbr)
    {
        // check if already exists
        String updateClassName="class "+className;
        Set<String> currnbrs=edges2.get(updateClassName);
        if(currnbrs!=null && currnbrs.contains(newnbr)==true)
            return;

        //add
        if(currnbrs==null)
            currnbrs=new HashSet<>();

        currnbrs.add(newnbr);
        edges2.put(updateClassName,currnbrs);

        //remove
        if(findCycle(edges2)==true)
        {
            System.out.println(className+" -> "+newnbr+"  .... faulty dependency ");
            currnbrs.remove(newnbr);
            edges2.put(updateClassName,currnbrs);
        }

        return;
    }
    private static void addCommonEdge(String className,String newnbr) // add edge only if it is present in the older graph
    {
        String updateClassName="class "+className;
        Set<String> currnbrs=edges.get(updateClassName);

        Set<String> st=edges.get(updateClassName);
        boolean isPresentInOld=false;
        if(st!=null && st.contains(newnbr)==true)
            isPresentInOld=true;

        if(isPresentInOld)
        {
            if(currnbrs != null)
            {
                currnbrs.add(newnbr);
                edges2.put(updateClassName, currnbrs);

            }
            else
            {
                Set<String> currnbr = new HashSet<>();
                currnbr.add(newnbr);
                edges2.put(updateClassName, currnbr);

            }

        }


        return;
    }

    private static void addEdge(String className,String newnbr)
    {
        String updateClassName="class "+className;
        Set<String> currnbrs=edges.get(updateClassName);

        if(currnbrs!=null)
        {
            currnbrs.add(newnbr);
            edges.put(updateClassName, currnbrs);

        }
        else {
            Set<String> currnbr = new HashSet<>();
            currnbr.add(newnbr);
            edges.put(updateClassName,currnbr);

        }
    }

    private static void makeDirectedGraph(ArrayList<String> beans) throws ClassNotFoundException {

        for(String className:beans){
//            System.out.println("\n"+className);

            Class<?> thisClass = Class.forName(className);    // should not have a prefix "class"


            try {

                Field[] fields=thisClass.getDeclaredFields();       // declared se private bhi aa jaaenge but inherited nahi aenge
                Method[] methods = thisClass.getDeclaredMethods();      // inherited nahi aenge
                Constructor[] constructors= thisClass.getConstructors();


                // find auto wires in fields (works with reqd=false as well)
                for(int i=0;i<fields.length;i++){
                    if(fields[i].isAnnotationPresent(Autowired.class)!=false)
                    {
                        String newnbr=fields[i].getType().toString();
                        addEdge(className,newnbr);
                    }

                }


//              finding autowires in constructors
                for(int i=0;i< constructors.length;i++)
                {
                    if(constructors[i].isAnnotationPresent(Autowired.class)!=false)
                    {
                        List<Class> arguments= Arrays.stream(constructors[i].getParameterTypes()).toList();
                        for(int j=0;j<arguments.size();j++)
                        {
                            String newnbr=arguments.get(j).toString();
                            addEdge(className,newnbr);

                        }

                    }
                }



            } catch (Throwable e) {
                System.err.println(e);
            }


        }

    }

    public static boolean isCyclicUtil(String i, Set<String> visited, Set<String> recStack,Map<String,Set<String>> ed) {
        if (recStack.contains(i)) {
            loop+="<- ";
            baseElement = i;
            return true;
        }
        if (visited.contains(i))
            return false;

        recStack.add(i);
        visited.add(i);
        Set<String> children = ed.get(i);
        if (children == null)
        {
            recStack.remove(i);
            return false;
        }
        for (String child : children) {
            if (isCyclicUtil(child, visited, recStack,ed)) {
                if (!baseElement.equals(""))
                {
                    loop += child;
                    loop += " <- ";
                }
                else if (child.equals(baseElement))
                {
                    baseElement = "";
                }
                return true;
            }
        }

        recStack.remove(i);
        return false;
    }

    public static boolean findCycle(Map<String,Set<String>> ed) {
        loop="";
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();
        for (String s : ed.keySet())
        {
            if (isCyclicUtil(s, visited, recStack,ed))
            {
                System.out.println("cycle is "+loop);
                return true;
            }

        }
        return false;
    }



    private static void printGraph(Map<String,Set<String>> edgess)
    {
        System.out.println("graph looks like : ");
        System.out.println(Arrays.deepToString(edgess.entrySet().toArray()));
    }
}

// qualifier
// beans/bean

// commit